package org.web25.felix.jobs

typealias JobCreator = (processableFile: ProcessableFile) -> Job<*>