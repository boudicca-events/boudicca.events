document.addEventListener("DOMContentLoaded", () => {
  const linksWithKeepQuery = document.querySelectorAll("a.wantsSearchQuery")
  linksWithKeepQuery.forEach((a => {
      a.setAttribute("data-originalhref", a.href)
      a.href = a.href + location.search
    }
  ))
  window.addEventListener("searchChanged", function (event) {
    linksWithKeepQuery.forEach((a => {
        a.href = a.getAttribute("data-originalhref") + "?" + event.detail
      }
    ))
  })
});
