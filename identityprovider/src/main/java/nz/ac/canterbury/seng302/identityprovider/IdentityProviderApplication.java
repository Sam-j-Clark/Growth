package nz.ac.canterbury.seng302.identityprovider;

import nz.ac.canterbury.seng302.identityprovider.demodata.DataInitialisationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * The main IdP application class using springboot.
 */
@SpringBootApplication
public class IdentityProviderApplication {

    /** Initialised the test and setup data for the IdP */
    @Autowired
    DataInitialisationManager dataInitialiser;

    /**
     * Initialises test data when the boolean variables are true
     */
    @EventListener(ApplicationReadyEvent.class)
    public void setup() {
        dataInitialiser.initialiseData();
    }


    /**
     * Main method see class documentation.
     *
     * @param args - default main params
     */
    public static void main(String[] args) {
        SpringApplication.run(IdentityProviderApplication.class, args);
    }

}
