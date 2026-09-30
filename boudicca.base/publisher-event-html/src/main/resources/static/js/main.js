document.addEventListener("DOMContentLoaded", () => {
    const searchForm = document.getElementById("searchForm");
    const eventsContainer = document.getElementById("eventsContainer");
    const filterButton = document.getElementById("filterButton");
    const loadMoreButton = document.getElementById("loadMoreButton");
    const searchInput = document.querySelector("input.search-input");
    const modal = document.getElementById("modal");
    const modalContent = modal.querySelector("#modal-content");
    const modalContainer = modal.querySelector("#modal-container");
    const closeModalButton = modalContainer.querySelector(".modal-close");
    const toggleModalButton = modalContainer.querySelector(".modal-toggle");
    const mobileMenu = document.getElementById("mobile-menu");
    const openMenuButton = document.getElementById("openMenuButton");
    const closeMenuButton = document.getElementById("closeMenuButton");
    const header = document.querySelector("header");
    const map = document.getElementById("map");
    const multiselectFilterInputs = ["locationCities", "locationNames", "bandNames", "tags", "types", "concertGenres"];
    let lastFocusedEventCard = null;

    closeModalButton.addEventListener("click", () => {
        closeModal();
    })
    toggleModalButton.addEventListener("click", () => {
        toggleModal();
    })

    const openModal = async (eventCard) => {
        let content = await fetch("api/event?id=" + eventCard.dataset.id);
        modalContent.innerHTML = await content.text()
        modal.style.display = "block";
        document.body.style.overflow = "hidden";
        lastFocusedEventCard = eventCard;
        modalContent.querySelector("h2").focus();
    };

    const closeModal = () => {
        modal.style.display = "none";
        document.body.style.overflow = "initial";
        if (lastFocusedEventCard != null) {
            lastFocusedEventCard.focus();
        }
    };

    const toggleModal = () => {
        let prettyEvent = modalContent.querySelector(".prettyEvent");
        let allProperties = modalContent.querySelector(".allProperties");
        if (prettyEvent.classList.contains("hidden")) {
            prettyEvent.classList.remove("hidden");
            allProperties.classList.add("hidden");
        } else {
            prettyEvent.classList.add("hidden");
            allProperties.classList.remove("hidden");
        }
    };

    modal.addEventListener('click', (event) => {
        if (!modalContainer.contains(event.target)) {
            closeModal();
        }
    });

    modalContainer.addEventListener('click', (event) => {
        event.stopPropagation();
    });

    if (loadMoreButton != null) {
        loadMoreButton.addEventListener("click", () => {
            onLoadMoreSearch();
        });
    }

    const initCheckboxLabelToggle = () => {
        const checkboxLabelsToToggle = document.querySelectorAll(".toggleFilterLabels");
        for (const label of checkboxLabelsToToggle) {
            label.addEventListener("click", (label) => toggleCheckboxLabels(label.currentTarget));
        }
    }

    const toggleCheckboxLabels = (clickedLabel) => {
        if (clickedLabel) {
            const currentForAttribute = clickedLabel.getAttribute('for');
            const chipsLabel = document.querySelector('label.chips[for="' + currentForAttribute + '"]');
            const listLabel = document.querySelector('li label[for="' + currentForAttribute + '"]');
            toggleSingleCheckboxLabel(chipsLabel);
            toggleSingleCheckboxLabel(listLabel);
        }
    }

    const toggleCheckboxLabelsByCheckbox = (checkbox) => {
        if (checkbox) {
            const chipsLabel = document.querySelector('label.chips[for="' + checkbox.id + '"]');
            const listLabel = document.querySelector('li label[for="' + checkbox.id + '"]');
            toggleSingleCheckboxLabel(chipsLabel);
            toggleSingleCheckboxLabel(listLabel);
        }
    }

    const toggleSingleCheckboxLabel = (label) => {
        if (label.style.position === "absolute") {
            label.style.position = "relative";
            label.style.opacity = "1";
            label.style.maxWidth = "100%"
        } else {
            label.style.position = "absolute";
            label.style.opacity = "0";
            label.style.maxWidth = "0"
        }
    }

    filterButton.addEventListener("click", () => {
        toggleDrawer();
    });

    const loadDrawer = async () => {

        const response = await fetch("/api/drawer");
        document.getElementById("drawer-container").innerHTML = await response.text();
        const closeDrawerButton = document.getElementById("closeDrawerButton");
        const resetSearchFormButton = document.getElementById("resetSearchForm");
        const drawer = document.getElementById("drawer");
        const categorySelect = document.getElementsByName("category");

        hydrateFormValues();
        drawer.addEventListener("submit", onSearch);

        document.addEventListener("click", (event) => {
            if (
                !drawer.contains(event.target) &&
                !searchForm.contains(event.target) &&
                !filterButton.contains(event.target) &&
                !event.target.classList.contains("event")
            ) {
                closeDrawer();
            }
        });

        resetSearchFormButton.addEventListener("click", () => {
            // toggle the checked labels to hide the chips before the rest of the form is reset
            let checkedLabels = document.querySelectorAll("input[type=checkbox]:checked + label.chips");
            for (const checkedLabel of checkedLabels) {
                toggleCheckboxLabels(checkedLabel);
            }
            searchForm.reset();
            drawer.reset();
            // remove the category specific filters
            categorySelect.forEach((checkbox) => onCategoryChange(checkbox));
        });

        closeDrawerButton.addEventListener("click", () => {
            closeDrawer();
        });
        categorySelect.forEach((checkbox) => checkbox.addEventListener("change", c => onCategoryChange(c.currentTarget)));
        categorySelect.forEach((checkbox) => onCategoryChange(checkbox));
        multiselectFilterInputs.forEach((identifier) => {
            document.getElementById("filter-" + identifier).addEventListener("input", () => filterMultiselectFieldsByInput(identifier));
        })

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
            } else if (event.code === "Space" && document.activeElement.type === "checkbox") {
                toggleCheckboxLabelsByCheckbox(document.activeElement);
            } else if (event.key === "Escape" && modal.style.display !== "none") {
                closeModal();
            } else if (event.key === "Escape" && drawer.style.display !== "none") {
                closeDrawer();
            } else if (event.key === "Enter" && event.target.classList.contains("event")) {
                openModal(event.target);
            }
        })
        initCheckboxLabelToggle();
    }

    let drawerLoaded = false;
    const toggleDrawer = async () => {

        if (!drawerLoaded) {
            await loadDrawer();
            drawerLoaded = true;
        }

        const drawer = document.getElementById("drawer");
        if (drawer.style.display === "flex") {
            closeDrawer();
        } else {
            drawer.setAttribute("aria-hidden", false);
            drawer.style.display = "flex";
            // close mobile menu
            mobileMenu.setAttribute("aria-hidden", true);
            mobileMenu.style.display = "none";
            openMenuButton.style.display = "block";
            closeMenuButton.style.display = "none";
            header.style.paddingBottom = "24px";
        }
    };

    const closeDrawer = () => {
        const drawer = document.getElementById("drawer");
        drawer.setAttribute("aria-hidden", true);
        drawer.style.display = "none";
    };

    const goToSearch = (paramsAsString) => {
        globalThis.dispatchEvent(new CustomEvent("searchChanged", {detail: paramsAsString}))
        if (map == null) {
            goTo(`/search?${paramsAsString}`);
        } else {
            goTo(`/map?${paramsAsString}`);
            globalThis.location.reload();
        }
    };

    const goTo = (url) => {
        if ("undefined" === typeof history.pushState) {
            globalThis.location.assign(url);
        } else {
            history.pushState({}, "", url);
        }
    };

    const updateResultsDisplay = (rawResponse, isInitialSearch) => {
        const loadMoreButton = document.getElementById("loadMoreButton");
        const endOfResultsInfo = document.getElementById("endOfResults");
        const resultNotFoundLink = document.getElementById("resultNotFound");
        const searchText = document.getElementById("searchText");
        const searchInputKeyword = searchInput.value;

        if (!loadMoreButton || !endOfResultsInfo) {
            return;
        }

        const response = rawResponse.trim();
        if (response) {
            loadMoreButton.style.display = "inline-block";
            endOfResultsInfo.style.display = "none";
            resultNotFoundLink.style.display = "none";
        } else {
            loadMoreButton.style.display = "none";

            if (searchInputKeyword === "") {
                endOfResultsInfo.style.display = "block";
                resultNotFoundLink.style.display = "none";
            } else {
                searchText.textContent = `"${searchInputKeyword}"`;
                // Initial search: show "result not found", hide "end of results"
                // Load more: show "end of results", hide "result not found"
                resultNotFoundLink.style.display = isInitialSearch ? "block" : "none";
                endOfResultsInfo.style.display = isInitialSearch ? "none" : "block";
            }
        }
    }

    let currentSearchParams = new URL(window.location.href).searchParams;
    const getSearchParams = () => {
        return currentSearchParams.toString();
    };
    const updateSearchParams = () => {
        const drawer = document.getElementById("drawer");
        const searchBar = new FormData(searchForm);
        const detailFilter = new FormData(drawer);
        for (const fields of searchBar.entries()) {
            detailFilter.append(fields[0], fields[1]);
        }
        currentSearchParams = new URLSearchParams(detailFilter);
    }

    const onSearch = async (e) => {
        e.preventDefault();
        updateSearchParams()
        const paramsAsString = getSearchParams();
        const apiUrl = `/api/search?${paramsAsString}&offset=0`;
        closeDrawer();

        try {
            const response = await fetch(apiUrl);
            const ssrDomEventString = await response.text();
            if (eventsContainer != null) {
                eventsContainer.innerHTML = ssrDomEventString;
                updateResultsDisplay(ssrDomEventString, true);
                initModals(eventsContainer.querySelectorAll(".event"));
            }
            goToSearch(paramsAsString);
        } catch (e) {
            console.error(e);
        }
    };

    const parser = new DOMParser();
    const onLoadMoreSearch = async () => {
        const paramsAsString = getSearchParams();
        const apiUrl = `/api/search?${paramsAsString}&offset=${eventsContainer.children.length}`;

        try {
            const response = await fetch(apiUrl);
            const ssrDomEventString = await response.text();
            updateResultsDisplay(ssrDomEventString, false);
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
            event.addEventListener("click", () => {
                openModal(event)
            });
        })
    }

    const hydrateFormValues = () => {
        const params = new URL(window.location.href).searchParams;
        params.forEach((value, key) => {
            if (key === "flags") {
                document.getElementById(value).checked = true;
            } else if (key === "includeRecurring") {
                document.getElementById(key).checked = true;
            } else if (key === "category" || multiselectFilterInputs.includes(key)) {
                const checkbox = document.getElementById(key + "-" + value.replaceAll(" ", "-"));
                checkbox.checked = true;
                toggleCheckboxLabelsByCheckbox(checkbox)
            } else {
                document.querySelector(`[name="${key}"]`).value = value;
            }
        });
    };

    searchForm.addEventListener("submit", onSearch);

    const onCategoryChange = (changedCategory) => {

        const changedCategoryName = changedCategory.value;
        let fieldSets = document.querySelectorAll("[data-category-wanted='" + changedCategoryName + "']");

        for (const fieldSet of fieldSets) {
            let categoryIsChecked = document.getElementById("category-" + fieldSet.dataset.categoryWanted).checked;
            if (categoryIsChecked) {
                fieldSet.classList.remove("hidden");
            } else {
                for (const select of fieldSet.querySelectorAll("select")) {
                    select.selectedIndex = 0;
                }
                for (const input of fieldSet.querySelectorAll("input")) {
                    input.checked = false;
                }
                fieldSet.classList.add("hidden");
            }
        }
    };

    const events = document.querySelectorAll(".event")
    initModals(events);

    const filterMultiselectFieldsByInput = (identifier) => {
        const filter = document.getElementById("filter-" + identifier).value.toLowerCase();
        const labels = document.getElementById("ul-" + identifier).getElementsByTagName("label");

        for (const label of labels) {
            if (label.textContent.toLowerCase().includes(filter)) {
                label.style.display = "";
            } else {
                label.style.display = "none";
            }
        }
    }
});
