package base.boudicca.publisher.event.html.model

data class MapSearchResultDTO(val error: String?, val locations: List<Location>)

data class Location(val name: String, val url: String?, val latitude: Double, val longitude: Double, val events: List<LocationEvent>)

data class LocationEvent(val name: String, val url: String?)
