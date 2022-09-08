package se.rrva.gitlab.pipeline.notifier

import jakarta.servlet.http.HttpServletRequest
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.handler.ErrorHandler
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer
import java.io.Writer

fun main() {
    val server = Server(8080)
    val connector: Connector = ServerConnector(server)
    disableSendServerVersion(server)
    server.addConnector(connector)

    val wsHandler = ServletContextHandler(ServletContextHandler.SESSIONS)
    wsHandler.contextPath = "/"

    val errorHandler = errorHandler()
    wsHandler.errorHandler = errorHandler
    server.handler = wsHandler

    wsHandler.addServlet(HookServlet::class.java, "/hook")
    wsHandler.addServlet(HealthServlet::class.java, "/health")

    JettyWebSocketServletContainerInitializer.configure(wsHandler) { _, wsContainer ->
        wsContainer.maxTextMessageSize = 1024 * 1024
        wsContainer.addMapping("/events/*", EventSocket::class.java)
    }

    server.start()
}

private fun errorHandler() = object : ErrorHandler() {
    override fun writeErrorPage(
        request: HttpServletRequest?,
        writer: Writer?,
        code: Int,
        message: String?,
        showStacks: Boolean
    ) {
        writer?.write(message)
    }
}

private fun disableSendServerVersion(server: Server) {
    server.connectors.forEach { connector ->
        connector.connectionFactories.forEach {
            (it as? HttpConnectionFactory)?.httpConfiguration?.sendServerVersion = false
        }
    }
}

