
/** For adding the skills to as the chips are added. */
let skillsToCreate = new Map()
const skillsInput = $("#skillsInput")
let oldInput = ""
let originalSkillName;


/**
 * Adds a string to the skillsToCreate list if it is not present.
 * If there is another skill in the list with the same Id, the other skill is given an undefined Id, since it must be an
 * edited version of this skill.
 * If there's a case-insensitive alternative in the skills array, use that instead.
 * Multiple consecutive underscores in a string will be condensed to a single space.
 *
 * @param skillName Name of skill to be added
 * @returns {boolean} True if the skill was added, false otherwise
 */
function addUniqueSkill(skillName) {
    let oneOrMoreUnderScores = new RegExp("[_]+", "g")
    let skillNameFormatted = replaceWithStringFromSkillArray(skillName.replaceAll(oneOrMoreUnderScores, " "))
    if (! Array.from(skillsToCreate.keys()).includes(skillNameFormatted)) {
        let skillId;
        const match = skillsArray.find(skill => {return skill.name === skillNameFormatted})
        if (match) {
            skillId = match.id
        }
        for(let [name, id] of skillsToCreate.entries()) {
            if (id === skillId) {
                skillsToCreate.set(name, undefined)
            }
        }

        if (skillNameFormatted.trim().length > 0) {
            skillsToCreate.set(skillNameFormatted, skillId)
            return true
        }
    }
    return false
}


/**
 * Updates an existing skill in the skills to create, and replaces it with the new name.
 *
 * If the old skill name was an existing skill for the user, then the updated skill name will replace the old skill name.
 * Otherwise, if the old skill was not on of the user's skills, and the new skill is, then the skill will retain its Id.
 * If the old skill and new skill are both skills that the user did not previously have, then the skill has an undefined
 * Id and will be added to the user as a new skill.
 */
function updateSkillInSkillsToCreate(newSkillName) {
    let oneOrMoreUnderScores = new RegExp("_+", "g")
    let re = new RegExp(String.fromCharCode(160), "g");
    let skillNameFormatted = replaceWithStringFromSkillArray(newSkillName.replaceAll(oneOrMoreUnderScores, " "))
    skillNameFormatted = skillNameFormatted.replaceAll(/  +/g, '_').replaceAll(re, '_').replaceAll(/_+/g, ' ')

    const originalId = parseInt(skillsToCreate.get(originalSkillName), 10)

    skillsToCreate.delete(originalSkillName)

    if (typeof originalId === "number") {
        skillsToCreate.set(skillNameFormatted, originalId)
    } else {
        skillsToCreate.set(skillNameFormatted, undefined)
    }
}


/**
 * Checks that a skill name is between 1 and 30 characters (inclusive), and is not a reserved skill.
 * Creates error messages and adds error classes as required.
 *
 * @param inputValue The skill name to be checked
 * @param showMessage Boolean value representing whether a message should be shown on fail
 * @returns {boolean} True if the skill is valid, false otherwise
 */
function validateSkillInput(inputValue, showMessage) {
    const evidenceSkillFeedback = $("#evidenceSkillFeedback")
    let isValid = true
    let errorMessage = ""

    if (inputValue.length > 30 && inputValue.indexOf(" ") === -1) {
        errorMessage = "Skill names cannot be longer than 30 characters."
        isValid = false
    } else if (! GENERAL_UNICODE_REGEX.test(inputValue)) {
        errorMessage =`Invalid character in skill name. \nSkill names${GENERAL_UNICODE_REQUIREMENTS}`
        isValid = false
    } else if (RESERVED_SKILL_TAGS.includes(inputValue.toLowerCase())) {
        errorMessage = "This is a reserved tag and cannot be manually created."
        isValid = false
    } else if (inputValue.trim().length === 0) {
        return false // does not style the div as there is no text to style
    } else if (inputValue.length > 0 && !skillRegex.test(inputValue)) { // TODO check this is used over gen unicode
        errorMessage = "Skill name must contain at least one letter."
        isValid = false
    }
    (!isValid ? skillsInput.addClass("skillChipInvalid") :
        skillsInput.removeClass("skillChipInvalid"))

    updateErrorMessage(evidenceSkillFeedback, errorMessage)
    return isValid
}


/**
 * Adds skill chips to the skill input.
 * Underscores are replaced with spaces.
 * Clears existing input.
 *
 * @param shouldClear true by default, defines if the input should be cleared on call.
 */
function updateSkillsInput(shouldClear = true) {
    let chipDisplay = $("#tagInputChips")
    $('[data-toggle="tooltip"]').tooltip("hide")

    chipDisplay.empty()
    skillsToCreate.forEach(function (value, key) {
        key = key.replaceAll("_", " ");
        if (skillRegex.test(key)) {
            chipDisplay.append(createSkillChip(key, value, undefined, true))
        }
    })
    if (shouldClear) {
        skillsInput.val("")
    }
    oldInput = ""
}


/**
 * Takes an event, containing a keypress, and either adds the last skill as a tag, removes the skill from the input, or
 * does nothing.
 * Deletes if backspace is pressed.
 * Adds as a tag if space bar, enter, or tab is pressed.
 *
 * @param event The event containing the key press value
 */
function handleSkillInputKeypress(event) {
    disableEnableSaveButtonOnValidity()
    const inputValue = skillsInput.val().trim()
    const isValidSkillName = validateSkillInput(inputValue, true)
    const evidenceSkillFeedback = $("#evidenceSkillFeedback")
    let needsUpdate = false

    if (event.key === "Backspace" && oldInput.length === 0 && skillsToCreate.size > 0) {
        const chipToRemove = $("#tagInputChips").children().last().find(".chipText").text()
        skillsToCreate.delete(chipToRemove)
        needsUpdate = true
    }

    if (event.key === " " || event.key === "Enter" || event.key === "Tab" ) {
        if (isValidSkillName) {
            needsUpdate = addUniqueSkill(inputValue)
        }

        skillsInput.removeClass("skillChipInvalid")
        skillsInput.val("")
        updateErrorMessage(evidenceSkillFeedback, "")
    }
    oldInput = inputValue
    if (needsUpdate) {
       updateSkillsInput()
    }


}


/**
 * Makes a tag/deletes the input for each input in the skill input.
 * Inputs are split by whitespace characters, and verified as valid skills.
 * If any skills are invalid, an error message displays. If there are 4 or less invalid skills, the skill names (trimmed
 * to 30 chars) are displayed. Otherwise, a message telling the user the number of removed inputs is displayed.
 */
function handleSkillInputPaste() {
    const inputValues = skillsInput.val().trim().split(/\s+/)
    const evidenceSkillFeedback = $("#evidenceSkillFeedback")
    const existingSkillFeedback = evidenceSkillFeedback.text()
    const invalidSkillNames = new Set()
    let errorMessage = ""

    inputValues.forEach(skillName => {
        if (validateSkillInput(skillName, false)) {
            addUniqueSkill(skillName)
        } else {
            invalidSkillNames.add(skillName.length > 30 ? skillName.substring(0, 27) + "..." : skillName)
        }
    })

    updateSkillsInput()
    skillsInput.val("")

    if (invalidSkillNames.size > 0) {
        if (invalidSkillNames.size < 5) {
            let skillNamesString = []
            invalidSkillNames.forEach((el) => {
                skillNamesString.push("\n" + el)
            })
            errorMessage = `${existingSkillFeedback} \nInvalid skill(s) not added: ${skillNamesString}`
        } else {
            errorMessage = `${existingSkillFeedback} \nDiscarded ${invalidSkillNames.size} invalid skills`
        }
    }

    updateErrorMessage(evidenceSkillFeedback, errorMessage)
}


/**
 * Removes the selected skill from the skills input and from the list of skills to be saved.
 *
 * @param event The click event on the skill delete button.
 */
function handleChipDelete(event) {
    event.stopPropagation()
    const skillName = $(this).siblings(".chipText").text()
    skillsToCreate.delete(skillName)

    updateSkillsInput(false)
}


// --------------------------------------------------- Autocomplete ----------------------------------------------------


/**
 * Autocomplete widget provided by jQueryUi
 * https://jqueryui.com/autocomplete/
 */
skillsInput
    // don't navigate away from the field on tab when selecting an item
    .on("keydown", function (event) {
        if (event.key === $.ui.keyCode.TAB && $(this).autocomplete("instance").menu.active) {
            event.preventDefault();
        }
    })
    .autocomplete({
        autoFocus: true, // This default selects the top result
        minLength: 1,
        source: function (request, response) {
            let filteredSkills = $.ui.autocomplete.filter(Array.from(skillsArray, skill => skill.name), extractLast(request.term))
            let existingSkills = [];
            skillsToCreate.forEach((value, key) => {
                existingSkills.push(key.toLowerCase())
            })
            filteredSkills = filteredSkills.filter(element => !existingSkills.includes(element.toLowerCase()))
            response(filteredSkills.sort());
        },
        focus: function () {
            // prevent value inserted on focus
            return false;
        },
        select: function (event, ui) {
            let terms = split(this.value);
            // remove the current input
            terms.pop();
            // add the selected item
            terms.push(ui.item.value);
            // add placeholder to get the space at the end
            terms.push("");
            this.value = terms.join(" ");
            return false;
        },
        appendTo: ".modalContent"
    })
    .data('ui-autocomplete')._renderItem = function (ul, item) {
    //This handles the display of the drop-down menu.
    return $("<li></li>")
        .data("ui-autocomplete-item", item)
        .append('<a>' + item.label + '</a>')
        .appendTo(ul);
};


/**
 * Autocomplete widget provided by jQueryUi
 * https://jqueryui.com/autocomplete/
 */
$("#linkUsersInput")
    .autocomplete({
        autoFocus: true, // This default selects the top result
        minLength: 1,
        delay: 700,
        appendTo: ".modalContent",
        source: function (request, response) {
            $.ajax({
                url: 'filteredUsers?name=' + request.term.toString(),
                type: "GET",
                contentType: "application/json",
                success: function (res) {
                    let users = [];
                    $.each(res, function (i) {
                        linkedUserIdsArray.push(userIdent)
                        if (!linkedUserIdsArray.includes(res[i].id)){
                            let user = {label: `${res[i].firstName} ${res[i].lastName}`, value: res[i]}
                            users.push(user)
                        }
                    })
                    response(users)
                }, error: function (error) {
                    createAlert(error.responseText, AlertTypes.Failure, ".modalBody")
                }
            })
        },
        focus: function () {
            // prevent value inserted on focus
            return false;
        },
        select: function (event, ui) {
            let terms = split(this.value);
            // remove the current input
            terms.pop();
            // add the selected item
            let user = ui.item.value
            addLinkedUser(user);
            $(this).val('')
            return false;
        }
    })
    .data('ui-autocomplete')._renderItem = function (ul, item) {
    //This handles the display of the drop-down menu.
    return $("<li></li>")
        .data("ui-autocomplete-item", item)
        .append('<a>' + item.label + '</a>')
        .appendTo(ul);
};


// --------------------------------------------------- Event listeners -------------------------------------------------

/**
 * Toggles category button appearance on the evidence creation form.
 */
$(".evidenceFormCategoryButton").on("click", function () {
    let button = $(this)
    if (button.hasClass("btn-secondary")) {
        button.removeClass("btn-secondary")
        button.addClass("btn-success")
        button.find(".evidenceCategoryTickIcon").show("slide", 200)
    } else {
        button.removeClass("btn-success")
        button.addClass("btn-secondary")
        button.find(".evidenceCategoryTickIcon").hide("slide", 200)
    }
})


/**
 * Listens out for a keydown event on the skills input.
 * If it is a delete button keydown then it removes the last word from the input box.
 * If it is a space, tab or enter then it checks for duplicates
 */
$(document).on("keydown", "#skillsInput", (event) => {
    setTimeout(() => handleSkillInputKeypress(event), 0)
})


/**
 * Runs the remove duplicates function after a paste event has occurred on the skills input
 */
$(document).on("paste", "#skillsInput", () => {
    setTimeout(() => handleSkillInputPaste(), 0)
    // Above is in a timeout so that it runs after the paste event has happened
})


/**
 * Listens for a click on the chip delete buttons, removes all the elements from the skill input that match the
 * skill we are deleting.
 */
$(document).on("click", ".chipDelete", handleChipDelete)