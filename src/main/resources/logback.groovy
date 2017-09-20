import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.INFO

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{5} - %msg%n"
    }
}

appender("FILE", FileAppender) {
    file = "log/debug.log"
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{5} - %msg%n"
    }
}


//logger("org.web25.felix.documents.Documents", ERROR)
logger("org.web25.felix.documents.Documents", DEBUG, ["FILE"], false)
logger("org.web25.felix.jobs.Job", DEBUG, ["FILE"], false)
logger("org.web25.felix.jobs.JobManager", DEBUG, ["FILE"], false)
root(WARN, ["STDOUT"])