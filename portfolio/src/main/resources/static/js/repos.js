/**
 * Performs all the actions required to close the repository details edit form
 */
function cancelRepoEdit() {
    const parent = $("#repoSettingsContainer");
    parent.slideUp(() => {
        const editButton = $(".editRepo");
        editButton.show();
    });
}


/**
 * Event listener for clicking the edit repo button. Opens a form.
 */
$(document).on("click", ".editRepo", () => {
    const editButton = $(".editRepo");
    editButton.hide();
    editButton.tooltip("hide");
    const parent = $("#repoSettingsContainer");

    const maxProjectIdNumber = 2147483647 // Max java integer
    parent.html(
        `<form id="editRepoForm" class="marginSides1">
            <div class="mb-1">
                <label class="form-label">Repository Name (cannot be empty):</label>
                <input type="text" id="repoName" class="form-control countable" 
                required minlength=1 maxlength="100" 
                value="${sanitise($("#groupSettingsPageRepoName").text())}">
                <small class="form-text-counted text-muted countChar">0 characters remaining</small>
            </div>
            <div class="mb-1">
                <label class="form-label">Project ID (must be a number):</label>
                <input type="number" id="projectId" class="form-control" required max=${maxProjectIdNumber} value="${sanitise($("#groupSettingsPageProjectId").text())}">
            </div>
            <div class="mb-1">
                <label class="form-label">Access Token 
                    (<a href="https://docs.gitlab.com/ee/user/project/settings/project_access_tokens.html">more info</a>)
                :</label>
                <svg xmlns="http://www.w3.org/2000/svg"
                     id="tokenHintTooltip"
                     width="16"
                     height="16"
                     fill="currentColor"
                     class="bi bi-question-circle"
                     viewBox="0 0 16 16"
                     data-bs-toggle="tooltip" data-bs-placement="top"
                     data-bs-custom-class="custom-tooltip"
                     data-bs-title="Generate a project access token from the settings tab of your repository. 
                     Your token must have a scope of at least 'read_api'. You can add additional 
                     permissions to the token if you need to, but we do not recommend this for security reasons.">
                    <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                    <path d="M5.255 5.786a.237.237 0 0 0 .241.247h.825c.138 0 .248-.113.266-.25.09-.656.54-1.134 1.342-1.134.686 0 1.314.343 1.314 1.168 0 .635-.374.927-.965 1.371-.673.489-1.206 1.06-1.168 1.987l.003.217a.25.25 0 0 0 .25.246h.811a.25.25 0 0 0 .25-.25v-.105c0-.718.273-.927 1.01-1.486.609-.463 1.244-.977 1.244-2.056 0-1.511-1.276-2.241-2.673-2.241-1.267 0-2.655.59-2.75 2.286zm1.557 5.763c0 .533.425.927 1.01.927.609 0 1.028-.394 1.028-.927 0-.552-.42-.94-1.029-.94-.584 0-1.009.388-1.009.94z"/>
                </svg>
                <input type="text" id="accessToken" class="form-control" 
                required maxlength=20 pattern="${GITLAB_TOKEN}" title="Access Token ${GITLAB_TOKEN_REQUIREMENTS}" 
                value="${sanitise($("#groupSettingsPageAccessToken").text())}">
            </div>
            <div class="mb-3 mt-3">
                <button type="submit" class="btn btn-primary">Save</button>
                <button type="button" class="btn btn-secondary cancelRepoEdit" >Cancel</button>
            </div>
        </form>`
    );
    $('#tokenHintTooltip').tooltip();
    parent.slideDown();
    startCharacterCounting();
})


/**
 * Event listener for the cancel button on the git repo edit form.
 */
$(document).on("click", ".cancelRepoEdit", cancelRepoEdit);


/**
 * Event listener for the submit button
 */
$(document).on("submit", "#editRepoForm", function (event) {
    event.preventDefault();

    const repoData = {
        "groupId": selectedGroupId,
        "projectId": $("#projectId").val(),
        "alias": $("#repoName").val(),
        "accessToken": $("#accessToken").val()
    }

    $.ajax({
        url: "editGitRepo",
        type: "post",
        data: repoData,
        success: function () {
            createAlert("Changes submitted", AlertTypes.Success);
            sendNotification("group", selectedGroupId, "updateGroup");
            cancelRepoEdit();
            retrieveGroupRepoInformation()
        },
        error: (error) => {
            createAlert(error.responseText, AlertTypes.Failure)
        }
    })
})