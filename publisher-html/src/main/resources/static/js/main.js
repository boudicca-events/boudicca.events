document.addEventListener("DOMContentLoaded", () => {
  const searchForm = document.getElementById("searchForm");

  const onSearch = (e) => {
    e.preventDefault();
    const asString = new URLSearchParams(new FormData(e.target)).toString();
    fetch(`/search?${asString}`);
  };

  searchForm.addEventListener("submit", onSearch);
});
