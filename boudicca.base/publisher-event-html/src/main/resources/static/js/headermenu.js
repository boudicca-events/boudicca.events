document.addEventListener("DOMContentLoaded", () => {
  const linksWithKeepQuery = document.querySelectorAll("a.keepQueryOnFollow")
  linksWithKeepQuery.forEach((a =>
      a.onclick = (event => {
        navigateWithQuery(a)
        event.stopPropagation()
        event.preventDefault()
      })
  ))

  function navigateWithQuery(aElement) {
    let href = aElement.href
    let newHref = href + location.search
    location.href = newHref
  }
});
