package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.demodata.DataInitialisationManagerPortfolio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class PortfolioApplication {

    /** Initialised the test and setup data for the IdP */
    @Autowired
    DataInitialisationManagerPortfolio dataInitializer;

    /**
     * Initialises test data when application starts
     */
    @EventListener(ApplicationReadyEvent.class)
    public void setup() {
        dataInitializer.initialiseData();
    }

    public static void main(String[] args) {
        SpringApplication.run(PortfolioApplication.class, args);
    }

}
