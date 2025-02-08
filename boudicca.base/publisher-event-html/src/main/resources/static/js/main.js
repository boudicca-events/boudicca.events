document.addEventListener("DOMContentLoaded", () => {
  const searchForm = document.getElementById("searchForm");
  const eventsContainer = document.getElementById("eventsContainer");
  const filterButton = document.getElementById("filterButton");
  const drawer = document.getElementById("drawer");
  const closeDrawerButton = document.getElementById("closeDrawerButton");
  const filterSearchButton = document.getElementById("filterSearchButton");
  const resetSearchFormButton = document.getElementById("resetSearchForm");
  const loadMoreButton = document.getElementById("loadMoreButton");
  const categorySelect = document.getElementById("categorySelect");
  const categoryFieldSets = document.querySelectorAll("[data-category-wanted]");
  const searchInput = document.querySelector("input.search-input");
  const modal = document.getElementById("modal");
  const modalContent = modal.querySelector("#modal-content");

  const openModal = (content) => {
    modalContent.innerHTML = content;
    modal.style.display = "block";
    document.body.style.overflow = "hidden";
    const closeButton = modalContent.querySelector(".modal-close");
    closeButton.addEventListener("click", () => {
      closeModal();
    })
  };

  const closeModal = () => {
    modal.style.display = "none";
    document.body.style.overflow = "initial";
  };

  modal.addEventListener('click', (event) => {
    if (!modalContent.contains(event.target)) {
      closeModal();
    }
  });

  modalContent.addEventListener('click', (event) => {
    event.stopPropagation();
  });

  modalContent.addEventListener('click', (event) => {
    event.stopPropagation();
  });

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
    toggleDrawer();
  });

  const initCheckboxLabelToggle = () => {
    const checkboxLabelsToToggle = document.querySelectorAll(".toggleFilterLabels");
    for (label of checkboxLabelsToToggle) {
      label.addEventListener("click", (label) => toggleCheckboxLabels(label));
    }
  }

  const toggleCheckboxLabels = (clickedLabel) => {
     const currentForAttribute = clickedLabel.currentTarget.getAttribute('for');
     const chipsLabel = document.querySelector('label.chips[for="'+currentForAttribute+'"]');
     const listLabel = document.querySelector('li label[for="'+currentForAttribute+'"]');
     toggleSingleCheckboxLabel(chipsLabel);
     toggleSingleCheckboxLabel(listLabel);
  }

  const toggleSingleCheckboxLabel = (label) => {
    if (label.style.display != "inline-block") {
       label.style.display = "inline-block";
     } else {
       label.style.display = "none";
    }
  }

  document.addEventListener("click", (event) => {
    if (
      !drawer.contains(event.target) &&
      !filterButton.contains(event.target) &&
      !event.target.classList.contains("anchor-to-event")
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

  const toggleDrawer = () => {
    if (drawer.style.display != "flex") {
      drawer.style.display = "flex";
    } else {
      closeDrawer();
    }
  };

  const closeDrawer = () => {
    drawer.style.display = "none";
  };

  const goToSearch = (paramsAsString) => {
    window.dispatchEvent(new CustomEvent("searchChanged", {detail: paramsAsString}))
    goTo(`/search?${paramsAsString}`);
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
      initModals(eventsContainer.querySelectorAll(".event"));
      goToSearch(paramsAsString);
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
      initModals([...newEvents.body.children]);
      eventsContainer.append(...newEvents.body.children);
      goToSearch(paramsAsString);
    } catch (e) {
      console.error(e);
    }
  };

  const initModals = (events) => {
    events.forEach(event => {
      const anchor = event.querySelector(".anchor-to-event");
      const content = event.querySelector(".modal-content");
      anchor.addEventListener("click", () => {
        openModal(content.innerHTML)
      });
    })
  }

  // TODO: could use `Proxy`
  const params = new URLSearchParams(window.location.search);
  const hydrateFormValues = () => {
    params.forEach((value, key) => {
      if (key === "flags") {
        document.getElementById(value).checked = true;
      } else {
        document.querySelector(`[name="${key}"]`).value = value;
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

  const events = document.querySelectorAll(".event")
  initModals(events);
  initCheckboxLabelToggle();
});
