package se.rrva.gitlab.pipeline.notifier

import kotlinx.coroutines.*
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.StatusCode
import org.eclipse.jetty.websocket.api.WebSocketAdapter
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer

class EventSocket : WebSocketAdapter() {
    private val log = LoggerFactory.getLogger(this.javaClass)
    private val scope = CoroutineScope(SupervisorJob())
    @Volatile
    private var job: Job? = null
    @Volatile
    private var pingJob: Job? = null

    override fun onWebSocketConnect(sess: Session) {
        super.onWebSocketConnect(sess)
        job = scope.launch {
            log.info("Starting event flow for ${session?.remote?.remoteAddress}")
            EventBus.events.collect {
                log.info("Sending to ${session?.remote?.remoteAddress}: ${it.payload}")
                withContext(Dispatchers.IO) {
                    session?.remote?.sendString(it.payload)
                }
            }
        }
        pingJob = scope.launch {
            log.info("Starting keepalive for ${session?.remote?.remoteAddress}")
            while (true) {
                try {
                    withContext(Dispatchers.IO) {
                        val payload = ByteBuffer.wrap("You There?".toByteArray())
                        session?.remote?.sendPing(payload)
                    }
                } catch (e: Exception) {
                    log.error(e.message, e)
                }
                delay(10000)
            }
        }

        log.info("Socket connected: ${sess.remote.remoteAddress}")
    }

    override fun onWebSocketText(message: String) {
        super.onWebSocketText(message)
        if (message.lowercase().contains("bye")) {
            job?.cancel("client closed")
            job = null
            pingJob?.cancel("client closed")
            pingJob = null
            session.close(StatusCode.NORMAL, "Thanks")
        }
    }

    override fun onWebSocketClose(statusCode: Int, reason: String?) {
        super.onWebSocketClose(statusCode, reason)
        job?.cancel("socket closed ${session.remote.remoteAddress}: [$statusCode] $reason")
        job = null
        pingJob?.cancel("client closed")
        pingJob = null
        log.info("Socket Closed: [$statusCode] $reason")
    }

    override fun onWebSocketError(cause: Throwable) {
        super.onWebSocketError(cause)
        log.error("websocket error", cause)
    }

}