package org.web25.felix.documents

import com.openhtmltopdf.util.XRLog
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.slf4j.LoggerFactory
import org.web25.felix.jobs.JobManager
import org.web25.felix.jobs.fileWalker
import org.web25.felix.jobs.simple.SimpleJobManager
import org.web25.felix.jobs.write
import java.io.File
import java.util.logging.LogManager
import kotlin.system.exitProcess

class Documents(val args: Array<String>) {

    val logger = LoggerFactory.getLogger(Documents::class.java)

    val options by lazy {
        val options = Options()
        options.addOption(
                Option.builder("d")
                        .longOpt("directory")
                        .desc("The directory that contains all markdown files to build")
                        .hasArg()
                        .argName("dir")
                        .build()
        )
        options.addOption(
                Option.builder("h")
                        .longOpt("help")
                        .desc("Print this help message")
                        .optionalArg(true)
                        .build()
        )
        options.addOption(
                Option.builder("t")
                        .longOpt("threadCount")
                        .desc("The number of threads to use for asynchronous operations")
                        .hasArg()
                        .build()
        )
    }

    val commandLineParser by lazy {
        DefaultParser()
    }

    var threadCount = 4

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            Documents(args)
        }
    }

    val jobManager: JobManager by singleton {
        SimpleJobManager(threadCount)
    }

    init {
        LogManager.getLogManager().reset()
        XRLog.setLoggingEnabled(false)
        val dir : File
        logger.debug("Starting document compiler")
        logger.debug("Checking for arguments")
        if(args.isNotEmpty()) {
            logger.debug("Arguments detected")
            val commandLine = commandLineParser.parse(options, args)
            logger.debug("Command line parsed successfully")
            if(commandLine.hasOption('h')) {
                logger.debug("Help requested")
                val helpFormatter = HelpFormatter()
                helpFormatter.printHelp("documents", options)
            }
            if(commandLine.hasOption('d')) {
                logger.debug("Option d detected")
                dir = File(commandLine.getOptionValue('d'))
            } else {
                dir = File(".")
            }
            if(commandLine.hasOption("t")) {
                logger.debug("Option t detected")
                threadCount = commandLine.getOptionValue('t').toInt()
            }
        } else {
            dir = File(".")
        }
        if(!dir.exists()) {
            logger.error("dir needs to exists")
            exitProcess(1)
        }
        if(!dir.isDirectory) {
            logger.error("dir needs to be a directory")
            exitProcess(1)
        }
        if(!dir.canRead()) {
            logger.error("Permission denied on dir!")
            exitProcess(1)
        }
        if(!dir.canWrite()) {
            logger.error("Can't write files")
            exitProcess(1)
        }

        jobManager.addJob(fileWalker(dir) {
            addFileWatcher(it) {
                checkForChanges(it, dir) {
                    parseMarkdown(it) {
                        visitFrontmatter(it) {
                            renderMarkdownToHtml(it) {
                                write(it) {
                                    renderMarkdownToPdf(it) {
                                        write(it)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })
        logger.debug("Requesting shutdown of executor")
        println()
        println()
        while (jobManager.busy) {
            CommandLineProgressPrinter.print()
            Thread.sleep(200)
        }
        CommandLineProgressPrinter.print()
        jobManager.waitForShutdown()
    }
}

