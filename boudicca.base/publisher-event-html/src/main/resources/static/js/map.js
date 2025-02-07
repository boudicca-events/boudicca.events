document.addEventListener("DOMContentLoaded", () => {
  let map = L.map('map').setView([47.6964118, 13.3457608], 7);

  L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
  }).addTo(map);

  async function fetchEvents() {
    let response = await fetch("api/mapSearch")
    let searchResult = await response.json()

    console.log(searchResult)
    if(searchResult.error){
      alert("whoops: "+searchResult.error)
      return
    }
    for(let event of searchResult.result){
      let marker = L.marker([48.31184357, 14.3116359]).addTo(map);
      marker.bindPopup(event.name)
    }
  }

  fetchEvents()
});
