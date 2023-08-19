package events.boudicca.api.eventcollector

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import java.io.StringWriter
import java.net.InetSocketAddress

class EventCollectorWebUi(port: Int, private val scheduler: EventCollectorScheduler) {

    private val server: HttpServer
    private val ve: VelocityEngine = VelocityEngine()

    init {
        //set velocity to load templates from the classpath
        ve.setProperty("resource.loader", "classpath")
        ve.setProperty(
            "classpath.resource.loader.class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader"
        )

        server = HttpServer.create(InetSocketAddress(port), 0)
        setupStaticFolder("/css/")
        setupStaticFolder("/js/")
        setupIndex()
    }

    private fun setupIndex() {
        server.createContext("/") {
            val context = VelocityContext()
            context.put("allCollectorNames", getAllCollectorNames())

            sendResponse(it, "/html/index.html.vm", context)
        }
    }

    private fun getAllCollectorNames(): List<String> {
        return scheduler.getCollectors().map { it.getName() }
    }

    private fun setupStaticFolder(prefix: String) {
        server.createContext(prefix) {
            val normalizedUri = it.requestURI.normalize().toString()
            if (normalizedUri.startsWith(prefix)) {
                sendResponse(it, "${normalizedUri}.vm", VelocityContext())
            }
            send404(it)
        }
    }

    private fun send404(it: HttpExchange) {
        it.sendResponseHeaders(404, 0)
    }

    private fun sendResponse(it: HttpExchange, templateName: String, context: VelocityContext) {

        val response = try {
            val template = ve.getTemplate(templateName)
            val writer = StringWriter()
            template.merge(context, writer)

            writer.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            "something went wrong"
        }

        val bytes = response.toByteArray(Charsets.UTF_8)

        it.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
        it.sendResponseHeaders(200, bytes.size.toLong())
        it.responseBody.write(bytes)
        it.close()
    }

    fun start() {
        println("webui starting and listening on ${server.address}")
        server.start()
    }

}
