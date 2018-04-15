package org.web25.felix.documents

import com.openhtmltopdf.DOMBuilder
import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension
import com.vladsch.flexmark.ext.gfm.strikethrough.SubscriptExtension
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension
import com.vladsch.flexmark.ext.ins.InsExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.ext.toc.TocExtension
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension
import com.vladsch.flexmark.superscript.SuperscriptExtension
import com.vladsch.flexmark.util.options.MutableDataSet
import org.slf4j.LoggerFactory
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.web25.felix.flexmark.extension.math.MathExtension
import org.web25.felix.jobs.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import com.sun.xml.internal.ws.streaming.XMLStreamReaderUtil.close
import com.openhtmltopdf.DOMBuilder.jsoup2DOM
import org.jsoup.Jsoup
import com.openhtmltopdf.bidi.support.ICUBidiReorderer
import com.openhtmltopdf.bidi.support.ICUBidiSplitter
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.swing.NaiveUserAgent
import java.io.OutputStream


class PdfRendererJob (val file: ProcessableFile, parent: Job<Any>? = null, val jobCreator: JobCreator?) : AbstractJob<Unit>(parent) {

    override val name: String = "renderer-pdf"

    private val logger = LoggerFactory.getLogger(Documents::class.java)

    val renderer by singleton {
        val rendererBuilder = HtmlRenderer.builder()
        rendererBuilder.extensions(
                listOf(
                        TablesExtension.create(),
                        TocExtension.create(),
                        AbbreviationExtension.create(),
                        FootnoteExtension.create(),
                        TaskListExtension.create(),
                        SuperscriptExtension.create(),
                        StrikethroughSubscriptExtension.create(),
                        InsExtension.create()
                )
        )

        rendererBuilder.build()
    }

    val templatingEngine by singleton {

        val templateResolver = ClassLoaderTemplateResolver()
        templateResolver.suffix = ".html"
        templateResolver.templateMode = TemplateMode.HTML

        val templateEngine = TemplateEngine()
        templateEngine.addTemplateResolver(templateResolver)

        templateEngine
    }

    override fun execute(jobContext: JobContext) {
        val fileWatcherConfig = this.file.getConfig(FileWatcherConfig::class)
        val parsedMarkdownConfig = this.file.getConfig(ParsedMarkdownDocumentConfig::class)
        val document = parsedMarkdownConfig.document

        val frontmatter = file.getConfig(FrontmatterData::class)
        val data = frontmatter.data

        fileWatcherConfig.stage = "pdf"

        val markdown = renderer.render(document)

        val context = Context()

        context.setVariable("markdown", markdown)
        if(data.containsKey("title")) {
            val title = data["title"]!!
            if (title.size > 0) {
                fileWatcherConfig.name = title.first()
                context.setVariable("title", title.first())
            } else {
                context.setVariable("title", file.src.nameWithoutExtension)
            }
        } else {
            context.setVariable("title", file.src.nameWithoutExtension)
        }
        if(data.containsKey("author")) {
            context.setVariable("authors", data["author"])
        } else if (data.containsKey("authors")) {
            context.setVariable("authors", data["authors"])
        }
        if(context.containsVariable("authors")) {
            context.setVariable("_authors", (context.getVariable("authors") as List<String>).joinToString(separator = ", "))
        }
        if(data.containsKey("version")) {
            val version = data["version"]!!
            if(version.size > 0) {
                context.setVariable("version", version.first())
            }
        }

        val pdf = ProcessableFile(file.src)
        pdf.copyConfig(file)

        pdf.stringContent = templatingEngine.process("markdown_pdf", context)

        val buffer = ByteArrayOutputStream()

        pdf.dst = File(file.src.parentFile, file.src.nameWithoutExtension + ".pdf")


        exportToPdf(buffer, pdf.stringContent, "", BaseRendererBuilder.TextDirection.LTR, file.src.parentFile)

        pdf.content = buffer.toByteArray()
        pdf.addConfig(frontmatter)


        fileWatcherConfig.done = true

        jobCreator?.let { jobCreator ->
            jobContext.jobManager.addJob(jobCreator(pdf))
        }
    }

}

fun exportToPdf(os: OutputStream, html: String, url: String, defaultTextDirection: BaseRendererBuilder.TextDirection?, workingDirectory: File) {
    try {
        // There are more options on the builder than shown below.
        val builder = PdfRendererBuilder()

        if (defaultTextDirection != null) {
            builder.useUnicodeBidiSplitter(ICUBidiSplitter.ICUBidiSplitterFactory())
            builder.useUnicodeBidiReorderer(ICUBidiReorderer())
            builder.defaultTextDirection(defaultTextDirection) // OR RTL
            builder.useUriResolver { _, uri ->
                val file = File(workingDirectory, uri)
                if(file.exists() && file.isFile) {
                    file.absoluteFile.toURI().toString()
                } else {
                    null
                }
            }
        }

        val doc = Jsoup.parse(html)

        val dom = DOMBuilder.jsoup2DOM(doc)
        builder.withW3cDocument(dom, url)
        builder.toStream(os)
        builder.run()
    } catch (e: Exception) {
        e.printStackTrace()
        // LOG exception
    } finally {
        try {
            os.close()
        } catch (e: IOException) {
            // swallow
        }

    }
}

fun renderMarkdownToPdf(processableFile: ProcessableFile, jobCreator: JobCreator?): PdfRendererJob {
    return PdfRendererJob(processableFile, jobCreator = jobCreator)
}
