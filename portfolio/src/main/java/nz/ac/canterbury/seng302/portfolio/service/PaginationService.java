package nz.ac.canterbury.seng302.portfolio.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
/**
 * A service class providing helper methods for pagination.  
 */
@Service
public class PaginationService {


    /**
     * This is used to set the numbers at the bottom of the screen for page navigation. Otherwise, at larger page values
     * it gets very messy. Creates a range of -5 to +5 from the current page if possible.
     *
     * @param footerNumberSequence The current footer number sequence.
     * @param totalPages The total number of pages.
     * @param pageNum The page number that the user is currently on.
     * @return a footer number sequence that will be displayed on the page.
     */
    public ArrayList<Integer> createFooterNumberSequence(ArrayList<Integer> footerNumberSequence, int totalPages, int pageNum) {
        footerNumberSequence.clear();
        int minNumber = 1;
        int maxNumber = 11;
        if (totalPages < 11) {
            maxNumber = totalPages;
        } else if (pageNum > 6) {
            if (pageNum + 5 < totalPages) {
                minNumber = pageNum - 5;
                maxNumber = pageNum + 5;
            } else {
                maxNumber = totalPages;
                minNumber = totalPages - 10;
            }
        }
        for (int i = minNumber; i <= maxNumber; i++) {
            footerNumberSequence.add(i);
        }
        return footerNumberSequence;
    }
}
