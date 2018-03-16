
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.core.spi.FilterReply

class StdOutFilter : ch.qos.logback.core.filter.AbstractMatcherFilter<Any>() {

    override fun decide(event: Any): FilterReply {
        if (!isStarted) {
            return FilterReply.NEUTRAL
        }

        val loggingEvent = event as LoggingEvent

        val eventsToKeep = listOf(Level.INFO)
        return if (eventsToKeep.contains(loggingEvent.level)) {
            FilterReply.NEUTRAL
        } else {
            FilterReply.DENY
        }
    }

}

class ErrOutFilter : ch.qos.logback.core.filter.AbstractMatcherFilter<Any>() {

    override fun decide(event: Any): FilterReply {
        if (!isStarted) {
            return FilterReply.NEUTRAL
        }

        val loggingEvent = event as LoggingEvent

        val eventsToKeep = listOf(Level.WARN, Level.ERROR)
        return if (eventsToKeep.contains(loggingEvent.level)) {
            FilterReply.NEUTRAL
        } else {
            FilterReply.DENY
        }
    }

}

class FileFilter : ch.qos.logback.core.filter.AbstractMatcherFilter<Any>() {

    override fun decide(event: Any): FilterReply {
        if (!isStarted) {
            return FilterReply.NEUTRAL
        }

         return FilterReply.ACCEPT
    }

}