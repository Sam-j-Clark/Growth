let thisUserIsEditing = false;

$(() => {
    removeElementIfNotAuthorized()

    startCharacterCounting("form-control");
})


/**
 * Removes an element from the DOM by its unique ID.
 *
 * @param elementId the ID of the element to be removed.
 */
function removeElement(elementId) {
    let element = $("#" + elementId)
    element.slideUp(400, function () {
        element.remove()
    })
}


/**
 * Removes an element from the DOM by its class.
 *
 * @param elementClass the class of the element to be removed.
 */
function removeClass(elementClass) {
    let elements = $("." + elementClass);
    for (let element of elements) {
        element.remove();
    }
}


/**
 * Sorts the elements passed by the date.
 *
 * @param div the div to sort.
 * @param childrenElement the elements to sort in the div
 * @param dateElement the date to sort by
 */
function sortElementsByDate(div, childrenElement, dateElement) {
    let result = $(div).children(childrenElement).sort(function (a, b) {
        let contentA = Date.parse($(a).find(dateElement).text());
        let contentB = Date.parse($(b).find(dateElement).text());
        return (contentA < contentB) ? -1 : (contentA > contentB) ? 1 : 0;
    });
    $(div).html(result);
}


/**
 * Displays the alert for when dates are the wrong way around (end before start)
 */
function triggerEventAlertForDate() {
    createAlert("Your event end date must be after your event start date!", AlertTypes.Failure)
}


// <--------------------------- Listener Functions --------------------------->


/**
 * Listens for a change to an event date input picker.
 * When it detects a change it checks that the event end date isn't before the event start date.
 * If it is then it disables the event submit button, adds an invalid class to the event date pickers and shows the
 * feedback.
 */
$(document).on("change", ".eventDateInput", function () {
    let eventForm = $(this).parents("form")
    let eventStart = eventForm.find(".eventInputStartDate").val()
    let eventEnd = eventForm.find(".eventInputEndDate").val()
    let eventSubmitButton = eventForm.find("button[type=submit]")
    let eventDateInput = eventForm.find(".eventDateInput")
    let eventInvalidFeedback = eventForm.find(".invalid-feedback")
    if (eventEnd <= eventStart) {
        eventDateInput.addClass("invalid")
        eventInvalidFeedback.show()
        eventSubmitButton.attr("disabled", "disabled")
    } else {
        eventDateInput.removeClass("invalid")
        eventInvalidFeedback.hide()
        eventSubmitButton.removeAttr("disabled")
    }
})


/**
 * Event listener that runs whenever a new event is submitted.
 * Does a basic date check (can't end before it starts), then
 * makes a call to the server to add the event to the project.
 */
$(document).on('submit', "#addEventForm", function (event) {
    event.preventDefault()

    let eventData = {
        "projectId": projectId,
        "eventName": $("#eventName").val(),
        "eventStart": $("#eventStart").val(),
        "eventEnd": $("#eventEnd").val(),
        "typeOfEvent": $(".typeOfEvent").val()
    }

    if (eventData.eventEnd <= eventData.eventStart) {
        triggerEventAlertForDate()
    } else {
        $.ajax({
            url: "addEvent",
            type: "put",
            data: eventData,
            success: function (response) {

                $(".eventForm").slideUp();
                $(".addEventSvg").toggleClass('rotated');
                //The id here is the ID of the newly created event, given to use by the database (through the response)
                /*
                If your database system and H2 provide this differently (I.E. if whatever database you use
                 doesn't give the object an id attribute like H2 does)
                Then this will break on the VM, and you'll have no idea why.
                Now you do.
                 */
                sendNotification("event", response.id, "create");
                createAlert("Event created!", AlertTypes.Success)
            },
            error: function (error) {
                createAlert(error.responseText, AlertTypes.Failure)
            }
        })
    }
})


/**
 * Event listener that runs whenever a new milestone is submitted.
 * makes a call to the server to add the milestone to the project.
 */
$(document).on("submit", ".milestoneForm", function (event) {
    event.preventDefault()
    let milestoneData = {
        "milestoneName": $("#milestoneName").val(),
        "milestoneEnd": $("#milestoneEnd").val(),
        "typeOfOccasion": $(".typeOfMilestone").val(),
        "projectId": projectId
    }
    $.ajax({
        url: "addMilestone",
        type: "PUT",
        data: milestoneData,
        success: function (response) {
            createAlert("Milestone created!", AlertTypes.Success)
            $(".milestoneForm").slideUp()
            $(".addEventSvg").toggleClass('rotated');
            sendNotification("milestone", response.id, "create");
        },
        error: function (error) {
            createAlert(error.responseText, AlertTypes.Failure)
        }
    })
})


/**
 * Event listener that runs whenever a new deadline is submitted.
 * makes a call to the server to add the deadline to the project.
 */
$(document).on('submit', "#addDeadlineForm", function (event) {
    event.preventDefault()

    let deadlineData = {
        "projectId": projectId,
        "deadlineName": $("#deadlineName").val(),
        "deadlineEnd": $("#deadlineEnd").val(),
        "typeOfOccasion": $(".typeOfDeadline").val()
    }
    //Ajax call to PUT the deadline
    $.ajax({
        url: "addDeadline",
        type: "put",
        data: deadlineData,
        success: function (response) {
            createAlert("Deadline created!", AlertTypes.Success)
            $(".deadlineForm").slideUp();
            $(".addDeadlineSvg").toggleClass('rotated');
            sendNotification("deadline", response.id, "create");
        },
        error: function (error) {
            createAlert(error.responseText, AlertTypes.Failure)
        }
    })
})


/**
 * Event listener that runs whenever an existing event is edited and submitted.
 * Checks that the name is still there, and that the end is not before the start.
 * Then it makes a call to the server to edit that event
 * Also sends notifications to other clients to update that event
 * and unlock it so others can edit it again
 */
$(document).on("submit", "#editEventForm", function (event) {
    event.preventDefault()

    let eventId = $(this).parent().find(".eventId").text()
    let eventData = {
        "projectId": projectId,
        "eventId": eventId,
        "eventName": $(this).find(".eventName").val(),
        "eventStart": $(this).find(".eventStart").val(),
        "eventEnd": $(this).find(".eventEnd").val(),
        "typeOfEvent": $(this).find(".typeOfEvent").val()
    }

    if (eventData.eventName.toString().length === 0 || eventData.eventName.toString().trim().length === 0) {
        $(this).closest(".existingEventForm").append(`
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <strong>Oh no!</strong> You probably should enter an event name!
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>`)
    } else if (eventData.eventEnd <= eventData.eventStart) {
        triggerEventAlertForDate()
    } else {
        $.ajax({
            url: "editEvent",
            type: "POST",
            data: eventData,
            success: function () {
                createAlert("Event edited successfully!", AlertTypes.Success)
                sendNotification("event", eventId, "stop") // Let the server know the event is no longer being edited
                sendNotification("event", eventId, "update") //Let the server know that other clients should update the element
            },
            error: function (error) {
                createAlert(error.responseText, AlertTypes.Failure)
            }
        })
    }
})


/**
 * Event listener that runs whenever an existing milestone is edited and submitted.
 * It makes a call to the server to edit that milestone
 * Also sends notifications to other clients to update that milestone
 * and unlock it so others can edit it again
 */
$(document).on("submit", "#milestoneEditForm", function (event) {
    event.preventDefault();
    let milestoneId = $(this).parent().find(".milestoneId").text()
    let milestoneData = {
        "projectId": projectId,
        "milestoneId": milestoneId,
        "milestoneName": $(this).find(".milestoneName").val(),
        "milestoneDate": $(this).find(".milestoneEnd").val(),
        "typeOfMilestone": $(this).find(".typeOfMilestone").val()
    }
    $.ajax({
        url: "editMilestone",
        type: "POST",
        data: milestoneData,
        success: function () {
            createAlert("Milestone edited successfully!", AlertTypes.Success)
            sendNotification("milestone", milestoneId, "stop")
            sendNotification("milestone", milestoneId, "update")
        },
        error: function (error) {
            createAlert(error.responseText, AlertTypes.Failure)
        }
    })
})


/**
 * Event listener that runs whenever an existing deadline is edited and submitted.
 * Checks that the name isn't empty.
 * Then it makes a call to the server to edit that deadline
 * Also sends notifications to other clients to update that deadline
 * and unlock it so others can edit it again
 */
$(document).on("submit", "#editDeadlineForm", function (event) {
    event.preventDefault()
    let deadlineId = $(this).parent().find(".deadlineId").text()
    let deadlineDate = $(this).find(".deadlineEnd").val()
    let deadlineTime = deadlineDate.split("T")[1]
    let returnDate = deadlineDate.split("T")[0]

    let deadlineData = {
        "projectId": projectId,
        "deadlineId": deadlineId,
        "deadlineName": $(this).find(".deadlineName").val(),
        "deadlineDate": returnDate,
        "deadlineTime": deadlineTime,
        "typeOfOccasion": $(this).find(".typeOfDeadline").val()
    }
    //Check that the name isn't empty
    if (deadlineData.deadlineName.toString().length === 0 || deadlineData.deadlineName.toString().trim().length === 0) {
        $(this).closest(".existingDeadlineForm").append(`
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <strong>Oh no!</strong> You probably should enter a deadline name!
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>`)
    } else {
        $.ajax({
            url: "editDeadline",
            type: "POST",
            data: deadlineData,
            success: function () {
                createAlert("Deadline edited successfully!", AlertTypes.Success)
                sendNotification("deadline", deadlineId, "stop") // Let the server know the deadline is no longer being edited
                sendNotification("deadline", deadlineId, "update") //Let the server know that other clients should update the element
            },
            error: function (error) {
                createAlert(error.responseText, AlertTypes.Failure)
            }
        })
    }
})


/**
 * Listens for when add event button is clicked.
 * Shows the event form via a slide-down transition
 */
$(document).on('click', '.addEventButton', function () {
    $("#eventStart").val(defaultOccasionStart)
    $("#eventEnd").val(defaultOccasionEnd)
    $(".eventForm").slideToggle();
})


/**
 * Listens for when add milestone button is clicked.
 * Shows the milestone form via a slide-down transition
 */
$(document).on('click', '.addMilestoneButton', function () {
    $("#milestoneEnd").val(defaultMilestoneStart)
    $(".milestoneForm").slideToggle();
})


/**
 * Listens for when add milestone button is clicked.
 * Shows the milestone form via a slide-down transition
 */
$(document).on('click', '.addDeadlineButton', function () {
    $("#deadlineEnd").val(defaultOccasionEnd)
    $(".deadlineForm").slideToggle();
})


/**
 * Listens for a click on the delete button
 * Finds out what occasion type the button was connected to
 * then makes a call to the server to delete that occasion.
 */
$(document).on("click", ".deleteButton", function () {
    let parent = $(this).closest(".occasion")
    //Hide edit and delete button tooltips
    $(".editButton").tooltip('hide')
    $(".deleteButton").tooltip('hide')
    if (parent.hasClass('event')) { // Checks if the button belongs to an event
        let eventData = {"eventId": $(this).closest(".occasion").find(".eventId").text()}
        $.ajax({
            url: "deleteEvent",
            type: "DELETE",
            data: eventData,
            success: function () {
                createAlert("Event deleted successfully!", AlertTypes.Success)
                sendNotification("event", eventData.eventId, "delete");
            },
            error: function (error) {
                createAlert(error.responseText, AlertTypes.Failure)
            }
        })
    } else if (parent.hasClass('milestone')) {
        let milestoneData = {"milestoneId": $(this).closest(".occasion").attr("id")}
        $.ajax({
            url: 'deleteMilestone',
            type: "DELETE",
            data: milestoneData,
            success: function () {
                createAlert("Milestone deleted successfully!", AlertTypes.Success)
                sendNotification("milestone", milestoneData.milestoneId, "delete");
            },
            error: function (error) {
                createAlert(error.responseText, AlertTypes.Failure)
            }
        })
    } else if (parent.hasClass('deadline')) {
        let deadlineData = {"deadlineId": $(this).closest(".occasion").attr("id")}
        $.ajax({
            url: 'deleteDeadline',
            type: "DELETE",
            data: deadlineData,
            success: function () {
                createAlert("Deadline deleted successfully!", AlertTypes.Success)
                sendNotification("deadline", deadlineData.deadlineId, "delete");
            },
            error: function (error) {
                createAlert(error.responseText, AlertTypes.Failure)
            }
        })
    }
})


/**
 * Listens for a click on the edit button.
 * Adds the edit form to the occasion and sends a notification
 * to other clients telling them that we are editing this occasion
 * (so please lock it).
 */
$(document).on("click", ".editButton", function () {
    thisUserIsEditing = true;
    let editOccasionButton = $(".editButton")
    let deleteOccasionButton = $(".deleteButton")
    editOccasionButton.hide()
    deleteOccasionButton.hide()
    //Hide edit and delete button tooltips
    editOccasionButton.tooltip('hide')
    deleteOccasionButton.tooltip('hide')
    let parent = $(this).closest(".occasion")
    let id = parent.attr("id")
    if (parent.hasClass("event")) {
        sendNotification("event", id, "edit");
        appendEventForm(parent)
    } else if (parent.hasClass("milestone")) {
        sendNotification("milestone", id, "edit");
        appendMilestoneForm(parent)
    } else if (parent.hasClass("deadline")) {
        sendNotification("deadline", id, "edit");
        appendDeadlineForm(parent)
    }
})


/**
 * Listens for a click on the event form cancel button
 * Removes the edit form from the occasion and sends a notification
 * to other clients telling them we've stopped editing the occasion (so you can unlock it).
 */
$(document).on("click", ".cancelEdit", function () {
    thisUserIsEditing = false;
    $(".addOccasionButton").show()
    $(".editButton").show()
    $(".deleteButton").show()
    $(".eventDateInput").removeClass("is-invalid")

    let parent = $(this).closest(".occasion")
    let form = parent.find("form")
    let id = parent.attr("id")
    form.slideUp(400, function () {
        form.remove();
    })

    if (parent.hasClass("event")) {
        sendNotification("event", id, "stop");
    } else if (parent.hasClass("milestone")) {
        sendNotification("milestone", id, "stop");
    } else if (parent.hasClass("deadline")) {
        sendNotification("deadline", id, "stop");
    }
})


// <--------------------------- General Functions --------------------------->

/**
 * Adds Events to the sprints
 * Displays the events in the sprints in which the dates overlap.
 */
function addEventsToSprints() {
    $.ajax({
        url: 'getEventsList',
        type: 'get',
        data: {'projectId': projectId},

        success: function (response) {
            let sprint = $(".sprint")

            $(".eventInSprint").remove();

            for (let index in response) {
                let event = response[index]

                sprint.each(function (index, element) {

                    let eventStart = Date.parse(event.startDate)
                    let eventEnd = Date.parse(event.endDate)
                    let sprintStart = Date.parse($(element).find(".sprintStart").text())
                    let sprintEnd = Date.parse($(element).find(".sprintEnd").text())
                    if (eventStart >= sprintStart && eventStart <= sprintEnd) { // Event start right between sprint dates
                        appendEventToSprint(element, event)
                    } else if (eventEnd >= sprintStart && eventEnd <= sprintEnd) { //Event end falls within the sprint dates
                        appendEventToSprint(element, event)
                    } else if (eventStart < sprintStart && eventEnd > sprintEnd) {
                        appendEventToSprint(element, event) // Event is happening during a sprint
                    }
                })

                sprint.each(function (index, element) {

                    let eventStart = Date.parse(event.startDate)
                    let eventEnd = Date.parse(event.endDate)
                    let sprintStart = Date.parse($(element).find(".sprintStart").text())
                    let sprintEnd = Date.parse($(element).find(".sprintEnd").text())
                    let eventInSprint = $(".eventInSprint" + event.id);
                    let sprintName = $(element).find(".sprintName").text()

                    if (eventStart >= sprintStart && eventStart <= sprintEnd) {
                        eventInSprint.find(".sprintEventStart").css("color", $(element).find(".sprintColour").text())
                        eventInSprint.find(".sprintEventStart").attr("data-bs-toggle", "tooltip")
                        eventInSprint.find(".sprintEventStart").attr("data-bs-placement", "top")
                        eventInSprint.find(".sprintEventStart").attr("title", sprintName)
                        $('#' + event.id).find(".eventStart").css("color", $(element).find(".sprintColour").text())
                    }
                    if (sprintStart <= eventEnd && eventEnd <= sprintEnd) {
                        eventInSprint.find(".sprintEventEnd").css("color", $(element).find(".sprintColour").text())
                        eventInSprint.find(".sprintEventEnd").attr("data-bs-toggle", "tooltip")
                        eventInSprint.find(".sprintEventEnd").attr("data-bs-placement", "top")
                        eventInSprint.find(".sprintEventEnd").attr("title", sprintName)
                        $('#' + event.id).find(".eventEnd").css("color", $(element).find(".sprintColour").text())
                    }
                })
                enableToolTips()
            }
        }, error: function (error) {
            console.log(error)
        }
    })
}


/**
 * Adds event to sprint box
 *
 * @param elementToAppendTo The element that you're appending to
 * @param event the event object (matching the format provided by /getEventsList) that holds the data to append
 */
function appendEventToSprint(elementToAppendTo, event) {
    let eventInSprint = `
                <div class="row">
                    <div class="col">
                        <div class="eventInSprint eventInSprint${sanitise(event.id)}" >
                            <div class="occasionInSprintName">
                                <svg data-toggle="tooltip" class="iconInSprint bi bi-calendar3-event-fill calendarOccasion" id="eventIconInSprint"
                                        xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="-3 -3 20 20">
                                        <path fill-rule="evenodd" d="M2 0a2 2 0 0 0-2 2h16a2 2 0 0 0-2-2H2zM0 14V3h16v11a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2zm12-8a1 1 0 1 0 2 0 1 1 0 0 0-2 0z"/>
                                        </svg>
                                <p class="sprintEventName text-truncate">${sanitise(event.name)} : </p>
                            </div>
                            <div class="occasionInSprintName">
                                <p class="sprintEventStart">${sanitise(event.startDateFormatted)}</p>
                                <p>-</p>
                                <p class="sprintEventEnd">${sanitise(event.endDateFormatted)}</p>
                            </div>
                        </div>
                    </div>
                </div>`
    $(elementToAppendTo).append(eventInSprint)
}


/**
 * Adds Milestones to the sprints
 * Displays the milestones in the sprints in which the dates overlap.
 */
function addMilestonesToSprints() {
    $.ajax({
        url: 'getMilestonesList', type: 'get', data: {'projectId': projectId},

        success: function (response) {
            $(".milestoneInSprint").remove();

            for (let index in response) {
                let milestone = response[index]
                $(".sprint").each(function (index, element) {

                    let milestoneEnd = Date.parse(milestone.endDate)
                    let sprintStart = Date.parse($(element).find(".sprintStart").text())
                    let sprintEnd = Date.parse($(element).find(".sprintEnd").text())
                    if (milestoneEnd >= sprintStart && milestoneEnd <= sprintEnd) { //Milestone end falls within the sprint dates
                        appendMilestoneToSprint(element, milestone)

                        // Find milestone, and set colour to that of the sprint
                        $(".milestoneInSprint" + milestone.id).find(".sprintMilestoneEnd").css("color", $(element).find(".sprintColour").text())
                        $('#' + milestone.id).find(".milestoneEnd").css("color", $(element).find(".sprintColour").text())
                    }
                })
            }
        }, error: function (error) {
            console.log(error)
        }
    })
}


/**
 * Adds milestone to sprint box
 * @param elementToAppendTo The element that you're appending to
 * @param milestone the milestone object (matching the format provided by /getEventsList) that holds the data to append
 */
function appendMilestoneToSprint(elementToAppendTo, milestone) {
    let milestoneInSprint = `
                <div class="row" >
                    <div class="milestoneInSprint milestoneInSprint${sanitise(milestone.id)}">
                        <div class="occasionInSprintName">
                            <svg data-toggle="tooltip" class="iconInSprint bi bi-trophy-fill calendarOccasion" id="milestoneIconInSprint"
                                       xmlns="http://www.w3.org/2000/svg" min-width="20px" height="20" fill="currentColor" viewBox="-3 -3 20 20">
                                       <path d="M2.5.5A.5.5 0 0 1 3 0h10a.5.5 0 0 1 .5.5c0 .538-.012 1.05-.034 1.536a3 3 0 1 1-1.133 5.89c-.79 1.865-1.878 2.777-2.833 3.011v2.173l1.425.356c.194.048.377.135.537.255L13.3 15.1a.5.5 0 0 1-.3.9H3a.5.5 0 0 1-.3-.9l1.838-1.379c.16-.12.343-.207.537-.255L6.5 13.11v-2.173c-.955-.234-2.043-1.146-2.833-3.012a3 3 0 1 1-1.132-5.89A33.076 33.076 0 0 1 2.5.5zm.099 2.54a2 2 0 0 0 .72 3.935c-.333-1.05-.588-2.346-.72-3.935zm10.083 3.935a2 2 0 0 0 .72-3.935c-.133 1.59-.388 2.885-.72 3.935z"/>
                                       </svg>
                            <p class="sprintMilestoneName text-truncate">${sanitise(milestone.name)} :&#160</p>
                        </div>
                        <p class="sprintMilestoneEnd">${sanitise(milestone.endDateFormatted)}</p>
                    </div>
                </div>`
    $(elementToAppendTo).append(milestoneInSprint)
}


/**
 * Adds Deadlines to the sprints
 * Displays the deadlines in the sprints in which contain the deadline
 */
function addDeadlinesToSprints() {
    $.ajax({
        url: 'getDeadlinesList', type: 'get', data: {'projectId': projectId},

        success: function (response) {
            $(".deadlineInSprint").remove();

            for (let index in response) {
                let deadline = response[index]
                $(".sprint").each(function (index, element) {

                    let deadlineEnd = Date.parse(deadline.endDate)
                    let sprintStart = Date.parse($(element).find(".sprintStart").text())
                    let sprintEnd = Date.parse($(element).find(".sprintEnd").text())
                    if (deadlineEnd >= sprintStart && deadlineEnd <= sprintEnd) { //Deadline end falls within the sprint dates
                        appendDeadlineToSprint(element, deadline)

                        // Find deadline, and set colour to that of the sprint
                        $(".deadlineInSprint" + deadline.id).find(".sprintDeadlineEnd").css("color", $(element).find(".sprintColour").text())
                        $('#' + deadline.id).find(".deadlineEnd").css("color", $(element).find(".sprintColour").text())

                    }
                })
            }
        }, error: function (error) {
            console.log(error)
        }
    })
}


/**
 * Adds milestone to sprint box
 * @param elementToAppendTo The element that you're appending to
 * @param deadline the deadline object (matching the format provided by /getDeadlinesList) that holds the data to append
 */
function appendDeadlineToSprint(elementToAppendTo, deadline) {
    let deadlineInSprint = `
                <div class="row" >
                    <div class="deadlineInSprint deadlineInSprint${sanitise(deadline.id)}">
                        <div class="occasionInSprintName">
                            <svg data-toggle="tooltip" class="iconInSprint bi bi-hourglass-split calendarOccasion" id="deadlineIconInSprint"
                                    xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="-3 -3 20 20">
                                    <path d="M2.5 15a.5.5 0 1 1 0-1h1v-1a4.5 4.5 0 0 1 2.557-4.06c.29-.139.443-.377.443-.59v-.7c0-.213-.154-.451-.443-.59A4.5 4.5 0 0 1 3.5 3V2h-1a.5.5 0 0 1 0-1h11a.5.5 0 0 1 0 1h-1v1a4.5 4.5 0 0 1-2.557 4.06c-.29.139-.443.377-.443.59v.7c0 .213.154.451.443.59A4.5 4.5 0 0 1 12.5 13v1h1a.5.5 0 0 1 0 1h-11zm2-13v1c0 .537.12 1.045.337 1.5h6.326c.216-.455.337-.963.337-1.5V2h-7zm3 6.35c0 .701-.478 1.236-1.011 1.492A3.5 3.5 0 0 0 4.5 13s.866-1.299 3-1.48V8.35zm1 0v3.17c2.134.181 3 1.48 3 1.48a3.5 3.5 0 0 0-1.989-3.158C8.978 9.586 8.5 9.052 8.5 8.351z"/>
                                    </svg>
                            <p class="sprintDeadlineName text-truncate">${sanitise(deadline.name)} :</p>
                        </div>
                        <p class="sprintDeadlineEnd">${sanitise(deadline.endDateFormatted)}</p>
                    </div>
                </div>`
    $(elementToAppendTo).append(deadlineInSprint)
}


/**
 * Checks if the user has privilege and then removes all elements with the class
 * TeacherOrAbove if they don't.
 */
function removeElementIfNotAuthorized() {
    if (!checkPrivilege()) {
        $(".hasTeacherOrAbove").remove()
    }
}


/**
 * Checks if a user has a role above student.
 * @returns {boolean} returns true if userRole is above student.
 */
function checkPrivilege() {
    return userRoles.includes('COURSE_ADMINISTRATOR') || userRoles.includes('TEACHER');
}


/**
 * Appends form to the element that is passed to it.
 * Also gets data from that element.
 * @param element the element to append the form too.
 */
function appendEventForm(element) {
    let eventName = $(element).find(".eventName").text();
    let eventStart = $(element).find(".eventStartDateNilFormat").text().slice(0, 16);
    let eventEnd = $(element).find(".eventEndDateNilFormat").text().slice(0, 16);

    $(element).append(`
                <form class="existingEventForm" id="editEventForm" style="display: none">
                        <div class="mb-1">
                        <label for="eventName" class="form-label">Event name</label>
                        <input type="text" class="form-control form-control-sm eventName" pattern="${titleRegex}" title="${'Event title ' + titleRegexMessage}" value="${sanitise(eventName)}" maxlength="${eventNameLengthRestriction}" name="eventName" required>
                        <small class="form-text-counted text-muted countChar">0 characters remaining</small>
                    </div>
                    <div class="mb-3">
                        <label for="exampleFormControlInput1" class="form-label">Type of event</label>
                        <select class="form-select typeOfEvent" id="exampleFormControlInput1">
                            <option value="1">Event</option>
                            <option value="2">Test</option>
                            <option value="3">Meeting</option>
                            <option value="4">Workshop</option>
                            <option value="5">Special Event</option>
                            <option value="6">Attention Required</option>
                        </select>
                    </div>
                    <div class="row mb-1 eventDatePickerDiv">
                        <div class="col">
                            <label for="eventStart" class="form-label">Start</label>
                            <input type="datetime-local" class="form-control form-control-sm eventInputStartDate eventStart eventDateInput" value="${sanitise(eventStart)}" min="${projectStart}" max="${projectEnd}" name="eventStart" required>
                        </div>
                        <div class="col">
                            <label for="eventEnd" class="form-label">End</label>
                            <input type="datetime-local" class="form-control form-control-sm eventInputEndDate eventEnd eventDateInput" value="${sanitise(eventEnd)}" min="${projectStart}" max="${projectEnd}" name="eventEnd" required>
                        </div>
                    </div>
                    <div class="invalid-feedback">Start date must be before end date</div>
                    <div class="mb-1">
                        <button type="submit" class="btn btn-primary existingEventSubmit">Save</button>
                        <button type="button" class="btn btn-secondary cancelEdit" >Cancel</button>
                    </div>
                </form>`)

    let eventType = $(element).find(".typeOfEvent")
    $("#exampleFormControlInput1 > option").each(function () {
        if (this.value === eventType.text().split(" ")[0].trim()) {
            this.setAttribute("selected", "selected")
        }
    });

    startCharacterCounting("form-control");
    $("#editEventForm").slideDown();
}

/**
 * Appends form to the element that is passed to it.
 * Also gets data from that element.
 * @param element the element to append the form too.
 */
function appendMilestoneForm(element) {

    let milestoneName = $(element).find(".milestoneName").text();
    let milestoneEnd = $(element).find(".milestoneEndDateNilFormat").text().slice(0, 16);
    let projectStartDate = projectStart.split("T")[0]
    let projectEndDate = projectEnd.split("T")[0]

    $(element).append(`
                <form class="existingMilestoneForm" id="milestoneEditForm" style="display: none">
                        <div class="mb-1">
                        <label for="milestoneName" class="form-label">Milestone name</label>
                        <input type="text" class="form-control form-control-sm milestoneName" id="milestoneName" pattern="${titleRegex}" title="${'Milestone title ' + titleRegexMessage}" value="${sanitise(milestoneName)}" maxlength="${eventNameLengthRestriction}" name="milestoneName" required>
                        <small class="form-text-counted text-muted countChar">0 characters remaining</small>
                    </div>
                    <div class="mb-3" >
                        <label for="exampleFormControlInput2" class="form-label" >Type of milestone</label>
                        <select class="form-select typeOfMilestone" id="exampleFormControlInput2" >
                            <option value="1" selected disabled>Event</option>
                            <option value="2">Test</option>
                            <option value="3">Meeting</option>
                            <option value="4">Workshop</option>
                            <option value="5">Special Event</option>
                            <option value="6">Attention Required</option>
                        </select>
                    </div>
                    <div class="row mb-1">
                        <div class="col">
                            <label for="milestoneEnd" class="form-label">End</label>
                            <input type="date" class="form-control form-control-sm milestoneInputEndDate milestoneEnd" value="${sanitise(milestoneEnd)}" min="${projectStartDate}" max="${projectEndDate}" name="milestoneEnd" required>
                        </div>
                    </div>
                    <div class="mb-1">
                        <button type="submit" class="btn btn-primary existingMilestoneSubmit">Save</button>
                        <button type="button" class="btn btn-secondary cancelEdit" >Cancel</button>
                    </div>
                </form>`)
    let milestoneType = $(element).find(".typeOfMilestone")
    $("#exampleFormControlInput2 > option").each(function () {
        if (this.value === milestoneType.text().split(" ")[0].trim()) {
            this.setAttribute("selected", "selected")
        }
    });
    startCharacterCounting("form-control");
    $("#milestoneEditForm").slideDown();
}


/**
 * Appends form to the element that is passed to it.
 * Also gets data from that element.
 *
 * @param element the element to append the form too.
 */
function appendDeadlineForm(element) {
    let deadlineName = $(element).find(".deadlineName").text();
    let deadlineEnd = $(element).find(".deadlineEndDateNilFormat").text().slice(0, 16);

    $(element).append(`
                <form class="existingDeadlineForm" id="editDeadlineForm" style="display: none">
                        <div class="mb-1">
                        <label for="deadlineName" class="form-label">Deadline name</label>
                        <input type="text" class="form-control form-control-sm deadlineName" pattern="${titleRegex}" title="${'Deadline title ' + titleRegexMessage}" value="${sanitise(deadlineName)}" maxlength="${eventNameLengthRestriction}" name="deadlineName" required>
                        <small class="form-text-counted text-muted countChar">0 characters remaining</small>
                    </div>
                    <div class="mb-3">
                        <label for="exampleFormControlInput1" class="form-label">Type of deadline</label>
                        <select class="form-select typeOfDeadline" id="exampleFormControlInput3">
                            <option value="1">Event</option>
                            <option value="2">Test</option>
                            <option value="3">Meeting</option>
                            <option value="4">Workshop</option>
                            <option value="5">Special Event</option>
                            <option value="6">Attention Required</option>
                        </select>
                    </div>
                    <div class="row mb-1">
                        <div class="col">
                            <label for="deadlineEnd" class="form-label">End</label>
                            <input type="datetime-local" class="form-control form-control-sm deadlineInputEndDate deadlineEnd" value="${sanitise(deadlineEnd)}" min="${projectStart}" max="${projectEnd}" name="deadlineEnd" required>
                        </div>
                    </div>
                    <div class="mb-1">
                        <button type="submit" class="btn btn-primary existingDeadlineSubmit">Save</button>
                        <button type="button" class="btn btn-secondary cancelEdit" >Cancel</button>
                    </div>
                </form>`)
    let deadlineType = $(element).find(".typeOfDeadline")
    $("#exampleFormControlInput3 > option").each(function () {
        if (this.value === deadlineType.text().split(" ")[0].trim()) {
            this.setAttribute("selected", "selected")
        }
    });
    startCharacterCounting("formControl");
    $("#editDeadlineForm").slideDown();
}


/**
 * Creates the event div from the eventObject.
 *
 * @param eventObject A Json object with event details
 * @returns {string} A div containing the event details
 */
function createEventDiv(eventObject) {
    let iconElement;
    switch (eventObject.typeOfEvent) {
        case 1:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="right" title="Event" th:case="'1'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-calendar-event" viewBox="0 0 16 16"><path d="M11 6.5a.5.5 0 0 1 .5-.5h1a.5.5 0 0 1 .5.5v1a.5.5 0 0 1-.5.5h-1a.5.5 0 0 1-.5-.5v-1z"/><path d="M3.5 0a.5.5 0 0 1 .5.5V1h8V.5a.5.5 0 0 1 1 0V1h1a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V3a2 2 0 0 1 2-2h1V.5a.5.5 0 0 1 .5-.5zM1 4v10a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V4H1z"/></svg>`
            break;
        case 2:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="right" title="Test" th:case="'2'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-pencil-square" viewBox="0 0 16 16"><path d="M15.502 1.94a.5.5 0 0 1 0 .706L14.459 3.69l-2-2L13.502.646a.5.5 0 0 1 .707 0l1.293 1.293zm-1.75 2.456-2-2L4.939 9.21a.5.5 0 0 0-.121.196l-.805 2.414a.25.25 0 0 0 .316.316l2.414-.805a.5.5 0 0 0 .196-.12l6.813-6.814z"/><path fill-rule="evenodd" d="M1 13.5A1.5 1.5 0 0 0 2.5 15h11a1.5 1.5 0 0 0 1.5-1.5v-6a.5.5 0 0 0-1 0v6a.5.5 0 0 1-.5.5h-11a.5.5 0 0 1-.5-.5v-11a.5.5 0 0 1 .5-.5H9a.5.5 0 0 0 0-1H2.5A1.5 1.5 0 0 0 1 2.5v11z"/></svg>`
            break;
        case 3:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="right" title="Meeting" th:case="'3'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-cpu" viewBox="0 0 16 16"><path d="M5 0a.5.5 0 0 1 .5.5V2h1V.5a.5.5 0 0 1 1 0V2h1V.5a.5.5 0 0 1 1 0V2h1V.5a.5.5 0 0 1 1 0V2A2.5 2.5 0 0 1 14 4.5h1.5a.5.5 0 0 1 0 1H14v1h1.5a.5.5 0 0 1 0 1H14v1h1.5a.5.5 0 0 1 0 1H14v1h1.5a.5.5 0 0 1 0 1H14a2.5 2.5 0 0 1-2.5 2.5v1.5a.5.5 0 0 1-1 0V14h-1v1.5a.5.5 0 0 1-1 0V14h-1v1.5a.5.5 0 0 1-1 0V14h-1v1.5a.5.5 0 0 1-1 0V14A2.5 2.5 0 0 1 2 11.5H.5a.5.5 0 0 1 0-1H2v-1H.5a.5.5 0 0 1 0-1H2v-1H.5a.5.5 0 0 1 0-1H2v-1H.5a.5.5 0 0 1 0-1H2A2.5 2.5 0 0 1 4.5 2V.5A.5.5 0 0 1 5 0zm-.5 3A1.5 1.5 0 0 0 3 4.5v7A1.5 1.5 0 0 0 4.5 13h7a1.5 1.5 0 0 0 1.5-1.5v-7A1.5 1.5 0 0 0 11.5 3h-7zM5 6.5A1.5 1.5 0 0 1 6.5 5h3A1.5 1.5 0 0 1 11 6.5v3A1.5 1.5 0 0 1 9.5 11h-3A1.5 1.5 0 0 1 5 9.5v-3zM6.5 6a.5.5 0 0 0-.5.5v3a.5.5 0 0 0 .5.5h3a.5.5 0 0 0 .5-.5v-3a.5.5 0 0 0-.5-.5h-3z"/></svg>`
            break;
        case 4:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="right" title="Workshop" th:case="'4'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-bookmark" viewBox="0 0 16 16"><path d="M2 2a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v13.5a.5.5 0 0 1-.777.416L8 13.101l-5.223 2.815A.5.5 0 0 1 2 15.5V2zm2-1a1 1 0 0 0-1 1v12.566l4.723-2.482a.5.5 0 0 1 .554 0L13 14.566V2a1 1 0 0 0-1-1H4z"/></svg>`
            break;
        case 5:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="right" title="Special Event" th:case="'5'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-bell" viewBox="0 0 16 16"><path d="M8 16a2 2 0 0 0 2-2H6a2 2 0 0 0 2 2zM8 1.918l-.797.161A4.002 4.002 0 0 0 4 6c0 .628-.134 2.197-.459 3.742-.16.767-.376 1.566-.663 2.258h10.244c-.287-.692-.502-1.49-.663-2.258C12.134 8.197 12 6.628 12 6a4.002 4.002 0 0 0-3.203-3.92L8 1.917zM14.22 12c.223.447.481.801.78 1H1c.299-.199.557-.553.78-1C2.68 10.2 3 6.88 3 6c0-2.42 1.72-4.44 4.005-4.901a1 1 0 1 1 1.99 0A5.002 5.002 0 0 1 13 6c0 .88.32 4.2 1.22 6z"/></svg>`
            break;
        case 6:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="right" title="Important" th:case="'6'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-exclamation" viewBox="0 0 16 16"><path d="M7.002 11a1 1 0 1 1 2 0 1 1 0 0 1-2 0zM7.1 4.995a.905.905 0 1 1 1.8 0l-.35 3.507a.553.553 0 0 1-1.1 0L7.1 4.995z"/></svg>`
            break;

    }

    return `
            <div class="occasion event " id="${sanitise(eventObject.id)}">
                <p class="eventId" style="display: none">${sanitise(eventObject.id)}</p>
                <p class="eventStartDateNilFormat" style="display: none">${sanitise(eventObject.start)}</p>
                <p class="eventEndDateNilFormat" style="display: none">${sanitise(eventObject.end)}</p>
                <p class="typeOfEvent" style="display: none">${sanitise(eventObject.typeOfEvent)}</p>
                <div class="mb-2 occasionTitleDiv">
                    <div class="occasionIcon">
                        ${iconElement}
                    </div>
                    <p class="eventName name" >${sanitise(eventObject.name)}</p>
                </div>
                <div class="controlButtons">
                    <button class="editButton noStyleButton hasTeacherOrAbove"  data-bs-toggle="tooltip" data-bs-placement="top" title="Edit Event">
                        <svg class="bi bi-pencil" fill="currentColor" height="18" viewBox="0 0 16 16" width="18"
                                 xmlns="http://www.w3.org/2000/svg">
                                <path d="M12.146.146a.5.5 0 0 1 .708 0l3 3a.5.5 0 0 1 0 .708l-10 10a.5.5 0 0 1-.168.11l-5 2a.5.5 0 0 1-.65-.65l2-5a.5.5 0 0 1 .11-.168l10-10zM11.207 2.5 13.5 4.793 14.793 3.5 12.5 1.207 11.207 2.5zm1.586 3L10.5 3.207 4 9.707V10h.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.5h.293l6.5-6.5zm-9.761 5.175-.106.106-1.528 3.821 3.821-1.528.106-.106A.5.5 0 0 1 5 12.5V12h-.5a.5.5 0 0 1-.5-.5V11h-.5a.5.5 0 0 1-.468-.325z"/>
                        </svg>                    
                    </button>
                    <button type="button" class="deleteButton noStyleButton hasTeacherOrAbove"  data-bs-toggle="tooltip" data-bs-placement="top" title="Delete Event">
                         <svg class="bi bi-trash" fill="currentColor" height="20" viewBox="0 0 16 16" width="20"
                             xmlns="http://www.w3.org/2000/svg">
                            <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/>
                            <path d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z"
                                  fill-rule="evenodd"/>
                        </svg>
                    </button>
                </div>
                
                <div class="eventDateDiv occasionDates">
                    <p class="eventStart">${sanitise(eventObject.startFormatted)}</p>
                    <p class="dateDivider">-</p>
                    <p class="eventEnd">${sanitise(eventObject.endFormatted)}</p>
                </div>
            </div>`;
}


/**
 * Creates the Milestone divs from the milestoneObject
 *
 * @param milestoneObject A Json object with event details
 * @returns {string} A div
 */
function createMilestoneDiv(milestoneObject) {
    let iconElement;
    switch (milestoneObject.type) {
        case 1:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Event" th:case="'1'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-calendar-event" viewBox="0 0 16 16"><path d="M11 6.5a.5.5 0 0 1 .5-.5h1a.5.5 0 0 1 .5.5v1a.5.5 0 0 1-.5.5h-1a.5.5 0 0 1-.5-.5v-1z"/><path d="M3.5 0a.5.5 0 0 1 .5.5V1h8V.5a.5.5 0 0 1 1 0V1h1a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V3a2 2 0 0 1 2-2h1V.5a.5.5 0 0 1 .5-.5zM1 4v10a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V4H1z"/></svg>`
            break;
        case 2:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Test" th:case="'2'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-pencil-square" viewBox="0 0 16 16"><path d="M15.502 1.94a.5.5 0 0 1 0 .706L14.459 3.69l-2-2L13.502.646a.5.5 0 0 1 .707 0l1.293 1.293zm-1.75 2.456-2-2L4.939 9.21a.5.5 0 0 0-.121.196l-.805 2.414a.25.25 0 0 0 .316.316l2.414-.805a.5.5 0 0 0 .196-.12l6.813-6.814z"/><path fill-rule="evenodd" d="M1 13.5A1.5 1.5 0 0 0 2.5 15h11a1.5 1.5 0 0 0 1.5-1.5v-6a.5.5 0 0 0-1 0v6a.5.5 0 0 1-.5.5h-11a.5.5 0 0 1-.5-.5v-11a.5.5 0 0 1 .5-.5H9a.5.5 0 0 0 0-1H2.5A1.5 1.5 0 0 0 1 2.5v11z"/></svg>`
            break;
        case 3:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Meeting" th:case="'3'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-cpu" viewBox="0 0 16 16"><path d="M5 0a.5.5 0 0 1 .5.5V2h1V.5a.5.5 0 0 1 1 0V2h1V.5a.5.5 0 0 1 1 0V2h1V.5a.5.5 0 0 1 1 0V2A2.5 2.5 0 0 1 14 4.5h1.5a.5.5 0 0 1 0 1H14v1h1.5a.5.5 0 0 1 0 1H14v1h1.5a.5.5 0 0 1 0 1H14v1h1.5a.5.5 0 0 1 0 1H14a2.5 2.5 0 0 1-2.5 2.5v1.5a.5.5 0 0 1-1 0V14h-1v1.5a.5.5 0 0 1-1 0V14h-1v1.5a.5.5 0 0 1-1 0V14h-1v1.5a.5.5 0 0 1-1 0V14A2.5 2.5 0 0 1 2 11.5H.5a.5.5 0 0 1 0-1H2v-1H.5a.5.5 0 0 1 0-1H2v-1H.5a.5.5 0 0 1 0-1H2v-1H.5a.5.5 0 0 1 0-1H2A2.5 2.5 0 0 1 4.5 2V.5A.5.5 0 0 1 5 0zm-.5 3A1.5 1.5 0 0 0 3 4.5v7A1.5 1.5 0 0 0 4.5 13h7a1.5 1.5 0 0 0 1.5-1.5v-7A1.5 1.5 0 0 0 11.5 3h-7zM5 6.5A1.5 1.5 0 0 1 6.5 5h3A1.5 1.5 0 0 1 11 6.5v3A1.5 1.5 0 0 1 9.5 11h-3A1.5 1.5 0 0 1 5 9.5v-3zM6.5 6a.5.5 0 0 0-.5.5v3a.5.5 0 0 0 .5.5h3a.5.5 0 0 0 .5-.5v-3a.5.5 0 0 0-.5-.5h-3z"/></svg>`
            break;
        case 4:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Workshop" th:case="'4'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-bookmark" viewBox="0 0 16 16"><path d="M2 2a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v13.5a.5.5 0 0 1-.777.416L8 13.101l-5.223 2.815A.5.5 0 0 1 2 15.5V2zm2-1a1 1 0 0 0-1 1v12.566l4.723-2.482a.5.5 0 0 1 .554 0L13 14.566V2a1 1 0 0 0-1-1H4z"/></svg>`
            break;
        case 5:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Special Event" th:case="'5'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-bell" viewBox="0 0 16 16"><path d="M8 16a2 2 0 0 0 2-2H6a2 2 0 0 0 2 2zM8 1.918l-.797.161A4.002 4.002 0 0 0 4 6c0 .628-.134 2.197-.459 3.742-.16.767-.376 1.566-.663 2.258h10.244c-.287-.692-.502-1.49-.663-2.258C12.134 8.197 12 6.628 12 6a4.002 4.002 0 0 0-3.203-3.92L8 1.917zM14.22 12c.223.447.481.801.78 1H1c.299-.199.557-.553.78-1C2.68 10.2 3 6.88 3 6c0-2.42 1.72-4.44 4.005-4.901a1 1 0 1 1 1.99 0A5.002 5.002 0 0 1 13 6c0 .88.32 4.2 1.22 6z"/></svg>`
            break;
        case 6:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Important" th:case="'6'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-exclamation" viewBox="0 0 16 16"><path d="M7.002 11a1 1 0 1 1 2 0 1 1 0 0 1-2 0zM7.1 4.995a.905.905 0 1 1 1.8 0l-.35 3.507a.553.553 0 0 1-1.1 0L7.1 4.995z"/></svg>`
            break;

    }

    return `
            <div class="occasion milestone" id="${sanitise(milestoneObject.id)}">
                <p class="milestoneId" style="display: none">${sanitise(milestoneObject.id)}</p>
                <p class="milestoneEndDateNilFormat" style="display: none">${sanitise(milestoneObject.endDate)}</p>
                <p class="typeOfMilestone" style="display: none">${sanitise(milestoneObject.type)}</p>
                <div class="mb-2 occasionTitleDiv">
                    <div class="occasionIcon">
                        ${iconElement}
                    </div>
                    <p class="milestoneName name">${sanitise(milestoneObject.name)}</p>
                </div>
                <div class="controlButtons">
                    <button class="editButton noStyleButton hasTeacherOrAbove" data-bs-toggle="tooltip" data-bs-placement="top" title="Edit Milestone">
                            <svg class="bi bi-pencil" fill="currentColor" height="18" viewBox="0 0 16 16" width="18"
                                 xmlns="http://www.w3.org/2000/svg">
                                <path d="M12.146.146a.5.5 0 0 1 .708 0l3 3a.5.5 0 0 1 0 .708l-10 10a.5.5 0 0 1-.168.11l-5 2a.5.5 0 0 1-.65-.65l2-5a.5.5 0 0 1 .11-.168l10-10zM11.207 2.5 13.5 4.793 14.793 3.5 12.5 1.207 11.207 2.5zm1.586 3L10.5 3.207 4 9.707V10h.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.5h.293l6.5-6.5zm-9.761 5.175-.106.106-1.528 3.821 3.821-1.528.106-.106A.5.5 0 0 1 5 12.5V12h-.5a.5.5 0 0 1-.5-.5V11h-.5a.5.5 0 0 1-.468-.325z"/>
                            </svg>
                        </button>
                        <button type="button" class="deleteButton noStyleButton hasTeacherOrAbove"  data-bs-toggle="tooltip" data-bs-placement="top" title="Delete Milestone">
                            <svg class="bi bi-trash" fill="currentColor" height="20" viewBox="0 0 16 16" width="20"
                                 xmlns="http://www.w3.org/2000/svg">
                                <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/>
                                <path d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z"
                                      fill-rule="evenodd"/>
                            </svg>
                        </button>
                </div>
                
                <div class="milestoneDateDiv occasionDates">
                    <p class="milestoneEnd">${sanitise(milestoneObject.endDateFormatted)}</p>
                </div>
            </div>
            `;
}


/**
 * Creates the Deadline div from the deadlineObject.
 *
 * @param deadlineObject A Json object with deadline details
 * @returns {string} A div containing the deadline details
 */
function createDeadlineDiv(deadlineObject) {

    let iconElement;
    switch (deadlineObject.type) {
        case 1:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Event" th:case="'1'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-calendar-event" viewBox="0 0 16 16"><path d="M11 6.5a.5.5 0 0 1 .5-.5h1a.5.5 0 0 1 .5.5v1a.5.5 0 0 1-.5.5h-1a.5.5 0 0 1-.5-.5v-1z"/><path d="M3.5 0a.5.5 0 0 1 .5.5V1h8V.5a.5.5 0 0 1 1 0V1h1a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V3a2 2 0 0 1 2-2h1V.5a.5.5 0 0 1 .5-.5zM1 4v10a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V4H1z"/></svg>`
            break;
        case 2:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Test" th:case="'2'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-pencil-square" viewBox="0 0 16 16"><path d="M15.502 1.94a.5.5 0 0 1 0 .706L14.459 3.69l-2-2L13.502.646a.5.5 0 0 1 .707 0l1.293 1.293zm-1.75 2.456-2-2L4.939 9.21a.5.5 0 0 0-.121.196l-.805 2.414a.25.25 0 0 0 .316.316l2.414-.805a.5.5 0 0 0 .196-.12l6.813-6.814z"/><path fill-rule="evenodd" d="M1 13.5A1.5 1.5 0 0 0 2.5 15h11a1.5 1.5 0 0 0 1.5-1.5v-6a.5.5 0 0 0-1 0v6a.5.5 0 0 1-.5.5h-11a.5.5 0 0 1-.5-.5v-11a.5.5 0 0 1 .5-.5H9a.5.5 0 0 0 0-1H2.5A1.5 1.5 0 0 0 1 2.5v11z"/></svg>`
            break;
        case 3:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Meeting" th:case="'3'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-cpu" viewBox="0 0 16 16"><path d="M5 0a.5.5 0 0 1 .5.5V2h1V.5a.5.5 0 0 1 1 0V2h1V.5a.5.5 0 0 1 1 0V2h1V.5a.5.5 0 0 1 1 0V2A2.5 2.5 0 0 1 14 4.5h1.5a.5.5 0 0 1 0 1H14v1h1.5a.5.5 0 0 1 0 1H14v1h1.5a.5.5 0 0 1 0 1H14v1h1.5a.5.5 0 0 1 0 1H14a2.5 2.5 0 0 1-2.5 2.5v1.5a.5.5 0 0 1-1 0V14h-1v1.5a.5.5 0 0 1-1 0V14h-1v1.5a.5.5 0 0 1-1 0V14h-1v1.5a.5.5 0 0 1-1 0V14A2.5 2.5 0 0 1 2 11.5H.5a.5.5 0 0 1 0-1H2v-1H.5a.5.5 0 0 1 0-1H2v-1H.5a.5.5 0 0 1 0-1H2v-1H.5a.5.5 0 0 1 0-1H2A2.5 2.5 0 0 1 4.5 2V.5A.5.5 0 0 1 5 0zm-.5 3A1.5 1.5 0 0 0 3 4.5v7A1.5 1.5 0 0 0 4.5 13h7a1.5 1.5 0 0 0 1.5-1.5v-7A1.5 1.5 0 0 0 11.5 3h-7zM5 6.5A1.5 1.5 0 0 1 6.5 5h3A1.5 1.5 0 0 1 11 6.5v3A1.5 1.5 0 0 1 9.5 11h-3A1.5 1.5 0 0 1 5 9.5v-3zM6.5 6a.5.5 0 0 0-.5.5v3a.5.5 0 0 0 .5.5h3a.5.5 0 0 0 .5-.5v-3a.5.5 0 0 0-.5-.5h-3z"/></svg>`
            break;
        case 4:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Workshop" th:case="'4'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-bookmark" viewBox="0 0 16 16"><path d="M2 2a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v13.5a.5.5 0 0 1-.777.416L8 13.101l-5.223 2.815A.5.5 0 0 1 2 15.5V2zm2-1a1 1 0 0 0-1 1v12.566l4.723-2.482a.5.5 0 0 1 .554 0L13 14.566V2a1 1 0 0 0-1-1H4z"/></svg>`
            break;
        case 5:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Special Event" th:case="'5'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-bell" viewBox="0 0 16 16"><path d="M8 16a2 2 0 0 0 2-2H6a2 2 0 0 0 2 2zM8 1.918l-.797.161A4.002 4.002 0 0 0 4 6c0 .628-.134 2.197-.459 3.742-.16.767-.376 1.566-.663 2.258h10.244c-.287-.692-.502-1.49-.663-2.258C12.134 8.197 12 6.628 12 6a4.002 4.002 0 0 0-3.203-3.92L8 1.917zM14.22 12c.223.447.481.801.78 1H1c.299-.199.557-.553.78-1C2.68 10.2 3 6.88 3 6c0-2.42 1.72-4.44 4.005-4.901a1 1 0 1 1 1.99 0A5.002 5.002 0 0 1 13 6c0 .88.32 4.2 1.22 6z"/></svg>`
            break;
        case 6:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Important" th:case="'6'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-exclamation" viewBox="0 0 16 16"><path d="M7.002 11a1 1 0 1 1 2 0 1 1 0 0 1-2 0zM7.1 4.995a.905.905 0 1 1 1.8 0l-.35 3.507a.553.553 0 0 1-1.1 0L7.1 4.995z"/></svg>`
            break;
    }

    return `
            <div class="occasion deadline" id="${sanitise(deadlineObject.id)}">
                <p class="deadlineId" style="display: none">${sanitise(deadlineObject.id)}</p>
                <p class="deadlineEndDateNilFormat" style="display: none">${sanitise(deadlineObject.dateTime)}</p>
                <p class="typeOfDeadline" style="display: none">${sanitise(deadlineObject.type)}</p>
                <div class="mb-2 occasionTitleDiv">
                    <div class="occasionIcon">
                        ${iconElement}
                    </div>
                    <p class="deadlineName name">${sanitise(deadlineObject.name)}</p>
                </div>
                <div class="controlButtons">
                        <button class="editButton noStyleButton hasTeacherOrAbove" data-bs-toggle="tooltip" data-bs-placement="top" title="Edit Deadline">
                             <svg class="bi bi-pencil" fill="currentColor" height="18" viewBox="0 0 16 16" width="18"
                                 xmlns="http://www.w3.org/2000/svg">
                                <path d="M12.146.146a.5.5 0 0 1 .708 0l3 3a.5.5 0 0 1 0 .708l-10 10a.5.5 0 0 1-.168.11l-5 2a.5.5 0 0 1-.65-.65l2-5a.5.5 0 0 1 .11-.168l10-10zM11.207 2.5 13.5 4.793 14.793 3.5 12.5 1.207 11.207 2.5zm1.586 3L10.5 3.207 4 9.707V10h.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.5h.293l6.5-6.5zm-9.761 5.175-.106.106-1.528 3.821 3.821-1.528.106-.106A.5.5 0 0 1 5 12.5V12h-.5a.5.5 0 0 1-.5-.5V11h-.5a.5.5 0 0 1-.468-.325z"/>
                            </svg>
                        </button>
                        <button type="button" class="deleteButton noStyleButton hasTeacherOrAbove"  data-bs-toggle="tooltip" data-bs-placement="top" title="Delete Deadline">
                             <svg class="bi bi-trash" fill="currentColor" height="20" viewBox="0 0 16 16" width="20"
                                 xmlns="http://www.w3.org/2000/svg">
                                <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/>
                                <path d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z"
                                      fill-rule="evenodd"/>
                            </svg>
                        </button>
                </div>
                        <div class="deadlineDateDiv occasionDates">
                            <p class="deadlineEnd">${sanitise(deadlineObject.endDateFormatted)}</p>
                        </div>
            </div>`;
}


/**
 * Refreshes the event div section of the page
 */
function refreshEvents() {
    $("#eventContainer").find(".occasion").remove() // Finds all event divs are removes them
    $.ajax({
        url: 'getEventsList',
        type: 'get',
        data: {'projectId': projectId},

        success: function (response) {

            for (let event in response) { // Goes through all the data from the server and creates an eventObject
                let eventObject = {
                    "id": response[event].id,
                    "name": response[event].name,
                    "start": response[event].startDate,
                    "end": response[event].dateTime,
                    "startFormatted": response[event].startDateFormatted,
                    "endFormatted": response[event].endDateFormatted,
                    "typeOfEvent": response[event].type,
                }

                $("#eventContainer").append(createEventDiv(eventObject)) // Passes the eventObject to the createDiv function
                removeElementIfNotAuthorized()

            }
            addEventsToSprints()

        }, error: function (error) {
            console.log(error)
        }
    })
}


/**
 * Refreshes the milestone div section of the page
 *
 * @param projectId The Id of the project to be refreshed
 */
function refreshMilestones(projectId) {
    let milestoneContainer = $("#milestoneContainer")
    milestoneContainer.find(".occasion").remove()
    milestoneContainer.append(`<div id="infoMilestoneContainer" class="infoMessageParent alert alert-primary alert-dismissible fade show" role="alert" style="display: none">
            </div>`) // Adds an info box to the page
    $.ajax({
        url: 'getMilestonesList',
        type: 'get',
        data: {'projectId': projectId},

        success: function (response) {
            for (let milestone in response) { // Goes through all the data from the server and creates an eventObject
                let milestoneObject = {
                    "id": response[milestone].id,
                    "name": response[milestone].name,
                    "endDate": response[milestone].endDate,
                    "endDateFormatted": response[milestone].endDateFormatted,
                    "type": response[milestone].type,
                }

                $("#milestoneContainer").append(createMilestoneDiv(milestoneObject)) // Passes the milestoneObject  to the createDiv function
                sortElementsByDate("#milestoneContainer", ".occasion", ".milestoneEndDateNilFormat")
                removeElementIfNotAuthorized()
            }
            addMilestonesToSprints()

        }, error: function (error) {
            console.log(error)
        }
    })
}


/**
 * Refreshes the deadline div section of the page
 *
 * @param projectId The Id of the project to be refreshed
 */
function refreshDeadlines(projectId) {
    let deadlineContainer = $("#deadlineContainer")
    deadlineContainer.find(".occasion").remove() // Finds all deadline divs are removes them
    deadlineContainer.append(`<div id="infoDeadlineContainer" class="infoMessageParent alert alert-primary alert-dismissible fade show" role="alert" style="display: none">
            </div>`) // Adds an info box to the page
    $.ajax({
        url: 'getDeadlinesList',
        type: 'GET',
        data: {'projectId': projectId},

        success: function (response) {

            for (let deadline in response) { // Goes through all the data from the server and creates an eventObject
                let deadlineObject = response[deadline];
                $("#deadlineContainer").append(createDeadlineDiv(deadlineObject)) // Passes the deadlineObject to the createDiv function

            }
            sortElementsByDate("#deadlineContainer", ".occasion", ".deadlineEndDateNilFormat")
            removeElementIfNotAuthorized()
            addDeadlinesToSprints()
        },
        error: function (error) {
            console.log(error)
        }
    })
}


/**
 * Reloads a single element on the page dependent on its classname
 *
 * @param id the id of the element to reload
 */
function reloadElement(id) {
    let elementToReload = $("#" + id)
    elementToReload.slideUp() // Hides the element
    if (elementToReload.hasClass("event")) {
        $.ajax({
            url: 'getEvent',
            type: 'get',
            data: {'eventId': id},
            success: function (response) {

                let eventObject = {
                    "id": response.id,
                    "name": response.name,
                    "start": response.startDate,
                    "end": response.dateTime,
                    "startFormatted": response.startDateFormatted,
                    "endFormatted": response.endDateFormatted,
                    "typeOfEvent": response.type,
                }

                elementToReload.replaceWith(createEventDiv(eventObject)) // Passes the eventObject to the createDiv function
                elementToReload.slideDown()
                addEventsToSprints()
                sortElementsByDate("#eventContainer", ".occasion", ".eventStartDateNilFormat")
                removeElementIfNotAuthorized()

            }, error: function () {
                location.href = "error" // Moves the user to the error page
            }
        })
    } else if (elementToReload.hasClass("milestone")) {
        $.ajax({
            url: 'getMilestone',
            type: 'get',
            data: {'milestoneId': id},
            success: function (response) {

                elementToReload.replaceWith(createMilestoneDiv(response))
                elementToReload.slideDown()
                addMilestonesToSprints()
                sortElementsByDate("#milestoneContainer", ".occasion", ".milestoneEndDateNilFormat")
                removeElementIfNotAuthorized()

            }, error: function () {
                location.href = "error" // Moves the user to the error page
            }
        })
    } else if (elementToReload.hasClass("deadline")) {
        $.ajax({
            url: 'getDeadline',
            type: 'get',
            data: {'deadlineId': id},
            success: function (response) {

                elementToReload.replaceWith(createDeadlineDiv(response))
                elementToReload.slideDown()
                addDeadlinesToSprints();
                sortElementsByDate("#deadlineContainer", ".occasion", ".deadlineEndDateNilFormat")
                removeElementIfNotAuthorized()
            },
            error: function () {
                location.href = "/error" // Moves the user to the error page
            }
        })
    }
    $(".editButton").show()
    $(".deleteButton").show()
}


/**
 * Gets the details of the event and adds it to the page.
 *
 * @param eventId the event to add.
 */
function addEvent(eventId) {
    $.ajax({
        url: 'getEvent',
        type: 'get',
        data: {'eventId': eventId},
        success: function (response) {

            let eventObject = {
                "id": response.id,
                "name": response.name,
                "start": response.startDate,
                "end": response.dateTime,
                "startFormatted": response.startDateFormatted,
                "endFormatted": response.endDateFormatted,
                "typeOfEvent": response.type,
            }

            $("#eventContainer").append(createEventDiv(eventObject)) // Passes the eventObject to the createDiv function
            sortElementsByDate("#eventContainer", ".occasion", ".eventStartDateNilFormat")
            addEventsToSprints()
            removeElementIfNotAuthorized()

        }, error: function () {
            location.href = "error" // Moves the user to the error page
        }
    })
}


/**
 * Gets a single milestone then adds it to the page
 *
 * @param milestoneId the id of the milestone
 */
function addMilestone(milestoneId) {
    $.ajax({
        url: "getMilestone", type: "GET", data: {"milestoneId": milestoneId}, success: function (response) {

            let milestoneObject = {
                "id": response.id,
                "name": response.name,
                "endDate": response.endDate,
                "endDateFormatted": response.endDateFormatted,
                "type": response.type,
            }

            $("#milestoneContainer").append(createMilestoneDiv(milestoneObject)) // Passes the eventObject to the createDiv function
            addMilestonesToSprints()
            removeElementIfNotAuthorized()

            nextMilestoneNumber++;
            document.getElementById("milestoneName").setAttribute("value", "Milestone " + nextMilestoneNumber)
            document.getElementById("milestoneName").setAttribute("placeholder", "Milestone " + nextMilestoneNumber)

            sortElementsByDate("#milestoneContainer", ".occasion", ".milestoneEndDateNilFormat")

        }, error: function () {
            location.href = "error" // Moves the user to the error page
        }
    })
}


/**
 * Gets the details of the deadline and adds it to the page.
 *
 * @param deadlineId the event to add.
 */
function addDeadline(deadlineId) {
    $.ajax({
        url: 'getDeadline',
        type: 'get',
        data: {'deadlineId': deadlineId},
        success: function (response) {


            $("#deadlineContainer").append(createDeadlineDiv(response)) // Passes the eventObject to the createDiv function
            sortElementsByDate("#deadlineContainer", ".occasion", ".deadlineEndDateNilFormat")
            addDeadlinesToSprints()
            removeElementIfNotAuthorized()

        },
        error: function () {
            location.href = "/error" // Moves the user to the error page
        }
    })
}


/**
 * Checks if element is empty
 *
 * @param el the element to check
 * @returns {boolean} true if empty, false if not
 */
function isEmpty(el) {
    return !$.trim(el.html())
}


/**
 * Initialises tooltips, so they display when their element is hovered-over
 */
function enableToolTips() {
    let tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
    let tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl)
    })
}


/**
 * A helper function to display a live edit notification. This checks if a user has edit privileges and if they are
 * not the user that made the change.
 */
function displayLiveUpdateMessage(message, editorId, eventId) {
    if (checkPrivilege() && editorId != userIdent) {
        createLiveAlert(message, eventId);
        setTimeout(removeLiveAlert, 10000, eventId)
    }
}


//  ------------------------------- Handle the incoming websocket notifications ---------------------------------------


/**
 * Processes a create notification by adding boxes for that notification to the DOM
 *
 * @param notification The JSON object we receive (modeled by OutgoingNotification).
 */
function handleCreateEvent(notification) {
    const editorId = notification.editorId;
    const editorName = notification.editorName;
    const occasionType = notification.data;
    const occasionId = notification.id;

    switch (occasionType) {
        case 'event':
            addEvent(occasionId)
            break
        case 'milestone':
            addMilestone(occasionId)
            break
        case 'deadline':
            addDeadline(occasionId)
            break
        case "sprint":
            $(".sprintsContainer").empty()
            getSprints(() => {
                refreshDeadlines(projectId);
                refreshMilestones(projectId);
                refreshEvents(projectId);
            })
            break
        default:
            break
    }

    displayLiveUpdateMessage(editorName + " has created a new " + occasionType, editorId, occasionId);
}


/**
 * Helper function for handling an update event. Tells us to reload the element with the specific ID.
 * Occasion types are handled in the reloading method, so we only need to provide it the ID
 *
 * @param notification The update notification, from which we extract the ID (and also the type for logging)
 */
function handleUpdateEvent(notification) {
    const occasionType = notification.data;
    const occasionId = notification.id;
    const editorId = notification.editorId;
    const editorName = notification.editorName;
    let eventDiv = $("#" + occasionId)
    let eventName = eventDiv.find(".name").text();

    switch (occasionType) {
        case 'event' :
            reloadElement(occasionId)
            break
        case 'milestone' :
            reloadElement(occasionId)
            break
        case 'deadline' :
            reloadElement(occasionId)
            break
        case "sprint" :
            $(".sprintsContainer").empty()
            getSprints(() => {
                refreshDeadlines(projectId);
                refreshMilestones(projectId);
                refreshEvents(projectId);
            })
            break
        default :
            // Add debug log here
            break
    }
    displayLiveUpdateMessage(editorName + " updated " + occasionType + ": " + eventName, editorId, occasionId);
}


/**
 * Processes a delete notification by removing the element from the DOM
 *
 * @param notification The JSON object we receive (modeled by OutgoingNotification).
 */
function handleDeleteEvent(notification) {
    const occasionType = notification.data;
    const occasionId = notification.id;
    const editorId = notification.editorId;
    const editorName = notification.editorName;
    let eventDiv = $("#" + occasionId)
    let eventName = eventDiv.find(".name").text();

    removeElement(occasionId) // removes specific event

    removeClass()
    //Now reload the elements, depending on what type of element was removed
    switch (occasionType) {
        case "event":
            removeClass(`eventInSprint${occasionId}`);
            break;
        case "milestone":
            removeClass(`milestoneInSprint${occasionId}`);
            break;
        case "deadline":
            removeClass(`deadlineInSprint${occasionId}`);
            break;
        case "sprint" :
            $(".sprintsContainer").empty()
            getSprints(() => {
                refreshDeadlines(projectId);
                refreshMilestones(projectId);
                refreshEvents(projectId);
            })
            break
    }
    displayLiveUpdateMessage(editorName + " deleted " + occasionType + ": " + eventName, editorId, occasionId);
}


/**
 * Opens a dialog box at the top of the screen, and disables the edit buttons for the
 * occasion that is being edited.
 *
 * @param notification The JSON object we receive (modeled by OutgoingNotification).
 */
function handleNotifyEvent(notification) {

    const editorId = notification.editorId;
    const editorName = notification.editorName;
    const occasionId = notification.id;

    if (checkPrivilege()) {
        let infoContainer = $("#informationBar");
        let eventDiv = $("#" + occasionId)
        let noticeSelector = $("#notice" + occasionId)
        if (eventDiv.length < 1) {
            // Solves the race condition that was happening when a user refreshed their page.
            // The element that lets the user know if another user is editing the page was working too fast.
            setTimeout(() => handleNotifyEvent(notification), 200)
            return
        }
        let eventName = eventDiv.find(".name").text();
        if (!noticeSelector.length) {
            let infoString = editorName + " is editing element: " + eventName
            infoContainer.append(`<p class="infoMessage noticeEditor${sanitise(editorId)}" id="notice${sanitise(occasionId)}"> ` + infoString + `</p>`)
            eventDiv.addClass("beingEdited") // Add class that shows which event is being edited
            eventDiv.addClass("editor" + editorId)
            if (eventDiv.hasClass("beingEdited")) {
                eventDiv.find(".controlButtons").hide()
            }
            infoContainer.slideDown() // Show the notice.
        }
    }
}


/**
 * Reverts all the changes made by handleNotifyEvent
 *
 * @param notification The JSON object we receive (modeled by OutgoingNotification).
 */
function handleStopEvent(notification) {

    const occasionId = notification.id;
    const editorId = notification.editorId

    if (checkPrivilege()) {
        let elementDiv;
        let notice;
        if (occasionId === "*") { // A websocket disconnected, so we need to remove the element by the editorId
            notice = $(".noticeEditor" + editorId);
            elementDiv = $(".editor" + editorId);
        } else {
            elementDiv = $("#" + occasionId);
            notice = $("#notice" + occasionId);
        }
        notice.remove();
        elementDiv.removeClass("beingEdited")
        elementDiv.removeClass("editor" + editorId);
        if (!thisUserIsEditing) {
            $(".controlButtons").show()
            $(".beingEdited ").find(".controlButtons").hide()
        }
        if (elementDiv.hasClass("beingEdited")) {
            elementDiv.find(".controlButtons").hide()
        }
        let infoContainer = $("#informationBar");
        if (isEmpty(infoContainer)) {
            infoContainer.slideUp() // Hide the notice.
        }
    }
}
