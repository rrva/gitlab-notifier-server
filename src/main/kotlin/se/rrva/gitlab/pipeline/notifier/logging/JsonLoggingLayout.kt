package se.rrva.gitlab.pipeline.notifier.logging

import ch.qos.logback.classic.pattern.ThrowableProxyConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.contrib.json.JsonLayoutBase
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class JsonLoggingLayout : JsonLayoutBase<ILoggingEvent>() {

    private val throwableProxyConverter = ThrowableProxyConverter()
    private val timeZone = ZoneId.systemDefault()

    override fun toJsonMap(e: ILoggingEvent): MutableMap<Any?, Any?> {
        val m = LinkedHashMap<Any?, Any?>()

        m["time"] = Instant.ofEpochMilli(e.timeStamp).atZone(timeZone).toOffsetDateTime()
            .format(DateTimeFormatter.ISO_DATE_TIME)
        m["severity"] = e.level?.levelStr
        m["message"] = e.formattedMessage
        m["thread"] = e.threadName
        if (e.throwableProxy != null) {
            m["stacktrace"] = throwableProxyConverter.convert(e)
        }
        m["logger"] = e.loggerName

        return m
    }

    override fun start() {
        this.throwableProxyConverter.start()
        super.start()
    }

    override fun stop() {
        this.throwableProxyConverter.stop()
        super.stop()
    }


}