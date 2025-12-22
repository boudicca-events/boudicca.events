package base.boudicca.enricherutils

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.zip.GZIPOutputStream
import kotlin.streams.asSequence

fun main() {
    val reader = BufferedReader(FileReader(File("./boudicca.base/enricher-utils/data/artist")))
    val dataOut =
        OutputStreamWriter(
            GZIPOutputStream(
                BufferedOutputStream(
                    FileOutputStream(
                        "./boudicca.base/enricher-utils/data/artist_parsed.json.gz",
                        false,
                    ),
                ),
            ),
        )
    val indexOut =
        GZIPOutputStream(
            BufferedOutputStream(
                FileOutputStream(
                    "./boudicca.base/enricher-utils/data/artist.index.gz",
                    false,
                ),
            ),
        )

    val artists =
        reader
            .lines()
            .asSequence()
//        .take(100)
            .map { mapArtist(it) }
            .toList()

    val filteredArtists = getFilteredArtists(artists)

    serialize(filteredArtists).write(dataOut)
    writeIndex(indexOut, filteredArtists)
    indexOut.close()
    dataOut.close()
    reader.close()
}

@Suppress("detekt:MagicNumber")
fun writeIndex(
    indexOut: OutputStream,
    filteredArtists: List<Artist>,
) {
    val allNames = filteredArtists.mapIndexed { i, artist -> Pair(i, artist.name.lowercase()) }
    val sortedList = allNames.sortedBy { it.second }

    for (pair in sortedList) {
        val i = pair.first
        indexOut.write(i.shr(24))
        indexOut.write(i.shr(16))
        indexOut.write(i.shr(8))
        indexOut.write(i)
    }
}

private const val MIN_ARTIST_NAME_LENGTH = 3

private fun getFilteredArtists(artists: List<Artist>): List<Artist> {
    var filtered =
        artists.filter {
            it.name.length >= MIN_ARTIST_NAME_LENGTH // && it.aliases.all { it.length >= 3 }
        }

    filtered = filtered.filter { !it.ended }

//    filtered = filtered.filter { it.genre != null }

    val names = filtered.map { it.name.lowercase() }.groupBy { it }.mapValues { it.value.size }
    filtered = filtered.filter { names[it.name.lowercase()]!! == 1 }

    return filtered
}

fun serialize(artists: List<Artist>): JSONArray {
    val array = JSONArray()
    for (artist in artists) {
        val artistObject = JSONObject()
        artistObject.put("name", artist.name)
        artistObject.put("genre", artist.genre)
        val aliasesArray = JSONArray()
        for (alias in artist.aliases) {
            aliasesArray.put(alias)
        }
        array.put(artistObject)
    }
    return array
}

fun mapArtist(line: String): Artist {
    val jsonObject = JSONObject(line)
    val name = jsonObject.getString("name")
    val genre = mapGenre(jsonObject.getJSONArray("genres"))
    val aliases = jsonObject.getJSONArray("aliases").map { (it as JSONObject).getString("name") }
    val ended = jsonObject.has("ended") && jsonObject.getBoolean("ended")
    return Artist(name, genre, aliases, ended)
}

fun mapGenre(jsonArray: JSONArray): String? {
    val list = mutableListOf<Pair<String, Int>>()
    for (entry in jsonArray) {
        val obj = entry as JSONObject
        list.add(Pair(obj.getString("name"), obj.getInt("count")))
    }
    return list
        .sortedWith(
            Comparator
                .comparing<Pair<String, Int>?, Int?> { it.second }
                .reversed()
                .then(Comparator.comparing { it.first }),
        ).firstOrNull()
        ?.first
}

data class Artist(
    val name: String,
    val genre: String?,
    val aliases: List<String>,
    val ended: Boolean,
)
