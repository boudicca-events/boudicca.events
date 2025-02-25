package base.boudicca.enricher.service.location

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.FileInputStream

class LocationEnricherGoogleSheetsUpdater(
    private val googleCredentialsPath: String,
    private val spreadsheetId: String,
) : LocationEnricherUpdater {
    private val logger = KotlinLogging.logger {}
    private val JSON_FACTORY = GsonFactory.getDefaultInstance()
    private val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
    private val range = "LocationData!A1:Z"
    private val credentials = createCredentials()
    private val service = Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, HttpCredentialsAdapter(credentials))
        .setApplicationName("Boudicca Location Data Enricher")
        .build()

    private fun createCredentials(): GoogleCredentials {
        var credentials: GoogleCredentials =
            GoogleCredentials.fromStream(FileInputStream(googleCredentialsPath))
        credentials = credentials.createScoped(SheetsScopes.SPREADSHEETS_READONLY)
        return credentials
    }

    override fun updateData(): List<LocationData> {
        credentials.refreshIfExpired()
        val response: ValueRange = service.spreadsheets().values()[spreadsheetId, range]
            .execute()
        val values: List<List<Any?>>? = response.getValues()
        if (values.isNullOrEmpty()) {
            logger.error { "no data found in spreadsheet!" }
            return emptyList()
        }

        val headers = values[0]
            .mapIndexed { i, value -> Pair(i, value?.toString()?.trim()) }

        val allLocationData = mutableListOf<LocationData>()
        for (row in values.subList(1, values.size)) {
            val locationData = mutableMapOf<String, List<String>>()

            for (header in headers) {
                if (header.second.isNullOrBlank() || header.first >= row.size) {
                    continue
                }
                val value = row[header.first] as String?
                if (!value.isNullOrBlank()) {
                    locationData[header.second!!] = value.split("\n")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                }
            }
            allLocationData.add(locationData)
        }

        return allLocationData
    }
}
