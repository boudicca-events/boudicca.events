document.addEventListener("DOMContentLoaded", () => {
  const searchForm = document.getElementById("searchForm");
  const eventsContainer = document.getElementById("eventsContainer");
  const filterButton = document.getElementById("filterButton");
  const drawer = document.getElementById("drawer");
  const closeDrawerButton = document.getElementById("closeDrawerButton");
  const filterSearchButton = document.getElementById("filterSearchButton");
  const resetSearchFormButton = document.getElementById("resetSearchForm");
  const loadMoreButton = document.getElementById("loadMoreButton");
  const categorySelect = document.getElementById("category");
  const categoryFieldSets = document.querySelectorAll("[data-category-wanted]");

  ////// TODO: find a way to add event listener to newly added events after "merh laden" button is pressed
  ////// TODO: refactor prototype code, follow existing code style
  const events = document.querySelectorAll('.event');
  const modal = document.getElementById('event-details-modal');
  const modalContent = modal.querySelector('.modal-content');

  events.forEach(event => {
    event.addEventListener('click', () => {

      const eventAttributes = event.dataset;

      modalContent.innerHTML = `
        <div class="modal-flex-container">
            <div class="modal-column">
                <img src="/picture?uuid=${eventAttributes.pictureUuid}"
                    style="height:100px;max-height:250px;max-width:300px;"
                    alt="{{#if event.pictureAltText}}{{event.pictureAltText}}{{else}}Event Bild{{/if}}"
                    aria-label="Event Bild"
                    aria-labelledby="image"
                />
            </div>
            <div class="modal-column">
                <h2>${eventAttributes.name}</h2>
                <p><strong>Start Date:</strong> ${eventAttributes.startDate}</p>
                <p><strong>Location:</strong> ${eventAttributes.locationName}${eventAttributes.city ? ', ' + eventAttributes.city : ''}</p>
            </div>
        </div>
    
    `;

      modal.style.display = "block";
    });
  });

  window.onclick = function(event) {
    if (event.target == modal) {
      modal.style.display = "none";
    }
  };
  //////


  // <div class="modal-tabs">
  //     <button class="tab-button active" onclick="${openTab(event, 'tab1')}">Tab 1</button>
  //     <button class="tab-button" onclick="${openTab(event, 'tab2')}">Tab 2</button>
  //     <button class="tab-button" onclick="${openTab(event, 'tab3')}">Tab 3</button>
  // </div>

  // <div id="tab1" class="tab-content active">
  //   tab 1
  // </div>
  // <div id="tab2" class="tab-content">
  //   tab 2
  // </div>
  // <div id="tab3" class="tab-content">
  //   tab 3
  // </div>

  loadMoreButton.addEventListener("click", () => {
    onLoadMoreSearch();
  });

  resetSearchFormButton.addEventListener("click", () => {
    searchForm.reset();
  });

  filterSearchButton.addEventListener("click", () => {
    closeDrawer();
  });

  closeDrawerButton.addEventListener("click", () => {
    closeDrawer();
  });

  filterButton.addEventListener("click", () => {
    openDraw();
  });

  document.addEventListener("click", (event) => {
    if (
      !drawer.contains(event.target) &&
      !filterButton.contains(event.target)
    ) {
      closeDrawer();
    }
  });

  const openDraw = () => {
    drawer.classList.add("drawer-open");
    document.body.style.overflow = "hidden";
  };

  const closeDrawer = () => {
    drawer.classList.remove("drawer-open");
    document.body.style.overflow = "initial";
  };

  const goTo = (url) => {
    if ("undefined" !== typeof history.pushState) {
      history.pushState({}, "", url);
    } else {
      window.location.assign(url);
    }
  };

  const onSearchLoadMoreButtonBehaviour = (response) => {
    const loadMoreButton = document.getElementById("loadMoreButton");
    const endOfResultsInfo = document.getElementById("endOfResults");
    if (!loadMoreButton || !endOfResultsInfo) {
      return;
    }

    if (!response) {
      if (loadMoreButton) {
        loadMoreButton.style.display = "none";
        endOfResultsInfo.style.display = "block";
      }
    } else {
      loadMoreButton.style.display = "inline-block";
      endOfResultsInfo.style.display = "none";
    }
  };

  const onSearch = async (e) => {
    e.preventDefault();
    const paramsAsString = new URLSearchParams(
      new FormData(e.target)
    ).toString();
    const apiUrl = `/api/search?${paramsAsString}&offset=0`;

    try {
      const response = await fetch(apiUrl);
      const ssrDomEventString = await response.text();
      eventsContainer.innerHTML = ssrDomEventString;
      onSearchLoadMoreButtonBehaviour(ssrDomEventString);
      goTo(`/search?${paramsAsString}`);
    } catch (e) {
      console.error(e);
    }
  };

  const parser = new DOMParser();
  const onLoadMoreSearch = async () => {
    const paramsAsString = new URLSearchParams(
      new FormData(searchForm)
    ).toString();
    const apiUrl = `/api/search?${paramsAsString}&offset=${eventsContainer.children.length}`;

    try {
      const response = await fetch(apiUrl);
      const ssrDomEventString = await response.text();

      onSearchLoadMoreButtonBehaviour(ssrDomEventString);
      const newEvents = parser.parseFromString(ssrDomEventString, "text/html");
      eventsContainer.append(...newEvents.body.children);
      goTo(`/search?${paramsAsString}`);
    } catch (e) {
      console.error(e);
    }
  };

  // TODO: could use `Proxy`
  const params = new URLSearchParams(window.location.search);
  const hydrateFormValues = () => {
    params.forEach((value, key) => {
      if (key === "flags") {
        document.getElementById(value).checked = true;
      } else {
        document.getElementById(key).value = value;
      }
    });
  };
  hydrateFormValues();

  searchForm.addEventListener("submit", onSearch);

  const onCategoryChange = () => {
    let category = categorySelect.value;
    for (fieldSet of categoryFieldSets) {
      if (fieldSet.dataset["categoryWanted"] === category) {
        fieldSet.classList.remove("hidden");
      } else {
        for (select of fieldSet.querySelectorAll("select")) {
          select.selectedIndex = 0;
        }
        for (input of fieldSet.querySelectorAll("input")) {
          input.value = "";
        }
        fieldSet.classList.add("hidden");
      }
    }
  };
  categorySelect.addEventListener("change", onCategoryChange);
  onCategoryChange();
});

function openTab(event, tabName) {
  console.log("here in the open_tab function")

  const tabContents = document.querySelectorAll('.tab-content');
  tabContents.forEach(tabContent => {
    console.log("here in the tab function in contens")
    tabContent.classList.remove('active');
  });

  const tabButtons = document.querySelectorAll('.tab-button');
  tabButtons.forEach(tabButton => {
    tabButton.classList.remove('active');
  });

  document.getElementById(tabName).classList.add('active');

  event.currentTarget.classList.add('active');
}