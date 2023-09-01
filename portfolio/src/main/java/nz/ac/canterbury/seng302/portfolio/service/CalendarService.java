package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.domain.projects.deadlines.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.deadlines.DeadlineRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.events.EventRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.milestones.Milestone;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.milestones.MilestoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CalendarService {

    private static final int TOOLTIP_LENGTH = 20;
    private final DeadlineRepository deadlineRepository;
    private final MilestoneRepository milestoneRepository;

    @Autowired
    public CalendarService(DeadlineRepository deadlineRepository, MilestoneRepository milestoneRepository) {
        this.deadlineRepository = deadlineRepository;
        this.milestoneRepository = milestoneRepository;
    }


    /**
     * Retrieved JSONed occasion data for given type of occasion. The data is formatted to populate the calendar with
     * the given type of occasion.
     *
     * @param projectId    The Id of the project to which the occasions belong.
     * @param occasionType The type of occasion for which the data will be retrieved.
     * @return A list containing a mapping of calendar parameter names to occasion values.
     */
    public List<HashMap<String, String>> getOccasionsAsFeed(long projectId, String occasionType) throws Exception {
        List<HashMap<String, String>> occasionsList = null;

        try {
            if ("deadline".equals(occasionType)) {
                occasionsList = countDeadlines(projectId);
            } else if ("milestone".equals(occasionType)) {
                occasionsList = countMilestones(projectId);
            }
        } catch (DateTimeException e) {
            throw new DateTimeException(e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return occasionsList;
    }


    /**
     * Retrieves the deadline data for the calendar.
     *
     * @param projectId The project to which the deadline belong.
     * @return The JSONed deadline data from the given project Ids project
     */
    private List<HashMap<String, String>> countDeadlines(long projectId) {
        HashMap<LocalDate, Integer> occasionsCount = new HashMap<>();
        HashMap<LocalDate, String> occasionsNames = new HashMap<>();

        List<Deadline> deadlines = deadlineRepository.findAllByProjectIdOrderByEndDate(projectId);

        for (Deadline deadline : deadlines) {
            countOccasionsPerDay(occasionsCount, occasionsNames, deadline.getEndDate(), deadline.getName());
        }

        return createOccasionsList(occasionsCount, occasionsNames, "deadlineCalendar");
    }


    /**
     * Retrieves the milestone data for the calendar.
     *
     * @param projectId The project to which the milestones belong.
     * @return The JSONed milestone data from the given project Ids project
     */
    private List<HashMap<String, String>> countMilestones(long projectId) {
        HashMap<LocalDate, Integer> occasionsCount = new HashMap<>();
        HashMap<LocalDate, String> occasionsNames = new HashMap<>();

        List<Milestone> milestones = milestoneRepository.findAllByProjectIdOrderByEndDate(projectId);

        for (Milestone milestone : milestones) {
            countOccasionsPerDay(occasionsCount, occasionsNames, milestone.getEndDate(), milestone.getName());
        }

        return createOccasionsList(occasionsCount, occasionsNames, "milestoneCalendar");
    }


    /**
     * Returns a list containing a mapping of fullcalendar event parameters to their values for the given milestones.
     *
     * @param occasionsCount A mapping of dates to the number of occasions that occur on them, of the given occasion type.
     * @param occasionsNames A mapping of dates to the names of occasions that occur on them, of the given occasion type.
     * @param classNames     The classNames used to style the occasions on the calendar. Either "milestoneCalendar" or "deadlineCalendar".
     * @return The list of JSON occasions.
     */
    private List<HashMap<String, String>> createOccasionsList(Map<LocalDate, Integer> occasionsCount, Map<LocalDate, String> occasionsNames, String classNames) {
        List<HashMap<String, String>> occasionsList = new ArrayList<>();

        for (Map.Entry<LocalDate, Integer> entry : occasionsCount.entrySet()) {
            HashMap<String, String> jsonedOccasion = new HashMap<>();
            jsonedOccasion.put("title", String.valueOf(entry.getValue()));
            jsonedOccasion.put("occasionTitles", occasionsNames.get(entry.getKey()));
            jsonedOccasion.put("classNames", classNames);
            jsonedOccasion.put("content", "");
            jsonedOccasion.put("start", entry.getKey().toString());
            jsonedOccasion.put("end", entry.getKey().toString());
            occasionsList.add(jsonedOccasion);
        }

        return occasionsList;
    }


    /**
     * Retrieves the number of occasions on each day and adds the count to the occasionsCount mapping of dates to
     * occasion counts.
     *
     * @param occasionsCount The mapping of dates to the number of occasions that occur on them.
     * @param occasionNames  The mapping of dates to the occasions that occur on them.
     * @param endDate        The end date of the occasion being added to the occasion count and name mappings.
     * @param name           The name of the occasion being added to the occasion count and name mappings.
     */
    private void countOccasionsPerDay(Map<LocalDate, Integer> occasionsCount, Map<LocalDate, String> occasionNames, LocalDate endDate, String name) {
        Integer countByDate = occasionsCount.get(endDate);
        String namesByDate = occasionNames.get(endDate);
        String lineEnd = "\r";
        if (name.length() > TOOLTIP_LENGTH) {
            lineEnd = "...\r";
        }
        if (countByDate == null) {
            occasionsCount.put(endDate, 1); //add date to map as key
            occasionNames.put(endDate, name.substring(0, Math.min(name.length(), TOOLTIP_LENGTH)) + lineEnd);
        } else {
            countByDate++;
            occasionsCount.replace(endDate, countByDate);
            namesByDate += (name.substring(0, Math.min(name.length(), TOOLTIP_LENGTH)) + lineEnd);
            occasionNames.replace(endDate, namesByDate);
        }
    }
}
