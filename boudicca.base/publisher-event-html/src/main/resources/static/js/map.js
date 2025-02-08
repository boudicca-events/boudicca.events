document.addEventListener("DOMContentLoaded", () => {
  let map = L.map('map').setView([47.6964118, 13.3457608], 7);

  L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
  }).addTo(map);

  async function fetchEvents() {
    let response = await fetch("api/mapSearch"+location.search)
    let searchResult = await response.json()

    console.log(searchResult)
    if (searchResult.error) {
      alert("whoops: " + searchResult.error)
      return
    }
    for (let location of searchResult.locations) {
      let marker = L.marker([location.latitude, location.longitude]).addTo(map);
      marker.bindPopup(createMarkerText(location))
    }
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
      locationSpan.text = location.name
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
        eventSpan.text = event.name
        eventLi.appendChild(eventSpan)
      }
      eventUl.appendChild(eventLi)
    }

    div.appendChild(eventUl)

    return div.innerHTML
  }

  fetchEvents()
});
