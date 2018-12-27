package proxy.ian4hu.github.com

import java.util.logging.Logger
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


private val logger = Logger.getLogger("UiServlet")

class UiServlet : HttpServlet() {

    override fun service(req: HttpServletRequest, resp: HttpServletResponse) {
        //super.service(req, resp);
        val builder = StringBuilder()
        val protocol = req.protocol
        val servletPath = req.servletPath
        val queryString = req.queryString
        builder.append(protocol).append(" ").append(servletPath)
        if (queryString != null) {
            builder.append("?").append(queryString)
        }
        builder.append("\n")
        req.headerNames.toList().forEach { headerName ->
            builder.append(headerName).append(": ")
            req.getHeaders(headerName).toList().joinTo(builder, ", ")
            builder.append("\n")
        }

        resp.setHeader("content-type", "text/plain;charset=utf-8")
        val content = builder.toString()
        resp.writer.println(content)
        logger.info(content)
    }
}