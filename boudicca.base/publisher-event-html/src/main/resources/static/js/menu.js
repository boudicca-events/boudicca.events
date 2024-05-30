document.addEventListener("DOMContentLoaded", () => {
    const mobileMenu = document.getElementById("mobile-menu");
    const openMenuButton = document.getElementById("openMenuButton");
    const closeMenuButton = document.getElementById("closeMenuButton");

    openMenuButton.addEventListener("click", () => {
        openMenu();
    })

    closeMenuButton.addEventListener("click", () => {
        closeMenu();
    })

    const openMenu = () => {
        mobileMenu.classList.add("mobile-menu-open");
        document.body.style.overflow = "hidden";
    }

    const closeMenu = () => {
        mobileMenu.classList.remove("mobile-menu-open");
        document.body.style.overflow = "initial";
    }
})
