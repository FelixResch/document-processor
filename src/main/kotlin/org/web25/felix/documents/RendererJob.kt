package org.web25.felix.documents

import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension
import com.vladsch.flexmark.ext.ins.InsExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.ext.toc.SimTocExtension
import com.vladsch.flexmark.ext.toc.TocExtension
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.superscript.SuperscriptExtension
import org.slf4j.LoggerFactory
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.web25.felix.flexmark.extension.math.MathExtension
import org.web25.felix.jobs.*
import java.io.File


class RendererJob (val file: ProcessableFile, parent: Job<Any>? = null, val jobCreator : JobCreator? = null): AbstractJob<Unit>(parent) {

    override val name: String = "renderer"

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
                        InsExtension.create(),
                        StrikethroughExtension.create(),
                        MathExtension.create()
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
        val file = this.file.src
        val parsedMarkdownConfig = this.file.getConfig(ParsedMarkdownDocumentConfig::class)
        val fileWatcherConfig = this.file.getConfig(FileWatcherConfig::class)
        val document = parsedMarkdownConfig.document
        val frontmatter = this.file.getConfig(FrontmatterData::class)
        val data = frontmatter.data
        
        fileWatcherConfig.stage = "html"

        logger.debug("Data for $file: ${data}")

        this.file.dst = File(file.parentFile, file.nameWithoutExtension + ".html")

        val markdown = renderer.render(document)

        val context = Context()

        context.setVariable("markdown", markdown)
        if(data.containsKey("title")) {
            val title = data["title"]!!
            if (title.size > 0) {
                fileWatcherConfig.name = title.first()
                context.setVariable("title", title.first())
            } else {
                context.setVariable("title", file.nameWithoutExtension)
            }
        } else {
            context.setVariable("title", file.nameWithoutExtension)
        }
        if(data.containsKey("author")) {
            context.setVariable("authors", data["author"])
        } else if (data.containsKey("authors")) {
            context.setVariable("authors", data["authors"])
        }
        if(data.containsKey("version")) {
            val version = data["version"]!!
            if(version.size > 0) {
                context.setVariable("version", version.first())
            }
        }

        this.file.stringContent = templatingEngine.process("markdown", context)

        fileWatcherConfig.state = FileState.RENDERED

        if(jobCreator != null) {
            jobContext.jobManager.addJob(jobCreator.invoke(this.file))
        }
    }

    override fun handleError(t: Throwable) {
        val fileWatcherConfig = this.file.getConfig(FileWatcherConfig::class)
        fileWatcherConfig.done = true
        fileWatcherConfig.errored = true
    }
}

fun renderMarkdownToHtml(processableFile: ProcessableFile, jobCreator: JobCreator?): RendererJob {
    return RendererJob(processableFile, jobCreator = jobCreator)
}