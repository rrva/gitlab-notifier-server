package se.rrva.gitlab.pipeline.notifier

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import java.io.IOException
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicLong

class HookServlet : HttpServlet() {
    private val log = LoggerFactory.getLogger(this.javaClass)
    private val jsonMapper = JsonMapper()
    private val secretToken = System.getenv("SECRET_TOKEN")
    private val timeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    private val seqNo = AtomicLong()
    private val epoch = Instant.now().epochSecond

    override fun service(request: HttpServletRequest, response: HttpServletResponse) {
        if (secretToken != null && secretToken != request.getHeader("X-Gitlab-Token")) {
            response.status = 403
            return
        }
        val node = try {
            jsonMapper.readTree(request.inputStream)
        } catch (e: IOException) {
            null
        }
        val objectKind = node?.get("object_kind")?.textValue()
        if (objectKind != "pipeline") {
            response.status = 400
            response.writer.println("only pipeline events supported")
            return
        }
        val objectNode = node as ObjectNode
        objectNode.put("received_at", timeFormatter.format(OffsetDateTime.now()))
        objectNode.put("epoch", epoch)
        objectNode.put("seq", seqNo.incrementAndGet())
        val payload = jsonMapper.writeValueAsString(objectNode)
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