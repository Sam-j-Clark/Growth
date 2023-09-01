/** The calendar so it can be accessed globally */
let calendar;


/**
 * Runs when a sprint is resized on the calendar.
 * Sends a message (error or success) and then sets the handles of the sprints displayed on the calendar.
 *
 * @param info - the fullcalendar event information being sent to the function
 */
function eventResize(info) {
    let fcEvent = $(".fc-event")
    // Data to send in post request to server

    // Add a day to returned start date due to how full calendar defines start date
    let startDate = new Date(info.event.start.toISOString().split("T")[0])
    startDate.setDate(startDate.getDate() + 1);

    let dataToSend = {
        "sprintId": info.event.id,
        "sprintName": info.event.title,
        "sprintStartDate": startDate.toISOString().split("T")[0],
        "sprintEndDate": info.event.end.toISOString().split("T")[0],
        "sprintDescription": info.event.extendedProps.description,
        "sprintColour": info.event.extendedProps.defaultColor
    }

    // Update sprint to have new start and end dates
    $.ajax({
        url: "sprintSubmit",
        type: "post",
        data: dataToSend,
        success: function () {
            createAlert("Sprint dates updated successfully", AlertTypes.Success)
            fcEvent.css("border-right", "solid 0px #13CEE2");
            fcEvent.css("border-left", "solid 0px #13CEE2");
            info.event.setProp("borderColor", '#c2080b');
            $(".fc-event-resizer-start").parent().css("border-left", "solid 5px red");
            $(".fc-event-resizer-end").parent().css("border-right", "solid 5px red");
        },
        error: function (error) {
            createAlert(error.responseText, AlertTypes.Failure)
            fcEvent.css("border-right", "solid 0px #13CEE2");
            fcEvent.css("border-left", "solid 0px #13CEE2");
            info.revert()
            info.event.setProp("borderColor", '#c2080b');
            $(".fc-event-resizer-start").parent().css("border-left", "solid 5px red");
            $(".fc-event-resizer-end").parent().css("border-right", "solid 5px red");
        }
    })
}


/**
 * Runs when a sprint has finished resizing. Runs before the eventResize function above. This is used to fix the colours
 * when the sprint is not actually changed as the eventResize only runs if the dates change, thus causing the colours to
 * revert on the selected sprint.
 *
 * @param info - the fullcalendar event information being sent to the function
 */
function eventResizeStop(info) {
    info.event.setProp("borderColor", '#c2080b');
    $(".fc-event-resizer-start").parent().css("border-left", "solid 5px red");
    $(".fc-event-resizer-end").parent().css("border-right", "solid 5px red");
}


/**
 * Handles event selection when clicked. Called by Full Calendar eventClick property.
 *
 * @param info object supplied by FullCalendar contains various relevant properties
 */
function eventClick(info) {
    let fcEvent = $(".fc-event")
    let canEdit = checkPrivilege()
    if (!canEdit || !info.event.extendedProps.isSprint) {
        return;
    }
    let events = info.view.calendar.getEvents();
    if (!info.event.extendedProps.selected) {
        // Deselects all events
        for (calEvent of events) {
            if (calEvent.extendedProps.isSprint) {
                calEvent.setExtendedProp("selected", false);
                calEvent.setProp("durationEditable", false);
                calEvent.setProp("backgroundColor", calEvent.extendedProps.defaultColor);
                calEvent.setProp("borderColor", '#13CEE2');
                fcEvent.css("border-right", "solid 0px #13CEE2");
                fcEvent.css("border-left", "solid 0px #13CEE2");
            }
        }

        // Selects this event
        info.event.setExtendedProp("selected", true);
        info.event.setProp("durationEditable", true);
        info.event.setProp("backgroundColor", '#aaa');
        info.event.setProp("borderColor", '#c2080b');
        $(".fc-event-resizer-start").parent().css("border-left", "solid 5px red");
        $(".fc-event-resizer-end").parent().css("border-right", "solid 5px red");

    } else {
        // Deselects this event
        info.event.setExtendedProp("selected", false);
        info.event.setProp("durationEditable", false);
        info.event.setProp("backgroundColor", info.event.extendedProps.defaultColor)
        info.event.setProp("borderColor", '#13CEE2');
        fcEvent.css("border-right", "solid 0px #13CEE2");
        fcEvent.css("border-left", "solid 0px #13CEE2");
    }
}


/**
 * Turns a html string into a Node object to be added into a div.
 *
 * @param htmlString
 * @returns {ChildNode}
 */
function createElementFromHTML(htmlString) {
    let div = document.createElement('div');
    div.innerHTML = htmlString.trim();

    // Change this to div.childNodes to support multiple top-level nodes.
    return div.firstChild;
}


/**
 * A helper function to add a day to a date. This is needed as full calendar will cut off one day if we use the normal
 * end date
 */
function addDay(date) {
    let result = new Date(date);
    result.setDate(result.getDate() + 1);
    return result;
}


/**
 * A helper function to turn a JavaScript date into a string that full calendar can use
 */
function formatDateString(date) {
    const yyyy = date.getFullYear();
    let mm = date.getMonth() + 1; // Months start at 0!
    let dd = date.getDate();

    if (dd < 10) dd = '0' + dd;
    if (mm < 10) mm = '0' + mm;

    date = yyyy + '-' + mm + '-' + dd;
    return date;
}


/**
 * $(document).ready fires off a function when the document has finished loading.
 * https://learn.jquery.com/using-jquery-core/document-ready/
 */
$(function () {
    let projectId = $("#projectId").html();
    let calendarEl = document.getElementById('calendar');
    $("body").tooltip({selector: '[data-toggle=tooltip]'});
    let endDate = Date.parse($('#projectEndUnformatted').text());
    endDate = formatDateString(addDay(endDate));

    /**
     * Calendar functionality
     * https://fullcalendar.io/docs
     */
    calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        eventDurationEditable: false,
        eventResizableFromStart: true,
        nextDayThreshold: '00:00:00',
        validRange: {
            start: $('#projectStartUnformatted').text(),
            end: endDate
        },
        eventResize: function (info) {
            eventResize(info)
        },
        eventResizeStop: function (info) {
            eventResizeStop(info)
        },
        eventClick: function (info) {
            eventClick(info)
        },
        themeSystem: 'bootstrap5',
        eventSources: [{ //The sources to grab the events from.
            url: 'getProjectSprintsWithDatesAsFeed', //Project sprints
            method: "get",
            extraParams: {
                projectId: projectId.toString()
            },
            failure: function (err) {
                console.log(err.responseText)
            }
        },
            {
                url: 'getProjectAsFeed', // Project itself
                method: "get",
                display: "inverse-background",
                extraParams: {
                    projectId: projectId.toString()
                },
                failure: function (err) {
                    console.log(err.responseText)
                }
            },
            {
                url: 'getEventsAsFeed', // Get all events
                method: "get",
                extraParams: {
                    projectId: projectId.toString()
                },
                success: () => {
                },
                failure: function (err) {
                    console.log(err.responseText)
                }
            },
            {
                url: 'getDeadlinesAsFeed', // Get all deadlines
                method: "get",
                extraParams: {
                    projectId: projectId.toString()
                },
                failure: function (err) {
                    console.log(err.responseText)
                }
            },
            {
                url: 'getMilestonesAsFeed', // Get all milestones
                method: "get",
                extraParams: {
                    projectId: projectId.toString()
                },
                success: () => {

                },
                failure: function (err) {
                    console.log(err.responseText)
                },
            }
        ],

        /**
         * Executes once for each calendar event (sprint, deadline, milestone, and event) added to the DOM.
         * If the event is a sprint, it receives border styling.
         * Adds borders to sprints, then creates occasion icons to display events, deadlines, and milestones.
         *
         * @param info The details for one event
         */
        eventDidMount: function (info) {
            try {
                // Set border colour for all events (Calendar occasions will have this overwritten in the css)
                info.event.setProp("borderColor", '#13CEE2');
                // Get parent HTML element of event
                let parentElement = info.el.querySelector(".fc-event-title").parentElement;
                let eventTitles = "";
                if (["milestoneCalendar", "deadlineCalendar", "eventCalendar"].includes(info.event.classNames.toString())) {
                    info.event.setProp("textColor", "black"); // Set the text colour of calendar occasions to black
                    eventTitles += info.event.extendedProps.occasionTitles;
                } // Add svg icons to parent html element for calendar occasions
                if (info.event.classNames.toString() === "milestoneCalendar") {
                    parentElement.insertBefore(createElementFromHTML(` <svg data-toggle="tooltip" data-bs-placement="left" data-html="true" title='${sanitise(eventTitles)}'
                                       xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trophy-fill calendarOccasion" viewBox="0 0 16 16">
                                       <path d="M2.5.5A.5.5 0 0 1 3 0h10a.5.5 0 0 1 .5.5c0 .538-.012 1.05-.034 1.536a3 3 0 1 1-1.133 5.89c-.79 1.865-1.878 2.777-2.833 3.011v2.173l1.425.356c.194.048.377.135.537.255L13.3 15.1a.5.5 0 0 1-.3.9H3a.5.5 0 0 1-.3-.9l1.838-1.379c.16-.12.343-.207.537-.255L6.5 13.11v-2.173c-.955-.234-2.043-1.146-2.833-3.012a3 3 0 1 1-1.132-5.89A33.076 33.076 0 0 1 2.5.5zm.099 2.54a2 2 0 0 0 .72 3.935c-.333-1.05-.588-2.346-.72-3.935zm10.083 3.935a2 2 0 0 0 .72-3.935c-.133 1.59-.388 2.885-.72 3.935z"/>
                                       </svg>`), parentElement.firstChild);
                } else if (info.event.classNames.toString() === "deadlineCalendar") {
                    parentElement.insertBefore(createElementFromHTML(`<svg data-toggle="tooltip" data-bs-placement="left" title='${sanitise(eventTitles)}'
                                        xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-hourglass-split calendarOccasion" viewBox="0 0 16 16">
                                        <path d="M2.5 15a.5.5 0 1 1 0-1h1v-1a4.5 4.5 0 0 1 2.557-4.06c.29-.139.443-.377.443-.59v-.7c0-.213-.154-.451-.443-.59A4.5 4.5 0 0 1 3.5 3V2h-1a.5.5 0 0 1 0-1h11a.5.5 0 0 1 0 1h-1v1a4.5 4.5 0 0 1-2.557 4.06c-.29.139-.443.377-.443.59v.7c0 .213.154.451.443.59A4.5 4.5 0 0 1 12.5 13v1h1a.5.5 0 0 1 0 1h-11zm2-13v1c0 .537.12 1.045.337 1.5h6.326c.216-.455.337-.963.337-1.5V2h-7zm3 6.35c0 .701-.478 1.236-1.011 1.492A3.5 3.5 0 0 0 4.5 13s.866-1.299 3-1.48V8.35zm1 0v3.17c2.134.181 3 1.48 3 1.48a3.5 3.5 0 0 0-1.989-3.158C8.978 9.586 8.5 9.052 8.5 8.351z"/>
                                        </svg>`), parentElement.firstChild);
                } else if (info.event.classNames.toString() === "eventCalendar") {
                    parentElement.insertBefore(createElementFromHTML(`<svg data-toggle="tooltip" data-bs-placement="left" title='${sanitise(eventTitles)}'
                                        xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-calendar3-event-fill calendarOccasion" viewBox="0 0 16 16">
                                        <path fill-rule="evenodd" d="M2 0a2 2 0 0 0-2 2h16a2 2 0 0 0-2-2H2zM0 14V3h16v11a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2zm12-8a1 1 0 1 0 2 0 1 1 0 0 0-2 0z"/>
                                        </svg>`), parentElement.firstChild);
                }
            } catch (e) {
                // Ensures the event is defined.
            }
        },
    });
    calendar.render();
})


// ---------------------------------------------- Notification Handlers ------------------------------------------------


/**
 * Implements the handleCreateEvent function defined in notifications.js.
 *
 * In this case the handling of create events fetches the calendar events.
 *
 * @param message - unused but required to match function definition.
 */
function handleCreateEvent (message) {
    calendar.refetchEvents();
}


/**
 * Implements the handleUpdateEvent function defined in notifications.js.
 *
 * In this case the handling of update events fetches the calendar events.
 *
 * @param message - unused but required to match function definition.
 */
function handleUpdateEvent (message) {
    calendar.refetchEvents();
}


/**
 * Implements the handleDeleteEvent function defined in notifications.js.
 *
 * In this case the handling of delete events fetches the calendar events.
 *
 * @param message - unused but required to match function definition.
 */
function handleDeleteEvent (message) {
    calendar.refetchEvents();
}
