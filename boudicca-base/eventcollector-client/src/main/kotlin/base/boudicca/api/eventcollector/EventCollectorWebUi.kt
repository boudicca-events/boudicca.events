package base.boudicca.api.eventcollector

import base.boudicca.api.eventcollector.collections.Collections
import base.boudicca.api.eventcollector.collections.FullCollection
import base.boudicca.api.eventcollector.collections.HttpCall
import base.boudicca.api.eventcollector.collections.SingleCollection
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.tools.generic.EscapeTool
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.StringWriter
import java.net.InetSocketAddress
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class EventCollectorWebUi(port: Int, private val scheduler: EventCollectorScheduler) {

    private val server: HttpServer
    private val ve: VelocityEngine = VelocityEngine()
    private val LOG = LoggerFactory.getLogger(this::class.java)

    init {
        //set velocity to load templates from the classpath
        ve.setProperty("resource.loaders", "classpath")
        ve.setProperty(
            "resource.loader.classpath.class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader"
        )

        server = HttpServer.create(InetSocketAddress(port), 0)
        setupStaticFolder("/css/")
        setupStaticFolder("/js/")
        setupIndex()
        setupSingleCollection()
        setupFullCollection()
        setupNoFavicon()
    }

    private fun setupIndex() {
        server.createContext("/") { httpExchange ->
            try {
                val context = VelocityContext()

                val fullCollection = Collections.getCurrentFullCollection()
                context.put("hasOngoingFullCollection", fullCollection != null)

                if (fullCollection != null) {
                    context.put(
                        "fullCollection",
                        mapFullCollectionToFrontEnd(fullCollection)
                    )
                    context.put("log", formatLogLines(fullCollection.logLines))
                }

                context.put("fullCollections",
                    Collections.getAllPastCollections().map { mapFullCollectionToFrontEnd(it) })

                sendResponse(httpExchange, "/html/index.html.vm", context)
            } catch (e: Exception) {
                LOG.error("error while handling request", e)
                send500(httpExchange)
            }
        }
    }

    private fun setupSingleCollection() {
        server.createContext("/singleCollection") { httpExchange ->
            try {
                val id = parseId(httpExchange.requestURI.query)
                if (id != null) {
                    val singleCollection = findSingleCollection(id)
                    if (singleCollection != null) {
                        val context = VelocityContext()

                        context.put("singleCollection", mapSingleCollectionToFrontend(singleCollection))
                        context.put("httpCalls",

                            singleCollection.httpCalls
                                .sortedBy { it.startTime }
                                .map {
                                    mapHttpCallToFrontend(it)
                                }
                        )
                        context.put("log", formatLogLines(singleCollection.logLines))

                        sendResponse(httpExchange, "/html/singleCollection.html.vm", context)
                        return@createContext
                    }
                }
                send404(httpExchange)
            } catch (e: Exception) {
                e.printStackTrace()
                send500(httpExchange)
            }
        }
    }

    private fun setupFullCollection() {
        server.createContext("/fullCollection") {
            try {
                val id = parseId(it.requestURI.query)
                if (id != null) {
                    val fullCollection = findFullCollection(id)
                    if (fullCollection != null) {
                        val context = VelocityContext()

                        context.put("fullCollection", mapFullCollectionToFrontEnd(fullCollection))

                        context.put("log", formatLogLines(fullCollection.logLines))

                        sendResponse(it, "/html/fullCollection.html.vm", context)
                        return@createContext
                    }
                }
                send404(it)
            } catch (e: Exception) {
                e.printStackTrace()
                send500(it)
            }
        }
    }

    private fun setupNoFavicon() {
        server.createContext("/favicon.ico") {
            send404(it)
        }
    }

    private fun mapFullCollectionToFrontEnd(fullCollection: FullCollection): Map<String, *> {
        val singleCollections = fullCollection.singleCollections.associateBy { it.collector!!.getName() }

        return mapOf(
            "id" to fullCollection.id.toString(),
            "duration" to formatDuration(fullCollection.startTime, fullCollection.endTime),
            "startEndTime" to formatStartEndTime(fullCollection.startTime, fullCollection.endTime),
            "errorsCount" to fullCollection.singleCollections
                .flatMap { it.logLines }
                .union(fullCollection.logLines)
                .count { it.first }.toString(),
            "totalEventsCollected" to fullCollection.singleCollections
                .sumOf { it.totalEventsCollected ?: 0 },
            "singleCollections" to
                    scheduler.getCollectors()
                        .map { it.getName() }
                        .sorted()
                        .map {
                            mapSingleCollectionToFrontend(it, singleCollections[it])
                        }
        )
    }

    private fun mapSingleCollectionToFrontend(it: SingleCollection): Map<String, String?> {
        return mapSingleCollectionToFrontend(it.collector!!.getName(), it)
    }

    private fun mapSingleCollectionToFrontend(name: String, it: SingleCollection?): Map<String, String?> {
        if (it != null) {
            return mapOf(
                "id" to it.id.toString(),
                "name" to EscapeTool().html(it.collector!!.getName()),
                "duration" to formatDuration(it.startTime, it.endTime),
                "startEndTime" to formatStartEndTime(it.startTime, it.endTime),
                "errorsCount" to it.logLines.count { it.first }.toString(),
                "totalEventsCollected" to (it.totalEventsCollected ?: "-").toString(),
            )
        } else {
            return mapOf(
                "id" to null,
                "name" to name,
                "duration" to "-",
                "startEndTime" to "-",
                "errorsCount" to "-",
                "totalEventsCollected" to "-",
            )
        }
    }

    private fun mapHttpCallToFrontend(it: HttpCall): Map<String, String> {
        return mapOf(
            "url" to it.url!!,
            "responseCode" to if (it.responseCode == 0) "-" else it.responseCode.toString(),
            "duration" to formatDuration(it.startTime, it.endTime),
            "startEndTime" to formatStartEndTime(it.startTime, it.endTime),
            "postData" to (it.postData ?: ""),
        )
    }

    private fun findSingleCollection(id: UUID): SingleCollection? {
        return Collections.getAllPastCollections()
            .union(listOfNotNull(Collections.getCurrentFullCollection()))
            .flatMap { it.singleCollections }
            .find { it.id == id }
    }

    private fun findFullCollection(id: UUID): FullCollection? {
        return Collections.getAllPastCollections().find { it.id == id }
    }

    private fun parseId(query: String?): UUID? {
        if (query == null) {
            return null
        }

        val id = query.split('&')
            .map {
                it.split('=')
            }
            .find {
                it[0] == "id"
            }
        if (id != null) {
            return UUID.fromString(id[1])
        }
        return null
    }

    private fun formatStartEndTime(startTimeInMillis: Long, endTimeInMillis: Long): String {
        val startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimeInMillis), ZoneId.of("CET"))
        val formattedStartTime = DateTimeFormatter.ofPattern("d.M.uu HH:mm").format(startTime)

        if (endTimeInMillis == 0L) {
            return formattedStartTime +
                    " / ..."
        }

        val endTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTimeInMillis), ZoneId.of("CET"))
        return if (startTime.toLocalDate().equals(endTime.toLocalDate())) {
            formattedStartTime +
                    " / " +
                    DateTimeFormatter.ofPattern("HH:mm").format(endTime)
        } else {
            formattedStartTime +
                    " / " +
                    DateTimeFormatter.ofPattern("d.M.uu HH:mm").format(endTime)
        }
    }

    private fun formatDuration(startTime: Long, endTime: Long): String {
        val realEndTime = if (endTime != 0L) endTime else System.currentTimeMillis()
        val durationInMillis = realEndTime - startTime
        val duration = Duration.ofMillis(durationInMillis)
        return if (duration < Duration.ofSeconds(5)) {
            "${duration.toMillis()} ms"
        } else if (duration < Duration.ofMinutes(5)) {
            "${duration.toSeconds()} s"
        } else {
            "${duration.toMinutes()} m"
        }
    }

    private fun formatLogLines(logLines: List<Pair<Boolean, ByteArray>>): String? =
        EscapeTool().html(logLines.joinToString("\n") { String(it.second) })

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
        sendString(it, 404, "text/plain; charset=utf-8", "file not found")
    }

    private fun send500(it: HttpExchange) {
        sendString(it, 500, "text/plain; charset=utf-8", "something went wrong")
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

        sendString(it, 200, "text/html; charset=utf-8", response)
    }

    private fun sendString(it: HttpExchange, responseCode: Int, contentType: String, content: String) {
        try {
            val bytes = content.toByteArray(Charsets.UTF_8)
            it.responseHeaders.add("Content-Type", contentType)
            it.sendResponseHeaders(responseCode, bytes.size.toLong())
            it.responseBody.write(bytes)
            it.close()
        } catch (e: IOException) {
            LOG.debug("error sending response", e)
        }
    }

    fun start() {
        LOG.info("webui starting and listening on ${server.address}")
        server.start()
    }

    fun stop() {
        server.stop(2)
    }

}
