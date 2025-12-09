document.addEventListener("DOMContentLoaded", () => {
    const searchForm = document.getElementById("searchForm");
    const eventsContainer = document.getElementById("eventsContainer");
    const filterButton = document.getElementById("filterButton");
    const drawer = document.getElementById("drawer");
    const closeDrawerButton = document.getElementById("closeDrawerButton");
    const resetSearchFormButton = document.getElementById("resetSearchForm");
    const loadMoreButton = document.getElementById("loadMoreButton");
    const categorySelect = document.getElementsByName("category");
    const searchInput = document.querySelector("input.search-input");
    const modal = document.getElementById("modal");
    const modalContent = modal.querySelector("#modal-content");
    const mobileMenu = document.getElementById("mobile-menu");
    const openMenuButton = document.getElementById("openMenuButton");
    const closeMenuButton = document.getElementById("closeMenuButton");
    const header = document.querySelector("header");
    const accessibilityFlags = document.getElementsByName("flags");
    const map = document.getElementById("map");
    const multiselectFilterInputs = ["locationCities", "locationNames", "bandNames", "tags", "types", "concertGenres"];
    let lastFocusedEventCard = null;


    const openModal = (eventCard) => {
        modalContent.innerHTML = eventCard.querySelector(".modal-content").innerHTML;
        modal.style.display = "block";
        // whitespace pre-line property in css allows line breaks in text but also
        // adds leading ones we don't need, so we have to trim the text here
        const articleText = modal.querySelector("article p");
        articleText.innerHTML = articleText.innerHTML.trim();
        document.body.style.overflow = "hidden";
        const closeButton = modalContent.querySelector(".modal-close");
        lastFocusedEventCard = eventCard;
        modalContent.querySelector("h2").focus();
        closeButton.addEventListener("click", () => {
            closeModal();
        })
    };

    const closeModal = () => {
        modal.style.display = "none";
        document.body.style.overflow = "initial";
        if (lastFocusedEventCard != null) {
            lastFocusedEventCard.focus();
        }
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

    if (loadMoreButton != null) {
        loadMoreButton.addEventListener("click", () => {
            onLoadMoreSearch();
        });
    }

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

    filterButton.addEventListener("click", () => {
        toggleDrawer();
    });

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
            const checkbox = document.getElementById(currentForAttribute);
            setCheckboxAriaChecked(checkbox, true);
        }
    }

    const toggleCheckboxLabelsByCheckbox = (checkbox) => {
        if (checkbox) {
            const chipsLabel = document.querySelector('label.chips[for="' + checkbox.id + '"]');
            const listLabel = document.querySelector('li label[for="' + checkbox.id + '"]');
            toggleSingleCheckboxLabel(chipsLabel);
            toggleSingleCheckboxLabel(listLabel);
            setCheckboxAriaChecked(checkbox, true);
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

    const setCheckboxAriaChecked = (checkbox, negateChecked) => {
        if (negateChecked) {
            // if checkbox is toggled automatically afterwards, !checked has to be used
            checkbox.setAttribute('aria-checked', !checkbox.checked);
        } else {
            checkbox.setAttribute('aria-checked', checkbox.checked);
        }

    }

    accessibilityFlags.forEach((checkbox) => checkbox.addEventListener("change", c => setCheckboxAriaChecked(c.currentTarget, false)));

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

    const toggleDrawer = () => {
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
            // set checkbox aria attributes in case they are already checked by the search url
            accessibilityFlags.forEach((checkbox) => setCheckboxAriaChecked(checkbox, false));
        }
    };

    const closeDrawer = () => {
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

    const getSearchParams = () => {
        const searchBar = new FormData(searchForm);
        const detailFilter = new FormData(drawer);
        for (const fields of searchBar.entries()) {
            detailFilter.append(fields[0], fields[1]);
        }
        return new URLSearchParams(detailFilter).toString();
    }

    const onSearch = async (e) => {
        e.preventDefault();
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

    // TODO: could use `Proxy`
    const params = new URLSearchParams(globalThis.location.search);
    const hydrateFormValues = () => {
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
    hydrateFormValues();

    searchForm.addEventListener("submit", onSearch);
    drawer.addEventListener("submit", onSearch);

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

    categorySelect.forEach((checkbox) => checkbox.addEventListener("change", c => onCategoryChange(c.currentTarget)));
    categorySelect.forEach((checkbox) => onCategoryChange(checkbox));

    const events = document.querySelectorAll(".event")
    initModals(events);
    initCheckboxLabelToggle();

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

    multiselectFilterInputs.forEach((identifier) => {
        document.getElementById("filter-" + identifier).addEventListener("input", () => filterMultiselectFieldsByInput(identifier));
    })
});
