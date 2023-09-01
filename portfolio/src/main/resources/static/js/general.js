let liveAlertIsShown;
let alertIsShown;

const AlertTypes = {
    Failure: "failure",
    Success: "success",
    Info: "info"
}

$(function () {
    // Checks to see if there is an error message to be displayed
    if (!$(".errorMessage").is(':empty')) {
        $(".errorMessageParent").show();
    }
    if (!$(".infoMessage").is(':empty')) {
        $(".infoMessageParent").show();
    }

    if (!$(".successMessage").is(':empty')) {
        $(".successMessageParent").show();
    }

    let tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
    let tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl)
    })

    removeElementIfNotAuthorized()
});



/**
 * When a user clicks on a nav item it finds the a link inside it and goes to that address
 */
$(document).on("click", ".navItem", function() {
    let link = $(this).children("a").attr("href")
    location.replace(link)
})


/**
 * Checks if a user has a role above student.
 *
 * @returns {boolean} returns true if userRole is above student.
 */
function checkPrivilege() {
    if (typeof userRoles !== 'undefined') {
        return userRoles.includes('COURSE_ADMINISTRATOR') || userRoles.includes('TEACHER');
    } else {
        return false;
    }

}


/**
 * Returns true if the user has the Admin role.
 */
function isAdmin() {
    return userRoles.includes('COURSE_ADMINISTRATOR')
}


/**
 * Removes an element requiring teacher/admin permissions if the user does not have teacher/admin privileges.
 */
function removeElementIfNotAuthorized() {
    if (!checkPrivilege()) {
        $(".hasTeacherOrAbove").remove()
    }
}


/**
 * Helper function to remove alert element.
 */
function removeAlert() {
    alertIsShown = false;
    let alert = $("#alertPopUp")
    alert.hide("slide", 100, function () {
        alert.remove();
    })
    if (liveAlertIsShown) {
        let liveAlert = $("#liveAlertPopUp")
        liveAlert.animate({bottom: 10}, {duration: 100})
    }
}


/**
 * Displays a dismissible alert down the bottom right of the screen.
 *
 * @param alertMessage
 * @param type the type of alert. Accepts AlertTypes.Success, AlertTypes.Failure, and AlertTypes.Info.
 * @param window - the location to show the error
 */
function createAlert(alertMessage, type, window = "body") {
    let CheckAlert = $(".checkAlert")
    if (CheckAlert.is(":visible")) {
        CheckAlert.hide("slide", 100, function () {
            CheckAlert.remove();
        }).promise().then(function () { // If the alert is already displayed it removes it and then once that is done, runs the alert function
            alert(alertMessage, type, window)
        })
    } else {
        alert(alertMessage, type, window)
    }
}


/**
 * Helper function to remove live alert element. Will only remove if the id of the message is the same as the one on
 * the alert or if there was no message id. This is used for timing out a notification and making sure an old
 * notification doesn't accidentally delete a new one
 */
function removeLiveAlert(messageId) {
    let liveAlert = $("#liveAlertPopUp")
    let liveAlertId = liveAlert.find("#alertId").text()
    if (messageId === liveAlertId || messageId === undefined) {
        liveAlertIsShown = false;
        let alert = $("#liveAlertPopUp")
        alert.hide("slide", 100, function () {
            alert.remove();
        })
    }
}


/**
 * Displays a dismissible alert down the bottom right of the screen.
 *
 * @param alertMessage
 * @param alertId
 * @param window - the location to show the error
 */
function createLiveAlert(alertMessage, alertId, window = "body") {

    let CheckAlert = $("#liveAlertPopUp")
    if (CheckAlert.is(":visible")) {
        CheckAlert.hide("slide", 100, function () {
            CheckAlert.remove();
        }).promise().then(function () { // If the alert is already displayed it removes it and then once that is done, runs the alert function
            liveAlert(alertMessage, alertId, window)
        })
    } else {
        liveAlert(alertMessage, alertId, window)
    }
}


/**
 * Creates an alert message, and appends it to the window.
 *
 * The type of the alert determines the colour of the alert.
 * AlertTypes.Success = green, AlertTypes.Failure = red, and AlertTypes.Info = yellow.
 *
 * @param alertMessage The message to be displayed in the alert box.
 * @param type The type of the message to be displayed. Determines the alert background colour.
 * @param window The location to which the alert will be appended.
 */
function alert(alertMessage, type, window = "body") {
    let alertDiv = `<div id="alertPopUp" class="alert checkAlert" style="display: none">
                     <p id="alertPopUpMessage">${sanitise(alertMessage)}</p>
                     <button id="alertPopUpCloseButton" onclick="removeAlert()" class="noStyleButton">
                         <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"  class="bi bi-x-circle" viewBox="0 0 16 16">
                             <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                             <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                         </svg>
                     </button>
                 </div>`

    $(window).append(alertDiv)
    let alert = $("#alertPopUp")

    alertIsShown = true;

    switch (type) {
        case AlertTypes.Failure:
            alert.removeClass("backgroundGreen")
            alert.removeClass("backgroundYellow")
            alert.addClass("backgroundRed")
            break;
        case AlertTypes.Success:
            alert.removeClass("backgroundRed")
            alert.removeClass("backgroundYellow")
            alert.addClass("backgroundGreen")
            break;
        case AlertTypes.Info:
            alert.removeClass("backgroundRed")
            alert.removeClass("backgroundGreen")
            alert.addClass("backgroundYellow")
            break;
        default:
            alertIsShown = false;
    }

    if (window === "body") {
        alert.css("position", "fixed")
    } else {
        alert.css("position", "relative")
    }
    alertIsShown = true;
    addAlert(alert)
}


/**
 * Creates a live alert message, and appends it to the window. This is the same as the alert message, but also has an Id.
 *
 * @param alertMessage The message to be displayed.
 * @param alertId The Id of the alert message.
 * @param window The location where the alert will be appended.
 */
function liveAlert(alertMessage, alertId, window = "body") {
    let alertDiv = `<div id="liveAlertPopUp" class="alert" style="display: none">
                     <div id="alertId" style="display: none">${sanitise(alertId)}</div>
                     <p id="alertPopUpMessage">${sanitise(alertMessage)}</p>
                     <button id="alertPopUpCloseButton" onclick="removeLiveAlert()" class="noStyleButton"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"  class="bi bi-x-circle" viewBox="0 0 16 16">
                         <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                         <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                     </svg></button>

                 </div>`

    $(window).append(alertDiv)
    let alert = $("#liveAlertPopUp")

    liveAlertIsShown = true;
    addLiveAlert(alert)
}


/**
 * Displays a live alert message in the window.
 *
 * @param liveAlert The live alert to be displayed.
 */
function addLiveAlert(liveAlert) {
    let alert = $("#alertPopUp")
    if (alertIsShown) {
        let height = alert.outerHeight()
        liveAlert.css("bottom", height + 20 + "px")
    } else {
        liveAlert.css("bottom", "10px")
    }
    liveAlert.show("slide", 100)
}


/**
 * Adds an alert to the user's display for 10 seconds.
 *
 * @param alert The alert to be displayed.
 */
function addAlert(alert) {
    let liveAlert = $("#liveAlertPopUp")

    if (liveAlertIsShown) {
        let height = liveAlert.outerHeight()
        liveAlert.animate({bottom: height + 20 + "px"}, {duration: 100})
    }
    alert.show("slide", 100)
}

/**
 * Generic handler for changing your role that will be called if not overridden in a different file. Despite intellij
 * saying it's not used, it actually is
 * @param notification
 * @param action
 */
function handleRoleChangeEvent(notification, action) {
    displayRoleChangeMessage(notification, action)
}


/**
 * Handles the receiving of a role change event notification. If the logged-in user is the same as the user whose role
 * was changed, the user receives an alert describing the update of their role.
 *
 * @param notification The data describing the role that was changed, and the user who changed it.
 * @param action The action representing the addition of removal of a role.
 */
function displayRoleChangeMessage(notification, action) {
    if (userIdent === parseInt(notification.id)) {
        const editorName = notification.editorName;
        let roleChanged = notification.data;
        roleChanged = roleChanged.replace("_", " ").toLowerCase()

        let message
        if (action === "add") {
            message = `${editorName} gave you the role: ${roleChanged}`
        } else {
            message = `${editorName} removed from you the role: ${roleChanged}`
        }
        createAlert(message, AlertTypes.Info)
    }
}


/**
 * Removes HTML tags from a string
 * See https://stackoverflow.com/questions/2794137/sanitizing-user-input-before-adding-it-to-the-dom-in-javascript
 *
 * @param string The string to sanitise
 * @returns A sanitised string
 */
function sanitise(string) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#x27;',
        "/": '&#x2F;',
    };
    const reg = /[&<>"'/]/ig;
    return string.toString().replace(reg, (match) => (map[match]));
}

/**
 * Sets up listeners on any element that has the given class. Call this function again if you add new elements to
 * the DOM that you want to be counted.
 *
 * @param className The name of the class you wish to count. Defaults to countable if none given
 */
function startCharacterCounting(className="countable") {
    const countable = $("." + className)
    countable.each(countCharacters)
    countable.on("keyup", countCharacters) //Runs when key is pressed (well released) on form-control elements.
}



/**
 * Function that gets the maxlength of an input field and lets the user know how many characters they have left.
 */
function countCharacters() {
    let maxlength = $(this).attr("maxLength")
    let lengthOfCurrentInput = $(this).val().length;
    let counter = maxlength - lengthOfCurrentInput;
    let helper = $(this).siblings(".form-text-counted").first(); // Gets the next element with a class that is form-text

    //If one character remains, changes from "characters remaining" to "character remaining"
    if (counter !== 1) {
        helper.text(counter + " characters remaining")
    } else {
        helper.text(counter + " character remaining")
    }
}


/**
 * Regex that is all unicode letters, decimal numbers and punctuation
 */
let regex = new RegExp("[\\p{L}\\p{Nd}\\p{P}]+", 'u')


/**
 * Redirects to user's home page. This is currently the user's evidence page.
 */
function redirectToUsersHomePage(userId) {
    window.location.href = "evidence?userId=" + userId //redirect to the user's evidence page
}


/**
 * Updates the display of a message in a div. If the message length is 0, the div is not displayed. Otherwise, the text
 * is added and the div displayed.
 *
 * @param errorDiv The div that will have its text updated to the message's value
 * @param message The value to set the div's text attribute.
 */
function updateErrorMessage(errorDiv, message) {
    errorDiv.text(message)
    if (message.length === 0) {
        errorDiv.hide()
    } else {
        errorDiv.show()
    }
}

/**
 * Redirects to project home page.
 */
function redirectToProjectHomePage() {
    window.location.href = "portfolio?projectId=1" //redirect to the project evidence page
}