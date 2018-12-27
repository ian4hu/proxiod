package proxy.ian4hu.github.com

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import org.eclipse.jetty.proxy.ConnectHandler
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.FilterMapping
import org.eclipse.jetty.servlet.Holder
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.util.component.LifeCycle
import org.slf4j.LoggerFactory
import java.util.*
import javax.servlet.DispatcherType

// TODO: Rename actions, choose action names that describe tasks that this
// IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
const val ACTION_START = "proxy.ian4hu.github.com.action.START"
const val ACTION_STOP = "proxy.ian4hu.github.com.action.STOP"
const val ACTION_STATUS = "proxy.ian4hu.github.com.action.STATUS"
const val BROADCAST_STATUS = "proxy.ian4hu.github.com.broadcast.STATUS"

// TODO: Rename parameters
//private const val EXTRA_PARAM1 = "proxy.ian4hu.github.com.extra.PARAM1"
//private const val EXTRA_PARAM2 = "proxy.ian4hu.github.com.extra.PARAM2"

private val logger = LoggerFactory.getLogger(JettyService::class.java)

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
class JettyService : IntentService("JettyService"), LifeCycle.Listener {

    override fun onCreate() {
        super.onCreate()
        logger.info("onCreate")
        broadcastState()
        server.addLifeCycleListener(this)
    }

    private fun broadcastState() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(BROADCAST_STATUS).apply {
            val bundle = Bundle()
            bundle.putString("state", server.state)
            putExtras(bundle)
        })
    }

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_START -> {
                if (server.isRunning) {
                    return;
                }
                server.start()
                broadcastState()
                //server.join()
            }
            ACTION_STOP -> {
                if (!server.isRunning) {
                    return;
                }
                server.stop()
                broadcastState()
            }
            ACTION_STATUS -> {
                broadcastState()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.info("onDestroy")
        server.removeLifeCycleListener(this)
    }


    override fun lifeCycleStarted(event: LifeCycle?) {
        logger.info("lifeCycleStarted")
        broadcastState()
        logger.info(server.dump())
    }

    override fun lifeCycleStopped(event: LifeCycle?) {
        logger.info("lifeCycleStopped")
        broadcastState()
    }

    override fun lifeCycleFailure(event: LifeCycle?, cause: Throwable?) {
        logger.warn("lifeCycleFailure", cause)
        broadcastState()
    }

    override fun lifeCycleStopping(event: LifeCycle?) {
        logger.info("lifeCycleStopping")
        broadcastState()
    }

    override fun lifeCycleStarting(event: LifeCycle?) {
        logger.info("lifeCycleStarting")
        broadcastState()
    }

    companion object {
        @JvmStatic
        val server: Server;

        init {

            server = Server(8080);
            //server.isDumpAfterStart = true

            val servletContextHandler = ServletContextHandler()
            server.handler = ConnectHandler(servletContextHandler)

            val servletHandler = ServletHandler().apply {

                val proxyServlet = addServletWithMapping(proxy.ian4hu.github.com.ProxyServlet::class.java, "/").apply {
                    isAsyncSupported = true
                    setInitParameter("maxThreads", "16")
                    setInitParameter("maxConnections", "256")
                    setInitParameter("transparent", "true")
                }

                //FilterMapping
                val noneProxyFilter = newFilterHolder(Holder.Source.EMBEDDED).apply {
                    heldClass = NoneProxyFilter::class.java
                }

                val filterMapping = FilterMapping().apply {
                    filterName = noneProxyFilter.name
                    setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC))
                    setServletName(proxyServlet.name)
                }
                addFilter(noneProxyFilter, filterMapping)

                newServletHolder(Holder.Source.EMBEDDED).apply {
                    heldClass = UiServlet::class.java
                    isAsyncSupported = true
                    name = "ui"
                    addServlet(this)
                }
            }

            servletContextHandler.servletHandler = servletHandler

        }

        /**
         * Starts this service to perform action Foo with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        // TODO: Customize helper method
        @JvmStatic
        fun start(context: Context, action: String, extras: Bundle) {
            val intent = Intent(context, JettyService::class.java).apply {
                this.action = action
                putExtras(extras)
            }
            context.startService(intent)
        }

    }
}
