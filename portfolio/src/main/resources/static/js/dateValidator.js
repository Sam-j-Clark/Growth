/**
 * Compares the values of the start and end date inputs, and if they are invalid (the start date after the end date),
 * displays an error message
 *
 * Returns true if the date order is invalid (start date greater than end date).
 */
function checkDateOrder(startDate, endDate) {
    let pickers = $(".date").get()
    for (let picker of pickers) {
        picker.setCustomValidity("");
    }
    if (startDate >= endDate) {
        for (let picker of pickers) {
            picker.setCustomValidity("Start date must be before end date");
        }
    }
    return startDate >= endDate
}