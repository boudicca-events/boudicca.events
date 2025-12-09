document.addEventListener("DOMContentLoaded", () => {
    const linksWithKeepQuery = document.querySelectorAll("a.wantsSearchQuery");

    linksWithKeepQuery.forEach(a => {
        a.dataset.originalHref = a.href;
        a.href = a.href + location.search;
    });

    globalThis.addEventListener("searchChanged", event => {
        linksWithKeepQuery.forEach(a => {
            a.href = a.dataset.originalHref + "?" + event.detail;
        });
    });
});
