/**
 * Functions to be run when page has finished loading.
 * https://learn.jquery.com/using-jquery-core/document-ready/
 */
$(document).ready(function () {
    /**
     * Binds an event handler to the "click".
     * https://api.jquery.com/click/
     * The handler runs the function toggleDropdown()
     */
    $(".profileDropdown").click(() => {
        toggleDropDown()
    })
})


/**
 * Toggles the dropdown menu.
 * slideToggle() is a jQuery function https://api.jquery.com/slidetoggle/
 */
function toggleDropDown() {
    $(".dropdown-content").slideToggle();

}