/**
 * A JS file that contains utilities for pieces of evidence,
 * that can be used across multiple pages.
 */


const RESERVED_SKILL_TAGS = ["no_skill"];

/** A regex contain emoji */
const emojiRegex = new RegExp("/(\ud83c[\udf00-\udfff])|(\ud83d[\udc00-\ude4f\ude80-\udeff])|[\u2600-\u2B55]/g");

/** A regex contains at least one letter */
const skillRegex = new RegExp("(\\p{P}\\p{L}\\p{Nl}\\p{Nd})*[A-Za-z]+(\\p{P}\\p{L}\\p{Nl}\\p{Nd})*", 'u')

/** the user id of the user whose evidence page if being viewed */
let userBeingViewedId;

/** The id of the piece of evidence being displayed. */
let selectedEvidenceId;

/** WebLinksCount is used to restrict the amount of weblinks on a piece of evidence*/
let webLinksCount = 0;

/** The existing skills of the user, updated as the user's evidence is retrieved */
let skillsArray = []

/** The array of user ids who are linked to a piece of evidence */
let linkedUserIdsArray = []

/** Provides the options of categories and maps them to user-friendly strings */
let categoriesMapping = new Map([
    ["QUALITATIVE", "Qualitative"],
    ["QUANTITATIVE", "Quantitative"],
    ["SERVICE", "Service"]
])

/** Protocols urls can begin with **/
const VALID_PROTOCOLS = ["https://", "http://", "ftp://"]


$(() => {
    startCharacterCounting("text-input");
})


/**
 * Takes the response from an evidence list get request and adds the evidence previews to the left
 * side of the page.
 *
 * @param response - The response from GET /evidenceData
 */
function addEvidencePreviews(response) {
    let evidencePreviewsContainer = $("#evidenceList")
    evidencePreviewsContainer.empty()
    for (let pieceOfEvidence in response) {
        evidencePreviewsContainer.append(createEvidencePreview(response[pieceOfEvidence]))
    }
}


/**
 * This function is responsible for displaying the selected piece of evidence.
 *
 * If nothing is selected, it will default to either the first piece of evidence,
 * or a 'No evidence' display if none exist.
 *
 * It then calls the appropriate function for displaying said evidence.
 */
function showHighlightedEvidenceDetails() {
    if (selectedEvidenceId != null) {
        getHighlightedEvidenceDetails()
        return
    }
    let evidenceElements = $("#evidenceList").children()
    if (evidenceElements.length > 0) {
        selectedEvidenceId = evidenceElements.first().find(".evidenceId").text()
        getHighlightedEvidenceDetails()
    } else {
        setDetailsToNoEvidenceExists()
    }
}


/**
 * Adds the web links from the given request to the document.
 *
 * @param response The response from the backend, which contains the web links for a piece of evidence.
 */
function addWeblinksToDisplayedEvidence(response) {
    let webLinksDiv = $("#evidenceWebLinks")
    webLinksDiv.empty()

    $.each(response, (i, weblink) => {
        webLinksDiv.append(detailsWeblinkElement(weblink.url, weblink.alias))
    })

    if (webLinksDiv.children().length < 1) {
        $("#evidenceWebLinksBreakLine").hide()
    } else {
        $("#evidenceWebLinksBreakLine").show()
    }
    initialiseTooltips()
}


/**
 * Given a web url and an alias, creates and returns a web link element with a delete icon.
 * The main div will have the class 'secured' if it is https, or 'unsecured' otherwise
 *
 * If the url doesn't start with https, it will show an un-filled, unlocked icon.
 * If it does, it will show a locked, filled icon.
 *
 * @param url The web url of the web link
 * @param alias The alias/nickname of the web url.
 * @returns {string} A single-div webLink element, wrapped in ` - e.g. `<div>stuff!</div>`
 */
function deletableWeblinkElement(url, alias) {
    const icon = getWeblinkIcon(url)
    const formattedUrl = getFormattedUrl(url)
    const security = getWeblinkSecurity(url)
    let urlWithProtocol = url

    if (!hasProtocol(url)) {
        urlWithProtocol = "http://" + url
    }
    return (`
        <div class="webLinkElement ${security}" data-value="${sanitise(url)}">
            <button class="deleteWeblink deleteIcon">
                <svg class="bi bi-trash" fill="currentColor" height="20" viewBox="0 0 16 16" width="20"
                    xmlns="http://www.w3.org/2000/svg">
                    <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/>
                    <path d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z"
                    fill-rule="evenodd"/>
                </svg>
            </button>
            ${icon}
            <a href="${sanitise(urlWithProtocol)}" class="addedWebLink" data-bs-toggle="tooltip" data-bs-placement="top"
               data-bs-title="${formattedUrl}" data-bs-custom-class="webLinkTooltip" target="_blank">${sanitise(alias)}
            </a>
        </div>
    `)
}


/**
 * Given a web url and an alias, creates and returns a web link element with no delete icon.
 * The main div will have the class 'secured' if it is https, or 'unsecured' otherwise
 *
 * If the url doesn't start with https, it will show an un-filled, unlocked icon.
 * If it does, it will show a locked, filled icon.
 *
 * @param url The url of the weblink
 * @param alias The name associated with the weblink
 * @returns {string} HTML representing the weblink element's div.
 */
function detailsWeblinkElement(url, alias) {
    const icon = getWeblinkIcon(url)
    const security = getWeblinkSecurity(url)
    const formattedUrl = getFormattedUrl(url)
    let urlWithProtocol = url

    if (!hasProtocol(url)) {
        urlWithProtocol = "http://" + url
    }

    return (
        `<div class="webLinkElement ${security}" data-value="${sanitise(url)}">
            ${icon}
            <a href="${sanitise(urlWithProtocol)}" class="addedWebLink" data-bs-toggle="tooltip" data-bs-placement="top"
            data-bs-title="${formattedUrl}" data-bs-custom-class="webLinkTooltip" target="_blank">${sanitise(alias)}</a>
        </div>`
    )
}


/**
 * Gets the icon indicating weblink security based on the link protocol.
 *
 * @param url The url the icon is associated with
 * @returns {string} The HTML for the svg lock icon
 */
function getWeblinkIcon(url) {
    let icon

    if (url.startsWith("https://")) {
        icon = `
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-lock-fill lockIcon text-success" viewBox="0 0 16 16">
            <path d="M8 1a2 2 0 0 1 2 2v4H6V3a2 2 0 0 1 2-2zm3 6V3a3 3 0 0 0-6 0v4a2 2 0 0 0-2 2v5a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2z"/>
        </svg>
        `
    } else {
        icon = `
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-unlock lockIcon text-danger" viewBox="0 0 16 16">
            <path d="M11 1a2 2 0 0 0-2 2v4a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2h5V3a3 3 0 0 1 6 0v4a.5.5 0 0 1-1 0V3a2 2 0 0 0-2-2zM3 8a1 1 0 0 0-1 1v5a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V9a1 1 0 0 0-1-1H3z"/>
        </svg>
        `
    }

    return icon
}


/**
 * Gets the security value indicating weblink security based on the link protocol.
 *
 * @param url The url the icon is associated with
 * @returns {string} The security status of the weblink. Either "secure" or "unsecure".
 */
function getWeblinkSecurity(url) {
    let security

    if (url.startsWith("https://")) {
        security = "secure"
    } else {
        security = "unsecure"
    }

    return security
}


/**
 * Removes the protocol from the weblink, if it has one.
 * Protocols are defined globally, but the VALID_PROTOCOLS list.
 *
 * @param url The url to be formatted.
 * @returns The url, formatted to not include any protocol.
 */
function getFormattedUrl(url) {
    let formattedUrl

    if (hasProtocol(url)) {
        let slashIndex = url.search("//") + 2
            formattedUrl = url.slice(slashIndex)
    } else {
        formattedUrl = url
    }

    return formattedUrl
}


/**
 * Checks whether a given url address starts with any of the protocols defined in the VALID_PROTOCOLS list.
 *
 * @param url The url to be checked.
 * @returns {boolean} True if the url starts with one of the protocols, false otherwise/
 */
function hasProtocol(url) {
    let urlHasProtocol = false

    $.each(VALID_PROTOCOLS, (i, protocol) => {
        if (url.startsWith(protocol)) {
            urlHasProtocol = true
            return false
        }
    })

    return urlHasProtocol
}


// --------------------------- Server Queries ------------------------------------


/**
 * Gets the evidence data for the chosen user and adds it to the page.
 *
 * On successful retrieval, this adds the elements and calls the functions to populate the page.
 * To see these functions:
 *     - addEvidencePreviews(response): Populates the left side evidence menus
 *     - showHighlightedEvidenceDetails(): Populates the right side, the details of the highlighted evidence.
 *
 * Note: by default the first element is the highlighted element.
 */
function getAndAddEvidencePreviews() {

    let title = $(document).find(".evidenceTitle").first()
    title.text("Evidence");
    $(".selected").removeClass("selected")

    $.ajax({
        url: "evidenceData?userId=" + userBeingViewedId,
        success: function (response, status, xhr) {
            displayNameOrButton(xhr)
            addEvidencePreviews(response)
            updateSelectedEvidence();
            showHighlightedEvidenceDetails()
        }, error: function () {
            createAlert("Could not retrieve evidence data", AlertTypes.Failure)
        }
    })
}


/**
 *  Displays the create evidence button if the evidence being viewed is the logged-in user otherwise it displays the
 *  name of the user
 */
function displayNameOrButton(response) {
    let nameHolder = $("#nameHolder")
    if (userBeingViewedId !== userIdent) {
        $("#createEvidenceButton").remove();
        let usersName = response.getResponseHeader("Users-Name");
        nameHolder.html("Viewing evidence for " + usersName)
        nameHolder.show()
    } else {
        nameHolder.hide()
        $("#createEvidenceButton").show();
    }
}


/**
 * This is called to show the evidence details for the selected piece of evidence.
 *
 * If the selectedEvidenceId is null or the server cannot find the evidence, it selected the first
 * piece of evidence in the table, and sets the details to that. If there is no evidence, the appropriate
 * message is displayed.
 */
function getHighlightedEvidenceDetails() {
    if (selectedEvidenceId !== "") {
        $.ajax({
            url: "evidencePiece?evidenceId=" + selectedEvidenceId, success: function (response) {
                setHighlightEvidenceAttributes(response)
            }, error: function (error) {
                console.log(error)
                createAlert("Failed to receive active evidence", AlertTypes.Failure)
            }
        })
    } else {
        setDetailsToNoEvidenceExists()
    }
}


/**
 * Makes a call to the server and gets all the skills belonging to this user,
 * It then appends those skills to the list
 *
 * @param callback An optional callback function to be called upon successfully retrieving the skills
 */
function getSkills(callback = () => {
}) {
    $.ajax({
        url: "skills?userId=" + userBeingViewedId, type: "GET",
        success: function (response) {
            skillsArray = []
            $.each(response, function (i) {
                if (!skillsArray.includes(response[i].name)) {
                    skillsArray.push({
                        id: response[i].id,
                        name: response[i].name,
                        frequency: response[i].frequency
                    })
                }
            })
            callback()
        },
        error: function (response) {
            console.log(response)
        }
    })
}


// --------------------------- Functional HTML Components ------------------------------------

/**
 * Sets the evidence details (big display) values to the given piece of evidence.
 *
 * @param evidenceDetails The title, date, and description, skills, categories and associates for a piece of evidence.
 */
function setHighlightEvidenceAttributes(evidenceDetails) {
    let highlightedEvidenceId = $("#evidenceDetailsId")
    let highlightedEvidenceTitle = $("#evidenceDetailsTitle")
    let highlightedEvidenceDate = $("#evidenceDetailsDate")
    let highlightedEvidenceDescription = $("#evidenceDetailsDescription")

    highlightedEvidenceId.text(evidenceDetails.id)
    highlightedEvidenceTitle.text(evidenceDetails.title)
    highlightedEvidenceDate.text(evidenceDetails.date)
    highlightedEvidenceDescription.text(evidenceDetails.description)
    addWeblinksToDisplayedEvidence(evidenceDetails.webLinks)
    addLinkedUsersToEvidence(evidenceDetails.associates)
    addSkillsToEvidence(evidenceDetails.skills)

    highlightedEvidenceTitle.show()
    highlightedEvidenceDate.show()
    highlightedEvidenceDescription.show()
    $("#evidenceDetailsLinkedUsersDiv").show()
    addCategoriesToEvidence(evidenceDetails.categories)

    if (userBeingViewedId === userIdent) {
        $("#deleteEvidenceButton").show()
        $("#editEvidenceButton").show()
    } else {
        $("#deleteEvidenceButton").remove()
        $("#editEvidenceButton").remove()
    }
}


/**
 * Takes all the linked users associated with a piece of evidence and displays them on the evidence page, apart from the
 * owner as this is rather obvious
 *
 * @param users The associates for a piece of evidence.
 */

function addLinkedUsersToEvidence(users) {
    let linkedUsersDiv = $("#evidenceDetailsLinkedUsers")
    linkedUsersDiv.empty()

    $.each(users, function (i, user) {
        const userName = user.firstName + " " + user.lastName + " (" + user.username +")"
        linkedUsersDiv.append(linkedUserElement(user.id, userName,false));
    })
}


/**
 * Receives a list of skills and adds them to the focused evidence.
 *
 * @param skills The skills to be added.
 */
function addSkillsToEvidence(skills) {
    let highlightedEvidenceSkills = $("#evidenceDetailsSkills")
    highlightedEvidenceSkills.empty();

    // Sorts in alphabetical order
    skills.sort((a, b) => a.name.toLowerCase() > b.name.toLowerCase() ? 1 : -1)
    if (skills.length < 1) {
        highlightedEvidenceSkills.append(createSkillChip("No Skill", 0.5))
    } else {
        $.each(skills, function (i) {
            highlightedEvidenceSkills.append(createSkillChip(skills[i].name, skills[i].id, skills[i].frequency))
        })
    }
}


/**
 * A function to display all the categories for a piece of evidence
 *
 * @param categories A list of categories associated with a piece of evidence
 */
function addCategoriesToEvidence(categories) {
    let highlightedEvidenceCategories = $("#evidenceDetailsCategories")
    highlightedEvidenceCategories.empty();
    $.each(categories, function (category) {
        let categoryText = categoriesMapping.get(categories[category]);
        highlightedEvidenceCategories.append(createCategoryChip(categoryText, false))
    })
}


/**
 * Creates and returns an HTML element for an evidence preview
 *
 * @param evidence - A json object for a piece of evidence
 * @return the HTML component for previewing evidence of class evidenceListItem
 */
function createEvidencePreview(evidence) {
    let skills = getSkillTags(evidence.skills)
    let categories = getCategoryTags(evidence.categories)
    return `
        <div class="box evidenceListItem ${evidence.id === selectedEvidenceId ? 'selectedEvidence' : ''}">
            <div class="row evidenceListItemHeader">
                <p class="evidenceId" style="display: none">${sanitise(evidence.id)}</p>
                <p class="col evidenceListItemTitle">${sanitise(evidence.title)}</p>
                <p class="col evidenceListItemDate">${sanitise(evidence.date)}</p>
            </div>
            <div class="evidencePreviewTags categoryChipDisplay">${categories}</div>
            <div class="evidencePreviewTags skillChipDisplay">${skills}</div>
        </div>`
}


/**
 * Produces the HTML for the skill chip for each skill in the provided skills list.
 *
 * @param skills The skills to be added to the result.
 * @returns {string} HTMl to render the given skill names as skill chips.
 */
function getSkillTags(skills) {
    skills.sort((a, b) => a.name.toLowerCase() > b.name.toLowerCase() ? 1 : -1)
    let skillsHTML = ``
    $.each(skills, function (i) {
        skillsHTML += createSkillChip(skills[i].name, skills[i].id, skills[i].frequency)
    })
    return skillsHTML
}


/**
 * Produces the HTML for the skill chip for each category in the categories.
 *
 * @param categories The categories to be added to the result.
 * @returns {string} HTMl to render the given category names as skill chips.
 */
function getCategoryTags(categories) {
    categories.sort((a, b) => a.toLowerCase() > b.toLowerCase() ? 1 : -1)
    let categoriesHTML = ``
    $.each(categories, function (i) {
        categoriesHTML += createCategoryChip(categoriesMapping.get(categories[i]), false)
    })
    return categoriesHTML
}


/**
 * Hides the date and description fields and sets the Title field to no information.
 *
 * This function is called when the page is rendered and no evidence exists.
 */
function setDetailsToNoEvidenceExists() {
    let highlightedEvidenceTitle = $("#evidenceDetailsTitle")

    highlightedEvidenceTitle.text("No Evidence")
    highlightedEvidenceTitle.show()
    $("#evidenceDetailsDate").hide()
    $("#evidenceDetailsDescription").hide()
    $("#deleteEvidenceButton").hide()
    $("#editEvidenceButton").hide()
    $("#evidenceDetailsCategories").empty()
    $("#evidenceWebLinks").empty()
    $("#evidenceDetailsSkills").empty()
    $("#evidenceDetailsLinkedUsersDiv").hide()
}


//---- Tooltip Refresher----


/**
 * Refresh tooltip display
 */
function initialiseTooltips() {
    $('[data-bs-toggle="tooltip"]').tooltip();
}


/**
 * Check the number of Weblink, if it is more than 9, then the Add Web Link button not show
 */
function checkWeblinkCount() {
    let addWeblinkButton = $("#addWeblinkButton")
    let weblinkFullTab = $("#webLinkFull")
    if (webLinksCount > 9) {
        addWeblinkButton.hide()
        weblinkFullTab.show()
    } else {
        addWeblinkButton.show()
        weblinkFullTab.hide()
    }
}


/**
 * Resets the weblink count
 */
function resetWeblink() {
    let addWeblinkButton = $("#addWeblinkButton")
    let weblinkFullTab = $("#webLinkFull")
    addWeblinkButton.show()
    weblinkFullTab.hide()
    webLinksCount = 0
}


/**
 * Retrieves the added web links and creates a list of them in DTO form.
 *
 * @returns {*[]} A list of web links matching the web link DTO format.
 */
function getWeblinksList() {
    let evidenceCreationForm = $("#evidenceCreationForm")
    let weblinks = evidenceCreationForm.find(".webLinkElement")
    let weblinksList = []

    $.each(weblinks, function () {
        let weblinkDTO = {
            "url": this.querySelector(".addedWebLink").href,
            "name": this.querySelector(".addedWebLink").innerText
        }
        weblinksList.push(weblinkDTO)
    })
    return weblinksList
}


/**
 * Gets all the selected categories from the categories form
 *
 * @return a list of categories e.g., ["SERVICE", "QUANTITATIVE"]
 */
function getCategories() {
    let categoryButtons = $("#evidenceFormCategories")
    let selectedButtons = categoryButtons.find(".btn-success")
    let categories = []

    $.each(selectedButtons, function (button) {
        categories.push($(selectedButtons[button]).val())
    })
    return categories
}


// --------------------------------- Click listeners -----------------------------------------


/**
 * When an evidence div is clicked, it becomes selected and is displayed on the main display.
 *
 * There are 3 steps to this:
 *    1. remove the selected class from the previously selected div.
 *    2. Add the selected class to the clicked div, and assign it as selected
 *    3. Populate the display with the selected evidence details.
 */
$(document).on("click", ".evidenceListItem", function () {
    let previouslySelectedDiv = $(this).parent().find(".selectedEvidence").first()
    previouslySelectedDiv.removeClass("selectedEvidence")
    let newSelectedDiv = $(this).addClass("selectedEvidence")
    selectedEvidenceId = newSelectedDiv.find(".evidenceId").text()
    showHighlightedEvidenceDetails()
})


/**
 * Listen for a keypress in the weblink address field, and closes the alert box
 */
$(document).on('keypress', '#webLinkUrl', function () {
    updateErrorMessage($("#evidenceWeblinkAddressFeedback"), "")
})


/**
 * Listen for a keypress in the weblink name field, and closes the alert box
 */
$(document).on('keypress', '#webLinkName', function () {
    updateErrorMessage($("#evidenceWeblinkNameFeedback"), "")
})


/**
 * Calls the validity checking function on keyup of form inputs.
 */
$(document).on("change keyup", "#evidenceName", function () {
    disableEnableSaveButtonOnValidity()
    checkNameValidity()
})


/**
 * Calls the validity checking function on change of the description.
 */
$(document).on("change keyup", "#evidenceDescription", function () {
    disableEnableSaveButtonOnValidity()
    checkDescriptionValidity()
})


/**
 * Calls the validity checking function on change of form inputs.
 * This is different from keyup as it checks when the date changes.
 */
$(document).on("change", ".form-control", function () {
    disableEnableSaveButtonOnValidity()
    checkDateValidity()
})


/**
 * Pops up a confirmation message on the click of evidence deletion. If the confirmation is accepted,
 * then the delete request is sent. On a successful request the page is reloaded and an alert is made.
 */
$(document).on("click", "#deleteEvidenceButton", function () {
    const evidenceId = $("#evidenceDetailsId").text()
    const evidenceName = $("#evidenceDetailsTitle").text()
    if (window.confirm(`Are you sure you want to delete the evidence \n${evidenceName}`)) {
        $.ajax({
            url: `evidence?evidenceId=${evidenceId}`,
            type: "DELETE",
            success: () => {
                handleSuccessfulEvidenceDelete(evidenceName)
                 }, error: (response) => {
                createAlert(response.responseText, AlertTypes.Failure)
            }
        })
    }
})


/**
 * Refreshes the evidence page and creates an alert for successful evidence deletion.
 *
 * @param evidenceName The name of the deleted evidence.
 */
function handleSuccessfulEvidenceDelete(evidenceName) {
    selectedEvidenceId = null
    getAndAddEvidencePreviews()
    getSkills(addSkillsToSideBar)
    createAlert("Successfully deleted evidence: " + evidenceName, AlertTypes.Success)
}


// --------------------------------- Autocomplete -----------------------------------------


/** This split function splits the text by its spaces*/
function split(val) {
    return val.split(/\s+/);
}


/** this function splits the input by its spaces then returns the last word */
function extractLast(term) {
    return split(term).pop();
}


/**
 * Returns all the linked users' id's from the evidence creation form
 *
 * @returns [integer] the list of user id's to be attached
 */
function getLinkedUsers() {
    let linkedUsers = $("#linkedUsers").find(".linkedUser")
    let userIds = [];

    $.each(linkedUsers, function (i, user) {
        try {
            let userId = parseInt(user.id.replace("linkedUserId", ""), 10);
            userIds.push(userId)
        } catch (error) {
            createAlert("Oops! there was an error with one or more of the linked users", AlertTypes.Failure)
        }
    })
    return userIds;
}


/**
 * Searches the skill array for a version of the given string.
 * This is a case-insensitive search, though accents (like a vs â) are treated as different.
 * If the skill array contains the given string, return it.
 * Otherwise, return the original string.
 *
 * This does not perform whitespace to underscore conversion; you'll have to do it yourself.
 *
 * @param string The string you want to look for
 * @returns {*} The skill array's (possibly case-different) version of this string.
 * If it is not in the array, this returns the input string.
 */
function replaceWithStringFromSkillArray(string) {
    for (let skill of skillsArray) {
        if (skill.name.localeCompare(string, undefined, {sensitivity : 'accent'}) === 0) {
            return skill.name // There exists a skill, so use that
        }
    }
    return string
}


/**
 * Due to a weird bug where the page would reload if you closed an alert if the
 * alert was open in a modal, this was added to stop the form from submitting which
 * seemed to be the cause of the issue.
 */
$(document).on("submit", "#evidenceCreationForm", function (e) {
    e.preventDefault()
})


/**
 * Saves the evidence input during creating a new piece of evidence
 */
$(document).on("click", "#evidenceSaveButton", function (e) {
    e.preventDefault()
    let eventPress = {"key": " "}
    handleSkillInputKeypress(eventPress) // Sends a fake space input to create the final skill in the box
    handleEvidenceSave()
})


/**
 * If the weblink form is closed, calls the function to toggle it open.
 * If the weblink form is open, calls the function to submit the form.
 */
function handleWeblinkAdd() {
    const button = $("#addWeblinkButton");

    if (button.hasClass("toggled")) {
        submitWebLink()
    } else {
        webLinkButtonToggle()
    }
}


/**
 * Checks the validity of the address in the weblink error form against the central weblink requirements.
 *
 * @returns {boolean} True if the address is valid.
 */
function checkWeblinkAddressValidity() {
    const webLinkUrl = $("#webLinkUrl").val();
    const weblinkAddressErrorDiv = $("#evidenceWeblinkAddressFeedback")

    const weblinkRegex = new RegExp(weblinkRegexPattern)

    if (!weblinkRegex.test(webLinkUrl)) {
        updateErrorMessage(weblinkAddressErrorDiv, `Weblink address ${weblinkRegexRequirements}`)
        return false
    }

    if (webLinkUrl.length > 2000) {
        updateErrorMessage(weblinkAddressErrorDiv, `Weblink address cannot be longer than 2000 characters`)
        return false
    }

    updateErrorMessage(weblinkAddressErrorDiv, "")
    return true
}


/**
 * Checks the validity of the name in the weblink error form against the general unicode requirements.
 *
 * @returns {boolean} True if the name is valid.
 */
function checkWeblinkNameValidity() {
    const webLinkName = $("#webLinkName").val();
    const weblinkNameErrorDiv = $("#evidenceWeblinkNameFeedback")

    if (webLinkName.length === 0) {
        updateErrorMessage(weblinkNameErrorDiv, "Weblink name must not be empty")
        return false
    }

    if (!regex.test(webLinkName)) {
        updateErrorMessage(weblinkNameErrorDiv, `Weblink name ${GENERAL_UNICODE_REQUIREMENTS}`)
        return false
    }

    if (webLinkName.length > 50) {
        updateErrorMessage(weblinkNameErrorDiv, `Weblink name cannot be longer than 50 characters`)
        return false
    }

    updateErrorMessage(weblinkNameErrorDiv, "")
    return true
}


/**
 * Listens for when add web link button is clicked.
 * Slide-toggles the web link portion of the form.
 */
$(document).on('click', '#addWeblinkButton', function (e) {
    e.preventDefault()
    handleWeblinkAdd()
})


/**
 * Listens for when delete web link button is clicked.
 * the web link will be deleted.
 */
$(document).on('click', '.deleteWeblink', function () {
    $(this).parent().remove();
    webLinksCount -= 1;
    checkWeblinkCount();
})


/**
 * Listens for when delete web link button is clicked.
 * Refreshes error messages on the form.
 * */
$(document).on('change keyup', '#webLinkUrl', checkWeblinkAddressValidity)


/**
 * Listens for a change of the web link name in the web link form.
 * Refreshes error messages on the form.
 */
$(document).on('change keyup', '#webLinkName', checkWeblinkNameValidity)


/**
 * Listens for when delete web link button is clicked.
 * The user will be removed.
 */
$(document).on('click', '.deleteLinkedUserButton', function () {
    $(this).parent().remove();
})


/**
 * Closes the add weblink form and resets weblink form buttons when the weblink add is cancelled.
 */
$(document).on('click', '#cancelWeblinkButton', () => {
    webLinkButtonToggle()
})


/**
 * Prevents the add evidence modal from being closed if an alert is present.
 */
$('#addOrEditEvidenceModal').on('hide.bs.modal', function (e) {
    let alert = $("#alertPopUp")
    if (alert.is(":visible") && alert.hasClass("backgroundRed")) {
        alert.effect("shake")
        e.preventDefault();
        e.stopPropagation();
        return false;
    }
});


/**
 * This disabled the requirement for the web link forms to be filled out if they are empty.
 * This was because the overall "Add Evidence" form does a validation of all its fields when something changes.
 * Because these fields are required to both be filled then they don't allow that check to pass if they are empty.
 * This now disables those requirements if nothing is in them so that a form can be submitted if they are both empty.
 * It re-enables them if a user starts to type in them.
 */
function toggleRequiredIfCheckURLInputsAreEmpty() {
    let webLinkUrl = $("#webLinkUrl")
    let webLinkName = $("#webLinkName")
    if (webLinkUrl.val().length < 1 && webLinkName.val().length < 1) {
        webLinkUrl.removeAttr("required")
        webLinkName.removeAttr("required")
        webLinkName.removeAttr("minlength")
    } else {
        webLinkUrl.attr("required", "required")
        webLinkName.attr("required", "required")
        webLinkName.attr("minlength", "1")
    }
}


/**
 * Toggles the add weblink button,
 * and slide-toggles the form
 */
function webLinkButtonToggle() {
    let saveButton = $("#addWeblinkButton");
    let cancelButton = $("#cancelWeblinkButton")
    $("#weblinkForm").slideToggle();
    if (saveButton.hasClass("toggled")) {
        saveButton.text("Add Web Link")
        saveButton.removeClass("toggled")
        saveButton.removeClass("btn-primary")
        saveButton.addClass("btn-secondary")
        $(".weblinkAlert").alert('close')
        cancelButton.hide()
    } else {
        saveButton.text("Save Web Link")
        saveButton.addClass("toggled")
        saveButton.removeClass("btn-secondary")
        saveButton.addClass("btn-primary")
        cancelButton.show()
    }
}


/**
 * Toggles the add Linked Users button and slide-toggles the form
 */
$(document).on('click', '#linkUsersToEvidenceButton', function () {
    let linkedUsersForm = $("#linkUsersForm")
    let linkButton = $("#linkUsersToEvidenceButton");
    if (linkButton.hasClass("toggled")) {
        linkButton.text("Link Users")
        linkButton.removeClass("toggled")
    } else {
        linkButton.text("Cancel")
        linkButton.addClass("toggled")
    }
    linkedUsersForm.slideToggle();
})


/**
 * Appends a new link to the list of added links in the Add Evidence form, if the form contents are valid.
 */
function submitWebLink() {
    if (checkWeblinkAddressValidity() && checkWeblinkNameValidity()) {
        const alias = $("#webLinkName")
        const url = $("#webLinkUrl")
        const addedWebLinks = $("#addedWebLinks")
        const webLinkTitle = $("#webLinkTitle")

        webLinkTitle.show()
        addedWebLinks.append(deletableWeblinkElement(url.val(), alias.val()))
        url.val("")
        alias.val("")
        webLinksCount += 1
        checkWeblinkCount()
        initialiseTooltips()
    }
}


/**
 * Adds the user to be linked to the create evidence modal
 */
function addLinkedUser(user) {
    let linkedUsersDiv = $("#linkedUsers")
    if (!linkedUserIdsArray.includes(user.id)) {
        $("#linkedUsersTitle").show()
        linkedUserIdsArray.push(user.id)
        const userName = user.firstName + " " + user.lastName + " (" + user.username +")"
        linkedUsersDiv.append(linkedUserElement(user.id, userName, true))
        $('.deleteLinkedUserButton').show()
    }
}


/**
 * Creates the element for displaying the linked user
 */
function linkedUserElement(userId, userName, deletable) {
    let deleteIcon = ''
    if (deletable) {
        deleteIcon = `
            <button class="deleteLinkedUserButton">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trash" viewBox="0 0 16 16">
                    <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/>
                    <path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z"/>
                </svg>
            </button>
        `
    }

    return `<div id="linkedUserElement">
                ${deleteIcon}
                <div class="linkedUser" id="linkedUserId${userId}" data-id="${userId}">
                     ${userName}</div>
           </div> `
}


/**
 * Redirects to a user's evidence page when their name is clicked on a piece of evidence.
 */
$(document).on("click", ".linkedUser", function () {
    let userId = this.getAttribute("data-id")
    redirectToUsersHomePage(userId) //redirect to the user's evidence page
})


/**
 * Clears all fields (except the date field, which is reset to today) in the "Add Evidence" form.
 */
function clearAddEvidenceModalValues() {
    $("#evidenceName").val("")
    $("#evidenceDescription").val("")
    $("#webLinkUrl").val("")
    $("#webLinkName").val("")
    $("#evidenceDate").val(todaysDate)
    $("#addedWebLinks").empty()
    $("#linkedUsers").empty()
    $("#webLinkTitle").hide()
    skillsToCreate.clear()
    updateSkillsInput()
    $("#linkedUsersTitle").hide()
    $(".btn-success").addClass("btn-secondary").removeClass("btn-success")
    $(".evidenceCategoryTickIcon").hide();
    $(".countCharName").html("50 characters remaining")
    $(".countCharDescription").html("500 characters remaining")
    linkedUserIdsArray = []
}


// -------------------------------------- Validation -----------------------------------


/**
 * Checks the form is valid, enables or disables the save button depending on validity.
 */
function disableEnableSaveButtonOnValidity() {
    toggleRequiredIfCheckURLInputsAreEmpty()
    if ($("#evidenceCreationForm")[0].checkValidity() && $("#evidenceSkillFeedback").val().length < 1) {
        $("#evidenceSaveButton").prop("disabled", false)
    } else {
        $("#evidenceSaveButton").prop("disabled", true)
    }
}


/**
 * Checks that the name of a piece of evidence match the required regex.
 * Adds appropriate error messages for invalid inputs - not matching regex or empty fields.
 */
function checkNameValidity() {
    const name = $("#evidenceName")
    const nameVal = name.val()
    const nameError = $("#evidenceNameFeedback")
    const nameIsValid = GENERAL_UNICODE_REGEX.test(nameVal)

    if (nameIsValid) {
        name.removeClass("invalid")
        nameError.hide()
    } else {
        name.addClass("invalid")

        if (nameVal.trim().length === 0) {
            nameError.text("Name cannot be empty")
        } else {
            nameError.text("Name " + GENERAL_UNICODE_REQUIREMENTS)
        }

        nameError.show()
    }
}


/**
 * Checks that the description of a piece of evidence match the required regex.
 * Adds appropriate error messages for invalid inputs - not matching regex or empty fields.
 */
function checkDescriptionValidity() {
    const description = $("#evidenceDescription")
    const descriptionVal = description.val()
    const descriptionError = $("#evidenceDescriptionFeedback")
    const descriptionIsValid = GENERAL_UNICODE_REGEX.test(descriptionVal)

    if (descriptionIsValid) {
        description.removeClass("invalid")
        descriptionError.hide()
    } else {
        description.addClass("invalid")

        if (descriptionVal.trim().length === 0) {
            descriptionError.text("Description cannot be empty")
        } else {
            descriptionError.text("Description " + GENERAL_UNICODE_REQUIREMENTS)
        }

        descriptionError.show()
    }
}


/**
 * Checks that the current date in the evidence modal date picker is within the project dates and not in the future.
 * If the date is invalid, a relevant error message is displayed.
 */
function checkDateValidity() {
    const date = $("#evidenceDate")
    const proposedDate = Date.parse(date.val().toString())
    const earliestDate = Date.parse(projectStartDate)
    const latestDate = Date.parse(evidenceMaxDate)
    const dateError = $("#evidenceDateFeedback")

    if (proposedDate < earliestDate) {
        dateError.text(`Date cannot be before project start.\n Please choose a date between ${projectStartFormatted} and ${projectEndFormatted}`)
        dateError.show()
    }
    else if (proposedDate > latestDate) {
        dateError.text(`Evidence date must be before the project end and not in the future.\n Please choose a date between ${projectStartFormatted} and ${projectEndFormatted}`)
        dateError.show()
    } else {
        dateError.text("")
        dateError.hide()
    }
}


/**
 * Creates HTMl for a skill chip with the given skill name.
 *
 * @param skillName The name to be displayed in the skill chip.
 * @param skillId The ID of the skill saved in the database.
 * @param deletable Optional parameter determining if the chip is deletable, false by default
 * @returns {string} The string of HTMl representing the skill chip.
 */
//Todo make skill chips not bad
function createSkillChip(skillName, skillId, deletable = false) {
    const deleteIcon = deletable ? `
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-x-circle chipDelete" viewBox="0 0 16 16">
            <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
            <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
        </svg>` : ""

    return `
        <div class="chip skillChip ${deletable ? "editableChip" : "sortableChip"}" ${skillId !== undefined ? "data-id=" + skillId: ""}>
            <span class="chipText noDisplayInput focus" role="textbox">${sanitise(skillName)}</span>
            ${deleteIcon}
        </div>`
}


/**
 * Creates HTMl for a category chip with the given category name.
 *
 * @param categoryName The name to be displayed in the category chip.
 * @param isMenuItem Boolean value reflecting whether the chip will be displayed in the menu bar.
 * @returns {string} The string of HTMl representing the category chip.
 */
function createCategoryChip(categoryName, isMenuItem) {
    if (isMenuItem) {
        return `
            <div id=${sanitise("categoryCalled" + categoryName.replaceAll(" ", "_"))} class="chip categoryChip">
                <p class="chipText">${sanitise(categoryName)}</p>
            </div>`
    } else {
        return `
            <div class="chip categoryChip sortableChip">
                <p class="chipText">${sanitise(categoryName)}</p>
            </div>`
    }
}



// ----------------------------- SAVING EVIDENCE -----------------------------------

/**
 * Retrieves input values from the add evidence form and formats them in JSON.
 *
 * @returns {string} A JSON string of the evidence's data, formatted as an EvidenceDTO.
 */
function getDataFromEvidenceForm() {
    const evidenceId = $("#addOrEditEvidenceModal").attr("data-id")
    const title = $("#evidenceName").val()
    const date = $("#evidenceDate").val()
    const description = $("#evidenceDescription").val()
    const projectId = 1
    let webLinks = getWeblinksList();
    const skills = Array.from(skillsToCreate, ([key, val]) => ({"id" : val, "name" : key}))
    const categories = getCategories();
    const linkedUsers = getLinkedUsers();

    return JSON.stringify({
        "id": evidenceId,
        "title": title,
        "date": date,
        "description": description,
        "projectId": projectId,
        "webLinks": webLinks,
        "skills": skills,
        "categories": categories,
        "associateIds": linkedUsers
    })
}


/**
 * Updates the evidence page and resets the add evidence modal.
 *
 * @param response A server response containing data about the saved evidence, including its Id and skills.
 */
function handleSuccessfulEvidenceSave(response) {
    selectedEvidenceId = response.id
    getAndAddEvidencePreviews()
    getSkills(addSkillsToSideBar)
    closeModal()
    clearAddEvidenceModalValues()
    $(".alert").remove()
    disableEnableSaveButtonOnValidity() //Gets run to disable the save button on form clearance.
    resetWeblink()
}


/**
 * Makes an endpoint request to save a new piece of evidence.
 *
 * @param data the data for the evidence being created.
 */
function createEvidence(data) {
    $.ajax({
        url: 'evidence',
        type: "POST",
        contentType: "application/json",
        data,
        success: (response) => {
            handleSuccessfulEvidenceSave(response)
            createAlert("Created evidence", AlertTypes.Success)
        }, error: (error) => {
            createAlert(error.responseText, AlertTypes.Failure, ".modalBody")
        }
    })
}


/**
 * Makes an endpoint request to save a new piece of evidence.
 *
 * @param data the data for the evidence being created.
 */
function editEvidence(data) {
    $.ajax({
        url: 'evidence',
        type: "PATCH",
        contentType: "application/json",
        data,
        success: (response) => {
            handleSuccessfulEvidenceSave(response)
            createAlert("Successfully edited evidence", AlertTypes.Success)
        }, error: (error) => {
            createAlert(error.responseText, AlertTypes.Failure, ".modalBody")
        }
    })
}


/**
 * Validates the inputs in the evidence form. Calls the method to create a nw piece of evidence, if the evidence save
 * button has the text "Create".
 */
function handleEvidenceSave() {
    const evidenceCreationForm = $("#evidenceCreationForm")[0]
    toggleRequiredIfCheckURLInputsAreEmpty()

    if (!evidenceCreationForm.checkValidity()) {
        evidenceCreationForm.reportValidity()
    } else {
        const evidenceData = getDataFromEvidenceForm()
        const buttonName = $("#evidenceSaveButton").text()

        if (buttonName === "Create") { // create a new evidence
            createEvidence(evidenceData)

        } else { // edit an existing piece of evidence
            editEvidence(evidenceData)
        }
    }
}