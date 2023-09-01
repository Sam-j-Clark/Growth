$(() => {
    //Jquery selectors to remove duplicity
    let shortName = $("#shortName")
    let longName = $("#longName")
    startCharacterCounting("form-control");

    // On create group form submit
    $("#createGroupForm").on("submit", function (event) {
        event.preventDefault(); // Prevents submit

        let groupData = {
            "shortName": shortName.val(),
            "longName": longName.val(),
        }

        $.ajax({
            url: "groups/edit",
            type: "post",
            data: groupData,
            success: function () {
                sendNotification("group", 0, "newGroup");
                location.href = "groups" // On success reloads page
            },
            error: function (error) {//Displays error in box on failure
                createAlert(error.responseText, AlertTypes.Failure)
            }
        })
    })
})
