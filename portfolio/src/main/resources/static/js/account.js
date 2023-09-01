$(() => {

    //Jquery selectors to remove duplicity
    let editUserButton = $(".editUserButton")
    let firstname = $("#firstname")
    let middlename = $("#middlename")
    let lastname = $("#lastname")
    let nickname = $("#nickname")
    let bio = $("#bio")
    let personalPronouns = $("#personalPronouns")
    let email = $("#email")
    let accountForm = $("#accountForm")
    let passwordForm = $("#passwordChangeForm")


    //re-populates the account values with the correct values from the server
    function populateAccountInfo() {
        $.ajax({
            url: "getUser",
            success: function (response) {
                firstname.val(response.firstName)
                middlename.val(response.middleName)
                lastname.val(response.lastName)
                nickname.val(response.nickname)
                bio.val(response.bio)
                personalPronouns.val(response.pronouns)
                email.val(response.email)
            },
            error: function (error) {
                createAlert(error.responseText, "failure")
            }
        })
    }


    //toggles the edit form
    function toggleEditForm() {
        let canDisable = $(".canDisable")
        canDisable.prop("disabled", !canDisable.prop("disabled"));
        let editUserSubmit = $(".editUserSubmit")
        let passwordChangeDiv = $(".passwordChangeDiv")
        editUserSubmit.slideToggle() // Show submit button
        passwordChangeDiv.slideToggle() // Show password change form
        if (editUserButton.text() === "Edit Account") { //Toggle text change
            editUserButton.text("Cancel")
        } else {
            populateAccountInfo()
            editUserButton.text("Edit Account")
        }
    }


    /**
     * Performs validation of the bio so that errors are displayed immediately
     */
    bio.on("input", () => {
        if (GENERAL_UNICODE_REGEX.test(bio.val())) {
            bio[0].setCustomValidity("")
        } else {
            bio[0].setCustomValidity("invalid")
        }
    })


    //On Edit Account button click
    $(editUserButton).on("click", toggleEditForm)


    //On upload photo button click
    $("#uploadPhotoButton").on("click", () => {
        location.href = "uploadImage"; // change location
    });


    // On account form submit
    accountForm.on("submit", (event) => {
        event.preventDefault(); // Prevents submit
        if (accountForm[0].checkValidity()) {
            let accountData = {
                "firstname": firstname.val(),
                "middlename": middlename.val(),
                "lastname": lastname.val(),
                "nickname": nickname.val(),
                "bio": bio.val(),
                "personalPronouns": personalPronouns.val(),
                "email": email.val()
            }

            $.ajax({
                url: "edit/details",
                type: "post",
                data: accountData,
                success: (response) => {
                    createAlert("Updated details successfully!", "success")
                    sendNotification(JSON.stringify(response), userIdent, "update user details")
                    toggleEditForm()
                },
                error: function (error) {//Displays error in box on failure
                    createAlert(error.responseText, "failure")
                }
            })
        } else {
            event.stopPropagation();
            const errorElements = accountForm.find(".form-control:invalid")
            $('html, body').animate({
                scrollTop: $(errorElements[0]).offset().top - 100
            }, 50); // Scrolls to the first invalid field of the form
        }
    })


    // On password change form submit
    passwordForm.on("submit", (event) => {
        event.preventDefault()
        if (passwordForm[0].checkValidity()) {
            let data = {
                "oldPassword": $("#OldPassword").val(),
                "newPassword": $("#NewPassword").val(),
                "confirmPassword": $("#ConfirmPassword").val()
            }

            $.ajax({
                type: "post",
                data: data,
                url: "edit/password",
                success: function () {
                    createAlert("Password Changed Successfully!", "success")
                    toggleEditForm()
                    $("#OldPassword").val('')
                    $("#NewPassword").val('')
                    $("#ConfirmPassword").val('')
                },
                error: function (error) { // Display errors in box on failure
                    createAlert(error.responseText, "failure")
                }
            })
        } else {
            event.stopPropagation();
            const errorElements = passwordForm.find(".form-control:invalid")
            $('html, body').animate({
                scrollTop: $(errorElements[0]).offset().top - 100
            }, 50); // Scrolls to the first invalid field of the form
        }
    })

})

/**
 * Takes a role change notification and updates the roles box on the page, then passes it off to the generic role
 * change message function
 * @param notification The content of the role change notification
 * @param action The action taken. Unused, but needs to be here to match the signature
 */
function handleRoleChangeEvent(notification, action) {
    $.ajax({
        url: "getUser",
        success: function (response) {
            $("#roles").val(response.roles.join(", "))
        },
        error: function (error) {
            createAlert(error.responseText, "failure")
        }
    })
    displayRoleChangeMessage(notification, action)
}