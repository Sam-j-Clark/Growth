package nz.ac.canterbury.seng302.portfolio.demodata;

import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.events.EventRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.repositories.GitRepoRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.repositories.GitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The service to initialize the git repository data.
 */
@Service
public class GitRepositoryData {

    /** Logs the applications' initialisation process */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The repository containing git repository data */
    private final GitRepoRepository gitRepoRepository;


    @Autowired
    public GitRepositoryData(GitRepoRepository gitRepoRepository) {
        this.gitRepoRepository = gitRepoRepository;
    }


    /**
     * Adds in 2 default git repositories
     */
    public void createGitRepositoriesData() {
        logger.info("Creating default git repositories");
        GitRepository repo1 = new GitRepository(3, 13661, "Team 0's git Repository", "szMkVx_xM39gB5yRxSmL");
        gitRepoRepository.save(repo1);
        GitRepository repo2 = new GitRepository(4, 13737, "Team 1's git Repository", "ixgv4UTo--zGZ5Km1rQ");
        gitRepoRepository.save(repo2);
    }
}
