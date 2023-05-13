document.addEventListener("DOMContentLoaded", () => {
  const searchForm = document.getElementById("searchForm");
  const eventsContainer = document.getElementById("eventsContainer");
  const filterButton = document.getElementById("filterButton");
  const drawer = document.getElementById("drawer");
  const closeDrawerButton = document.getElementById("closeDrawerButton");
  const filterSearchButton = document.getElementById("filterSearchButton");

  filterSearchButton.addEventListener("click", () => {
    drawer.classList.remove("drawer-open");
  });

  closeDrawerButton.addEventListener("click", () => {
    drawer.classList.remove("drawer-open");
  });

  filterButton.addEventListener("click", () => {
    drawer.classList.add("drawer-open");
  });

  document.addEventListener("click", (event) => {
    if (
      !drawer.contains(event.target) &&
      !filterButton.contains(event.target)
    ) {
      drawer.classList.remove("drawer-open");
    }
  });

  const createEventDomElement = (event) => {
    // TODO: re use handlebars template
    const domElement = `<div class="event">
            <div class="event-image${
              event.type ? ` event-image-${event.type}` : ""
            }">
                ${
                  event.pictureUrl
                    ? `<img src="${event.pictureUrl}" height="100px" alt="Eventbild" />`
                    : `<svg title="Event Bild" viewBox="0 0 512 512" height="100px" >
                <use xlink:href="#${event.type ?? "image"}"></use>  
                </svg>`
                }
                

                ${
                  event.url
                    ? `<a class="anchor-to-eventpage" href="${event.url}" target="_blank" aria-describedby="Zur Eventseite von ${event.name}. (neues Fenster wird geöffnet)">
                    Zur Eventseite
                  </a>`
                    : ""
                }
            </div>

            <div class="event-description">
              <p class="event-title">
                  ${event.name}
              </p>
                <div class="event-details-wrapper">
                    <div class="event-details">
                        <svg height="24px" width="24px" title="Datum Logo" viewBox="0 0 512 512" >
                            <use xlink:href="#time"></use>  
                        </svg>
                        <p>${event.startDate}</p>
                    </div>
                        <div class="event-details">
                            <svg height="24px" width="24px" title="Ort Logo" viewBox="0 0 512 512" >
                            <use xlink:href="#location"></use>  
                        </svg>
                        <p>${event.locationName}${
      event.city ? `, ${event.city}` : ""
    }</p>
                      </div>

                      ${
                        event.description
                          ? ` <div class="event-details-description">
                      <details>
                          <summary>Mehr zum Event lesen</summary>
                          <p>${event.description} </p>
                      </details>
                  </div>`
                          : ""
                      } 
                </div>
            </div>
          </div>`;
    return domElement;
  };

  const goTo = (url) => {
    if ("undefined" !== typeof history.pushState) {
      history.pushState({}, "", url);
    } else {
      window.location.assign(url);
    }
  };

  const onSearch = async (e) => {
    e.preventDefault();
    const paramsAsString = new URLSearchParams(
      new FormData(e.target)
    ).toString();
    const apiUrl = `/api/search?${paramsAsString}`;

    try {
      const response = await fetch(apiUrl);
      const data = await response.json();

      const domEvents = data
        .map((event) => createEventDomElement(event))
        .join("");
      eventsContainer.innerHTML = domEvents;

      goTo(`/search?${paramsAsString}`);
    } catch (e) {
      console.error(e);
    }
  };

  // TODO: could use `Proxy`
  const params = new URLSearchParams(window.location.search);
  const hydrateFormValues = () => {
    params.forEach((x, y) => (document.getElementById(y).value = x));
  };
  hydrateFormValues();

  searchForm.addEventListener("submit", onSearch);
});
