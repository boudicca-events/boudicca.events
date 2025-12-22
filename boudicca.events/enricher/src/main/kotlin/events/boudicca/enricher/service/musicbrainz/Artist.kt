package events.boudicca.enricher.service.musicbrainz

data class Artist(val name: String, val genre: String?, val lowercaseName: String = name.lowercase())
