document.addEventListener("DOMContentLoaded", () => {
    const mobileMenu = document.getElementById("mobile-menu");
    const openMenuButton = document.getElementById("openMenuButton");
    const closeMenuButton = document.getElementById("closeMenuButton");
    const header = document.querySelector("header");
    const drawer = document.getElementById("drawer");

    openMenuButton.addEventListener("click", () => {
        openMenu();
        drawer.style.display = "none";
    })

    closeMenuButton.addEventListener("click", () => {
        closeMenu();
    })

    const openMenu = () => {
        mobileMenu.setAttribute('aria-hidden', false);
        mobileMenu.style.display = "block";
        openMenuButton.style.display = "none";
        closeMenuButton.style.display = "block";
        header.style.paddingBottom = "0px";
    }

    const closeMenu = () => {
        mobileMenu.setAttribute('aria-hidden', true);
        mobileMenu.style.display = "none";
        openMenuButton.style.display = "block";
        closeMenuButton.style.display = "none";
        header.style.paddingBottom = "24px";
    }
})
