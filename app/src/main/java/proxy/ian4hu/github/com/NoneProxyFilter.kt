package proxy.ian4hu.github.com

import android.util.LruCache
import java.net.InetAddress
import java.net.NetworkInterface
import javax.servlet.*

private val ATTR_NONE_PROXY_APPLIED = NoneProxyFilter::class.java.name + ".applied";

class NoneProxyFilter : Filter {

    private val localhostNameCache: LruCache<String, Boolean> = LruCache(512)

    override fun destroy() {
        localhostNameCache.evictAll()
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val targetHost = request.serverName
        if (
                // The request must from outside
                (DispatcherType.REQUEST.equals(request.dispatcherType) || DispatcherType.ASYNC.equals(request.dispatcherType))
                // Only filter once
                && true != request.getAttribute(ATTR_NONE_PROXY_APPLIED)
                // Only filter localhost
                && isLocalhost(targetHost)
        ) {
            request.setAttribute(ATTR_NONE_PROXY_APPLIED, true)
            request.servletContext.getNamedDispatcher("ui").forward(request, response)
        } else {
            chain.doFilter(request, response)
        }
    }

    override fun init(filterConfig: FilterConfig?) {
        val localhostAddress = NetworkInterface.getNetworkInterfaces().toList()
                .flatMap { it.inetAddresses.toList() }
                .map { it.hostAddress.substringBefore('%') }
                .toSet()
        localhostAddress.forEach {
            localhostNameCache.put(it, true)
        }
    }

    private fun isLocalhost(host: String): Boolean {
        var isLocal = localhostNameCache.get(host)
        if (isLocal == null) {
            isLocal = InetAddress.getAllByName(host)
                    .map { it.hostAddress }
                    .any { localhostNameCache.get(it) ?: false }
            localhostNameCache.put(host, isLocal)
        }
        return isLocal;
    }
}