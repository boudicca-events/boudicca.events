document.addEventListener("DOMContentLoaded", () => {
  const searchForm = document.getElementById("searchForm");

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
    await fetch(apiUrl);
    goTo(`/search?${paramsAsString}`);
  };

  searchForm.addEventListener("submit", onSearch);
});
