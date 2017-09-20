package org.web25.felix.documents

import org.slf4j.Logger
import java.util.concurrent.Callable

abstract class FileHandler(val jobManager: JobManager<FileHandlerResult>, val logger: Logger) : Callable<FileHandlerResult>{
}

