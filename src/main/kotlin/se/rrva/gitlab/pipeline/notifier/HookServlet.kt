package se.rrva.gitlab.pipeline.notifier

import com.fasterxml.jackson.databind.json.JsonMapper
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import java.io.IOException

class HookServlet : HttpServlet() {
    private val log = LoggerFactory.getLogger(this.javaClass)
    private val jsonMapper = JsonMapper()
    override fun service(request: HttpServletRequest, response: HttpServletResponse) {
        val node = try {
            jsonMapper.readTree(request.inputStream)
        } catch (e: IOException) {
            null
        }
        val objectKind = node?.get("object_kind")?.textValue()
        if (objectKind != "pipeline") {
            response.status = 400
            response.writer.println("only pipeline events supported")
        } else {
            val payload = jsonMapper.writeValueAsString(node)
            log.info("Received payload $payload")
            val produced = EventBus.produceEvent(Event(payload))
            if (produced) {
                response.status = 200
                response.writer.println("OK")
            } else {
                response.status = 500
                response.writer.println("Failed to produce event")
            }
        }
    }
}