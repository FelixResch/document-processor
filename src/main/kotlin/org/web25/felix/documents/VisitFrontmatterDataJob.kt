package org.web25.felix.documents

import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor
import org.web25.felix.jobs.*

class VisitFrontmatterDataJob(val file: ProcessableFile, val jobCreator: JobCreator?, parent: Job<Any>? = null) : AbstractJob<Unit>(parent) {

    override val name: String = "load-frontmatter-data"

    val fileStateTable : FileStateTable by singleton()

    override fun execute(jobContext: JobContext) {
        val visitor = AbstractYamlFrontMatterVisitor()
        val parsedMarkdownConfig = this.file.getConfig(ParsedMarkdownDocumentConfig::class)
        visitor.visit(parsedMarkdownConfig.document)

        this.file.addConfig(FrontmatterData(visitor.data))

        fileStateTable.update(file)

        jobCreator?.let { creator ->
            jobContext.jobManager.addJob(creator(file))
        }
    }
}

class FrontmatterData(val data: Map<String, MutableList<String>>) : Config

fun visitFrontmatter(processableFile: ProcessableFile, jobCreator: JobCreator?): VisitFrontmatterDataJob {
    return VisitFrontmatterDataJob(file = processableFile, jobCreator = jobCreator)
}
