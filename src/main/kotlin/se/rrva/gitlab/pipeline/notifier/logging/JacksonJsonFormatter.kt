package se.rrva.gitlab.pipeline.notifier.logging

import ch.qos.logback.contrib.json.JsonFormatter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import java.io.IOException

class JacksonJsonFormatter : JsonFormatter {
    private val writer: ObjectWriter = ObjectMapper().writer()

    @Throws(IOException::class)
    override fun toJsonString(m: Map<*, *>?): String {
        return writer.writeValueAsString(m)
    }

}