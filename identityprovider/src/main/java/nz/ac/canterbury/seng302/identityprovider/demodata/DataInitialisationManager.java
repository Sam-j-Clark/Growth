package nz.ac.canterbury.seng302.identityprovider.demodata;

import nz.ac.canterbury.seng302.identityprovider.service.PasswordEncryptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initialises the User and Group data used by the application.
 */
@Service
public class DataInitialisationManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Turn on (true) to create the default admin account */
    private static final boolean INCLUDE_ADMIN_ACCOUNT = true;

    /** Turn on (true) to create the 1000 test accounts */
    private static final boolean INCLUDE_TEST_USERS = true;

    /** Turn on (true) to create the test groups */
    private static final boolean INCLUDE_TEST_GROUPS = true;

    /** To add test users. */
    private final TestUserData testUserData;

    /** To add test groups and the default groups. */
    private final TestGroupData testGroupData;

    /**
     * Autowired constructor to inject the required services,
     *
     * @param testUserData - The TestUserData service used for adding initial users data
     * @param testGroupData - The TestUserData service used for adding initial group data
     */
    @Autowired
    public DataInitialisationManager(TestUserData testUserData, TestGroupData testGroupData) {
        this.testUserData = testUserData;
        this.testGroupData = testGroupData;
    }


    /**
     * Delegates the adding of test data, where data is required.
     */
    public void initialiseData() {
        try {
            testGroupData.addDefaultGroups();
            if (INCLUDE_TEST_GROUPS)
                testGroupData.addTestGroups();
            if (INCLUDE_ADMIN_ACCOUNT)
                testUserData.addAdminAccount();
            if (INCLUDE_TEST_USERS)
                testUserData.addTestUsers();
            if (INCLUDE_TEST_USERS && INCLUDE_TEST_GROUPS)
                testGroupData.addUsersToTestGroups();
            testGroupData.setInitialTeachersAndMWAGGroupMembers();
        } catch (PasswordEncryptionException exception) {
            logger.error("ERROR - Could not setup initial data");
            logger.error(exception.getMessage());
        }
    }
}
