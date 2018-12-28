package proxy.ian4hu.github.com

import org.eclipse.jetty.client.api.Request
import org.eclipse.jetty.http.HttpHeader
import javax.servlet.http.HttpServletRequest
import org.eclipse.jetty.proxy.AsyncProxyServlet as JettyProxtServlet

class ProxyServlet : JettyProxtServlet() {

    private var transparent = false

    override fun init() {
        super.init()
        transparent = servletConfig.getInitParameter("transparent").orEmpty().toBoolean()
    }

    override fun addProxyHeaders(clientRequest: HttpServletRequest?, proxyRequest: Request?) {
        if (!transparent) {
            super.addProxyHeaders(clientRequest, proxyRequest)
        }
    }

    /*
    override fun customizeProxyRequest(proxyRequest: Request, request: HttpServletRequest) {
        super.customizeProxyRequest(proxyRequest, request)
        if (transparent) {
            val headers = proxyRequest.headers
            headers.remove(HttpHeader.VIA)
            headers.remove(HttpHeader.X_FORWARDED_FOR)
            headers.remove(HttpHeader.X_FORWARDED_PROTO)
            headers.remove(HttpHeader.X_FORWARDED_HOST)
            headers.remove(HttpHeader.X_FORWARDED_SERVER)
        }
    }
    */
}