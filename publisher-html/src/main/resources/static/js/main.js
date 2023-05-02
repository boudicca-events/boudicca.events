document.addEventListener("DOMContentLoaded", () => {
  const searchForm = document.getElementById("searchForm");
  const eventsContainer = document.getElementById("eventsContainer");

  const createEventDomElement = (event) => {
    // TODO: re use handlebars template
    const domElement = `<div class="event">
            <svg class="event-image" title="Event Bild" viewBox="0 0 512 512" >
                <use xlink:href="#image"></use>  
            </svg>

            <div class="event-description">
                <p class="event-title">${event.name}</p>
                <div class="event-details-wrapper">
                    <div class="event-details">
                        <svg height="28px" width="28px" title="Datum Logo" viewBox="0 0 512 512" >
                            <use xlink:href="#time"></use>  
                        </svg>
                        <p>${event.startDate}</p>
                    </div>
                        <div class="event-details">
                            <svg height="28px" width="28px" title="Ort Logo" viewBox="0 0 512 512" >
                            <use xlink:href="#location"></use>  
                        </svg>
                        <p>todo</p>
                      </div>
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
