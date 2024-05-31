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
  const searchInput = document.querySelector("input.search-input");

  ////////////////////////////////////////////////////////////////////////////////////////////////////////
  // const events = document.querySelectorAll('.event');

  const setModalBehaviour = (events) => {
    /// debug
    console.log(events)
    ///

    events.forEach(event => {
      const modal = event.querySelector('#event-details-modal');
      const modalContent = event.querySelector('.modal-content')
      const detailButton = event.querySelector('.anchor-to-event')
      const closeButton = event.querySelector(".modal-close-button")

      /// open modal
      detailButton.addEventListener('click', () => {
        modal.style.display = "block";
        document.body.style.overflow = 'hidden';
      });

      /// close modal
      closeButton.addEventListener('click', () => {
        modal.style.display = "none";
        document.body.style.overflow = 'initial';
      })

      modalContent.addEventListener('click', function(event) {
        event.stopPropagation();
      });
    })
  };

  const setModalTabBehaviour = () => {
    // it could be moved outside
    const tabs = document.querySelectorAll(".details-box li a");
    const panels = document.querySelectorAll(".details-box article");

    for (i = 0; i < tabs.length; i++) {
      const tab = tabs[i];
      setTabHandler(tab, i);
    }

    const setTabHandler = (tab, tabPos) => {
      tab.onclick = function () {
        for (i = 0; i < tabs.length; i++) {
          tabs[i].className = "";
        }

        tab.className = "active-tab";

        for (i = 0; i < panels.length; i++) {
          panels[i].className = "";
        }

        panels[tabPos].className = "active-panel";
      };
    }
  }

  const openModal = (event) => {
    const modal = event.querySelector('#event-details-modal');
    const modalContent = event.querySelector('.modal-content')

    modal.addEventListener('click', () => {
      if (event.target === modalContent) {
        return;
      }

      modal.style.display = "none";
      document.body.style.overflow = 'initial';
    })
  }

  const closeModal = (event) => {}
  ////////////////////////////////////////////////////////////////////////////////////////////////////////

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

  document.addEventListener("keydown", (event) => {
    const drawerLastFocusableElement = document.querySelector("[data-drawer-last-focusable-element]")
    if (event.key === "Tab") {
      if (document.activeElement === drawerLastFocusableElement && !event.shiftKey) {
        closeDrawerButton.focus()
        event.preventDefault();
      } else if (document.activeElement === closeDrawerButton && event.shiftKey) {
        drawerLastFocusableElement.focus()
        event.preventDefault();
      }
    }
  })

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

  const onSearchButtonBehaviour = (rawResponse) => {
    const loadMoreButton = document.getElementById("loadMoreButton");
    const endOfResultsInfo = document.getElementById("endOfResults");
    const resultNotFoundLink = document.getElementById("resultNotFound");
    const searchText = document.getElementById("searchText");
    const searchInputKeyword = searchInput.value;

    if (!loadMoreButton || !endOfResultsInfo) {
      return;
    }

    const response = rawResponse.trim()
    if (!response) {
      loadMoreButton.style.display = "none";
      endOfResultsInfo.style.display = "block";

      if (searchInputKeyword === "") {
        resultNotFoundLink.style.display = "none";
      } else {
        searchText.textContent = `"${searchInputKeyword}"`;
        // in userflow from search field path, show the result not found link and hide the end of result
        resultNotFoundLink.style.display = "block";
        endOfResultsInfo.style.display = "none";
      }
    } else {
      loadMoreButton.style.display = "inline-block";
      endOfResultsInfo.style.display = "none";
      resultNotFoundLink.style.display = "none";
    }
  }

  const onLoadMoreButtonBehaviour = (rawResponse) => {
    const loadMoreButton = document.getElementById("loadMoreButton");
    const endOfResultsInfo = document.getElementById("endOfResults");
    const resultNotFoundLink = document.getElementById("resultNotFound");
    const searchText = document.getElementById("searchText");
    const searchInputKeyword = searchInput.value;

    if (!loadMoreButton || !endOfResultsInfo) {
      return;
    }

    const response = rawResponse.trim()
    if (!response) {
      loadMoreButton.style.display = "none";
      endOfResultsInfo.style.display = "block";

      if (searchInputKeyword === "") {
        resultNotFoundLink.style.display = "none";
      } else {
        searchText.textContent = `"${searchInputKeyword}"`;
        // in userflow from search field path, hide the result not found link and show the end of result
        resultNotFoundLink.style.display = "none";
        endOfResultsInfo.style.display = "block";
      }
    } else {
      loadMoreButton.style.display = "inline-block";
      endOfResultsInfo.style.display = "none";
      resultNotFoundLink.style.display = "none";
    }
  }

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
      onSearchButtonBehaviour(ssrDomEventString);
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

      onLoadMoreButtonBehaviour(ssrDomEventString);
      const newEvents = parser.parseFromString(ssrDomEventString, "text/html");

      eventsContainer.append(...newEvents.body.children);

      // /// debug
      // console.log(...newEvents.body.children)
      // console.log(newEvents.body.querySelectorAll('.event'))
      // ///

      console.log(newEvents.documentElement.querySelectorAll('.event'))

      // const newlyAddedEvents = eventsContainer.querySelectorAll('.event');
      setModalBehaviour(newEvents.documentElement.querySelectorAll('.event'))
      setModalTabBehaviour()

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

  ////////////////////////////////////////////////////////////////////////////////////////////////////////
  setModalBehaviour(events);
  setModalTabBehaviour();
  ////////////////////////////////////////////////////////////////////////////////////////////////////////
});
