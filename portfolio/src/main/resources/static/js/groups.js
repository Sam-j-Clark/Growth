let selectedGroupId
let group
const TEACHER_GROUP_ID = 1
const MWAG_GROUP_ID = 2
let groupPage
let groupMembersPage = 1

$(function () {
    getGroups()
})

//----------------------- jQuery UI User Selection -------------------------


/**
 * Implements the selectable widget from jQuery UI to enable the selection of users in the group members list.
 */
function manageTableSelection() {
    let anchorRow

    $("#groupTableBody").selectable({
        filter: ":not(td)",

        /**
         * Overrides the selected method for the jQuery UI selectable widget, to enable shift clicking.
         *
         * A non-shift select sets an "anchor" row. Shift clicking on either side of the anchor row selects rows between the
         * anchor row and the selected row (inclusive).
         * Ctrl clicks allow non-adjacent rows to be selected and deselected.
         * Ctrl clicks followed by a shift click will deselect all but the latest ctrl click.
         *
         * @param e An event (e.g. a key press)
         * @param ui The latest selected row
         */
        selected: function (e, ui) {  // overrides library function to enable shift clicking
            let currentRow = $(ui.selected)

            if (e.shiftKey) {
                let currentId = parseInt(currentRow.attr("userId"))
                let lastId

                if (typeof anchorRow == "undefined") {  // if first selection on table, set anchor to this row
                    anchorRow = $(ui.selected)
                    lastId = currentId
                } else {
                    lastId = parseInt(anchorRow.attr("userId"))
                }

                if (currentId > lastId) {  // latest selected row is below the previous selected row
                    currentRow.prevUntil(anchorRow).each((i, row) => {  //for every row between the current and last selected rows
                        $(row).addClass("ui-selected")
                    })
                } else if (currentId < lastId) {  // latest selected row is above the previous selected row
                    currentRow.nextUntil(anchorRow).each((i, row) => {
                        $(row).addClass("ui-selected")
                    })
                }

                currentRow.addClass("ui-selected")
                anchorRow.addClass("ui-selected")
            }
            checkToSeeIfHideOrShowOptions()
            addDraggable()
            showDraggableIcons()
            anchorRow = currentRow
        },

        /**
         * Overrides the unselected method for the jQuery UI selectable widget.
         * Hides the drag grip on each row that has been unselected.
         *
         * @param e An event (unused)
         * @param ui The unselected rows
         */
        unselected: function (e, ui) {
            let unselected = $(ui.unselected)
            $(unselected).each(function () {
                $(this).find(".dragGrip").hide()
            })
            if (e.metaKey || e.ctrlKey) {
                checkToSeeIfHideOrShowOptions()
            }
        }
    })
}


/**
 * Displays the grip element on each jQuery UI selected row.
 */
function showDraggableIcons() {
    $(".ui-selected").find(".dragGrip").show()
}


/**
 * Implements the jQuery UI draggable widget to enable the dragging of group members between groups.
 * Reference: https://api.jqueryui.com/draggable/
 */
function addDraggable() {
    $(".dragGrip").draggable({
        helper: function () {
            let helper = $("<table class='table colourForDrag'/>")
            return helper.append($(".ui-selected").clone())
        },
        revert: "invalid",
        appendTo: "body"
    })
}


/**
 * Handles the selecting, dragging, and dropping of group members.
 * Implemented using JQuery UI droppable https://api.jqueryui.com/droppable/
 */
function manageGroupTableInteraction() {
    if (!checkPrivilege()) {
        return
    }
    manageTableSelection()

    let listOfGroupDivs = $(".group") // gets a list of divs that have the class group
    for (let i = 0; i < listOfGroupDivs.length; i++) { // Loops over each div
        /**
         * Adds the droppable pluggin to each element that it loops over
         * https://api.jqueryui.com/droppable/
         */
        $(listOfGroupDivs[i]).droppable({
            /**
             * Triggered when an accepted draggable is dragged over the droppable (based on the tolerance option).
             * https://api.jqueryui.com/droppable/#event-over
             */
            over: function () {
                $(this).animate({left: '+=30px'}, 35);
                $(this).animate({left: '-=60px'}, 70);
                $(this).animate({left: '+=60px'}, 70);
                $(this).animate({left: '-=60px'}, 70);
                $(this).animate({left: '+=30px'}, 35);
            },

            /**
             * Triggered when an accepted draggable is dropped on the droppable (based on the tolerance option).
             * https://api.jqueryui.com/droppable/#event-drop
             */
            drop: function () {
                addUsers($(this).attr("id"))
                showDraggableIcons()
            },
            tolerance: "pointer"
        })
    }
}


//------------------------ Other Functions ------------------------------


/**
 * Listens for a change on one of the group display selectors
 * Calls getGroups.
 */
$(document).on("change", ".small-options", function (event) {
    event.preventDefault()
    getGroups(groupPage)
})


function liveUpdateGroupList() {
    getGroups()
    createAlert("New group has been created", "info")
}


/**
 * Gets the group data from the server for displaying the preview list of groups.
 */
function getGroups(page = groupPage) {
    let groupsPerPage = $("#groupDisplayAmountSelection").find("option:selected").text()
    groupsPerPage = groupsPerPage.toLowerCase()
    let sortBy = $("#groupSortBySelector").find("option:selected").text()
    $.ajax({
        url: "getGroups",
        type: "GET",
        data: {
            "page": page,
            "groupsPerPage": groupsPerPage,
            "sortBy": sortBy
        },
        success: function (data) {
            groupPage = data.page
            $(".optionForAmountOfGroups").each((index, element) => {
                // Iterates over the dropdown menu for the group selector and sets the attribute to selected based on the data from the server.
                $(element).attr("selected", Number($(element).val()) === data.groupsPerPage)
            })
            populateGroupPageSelector(data.footerNumberSequence, data.page)
            createListOfGroups(data.groups)
        },
        error: function (error) {
            createAlert(error.responseText, AlertTypes.Failure)
        }
    }).then(manageGroupTableInteraction)
}


/**
 * Populates the group page selector.
 * Appends the number elements after the "previous" selector
 *
 * @param data The numbers.
 * @param currentPage The current page that is being displayed
 */
function populateGroupPageSelector(data, currentPage) {
    let groupPaginationButtons = $(".groupPaginationSelector")
    // ".groupPageLink" in both ".groupPaginationSelector" and ".groupMembersAmountOptions", here only need the ".groupPaginationSelector" one
    groupPaginationButtons.find(".groupPageLink").each((index, element) => {
        if (!$(element).hasClass("specialFooterButton")) {
            $(element).remove()
        }
    })

    for (const number of data) {
        groupPaginationButtons.find(".groupFooterNext").before(createFooterNumberSelector(number))
    }

    let groupPageSelector = groupPaginationButtons.find(".groupPageSelector")
    groupPageSelector.removeClass("active")
    groupPageSelector.each((index, element) => {
        //Goes through the page selectors and adds a class of active if it's the current page we are on
        if (Number($(element).text()) === currentPage) {
            $(element).addClass("active")
        }
    })
    toggleGroupNavigationButtons(groupPaginationButtons)
}


/**
 * Checks if each special button on the footer navigator should be disabled or not.
 * For example: If we are on page 1, "first" and "previous" should be disabled.
 */
function toggleGroupNavigationButtons(parentDiv) {
    let footerPrevious = parentDiv.find(".groupFooterPrevious")
    let footerFirst = parentDiv.find(".groupFooterFirst")
    if (footerPrevious.next().hasClass("active")) {
        footerPrevious.addClass("disabled")
        footerFirst.addClass("disabled")
    } else {
        footerPrevious.removeClass("disabled")
        footerFirst.removeClass("disabled")
    }
    let footerNext = parentDiv.find(".groupFooterNext")
    let footerLast = parentDiv.find(".groupFooterLast")
    if (footerNext.prev().hasClass("active")) {
        footerNext.addClass("disabled")
        footerLast.addClass("disabled")
    } else {
        footerNext.removeClass("disabled")
        footerLast.removeClass("disabled")
    }
}


/**
 * Creates the elements that go into the group page selector
 *
 * @param number The number to go into the selection
 * @returns {string} A list element
 */
function createFooterNumberSelector(number) {
    return `<li class="page-item groupPageSelector groupPageLink"><a class="page-link" href="#">${number}</a></li>`
}


/**
 * Creates the group elements by iterating over a list of groups.
 *
 * @param groups The list of groups.
 */
function createListOfGroups(groups) {
    let groupOverviewContainer = $("#groupListDiv")
    $(".group").each((index, element) => {
        $(element).remove()
    })
    for (const groupsKey in groups) {
        groupOverviewContainer.append(createGroupPreviewDiv(groups[groupsKey]))
    }
}


/**
 * Creates the div that holds the group preview.
 *
 * @param group The group to get the data from
 * @returns {string} A string that is a div
 */
function createGroupPreviewDiv(group) {
    return `<div class="box group" id="${group.id}">
                    <div class="mb3">
                        <p class="groupId" style="display: none">${group.id}</p>
                        <h2 class="groupShortName showExtraWhitespace">${group.shortName}</h2>
                        <h3 class="groupLongName showExtraWhitespace">${group.longName}</h3>
                    </div>
                </div>`
}


/**
 * Using the notification system, this is called when a group has been updated.
 * It checks to see if the current group being displayed on the page is the one that has been updated.
 * If it is then it updates the groups information including fetching the users again.
 * If it isn't then it updates the elements on the left with the new names.
 *
 * @param notification The notification from the server.
 */
function updateGroup(notification) {
    let notificationGroupId = notification.id
    let currentDisplayGroup = $("#groupBeingDisplayId").text()
    if (currentDisplayGroup === notificationGroupId) {
        displayGroupUsersList()
        if (notification.editorId !== String(userIdent)) {
            createLiveAlert("This group has been updated by " + notification.editorName, notificationGroupId)
            $(".scrollableGroupDetails").effect("highlight", 500)
        }
    } else if (parseInt(currentDisplayGroup, 10) === MWAG_GROUP_ID) {
        updateGroupDetails(MWAG_GROUP_ID)
        displayGroupUsersList()
    }
    updateGroupDetails(notificationGroupId)
}


/**
 * Using the notification system, this is called when a group has been deleted.
 * It checks to see if the current group being displayed is the one that has been deleted.
 * If it is then it slides up the group information display and alerts the user that the group has been deleted and by who.
 * If it isn't then it just slides up the element on the left hand side.
 *
 * @param notification The notification from the server.
 */
function removeGroup(notification) {
    console.log(notification)
    let notificationGroupId = notification.id
    let currentDisplayGroup = $("#groupBeingDisplayId").text()
    if (currentDisplayGroup === notificationGroupId) {
        $("#groupInformationContainer").slideUp()
        createLiveAlert("This group has been deleted by " + notification.editorName, notificationGroupId)
    }
    let group = $("#" + notificationGroupId)
    if (group.length > 0) {
        group.slideUp("500", () => {
            group.remove()
        })
    }
}

/**
 * Updates the displayed name of the user if the user is in the group currently displayed.
 *
 * @param notification The STOMPJS message containing the details that need to change
 */
function updateUserDetails(notification) {
    const usersId = notification.id
    const userRow = $("#userid" + usersId)
    if (userRow.length) {
        const userData = JSON.parse(notification.data)
        userRow.find(".firstName").text(userData.firstName)
        userRow.find(".lastName").text(userData.lastName)
    }
}


/**
 * Using the notification system, when a user updates their profile details, it will update it automatically in the
 * group's page without refreshing.
 *
 * @param notification The notification from the server.
 */
function updateUserProfilePhoto(notification){
    const usersId = notification.id
    const userRow = $("#userid" + usersId)
    let imageSource;
    if (userRow.length) {
        let imagePath = userRow.find("#userImage").attr("src")
        imagePath = imagePath.split("?")[0]
        imageSource = imagePath + "?" + Date.now()
        userRow.find("#userImage").attr("src",imageSource)
    }
}


/**
 * Grabs the latest details of a group and updates the page to reflect them.
 * @param groupId The group's details to grab.
 */
function updateGroupDetails(groupId) {
    $.ajax({
        url: `group?groupId=${groupId}`,
        type: "GET",
        success: (response) => {
            let group = $("#" + response.id)
            if (group.length > 0) {
                group.find(".groupShortName").text(response.shortName)
                group.find(".groupLongName").text(response.longName)
            }
        },
        error: function (error) {
            console.log(error)
        }
    })
}


/**
 * Ajax post request to the server for moving users from one group to another.
 */
function addUsers(groupId) {
    let arrayOfIds = [];
    let selected = $(".ui-selected")
    selected.each(function () {
        arrayOfIds.push($(this).attr("userId"))
    })
    arrayOfIds = Array.from(new Set(arrayOfIds))
    selected.removeClass("selected")
    $.ajax({
        url: `groups/addUsers?groupId=${groupId}&userIds=${arrayOfIds}`,
        type: "post",
        success: function () {
            displayGroupUsersList()
            sendNotification("group", groupId, "updateGroup");
            if (parseInt(groupId) === MWAG_GROUP_ID) {
                createAlert("User(s) moved, and teachers role remains", AlertTypes.Success)
            } else {
                createAlert("User(s) moved", AlertTypes.Success)
            }
        }, error: function (error) {
            if (error.status === 401) {
                createAlert("You don't have permission to move users. This could be because " +
                    "your roles have been updated. Try refreshing the page", AlertTypes.Failure)
            } else {
                createAlert(error.responseText, AlertTypes.Failure)
            }
        }
    })
}


/**
 * Displays the options for what to do with selected users.
 */
function showOptions() {
    let groupsDisplayOptions = $("#groupRemoveUser")
    if ((parseInt(selectedGroupId, 10) !== TEACHER_GROUP_ID || isAdmin()) && (parseInt(selectedGroupId, 10) !== MWAG_GROUP_ID)) {
        groupsDisplayOptions.slideDown()
    } else {
        groupsDisplayOptions.slideUp()
    }
}


/**
 * Hides the options for what to do with selected users.
 */
function hideOptions() {
    let groupsDisplayOptions = $("#groupRemoveUser")
    groupsDisplayOptions.slideUp()
}


/**
 * Makes all the logic changes bootstrap does when changing the tab from settings to users.
 */
function changeToUsersTab() {
    let groupSettingsTab = $("#groupSettingsTab")
    let groupUsersTab = $("#groupUsersTab")
    let groupUsersButton = $("#pillsUsersTab")
    let groupSettingsButton = $("#pillsSettingsTab")
    let groupUsersPage = $("#pillsUsers")
    let GroupSettingsPage = $("#pillsSettings")

    groupUsersTab.prop("aria-selected", true)
    groupSettingsTab.prop("aria-selected", false)

    groupUsersButton.prop("aria-selected", true)
    groupUsersButton.addClass("active")
    groupSettingsButton.prop("aria-selected", false)
    groupSettingsButton.removeClass("active")

    groupUsersTab.addClass('active')
    groupSettingsTab.removeClass('active')
    groupUsersPage.addClass('show')
    groupUsersPage.addClass('active')
    GroupSettingsPage.removeClass('show')
    GroupSettingsPage.removeClass('active')
}


/**
 * Helper function that uses the amount of selected users to determine if to call the showOptions function
 */
function checkToSeeIfHideOrShowOptions() {
    let amountSelected = $(document).find(".ui-selected").length
    if (amountSelected > 0) {
        showOptions()
    } else {
        hideOptions()
    }
}


/**
 * Called when a group page is opened. This function sets the visibility of the group settings tab.
 * The visibility is true only if the user is in the group or is a teacher or admin.
 *
 * @param group - The newly selected group.
 */
function checkEditRights(group) {
    let groupSettingsTab = $("#groupSettingsTab")
    let groupEditButton = $("#editGroupNameButton")
    let groupId = group.id
    groupSettingsTab.hide()
    groupEditButton.hide()

    if (groupId === TEACHER_GROUP_ID) {
        $(".controlButtons").hide();
    } else if (groupId === MWAG_GROUP_ID) {
        $(".controlButtons").hide();
    } else {
        $(".controlButtons").show();
    }

    // only show settings page if the active page is not MWAG or Teachers & if the user has read access
    // i.e., the user is an admin, teacher or member of the group.
    if (groupId !== MWAG_GROUP_ID &&
        groupId !== TEACHER_GROUP_ID &&
        (checkPrivilege() || group.userList.some(member => member.id === userIdent))) {
        groupSettingsTab.show()
        groupEditButton.show()
        //show edit button
    } else {
        changeToUsersTab()
    }
}


/**
 * Makes an ajax get call to the server and gets all the information for a particular group.
 * Loops through the groups members and adds them to the table.
 */
function displayGroupUsersList() {
    groupMembersPage = 1

    $.ajax({
        url: `group?groupId=${selectedGroupId}`,
        type: "GET",
        success: (response) => {
            $("#groupTableBody").empty();
            $("#groupInformationShortName").text(response.shortName);
            $("#groupInformationLongName").text(response.longName);
            updateGroupName(response.shortName, response.longName)
            $("#groupBeingDisplayId").text(response.id);
            group = response;
            displayGroupMembers()
            $("#groupInformationContainer").slideDown()
            checkToSeeIfHideOrShowOptions()
            checkEditRights(response)
        },
        error: function (error) {
            createAlert(error.responseText, AlertTypes.Failure)
        }
    })
}


/**
 * Takes the details of a group and appends each user in the group to the group details user list div.
 */
function populateGroupMembers() {
    let members = group.userList

    // cut the userList by number of members per page
    let perPage = $("#membersPerPageSelect").val();

    perPage = parseInt(perPage)

    let memberPages = [[]];
    let numMembers = members.length

    for (let i = 0; i < numMembers; i += perPage) {
        memberPages.push(members.slice(i, i + perPage))
    }

    if (groupMembersPage === -1) {
        groupMembersPage = memberPages.length - 1
    }

    $.each(memberPages[groupMembersPage], function (i, member) {
        appendMemberToGroup(member)
    })
}


/**
 * Takes a given member and appends their information to the currently selected group's members table.
 * The row includes the user's Id, image, first and last names, and username.
 *
 * @param member The group member to be displayed.
 */
function appendMemberToGroup(member) {
    let membersContainer = $("#groupTableBody")
    let imageSource;
    if (member.imagePath.length === 0) {
        imageSource = "defaultProfile.png"
    } else {
        imageSource = member.imagePath
    }

    membersContainer.append(`
                    <tr class="userRow ${checkPrivilege() ? "clickableRow" : ""}" id=userid${sanitise(member.id)} userId=${sanitise(member.id)}>
                        <td class="userRowId">
                            <svg xmlns="http://www.w3.org/2000/svg" width="25" height="25" fill="currentColor" class="bi bi-grip-vertical dragGrip" style="display: none" viewBox="0 0 16 16">
                                    <path d="M7 2a1 1 0 1 1-2 0 1 1 0 0 1 2 0zm3 0a1 1 0 1 1-2 0 1 1 0 0 1 2 0zM7 5a1 1 0 1 1-2 0 1 1 0 0 1 2 0zm3 0a1 1 0 1 1-2 0 1 1 0 0 1 2 0zM7 8a1 1 0 1 1-2 0 1 1 0 0 1 2 0zm3 0a1 1 0 1 1-2 0 1 1 0 0 1 2 0zm-3 3a1 1 0 1 1-2 0 1 1 0 0 1 2 0zm3 0a1 1 0 1 1-2 0 1 1 0 0 1 2 0zm-3 3a1 1 0 1 1-2 0 1 1 0 0 1 2 0zm3 0a1 1 0 1 1-2 0 1 1 0 0 1 2 0z"/>
                            </svg>
                            ${sanitise(member.id)}</td>
                        <td>
                            <img src=${imageSource} alt="Profile image" class="profilePicGroupsList" id="userImage"> 
                        </td>
                        <td class="firstName">${sanitise(member.firstName)}</td>
                        <td class="lastName">${sanitise(member.lastName)}</td>
                        <td>${sanitise(member.username)}</td>
                    </tr>`
    )
}


/**
 * Removes existing displayed members from the group members container, and then repopulates it.
 */
function displayGroupMembers() {
    let membersContainer = $("#groupTableBody")
    membersContainer.empty()
    populateGroupMembers()
    populateGroupMembersPageSelector()
}


/**
 * Calls the method to redisplay the selected group's members when the number displayed per page is changed.
 */
$(document).on("click", "#membersPerPageSelect", () => {
    groupMembersPage = 1
    displayGroupMembers()
})


/**
 * Calls the method to redisplay the selected group's members when the selected group members page selectors div is
 * clicked.
 */
$(document).on("click", ".groupMembersAmountOptions", () => {
    displayGroupMembers()
})


/**
 * Populates the group members page selector.
 * Appends the number elements after the "previous" selector
 */
function populateGroupMembersPageSelector() {
    let pageSelectors = $(".groupMembersAmountOptions")
    // ".groupPageLink" in both ".groupPaginationSelector" and ".groupMembersAmountOptions", here only need the ".groupMembersAmountOptions" one
    pageSelectors.find(".groupPageLink").each((index, element) => {
        if (!$(element).hasClass("specialFooterButton")) {
            $(element).remove()
        }
    })

    createFooterNumberSequence()

    let groupMembersPageSelector = pageSelectors.find(".groupPageSelector")
    groupMembersPageSelector.removeClass("active")
    groupMembersPageSelector.each((index, element) => { // adds the active class to the currently selected group members page number
        if ($(element).text() === groupMembersPage.toString()) {
            $(element).addClass("active")
        }
    })
    toggleGroupNavigationButtons(pageSelectors)
}


/**
 * Calculates the numbers for the group members page selector. Calls a function to add each number to the page
 * selector.
 */
function createFooterNumberSequence() {
    let pageSelectors = $(".groupMembersAmountOptions")

    let pageSelectorNumbers = pageSelectors.find(".groupPageSelector")
    $.each(pageSelectorNumbers, (i, el) => {
        el.remove()
    })

    let totalPages = Math.ceil(group.userList.length / $("#membersPerPageSelect").val())
    if (totalPages === 0) {
        totalPages = 1
    }
    let minNumber = 1;
    let maxNumber = 11;
    if (totalPages < 11) {
        maxNumber = totalPages;
    } else if (groupMembersPage > 6) {
        if (groupMembersPage + 5 < totalPages) {
            minNumber = groupMembersPage - 5;
            maxNumber = groupMembersPage + 5;
        } else {
            maxNumber = totalPages;
            minNumber = totalPages - 10;
        }
    }
    for (let i = minNumber; i <= maxNumber; i++) {
        pageSelectors.find(".groupFooterNext").before(createFooterNumberSelector(i))
    }
}


/**
 * A function to get the git repo information from the repository and display it on the group page, if there is no repo
 * information then it changes the header to say there is no repository
 */
function retrieveGroupRepoInformation() {
    $.ajax({
        url: `getRepo?groupId=${selectedGroupId}`,
        type: "GET",
        success: (response) => {
            let repoInformationContainer = $("#gitRepo")
            repoInformationContainer.empty();
            if (response.length === 0) {
                populateEmptyGroupRepo(repoInformationContainer)
            } else {
                let group = response[0]
                populateGroupRepoInformation(repoInformationContainer, group)
            }
            getRepoCommits();
        },
        error: function (error) {
            if (error.status === 401) {
                let repoInformationContainer = $("#gitRepo")
                repoInformationContainer.empty();
                displayUnauthorisedRepo(repoInformationContainer)
                getRepoCommits();
            }
        }
    })
}


/**
 *  A function to display tell the user that they are unauthorised to view the repository. This should only occur if a
 *  user had and then lost edit privileges
 */
function displayUnauthorisedRepo(container) {
    container.append(`
            <div id="groupSettingsRepoInformationSection">
                <div id="groupSettingsRepoHeader">
                    <h3 id="groupSettingsPageRepoName" style="color: red">
                        Sorry! You don't have permission to see this section. Please refresh the page</h3>
                </div>
                <div id="repoSettingsContainer"></div>
            </div>`
    )
}


/**
 * Populates the group's git repo information section to display the lack of a repository.
 *
 * @param container The git repo information container.
 */
function populateEmptyGroupRepo(container) {
    container.append(`
        <div id="groupSettingsRepoInformationSection">
            <div id="groupSettingsRepoHeader">
                <h3 id="groupSettingsPageRepoName">No Repository</h3>
                <button type="button" class="editRepo noStyleButton marginSides1" data-bs-toggle="tooltip"
                        data-bs-placement="top" title="Edit Repository Settings">
                    <svg class="bi bi-pencil editIcon" fill="currentColor" height="20" viewBox="0 0 16 16" width="20"
                         xmlns="http://www.w3.org/2000/svg">
                        <path d="M12.146.146a.5.5 0 0 1 .708 0l3 3a.5.5 0 0 1 0 .708l-10 10a.5.5 0 0 1-.168.11l-5 2a.5.5 0 0 1-.65-.65l2-5a.5.5 0 0 1 .11-.168l10-10zM11.207 2.5 13.5 4.793 14.793 3.5 12.5 1.207 11.207 2.5zm1.586 3L10.5 3.207 4 9.707V10h.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.5h.293l6.5-6.5zm-9.761 5.175-.106.106-1.528 3.821 3.821-1.528.106-.106A.5.5 0 0 1 5 12.5V12h-.5a.5.5 0 0 1-.5-.5V11h-.5a.5.5 0 0 1-.468-.325z"/>
                    </svg>
                </button>
            </div>
            <div id="repoSettingsContainer"></div>
        </div>`
    )
}


/**
 * Populates the given container with the given git repo information.
 *
 * @param container The git repo information container.
 * @param repo The repo information.
 */
function populateGroupRepoInformation(container, repo) {
    container.empty();
    container.append(`
        <div id="groupSettingsRepoInformationSection">
            <div id="groupSettingsRepoHeader">
                <h3 id="groupSettingsPageRepoName" class="text-truncate">${sanitise(repo.alias)}</h3>
                <button type="button" class="editRepo editIcon noStyleButton marginSides1" data-bs-toggle="tooltip"
                        data-bs-placement="top" title="Edit Repository Settings">
                    <svg class="bi bi-pencil" fill="currentColor" height="18" viewBox="0 0 16 16" width="18"
                         xmlns="http://www.w3.org/2000/svg">
                        <path d="M12.146.146a.5.5 0 0 1 .708 0l3 3a.5.5 0 0 1 0 .708l-10 10a.5.5 0 0 1-.168.11l-5 2a.5.5 0 0 1-.65-.65l2-5a.5.5 0 0 1 .11-.168l10-10zM11.207 2.5 13.5 4.793 14.793 3.5 12.5 1.207 11.207 2.5zm1.586 3L10.5 3.207 4 9.707V10h.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.5h.293l6.5-6.5zm-9.761 5.175-.106.106-1.528 3.821 3.821-1.528.106-.106A.5.5 0 0 1 5 12.5V12h-.5a.5.5 0 0 1-.5-.5V11h-.5a.5.5 0 0 1-.468-.325z"/>
                    </svg>
                </button>
            </div>

            <div id="repoInfo" class="row marginSides1">
                <div class="inlineText col">
                    <p>Project Id:&nbsp;</p>
                    <p id="groupSettingsPageProjectId" class="groupRepoInfo greyText">${sanitise(repo.projectId)}</p>
                </div>
                <div class="inlineText col">
                    <p>Access Token:&nbsp;</p>
                    <p id="groupSettingsPageAccessToken" class="groupRepoInfo greyText">${sanitise(repo.accessToken)}</p>
                </div>
            </div>
            <div id="repoSettingsContainer"></div>
        </div>`
    )
}


/**
 * Gets the commits from the provided git repository and displays the first 3 if there are any, or changes the title to
 * state that there are no commits. If there is a problem accessing the webpage, an error is displayed under the repo
 * information.
 */
function getRepoCommits() {
    const commitContainer = $("#groupSettingsCommitSection");
    const repoID = $("#groupSettingsPageProjectId").text()
    const accessToken = $("#groupSettingsPageAccessToken").text();

    commitContainer.empty();

    if (repoID.length !== 0) {
        getCommits(repoID, accessToken, (data) => {
                if (data.length === 0) {
                    commitContainer.append(`<h5>No Recent Commits</h5>`)
                } else {
                    populateCommitContainer(commitContainer, data)
                }
            },
            () => {
                handleGitRepoError()
            }
        )
    }
}


/**
 * Populates the given commit container with the first 3 commits retrieved from the git repository.
 *
 * @param commitContainer The container in which commits will be appended.
 * @param data The data retrieved from the repo, which contains the recent commits to be appended to the repo container.
 * @property committed_date The commit date
 * @property committed_time The commit time
 * @property short_id  The commit short_id
 * @property long_id The commit long_id
 * @property message The commit message
 * @property web_url The commit web_url
 * @property author_name The commit author_name
 */
function populateCommitContainer(commitContainer, data) {
    commitContainer.append(`<h5>Recent Commits:</h5>`)

    const firstThree = data.slice(0, 3);

    for (let commit of firstThree) {
        const committedDate = sanitise(commit.committed_date).split("T")[0]
        const committedTime = sanitise(commit.committed_date).split("T")[1].split(".")[0]
        const commitText =
            `<div id="groupSettingsCommitContainer" class="marginSides1">
                <div class="gitCommitInfo">
                    <div class="row">
                        <div class="inlineText">
                            <p>Commit:&nbsp;</p>
                            <a class="greyText" href="${commit.web_url}">${sanitise(commit.short_id)}</a>
                        </div>
                    </div>
                    <div class="row">
                        <p>${sanitise(commit.message)}</p>
                    </div>
                    <div class="row">
                        <div class="col">
                            <p class="greyText">${sanitise(commit.author_name)}</p>
                        </div>
                        <div class="col commitDate">
                            <p class="greyText">${committedDate} &nbsp ${committedTime}</p>
                        </div>
                    </div>
                </div>
             </div>`
        commitContainer.append(commitText)
    }
}


/**
 * Populates the group's git repo container to indicate a lack of commits in the repo.
 */
function handleGitRepoError() {
    let repoInformationContainer = $("#gitRepo");
    let repoProjectId = $("#groupSettingsPageProjectId");
    let repoAccessToken = $("#groupSettingsPageAccessToken");

    repoProjectId.removeClass('greyText')
    repoProjectId.addClass("redText")
    repoAccessToken.removeClass("greyText")
    repoAccessToken.addClass("redText")

    repoInformationContainer.append(`
        <div>
            <p style="color: red">One or more repository settings are invalid</p>
        </div>`
    )
}


/**
 * When a group name is changed, this updates its new names to prevent the need to refresh the page
 */
function updateGroupName(shortname, longname) {
    const selectedGroup = $(".focusOnGroup");
    selectedGroup.find(".groupShortName").text(shortname);
    selectedGroup.find(".groupLongName").text(longname);
}


/**
 * Performs all the actions required to close the group details edit form
 */
function cancelGroupEdit() {
    const parent = $("#groupEditInfo");
    parent.slideUp(() => {
        const editButton = $(".editButton");
        editButton.show();
    });
}


// ******************************* Click listeners *******************************


/**
 * Listens for a click on a group page navigation link (one of the page numbers etc)
 * Uses a switch statement to determine what "group page number" to send to the server.
 */
$(document).on("click", ".groupPageLink", function (event) {
    event.preventDefault()

    if ($(this).hasClass("disabled")) {
        return
    }

    let parentDiv = $(this).closest(".groupAmountOptions")
    let isGroupMembers = parentDiv.hasClass("groupMembersAmountOptions")
    let currentPage
    if (isGroupMembers) {
        currentPage = groupMembersPage
    } else {
        currentPage = groupPage
    }

    let newPage
    switch ($(this).text()) {
        case "First":
            newPage = 1
            break
        case "Previous":
            newPage = currentPage - 1
            break
        case "Next":
            newPage = currentPage + 1
            break
        case "Last":
            newPage = -1
            break
        default:
            newPage = parseInt($(this).text(), 10)
    }

    if (isGroupMembers) {
        groupMembersPage = newPage
        displayGroupMembers()
    } else {
        groupPage = newPage
        getGroups()
    }
})


/**
 * When the remove button is clicked, a popup prompts confirmation of the action.
 *
 * On confirm, the request is sent
 */
$(document).on("click", "#groupRemoveUser", function () {
    let arrayOfIds = [];
    $(".ui-selected").each(function () {
        arrayOfIds.push($(this).attr("userId"))
    })
    if (window.confirm(`Confirm removal of ${arrayOfIds.length} user${arrayOfIds.length > 1 ? "s" : ""} from this group?`)) {
        $.ajax({
            url: `groups/removeUsers?groupId=${selectedGroupId}&userIds=${arrayOfIds}`,
            type: "DELETE",
            success: () => {
                displayGroupUsersList()
                createAlert("User removed", AlertTypes.Success)
                sendNotification("group", selectedGroupId, "updateGroup");
                checkToSeeIfHideOrShowOptions()
            }, error: function (error) {
                if (error.status === 401) {
                    createAlert("You don't have permission to remove users. This could be because " +
                        "your roles have been updated. Try refreshing the page", AlertTypes.Failure)
                } else {
                    createAlert(error.responseText, AlertTypes.Failure)
                }
            }
        })
    }
})


/**
 * Fires off when a click is detected on the delete button for the group. Sends an endpoint request to delete the
 * currently selected group.
 */
$(document).on("click", ".deleteButton", function () {
    if (window.confirm(`Are you sure you want to delete this group? ${sanitise(group.userList.length)} members will be removed. This action cannot be undone.`)) {
        $.ajax({
            url: `groups/edit?groupId=${group.id}`,
            type: "delete",
            success: function () {
                sendNotification("group", group.id, "deleteGroup");
                window.location.reload()
            }, error: function (error) {
                if (error.status === 401) {
                    createAlert("You don't have permission to delete groups. This could be because " +
                        "your roles have been updated. Try refreshing the page", AlertTypes.Failure)
                } else {
                    createAlert(error.responseText, AlertTypes.Failure)
                }
            }
        })
    }
})


/**
 * A listener for the edit group name button, opens up a form that allows teacher or admins to change the group names
 */
$(document).on("click", ".editButton", () => {
    const editButton = $(".editButton");
    editButton.hide();
    editButton.tooltip("hide");

    const shortName = $("#groupInformationShortName").text();
    const longName = $("#groupInformationLongName").text();

    $("#groupShortName").val(shortName)
    $("#groupLongName").val(longName)

    if (!checkPrivilege()) {
        $("#editShortNameInput").hide();
    }
    $("#groupEditInfo").slideDown();

    startCharacterCounting("form-control");
})


/**
 * Event listener for the submit button of editing a group name
 */
$(document).on("submit", "#editGroupForm", function (event) {
    event.preventDefault();
    let url;
    let type;

    if (checkPrivilege()) {
        url = "groups/edit/details";
        type = "post";
    } else {
        url = "groups/edit/longName";
        type = "patch";
    }
    const groupData = {
        "groupId": selectedGroupId,
        "shortName": $("#groupShortName").val(),
        "longName": $("#groupLongName").val(),
    }

    $.ajax({
        url: url,
        type: type,
        data: groupData,
        success: function () {
            createAlert("Changes submitted", AlertTypes.Success);
            cancelGroupEdit();
            displayGroupUsersList();
            updateGroupName($("#groupShortName").val(), $("#groupLongName").val());
            sendNotification("group", selectedGroupId, "updateGroup");
        }, error: function (error) {
            if (error.status === 401) {
                createAlert("You don't have permission to edit group details. This could be because " +
                    "your roles have been updated. Try refreshing the page", AlertTypes.Failure)
            } else {
                createAlert(error.responseText, AlertTypes.Failure)
            }
        }
    })
})


/**
 * Event listener for the cancel button on the git repo edit form.
 */
$(document).on("click", ".cancelGroupEdit", cancelGroupEdit);


/**
 * Makes an ajax get call to the server and gets all the information for a particular group.
 * Loops through the groups members and adds them to the table.
 * @param groupId the id of the group to fetch
 */
$(document).on("click", "#pillsSettingsTab", function () {
    retrieveGroupRepoInformation()
})


/**
 * When group div is clicked, the members for that group are retrieved.
 */
$(document).on("click", ".group", function () {
    $(".group").removeClass("focusOnGroup")
    selectedGroupId = $(this).closest(".group").find(".groupId").text()
    let groupShortname = $(this).closest(".group").find(".groupShortName").text();
    displayGroupUsersList();
    retrieveGroupRepoInformation()

    if (groupShortname === "Teachers") { // teacher group
        $(".controlButtons").hide();
    } else if (groupShortname === "Non-Group") { // non-group group
        $(".controlButtons").hide();
    } else {
        $(".controlButtons").show();
    }
    $("#confirmationForm").slideUp();
    $("#groupEditInfo").slideUp();
    $(this).closest(".group").addClass("focusOnGroup");
})
