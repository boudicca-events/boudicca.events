package at.cnoize.boudicca.api.util

import java.lang.RuntimeException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers

class Http {

    companion object {
        val HTTP = HttpClient.newHttpClient();
    }

    fun httpGet(url: String): String {
        val response = HTTP.send(HttpRequest.newBuilder(URI.create(url)).GET().build(), BodyHandlers.ofString())
        if (response.statusCode() == 200) {
            return response.body()
        } else {
            throw RuntimeException("invalid http return code: " + response.statusCode())
        }
    }

    fun httpPost(url: String, body: String): String? {
        println(body)
        val response = HTTP.send(HttpRequest.newBuilder(URI.create(url)).POST(BodyPublishers.ofString(body)).header("Content-Type", "application/json").build(), BodyHandlers.ofString())
        return if (response.statusCode() == 200) {
            response.body()
        }else if (response.statusCode() == 204) {
            null
        } else {
            throw RuntimeException("invalid http return code: " + response.statusCode())
        }
    }
}