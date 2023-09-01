package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.model.domain.repositories.GitRepoRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.repositories.GitRepository;
import nz.ac.canterbury.seng302.portfolio.service.RegexPattern;
import nz.ac.canterbury.seng302.portfolio.service.grpc.GroupsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.GetGroupDetailsRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.GroupDetailsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * The controller for managing requests to edit git repositories and their settings.
 */
@Controller
public class GitRepoController {

    /** For logging the requests related to git repositories. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The repository in which group git repositories are stored. */
    @Autowired
    private GitRepoRepository gitRepoRepository;

    /** For making gRpc requests to the IdP. */
    @Autowired
    private GroupsClientService groupsClientService;


    /**
     * Mapping for a post request to add a git repository to a group. Restricted to group members,teachers, and admin.
     * The method checks that the given group Id is valid, and then creates a git repository object using the provided
     * group Id, project Id (the Id of the git project), git repository alias, and git repository access token. The
     * created repository is then saved to the git repository repository: the repository which stores git repositories.
     * The created git repository is then returned, with an OK message.
     *
     * If the group Id is not valid or the user is not a member of the group, an exception is thrown and an HTTP
     * response with a BAD REQUEST status is returned.
     *
     * @param groupId     The Id of the group to which the created git repository belongs.
     * @param projectId   The project Id of the git repository.
     * @param alias       The user-defined alias for the git repository.
     * @param accessToken The access token of the git repository.
     * @return A response entity indicating success or an error. On success, also return the created git repository.
     */
    @PostMapping("/editGitRepo")
    public ResponseEntity<Object> addGitRepo(
            @RequestParam Integer groupId,
            @RequestParam Integer projectId,
            @RequestParam String alias,
            @RequestParam String accessToken) {
        logger.info("POST REQUEST /gitRepo/add - attempt to add git repo {} to group {}", alias, groupId);

        try {
            if (alias.isBlank()) {
                throw new CheckException("The repository name cannot be empty");
            } else if (alias.length() > 100) {
                throw new CheckException("The repository name must be no longer than 100 characters");
            } else if (!accessToken.matches(RegexPattern.GITLAB_TOKEN.getPatternString())) {
                throw new CheckException("Access token" + RegexPattern.GITLAB_TOKEN.getRequirements());
            }

            GitRepository repo = buildRepo(groupId, projectId, alias, accessToken);

            gitRepoRepository.save(repo);
            logger.info("POST /gitRepo/add: Success");
            return new ResponseEntity<>(repo, HttpStatus.OK);

        } catch (CheckException exception) {
            logger.error("ERROR /gitRepo/add - an error occurred while adding git repo {} to group {}", alias, groupId);
            logger.error(exception.getMessage());
            return new ResponseEntity<>("Unable to edit the repository information. \n" + exception.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * Mapping for a get request to retrieving git repositories from a group by group ID.
     * The method checks that the given group Id is valid, and then search for the git repo by group ID.
     * The method does not change anything about git repository.
     *
     * If the group Id is not valid, an exception is thrown and an HTTP response with a BAD REQUEST status is returned.
     *
     * @param groupId     The Id of the group to which the created git repository belongs.
     * @return A response entity indicating success or an error. On success, also return the created git repository.
     */
    @GetMapping("/getRepo")
    public ResponseEntity<Object> getGitRepo(
            @RequestParam Integer groupId) {
        logger.info("GET REQUEST /getRepo - attempt to get git repo on group {}", groupId);
        try {
            //check groupId is correct
            GetGroupDetailsRequest request = GetGroupDetailsRequest.newBuilder()
                    .setGroupId(groupId)
                    .build();
            GroupDetailsResponse groupExistsConfirmation = groupsClientService.getGroupDetails(request);
            if (groupExistsConfirmation.getGroupId() == -1) {
                logger.warn("GET REQUEST /getRepo - group {} not found on the IDP", groupId);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            List<GitRepository> gitRepos = gitRepoRepository.findAllByGroupId(groupId);
            logger.info("GET /getRepo: Success");
            return new ResponseEntity<>(gitRepos, HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("GET REQUEST /getRepo - error occurred getting repo for group {}", groupId);
            logger.error(exception.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Creates or edits a git repository depending on whether one already exists for the group
     *
     * @param groupId The group id to check
     * @param projectId The id of the project to associate the repo with
     * @param alias A string to refer to the repo in a readable way
     * @param accessToken The token to access the repo
     *
     * @return A reference to the repo object (edited or created)
     */
    private GitRepository buildRepo(int groupId, int projectId, String alias, String accessToken) {
        GitRepository repo;
        try {
            repo = gitRepoRepository.findAllByGroupId(groupId).get(0);
            repo.setProjectId(projectId);
            repo.setAlias(alias);
            repo.setAccessToken(accessToken);
        } catch (IndexOutOfBoundsException e) {
            repo = new GitRepository(groupId, projectId, alias, accessToken);
        }
        return repo;
    }
}

