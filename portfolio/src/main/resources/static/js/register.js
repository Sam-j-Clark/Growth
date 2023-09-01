$(() => {
    let username = $("#username")
    let password = $("#password")
    let firstname = $("#firstname")
    let middlename = $("#middlename")
    let lastname = $("#lastname")
    let nickname = $("#nickname")
    let bio = $("#bio")
    let personalPronouns = $("#personalPronouns")
    let email = $("#email")


    $("#registerForm").on("submit", function (event) {
        event.preventDefault();
        let registerData = {
            "username": username.val(),
            "password": password.val(),
            "firstname": firstname.val(),
            "middlename": middlename.val(),
            "lastname": lastname.val(),
            "nickname": nickname.val(),
            "bio": bio.val(),
            "personalPronouns": personalPronouns.val(),
            "email": email.val()
        }

        $.ajax({
            url: "register",
            type: "post",
            data: registerData,
            success: function () {
                location.href = "account"
            },
            error: function (error) {
                createAlert(error.responseText, AlertTypes.Failure)
            },
        })
    })
})


/**
 * Initiates removal of invalid characters from the user bio when input is changed.
 */
$('#bio').on('input propertychange', function() {
    let bio = $("#bio")
    bio.val(cleanInput(bio.val()));
});


/**
 * Checks input string for invalid characters, and creates an alert for the user if invalid characters are present.
 *
 * @param string The string to be checked.
 * @returns {*} The input string with invalid characters removed.
 */
function cleanInput (string) {
    const regex = /[\u2700-\u27bf]|(?:\ud83c[\udde6-\uddff]){2}|[\ud800-\udbff][\udc00-\udfff]|[\u0023-\u0039]\ufe0f?\u20e3|\u3299|\u3297|\u303d|\u3030|\u24c2|\ud83c[\udd70-\udd71]|\ud83c[\udd7e-\udd7f]|\ud83c\udd8e|\ud83c[\udd91-\udd9a]|\ud83c[\udde6-\uddff]|\ud83c[\ude01-\ude02]|\ud83c\ude1a|\ud83c\ude2f|\ud83c[\ude32-\ude3a]|\ud83c[\ude50-\ude51]|\u203c|\u2049|[\u25aa-\u25ab]|\u25b6|\u25c0|[\u25fb-\u25fe]|\u00a9|\u00ae|\u2122|\u2139|\ud83c\udc04|[\u2600-\u26FF]|\u2b05|\u2b06|\u2b07|\u2b1b|\u2b1c|\u2b50|\u2b55|\u231a|\u231b|\u2328|\u23cf|[\u23e9-\u23f3]|[\u23f8-\u23fa]|\ud83c\udccf|\u2934|\u2935|[\u2190-\u21ff]/g;
    if (regex.test(string)) {
        createAlert("Invalid character", AlertTypes.Failure)
    }
    return string.replace(regex, '');
}