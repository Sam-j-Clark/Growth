/**
 * Gets JSON data for all branches in given project
 *
 * @param repoID ID of repository
 * @param accessToken Personal/Project access token
 */
function getBranches(repoID, accessToken) {
    $.get("https://eng-git.canterbury.ac.nz/api/v4/projects/" + repoID + "/repository/branches?access_token=" + accessToken, function (data) {
        return data;
    });
}


/**
 * Gets JSON data for all commits in given project
 *
 * @param repoID ID of repository
 * @param accessToken Personal/Project access token
 * @param callback Function to be called with the commit data when the http request comes back
 * @param errorCallback Function to be called to handle errors thrown by http request
 */
function getCommits(repoID, accessToken, callback, errorCallback) {
    $.ajax({
        url: `https://eng-git.canterbury.ac.nz/api/v4/projects/${repoID}/repository/commits?access_token=${accessToken}`,
        type: "GET",
        success: (response) => {
            callback(response)
        },
        error: () => {
            errorCallback()
        }
    })
}


/**
 * Gets JSON data fora specific commit
 *
 * @param repoID ID of repository
 * @param accessToken Personal/Project access token
 * @param commitSha ID of commit to get JSON data for
 */
function getCommit(repoID, accessToken, commitSha) {
    $.get("https://eng-git.canterbury.ac.nz/api/v4/projects/" + repoID + "/repository/commits/" + commitSha + "?access_token=" + accessToken, function (data) {
        return data;
    });
}


/**
 * Gets JSON data for all members in given project
 *
 * @param repoID ID of repository
 * @param accessToken Personal/Project access token
 */
function getMembers(repoID, accessToken) {
    $.get("https://eng-git.canterbury.ac.nz/api/v4/projects/" + repoID + "/members?access_token=" + accessToken, function (data) {
        return data;
    });
}