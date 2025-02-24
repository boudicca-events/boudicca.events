document.addEventListener("DOMContentLoaded", () => {
  let map = L.map('map').setView([47.6964118, 13.3457608], 7);

  L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
  }).addTo(map);

  async function fetchEvents(map) {
    let response = await fetch("api/mapSearch"+location.search)
    let searchResult = await response.json()

    if (searchResult.error) {
      alert("whoops: " + searchResult.error)
      return
    }
    if(searchResult.locations.length === 0){
      return
    }
    let maxLat = searchResult.locations[0].latitude
    let minLat = searchResult.locations[0].latitude
    let maxLon = searchResult.locations[0].longitude
    let minLon = searchResult.locations[0].longitude
    for (let location of searchResult.locations) {
      maxLat = Math.max(maxLat, location.latitude)
      minLat = Math.min(minLat, location.latitude)
      maxLon = Math.max(maxLon, location.longitude)
      minLon = Math.min(minLon, location.longitude)
      let marker = L.marker([location.latitude, location.longitude]).addTo(map);
      marker.bindPopup(createMarkerText(location))
    }
    map.fitBounds([
      [minLat, minLon],
      [maxLat, maxLon]
    ]);
  }

  function createMarkerText(location) {
    let div = document.createElement("div")

    if (location.url) {
      let locationA = document.createElement("a")
      locationA.target = "_blank"
      locationA.href = location.url
      locationA.text = location.name
      div.appendChild(locationA)
    } else {
      let locationSpan = document.createElement("span")
      locationSpan.textContent = location.name
      div.appendChild(locationSpan)
    }

    div.appendChild(document.createElement("hr"))

    let eventUl = document.createElement("ul")

    for (let event of location.events) {
      let eventLi = document.createElement("li")
      if (event.url) {
        let eventA = document.createElement("a")
        eventA.target = "_blank"
        eventA.href = event.url
        eventA.text = event.name
        eventLi.appendChild(eventA)
      } else {
        let eventSpan = document.createElement("span")
        eventSpan.textContent = event.name
        eventLi.appendChild(eventSpan)
      }
      eventUl.appendChild(eventLi)
    }

    div.appendChild(eventUl)

    return div.innerHTML
  }

  fetchEvents(map)
});
