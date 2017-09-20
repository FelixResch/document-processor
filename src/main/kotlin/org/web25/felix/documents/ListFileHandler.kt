package org.web25.felix.documents

import org.slf4j.Logger
import java.io.File

class ListFileHandler(val file: File, jobManager: JobManager<FileHandlerResult>, logger: Logger) : FileHandler(jobManager, logger) {

    override fun call(): FileHandlerResult {
        logger.debug(file.path)
        return FileHandlerResult(file, true)
    }
}