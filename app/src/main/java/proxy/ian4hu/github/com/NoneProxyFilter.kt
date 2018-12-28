package proxy.ian4hu.github.com

import android.util.LruCache
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.UnknownHostException
import javax.servlet.*

private val ATTR_NONE_PROXY_APPLIED = NoneProxyFilter::class.java.name + ".applied";

class NoneProxyFilter : Filter {

    private val localHostNameCache: LruCache<String, Boolean> = LruCache(512)

    private lateinit var localHostNames: Set<String>

    private var resolve = false

    override fun destroy() {
        localHostNameCache.evictAll()
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
        filterConfig?.run {
            resolve = "true".equals(getInitParameter("resolve"), true)
        }
        localHostNames = NetworkInterface.getNetworkInterfaces().toList()
                .flatMap { it.inetAddresses.toList() }
                .map { it.hostAddress.substringBefore('%') }
                .toSet() + InetAddress.getLocalHost().let { setOf(it.hostName, it.hostAddress) }
        localHostNames.forEach {
            localHostNameCache.put(it, true)
        }
        InetAddress.getLocalHost().run {
            localHostNameCache.put(hostName, true)
            localHostNameCache.put(hostAddress, true)
        }
    }

    private fun isLocalhost(host: String): Boolean {
        return localHostNames.contains(host) || (resolve && resolveAndCache(host))
    }

    private fun resolveAndCache(host: String): Boolean {
        var isLocal = localHostNameCache.get(host)
        if (isLocal == null) {
            try {
                isLocal = InetAddress.getAllByName(host)
                        .map { it.hostAddress }
                        .any { localHostNameCache.get(it) ?: false }
            } catch (e: UnknownHostException) {
                isLocal = false
            }
            localHostNameCache.put(host, isLocal)
        }
        return isLocal;
    }
}