$(() => {
    //Gets the project Id
    const projectId = $("#projectId").html()
    getSprints(() => {
        refreshDeadlines(projectId);
        refreshMilestones(projectId);
        refreshEvents(projectId);
    });


    let addSprint = $(".addSprint")
    addSprint.css("left", $(".eventContainer").width() + "px")
    addSprint.css("bottom", 0 - $(".addSprintSvg").height() / 2 + "px")
})


/**
 * When project edit button is clicked.
 * Redirect page.
 */
$("#editProject").on("click", () => {
    location.href = "editProject?projectId=" + projectId;
})


/**
 * When project add sprint button is pressed.
 * Redirect page.
 */
$("#projectAddSprint").on("click", () => {
    $.ajax({
        url: "portfolio/addSprint?projectId=" + projectId,
        success: function (response) {
            $(".sprintsContainer").slideUp(400, function () {
                $(".sprintsContainer").empty()
                getSprints(() => {
                    refreshDeadlines(projectId);
                    refreshMilestones(projectId);
                    refreshEvents(projectId);
                })
            })
            createAlert("Sprint created!", AlertTypes.Success)
            sendNotification("sprint", response.id, "create")
        },
        error: function (error) {
            createAlert(error.responseText, AlertTypes.Failure)
        }
    })
})


$(".collapseAlert").on("click", function () {
    $(this).parent().slideUp();
})



/**
 * When edit sprint button is clicked.
 * Redirect page.
 */
$(document).on("click", ".editSprint", function () {
    let sprintId = $(this).closest(".sprint").find(".sprintId").text();
    location.href = "sprintEdit?sprintId=" + sprintId + "&projectId=" + projectId;
})


/**
 * When sprint delete button is clicked.
 * Asks for confirmation,
 * if yes: sends ajax delete request then reloads page.
 * if no: does nothing.
 */
$(document).on("click", ".deleteSprint", function () {
    let sprintId = $(this).closest(".sprint").find(".sprintId").text();
    let sprintName = $(this).closest(".sprint").find(".name").text();

    if (window.confirm(`Are you sure you want to delete ${sprintName}?`)) {
        $.ajax({
            url: "deleteSprint",
            type: "DELETE",
            data: {"sprintId": sprintId},
            success: function () {
                $(".editSprint").tooltip('hide')
                $(".deleteSprint").tooltip('hide')
                createAlert("Sprint deleted!", "success")
                sendNotification("sprint", sprintId, "delete")
            },
            error: function (error) {
                createAlert(error.responseText, "failure")
            }
        }).done(function () {
            $(".sprintsContainer").slideUp(400, function () {
                $(".sprintsContainer").empty()
                getSprints(() => {
                    refreshDeadlines(projectId);
                    refreshMilestones(projectId);
                    refreshEvents(projectId);
                })
            })
        })
    }
})

/**
 * Gets a list of sprint objects from the backend and adds them to the page.
 *
 * @param callback A function to call when the ajax request returns
 */
function getSprints(callback = () => {
}) {
    $.ajax({
        url: 'getSprintList',
        type: 'GET',
        data: {"projectId": projectId},
        success: function (response) {
            let sprintContainer = $(".sprintsContainer")
            for (let index in response) {
                sprintContainer.append(appendSprint(response[index], index));
            }
            sprintContainer.slideDown(400)
            removeElementIfNotAuthorized()
            callback();
        }
    })
}

/**
 * Creates a sting containing html to display the given sprint on the page.
 *
 * @param sprintObject An object containing the properties of the sprint.
 * @param index Used for generating sprint labels. Is this sprint 1, sprint 2, etc.
 * @returns {string} HTML to be appended.
 */
function appendSprint(sprintObject, index) {
    index = parseInt(index) + 1

    return `
             <div class="sprint" id=${sanitise(sprintObject.id)} style="border-left: solid 0.3rem ${sanitise(sprintObject.colour)}; border-right: solid 0.3rem ${sanitise(sprintObject.colour)};">
                <p class="sprintColour" style="display: none">${sanitise(sprintObject.colour)}</p>
                <p class="sprintId" style="display: none">${sanitise(sprintObject.id)}</p>
                <p class="sprintStart" style="display: none">${sanitise(sprintObject.startDate)}</p>
                <p class="sprintEnd" style="display: none">${sanitise(sprintObject.endDate)}</p>
                <p class="sprintLabel" >Sprint ${sanitise(index)}</p>
                <div class="mb3">
                    <h2 class="name">${sanitise(sprintObject.name)}</h2>
                </div>
                <div class="dateParentDiv">
                    <h6>${sanitise(sprintObject.startDateFormatted)}</h6>
                    <p class="dateDivider">-</p>
                    <h6>${sanitise(sprintObject.endDateFormatted)}</h6>
                </div>
                <div class="mb-3">
                    <p id="sprintDescription">${sanitise(sprintObject.description)}</p>
                </div>
                <div class="mb3 hasTeacherOrAbove">
                    <button type="button" class="deleteSprint deleteIcon noStyleButton sprintButton" data-bs-toggle="tooltip"
                            data-bs-placement="top" title="Delete Sprint">
                            <svg class="bi bi-trash" fill="currentColor" height="20" viewBox="0 0 16 16" width="20"
                                 xmlns="http://www.w3.org/2000/svg">
                                <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/>
                                <path d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z"
                                      fill-rule="evenodd"/>
                            </svg>
                    </button>
                    <button type="button" class="editSprint editIcon noStyleButton sprintButton" data-bs-toggle="tooltip"
                            data-bs-placement="top" title="Edit Sprint">
                          <svg class="bi bi-pencil" fill="currentColor" height="18" viewBox="0 0 16 16" width="18"
                                 xmlns="http://www.w3.org/2000/svg">
                                <path d="M12.146.146a.5.5 0 0 1 .708 0l3 3a.5.5 0 0 1 0 .708l-10 10a.5.5 0 0 1-.168.11l-5 2a.5.5 0 0 1-.65-.65l2-5a.5.5 0 0 1 .11-.168l10-10zM11.207 2.5 13.5 4.793 14.793 3.5 12.5 1.207 11.207 2.5zm1.586 3L10.5 3.207 4 9.707V10h.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.5h.293l6.5-6.5zm-9.761 5.175-.106.106-1.528 3.821 3.821-1.528.106-.106A.5.5 0 0 1 5 12.5V12h-.5a.5.5 0 0 1-.5-.5V11h-.5a.5.5 0 0 1-.468-.325z"/>
                          </svg>                    
                    </button>
                </div>
            </div>`;
}


function removeElementIfNotAuthorized() {
    if (!checkPrivilege()) {
        $(".hasTeacherOrAbove").remove()
    }
}


