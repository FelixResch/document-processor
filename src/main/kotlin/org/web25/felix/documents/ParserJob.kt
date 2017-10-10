package org.web25.felix.documents

import com.vladsch.flexmark.ast.Node
import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension
import com.vladsch.flexmark.ext.gfm.strikethrough.SubscriptExtension
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension
import com.vladsch.flexmark.ext.ins.InsExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.ext.toc.TocExtension
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.superscript.SuperscriptExtension
import org.web25.felix.flexmark.extension.math.MathExtension
import org.web25.felix.jobs.*

class ParserJob(val file: ProcessableFile, parent: Job<Any>? = null, val jobCreator : JobCreator? = null): AbstractJob<Unit>(parent) {

    override val name: String = "parser"

    val parser by singleton {
        val parserBuilder = Parser.builder()
        parserBuilder.extensions(
                listOf(
                        TablesExtension.create(),
                        YamlFrontMatterExtension.create(),
                        TocExtension.create(),
                        AbbreviationExtension.create(),
                        FootnoteExtension.create(),
                        TaskListExtension.create(),
                        SuperscriptExtension.create(),
                        StrikethroughSubscriptExtension.create(),
                        InsExtension.create(),
                        MathExtension.create()
                )
        )
        parserBuilder.build()
    }

    override fun execute(jobContext: JobContext) {

        val fileWatcherConfig = file.getConfig(FileWatcherConfig::class)
        fileWatcherConfig.state = FileState.PARSED
        fileWatcherConfig.stage = "parse"

        val document = parser.parse(file.stringContent)
        file.addConfig(ParsedMarkdownDocumentConfig(document))
        if(jobCreator != null) {
            jobContext.jobManager.addJob(jobCreator.invoke(file))
        }

    }

}

fun parseMarkdown(processableFile: ProcessableFile, jobCreator: JobCreator? = null): ParserJob {
    return ParserJob(file = processableFile, jobCreator = jobCreator)
}

data class ParsedMarkdownDocumentConfig(val document: Node) : Config
