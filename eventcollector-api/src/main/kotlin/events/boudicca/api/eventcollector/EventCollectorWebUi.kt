package events.boudicca.api.eventcollector

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import events.boudicca.api.eventcollector.collections.Collections
import events.boudicca.api.eventcollector.collections.SingleCollection
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import java.io.StringWriter
import java.net.InetSocketAddress
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
        setupSingleCollection()
    }

    private fun setupIndex() {
        server.createContext("/") {
            try {
                var fullCollection = Collections.getCurrentFullCollection()
                var isFullCollectionOngoing = true
                if (fullCollection == null) {
                    isFullCollectionOngoing = false
                    fullCollection = Collections.getAllPastCollections().lastOrNull()
                }

                val context = VelocityContext()

                context.put("hasFullCollection", fullCollection != null)

                if (fullCollection != null) {
                    context.put("isFullCollectionOngoing", isFullCollectionOngoing)
                    context.put(
                        "fullCollectionDuration",
                        formatDuration(fullCollection.startTime, fullCollection.endTime)
                    )
                    context.put(
                        "fullCollectionStartEndTime",
                        formatStartEndTime(fullCollection.startTime, fullCollection.endTime)
                    )

                    val singleCollections = fullCollection.singleCollections
                        .map {
                            mapSingleCollectionToFrontend(it)
                        }
                        .associateBy { it["name"]!! }
                    context.put("singleCollections",
                        scheduler.getCollectors()
                            .map { it.getName() }
                            .sorted()
                            .map {
                                singleCollections[it] ?: mapOf(
                                    "id" to null,
                                    "name" to it,
                                    "duration" to "-",
                                    "startEndTime" to "-",
                                )
                            }
                    )
                }

                sendResponse(it, "/html/index.html.vm", context)
            } catch (e: Exception) {
                e.printStackTrace()
                send500(it)
            }
        }
    }

    private fun mapSingleCollectionToFrontend(it: SingleCollection): Map<String, String?> {
        return mapOf(
            "id" to it.id.toString(),
            "name" to it.collector!!.getName(),
            "duration" to formatDuration(it.startTime, it.endTime),
            "startEndTime" to formatStartEndTime(it.startTime, it.endTime),
        )
    }

    private fun setupSingleCollection() {
        server.createContext("/singleCollection") {
            try {
                val id = parseId(it.requestURI.query)
                if (id != null) {
                    val singleCollection = findSingleCollection(id)
                    if (singleCollection != null) {

                        val context = VelocityContext()

                        context.put("singleCollection", mapSingleCollectionToFrontend(singleCollection))
                        context.put("httpCalls",
                            singleCollection.httpCalls
                                .sortedBy { it.startTime }
                                .map {
                                    mapOf(
                                        "id" to it.id,
                                        "url" to it.url,
                                        "responseCode" to it.responseCode,
                                        "duration" to formatDuration(it.startTime, it.endTime),
                                        "startEndTime" to formatStartEndTime(it.startTime, it.endTime),
                                    )
                                }
                        )

                        sendResponse(it, "/html/singleCollection.html.vm", context)
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

    private fun findSingleCollection(id: Int): SingleCollection? {
        return Collections.getAllPastCollections()
            .union(listOf(Collections.getCurrentFullCollection()).filterNotNull())
            .flatMap { it.singleCollections }
            .find { it.id == id }
    }

    private fun parseId(query: String?): Int? {
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
            return id[1].toInt()
        }
        return null
    }

    private fun formatStartEndTime(startTime: Long, endTime: Long): String {
        val startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.of("CET"))
        val formattedStartTime = DateTimeFormatter.ofPattern("d.M.uu HH:mm").format(startTime)

        if (endTime == 0L) {
            return formattedStartTime +
                    " / ..."
        }

        val endTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.of("CET"))
        if (startTime.toLocalDate().equals(endTime.toLocalDate())) {
            return formattedStartTime +
                    " / " +
                    DateTimeFormatter.ofPattern("HH:mm").format(endTime)
        } else {
            return formattedStartTime +
                    " / " +
                    DateTimeFormatter.ofPattern("d.M.uu HH:mm").format(endTime)
        }
    }

    private fun formatDuration(startTime: Long, endTime: Long): String {
        val realEndTime = if (endTime != 0L) endTime else System.currentTimeMillis()
        val durationInMillis = realEndTime - startTime
        val duration = Duration.ofMillis(durationInMillis)
        if (duration.compareTo(Duration.ofSeconds(5)) < 0) {
            return "${duration.toMillis()} ms"
        } else if (duration.compareTo(Duration.ofMinutes(5)) < 0) {
            return "${duration.toSeconds()} s"
        } else {
            return "${duration.toMinutes()} m"
        }
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
        val bytes = content.toByteArray(Charsets.UTF_8)
        it.responseHeaders.add("Content-Type", contentType)
        it.sendResponseHeaders(responseCode, bytes.size.toLong())
        it.responseBody.write(bytes)
        it.close()
    }

    fun start() {
        println("webui starting and listening on ${server.address}")
        server.start()
    }

}
