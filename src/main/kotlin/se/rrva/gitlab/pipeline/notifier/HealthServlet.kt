package se.rrva.gitlab.pipeline.notifier

import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class HealthServlet : HttpServlet() {
    override fun service(request: HttpServletRequest, response: HttpServletResponse) {
        response.status = 200
        response.writer.println("OK")
    }
}