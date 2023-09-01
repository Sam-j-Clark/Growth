let selectedChip;

/**
 * Runs when the page is loaded. This gets the user being viewed and adds dynamic elements.
 */
$(() => {
    let urlParams = new URLSearchParams(window.location.search)
    if (urlParams.has("userId")) {
        userBeingViewedId = parseInt(urlParams.get('userId'))
    } else {
        userBeingViewedId = userIdent
    }

    getAndAddEvidencePreviews()
    addCategoriesToSidebar()
    getSkills(addSkillsToSideBar)
})


/**
 * Adds all the skills in the skills array to the sidebar
 * Note that the ID of each div is SkillCalled{skill_name} and includes underscores
 */
function addSkillsToSideBar() {
    let skillsContainer = $('#skillList')
    skillsContainer.empty()

    skillsContainer.append(createSkillChip("No Skill", undefined, 0.5));
    for (let skill of skillsArray) {
        skillsContainer.append(createSkillChip(
            skill.name.replaceAll("_", " "),
            skill.id,
            skill.frequency
        ))
    }
}


/**
 * Adds the categories to the side bar of the evidence page to allow for easy navigation
 */
function addCategoriesToSidebar() {
    let categoriesList = $('#categoryList')
    for (let category of categoriesMapping.values()) {
        categoriesList.append(createCategoryChip(category, true))
    }
}


/**
 * Populates the evidence table with all pieces of evidence with that
 * specific skill.
 */
function showEvidenceWithSkill() {
    // Get all the pieces of evidence related to that skill
    $.ajax({
        url: "evidenceLinkedToSkill?userId=" + userBeingViewedId,
        type: "GET",
        data: {
            "skillName": selectedChip
        },
        success: function (response) {
            addEvidencePreviews(response)
            updateSelectedEvidence()
            showHighlightedEvidenceDetails()
        }, error: function (error) {
            createAlert(error.responseText, AlertTypes.Failure)
        }
    })
}


/**
 * Populates the evidence table with all pieces of evidence with that
 * specific skill.
 */
function showEvidenceWithCategory() {
    // Get all the pieces of evidence related to that skill
    $.ajax({
        url: "evidenceLinkedToCategory?category=" + selectedChip + "&userId=" + userBeingViewedId,
        success: function (response) {
            addEvidencePreviews(response)
            updateSelectedEvidence()
            showHighlightedEvidenceDetails()
        }, error: function (error) {
            createAlert(error.responseText, AlertTypes.Failure)
        }
    })
}


/**
 * Updated which piece of evidence is currently selected
 */
function updateSelectedEvidence() {
    let previouslySelectedDiv = $(".selectedEvidence")
    previouslySelectedDiv.removeClass("selectedEvidence")

    let evidenceElements = $("#evidenceList").children()
    evidenceElements.first().addClass("selectedEvidence")
    selectedEvidenceId = evidenceElements.first().find(".evidenceId").text()
}


/* ------------ Event Listeners ----------------- */


/**
 * When a chip div is clicked, it selects the skill/category in the sidebar and is displays all
 * evidence with that skill/category.
 *
 * There are 3 steps to this:
 *    1. remove the selected class from selected divs.
 *    2. Add the selected class to the clicked div, and assign it as selected
 *    3. Populate the display with the selected evidence details.
 */
$(document).on("click", ".sortableChip" , function (event) {
    $(".selected").removeClass("selected")

    let clicked = $(this)
    selectedChip = clicked.find('.chipText').text()
    let isSkill = clicked.hasClass("skillChip")
    let title = $(".evidenceTitle").first()
    title.text(selectedChip)
    if (isSkill) {
        showEvidenceWithSkill()
    } else {
        showEvidenceWithCategory()
    }
    event.stopPropagation()
})


//todo document
function setChipToEditMode(chip) {
    chip.find(".chipText").attr("contenteditable", true)
    const input = chip.find(".noDisplayInput")
    if (!(input.is(":focus"))) {
        originalSkillName = chip.find(".chipText").text()
    }
    input.focus()
}


//todo document
$(document).on("click", ".editableChip", function() {
    setChipToEditMode($(this))
})


//todo document
$(document).on("click", "#showAllEvidence", () => getAndAddEvidencePreviews())


/**
 *  A Listener for the create evidence button. This displays the modal and prevents the page below from scrolling.
 *  Resets the form values to be empty.
 */
$(document).on("click", "#createEvidenceButton" , () => {
    resetAddOrEditEvidenceForm()
    startCharacterCounting("form-control")
    $("#addOrEditEvidenceTitle").html("Add Evidence")
    $("#evidenceSaveButton").html("Create")

    $("#addOrEditEvidenceModal").show()
    $(".modalContent").show("drop", {direction: "up"}, 200)
    $('body,html').css('overflow','hidden');
})


/**
 *  A listener for the cancel create evidence button. Calls the function to close the modal
 */
$(document).on("click", "#evidenceCancelButton", function () {
    closeModal()
})


/**
 *  When the mouse is clicked, if the modal is open, the click is outside the modal, and the click is not on an alert,
 *  calls the function to close the modal.
 */
window.onmousedown = function(event) {
    let modalDisplay = $("#addOrEditEvidenceModal").css("display")
    if (modalDisplay === "block" && !event.target.closest(".modalContent") && !event.target.closest(".alert")) {
        closeModal()
    }
}


/**
 *  Closes the modal and allows the page below to scroll again
 */
function closeModal() {
    $(".modalContent").hide("drop", {direction: "up"}, 200, () => {$("#addOrEditEvidenceModal").hide()})
    $('body,html').css('overflow','auto');
}


// -------------------------------------- Evidence Editing -----------------------------------


/**
 *  Get today date as format of yyyy-mm-dd
 */
function getTodayDate() {
    let today = new Date()
    let year = today.getFullYear()
    let month = String(today.getMonth() + 1).padStart(2,'0')
    let day = String(today.getDate()).padStart(2, '0')
    return year + '-' + month + '-' +day
}


/**
 *  Resets evidence modal values to be blank and disables the save button.
 */
function resetAddOrEditEvidenceForm() {
    $("#addOrEditEvidenceModal").removeAttr("data-id")

    $("#evidenceName").val("")
    $("#evidenceDate").val(getTodayDate())
    $("#evidenceDescription").val("");
    $("#tagInputChips").empty();
    $("#addedWebLinks").empty();

    $("#skillsInput").val("")
    $("#evidenceSkillFeedback").text("")

    $(".evidenceFormCategoryButton").each(function() {
        $(this).removeClass("btn-success")
        $(this).addClass("btn-secondary")
        $(this).parent().find(".evidenceCategoryTickIcon").hide()
    })

    $("#linkedUsers").empty()
    $("#evidenceSaveButton").prop("disabled", true)
    skillsToCreate.clear()
}


/**
 * Sets the evidence modal buttons & title for editing evidence.
 * "Save" buttons reads "Save Changes", and the title is "Edit Evidence"
 */
function resetEvidenceButtonsToEditing(){
    const evidenceSaveButton = $("#evidenceSaveButton")

    $("#addOrEditEvidenceTitle").html( "Edit Evidence");
    evidenceSaveButton.html("Save Changes");
    evidenceSaveButton.prop("disabled", false);
}


/**
 * Retrieves evidence name, date, and description from the highlighted evidence.
 * Sets these values in the edit evidence modal.
 */
function setEvidenceData() {
    const currentEvidenceTitle =  ($("#evidenceDetailsTitle").text())
    const currentEvidenceDate =  $("#evidenceDetailsDate").text()
    const currentEvidenceDescription =  ($("#evidenceDetailsDescription").text())

    $("#evidenceName").val(currentEvidenceTitle)
    $("#evidenceDate").val(currentEvidenceDate);
    $("#evidenceDescription").val(currentEvidenceDescription);
}


/**
 * Retrieves the skills from the given highlighted evidence div, and adds each skill as a tag to the #tagInputChips div.
 *
 * @param evidenceHighlight The highlighted evidence div containing the skills.
 */
function setSkills(evidenceHighlight) {
    const currentSkillsList = evidenceHighlight.find(".skillChip")
    skillsToCreate.clear()
    currentSkillsList.each(function() {
        const skillName = ($(this).find(".chipText").text())
        const skillId = $(this).attr("data-id")
        skillsToCreate.set(skillName, skillId)
        const skillChip = createSkillChip(skillName, skillId, undefined, true)
        if (skillName !== "No Skill") {
            $("#tagInputChips").append(skillChip);
        }
    })
}


/**
 * Hides the added weblinks title on the edit evidence modal.
 * Gets each weblink from the highlighted evidence and appends the weblink to the edit form.
 */
function setWeblinks() {
    const webLinksList = $(".addedWebLink")
    if (webLinksList.length > 0) {
        $("#webLinkTitle").show()
        $.each(webLinksList, function () {
            const webName = $(this).text()
            const webUrl = $(this).attr("href")
            $("#addedWebLinks").append(deletableWeblinkElement(webUrl, webName))
        })
    } else {
        $("#webLinkTitle").hide()
    }
}


/**
 * Gets the categories from the selected evidence and selects them in the evidence edit form.
 *
 * @param evidenceHighlight The highlighted evidence div containing the categories.
 */
function setCategories(evidenceHighlight) {
    evidenceHighlight.find(".categoryChip").each(function() {
        const categoryName = $(this).find(".chipText").text()
        const categoryButton = $(`#button${categoryName}`)
        categoryButton.addClass("btn-success")
        categoryButton.removeClass("btn-secondary")
        categoryButton.find(".evidenceCategoryTickIcon").css("display", "inline-block")
    })
}


/**
 * Retrieves linked users from the highlighted evidence and adds them to the edit evidence modal.
 */
function setLinkedUsers() {
    const userLinkedList = $("#evidenceDetailsLinkedUsers").find(".linkedUser")
    $("#linkedUsersTitle").show()
    $.each(userLinkedList, function (i, user) {
        const userId = user.getAttribute("data-id")
        linkedUserIdsArray.push(parseInt(userId, 10))
        const userName = user.innerText
        if (userId !== String(userIdent)){
            $("#linkedUsers").append(linkedUserElement(userId, userName,true))
        }
    })
}


/**
 * Sets values on the evidence edit form to match the currently selected piece of evidence.
 * Opens the modal with the populated fields.
 */
function handleEvidenceEdit() {
    let selectedEvidence = $("#evidenceDetailsContainer")
    resetAddOrEditEvidenceForm()
    resetEvidenceButtonsToEditing()
    setEvidenceData()
    setSkills(selectedEvidence)
    setCategories(selectedEvidence)
    setWeblinks(selectedEvidence)
    setLinkedUsers()
    const editModal = $("#addOrEditEvidenceModal")
    editModal.attr("data-id", parseInt($("#evidenceDetailsId").text(), 10))
    editModal.show()
    startCharacterCounting("form-control")
    $(".modalContent").show("drop", {direction: "up"}, 200)
    $('body,html').css('overflow','hidden');
}


/**
 *  A Listener for the edit evidence button. This displays the modal and prevents the page below from scrolling
 */
$(document).on("click", "#editEvidenceButton" , handleEvidenceEdit)


$(document).on("focusout", ".chipText", function () {
    const theElement = $(this)
    const skillChip = theElement.parent()
    skillChip.attr("contenteditable", false)
    const skillName = theElement.text()
    if (validateSkillInput(skillName, true)) {
        updateSkillInSkillsToCreate(skillName)
    }

    updateSkillsInput(false)
    originalSkillName = null
})

