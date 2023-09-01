package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.model.domain.preferences.UserPrefRepository;
import nz.ac.canterbury.seng302.portfolio.service.PaginationService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.PaginationRequestOptions;
import nz.ac.canterbury.seng302.shared.util.PaginationResponseOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.ui.Model;

import java.util.*;

import static org.mockito.Mockito.*;

@SpringBootTest
class UserListControllerTest {

    private static final UserAccountsClientService mockClientService = mock(UserAccountsClientService.class);


    private static final PaginationService paginationService = spy(PaginationService.class);


    private static final UserPrefRepository userPrefRepository = mock(UserPrefRepository.class);


    private static final UserListController userListController = new UserListController(mockClientService, userPrefRepository, paginationService);
    private final ArrayList<UserResponse> expectedUsersList = new ArrayList<>();
    private final Authentication principal = new Authentication(AuthState.newBuilder().addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build()).build());





    /** used to set the values for the tests, this number should be the same as the value in the UserListController **/
    private Integer usersPerPage = 10;

    /** First Name Comparator, has other name fields after to decide order if first names are the same*/
    Comparator<UserResponse> compareByFirstName = Comparator.comparing((UserResponse user) ->
            (user.getFirstName().toLowerCase(Locale.ROOT) + ' ' +
                    user.getMiddleName().toLowerCase(Locale.ROOT) + ' ' +
                    user.getLastName().toLowerCase(Locale.ROOT)));

    /** Middle Name Comparator, has other name fields after to decide order if middle names are the same */
    Comparator<UserResponse> compareByMiddleName = Comparator.comparing((UserResponse user) ->
            (user.getMiddleName().toLowerCase(Locale.ROOT) + ' ' +
                    user.getFirstName().toLowerCase(Locale.ROOT) + ' ' +
                    user.getLastName().toLowerCase(Locale.ROOT)));

    /** Last Name Comparator, has other name fields after to decide order if last names are the same */
    Comparator<UserResponse> compareByLastName = Comparator.comparing((UserResponse user) ->
            (user.getLastName().toLowerCase(Locale.ROOT) + ' ' +
                    user.getFirstName().toLowerCase(Locale.ROOT) + ' ' +
                    user.getMiddleName().toLowerCase(Locale.ROOT)));

    /** Username Comparator */
    Comparator<UserResponse> compareByUsername = Comparator.comparing((UserResponse user) ->
            (user.getUsername().toLowerCase(Locale.ROOT)));

    /** Alias Comparator, has name fields afterwards to decide order if the aliases are the same */
    Comparator<UserResponse> compareByAlias = Comparator.comparing((UserResponse user) ->
            (user.getNickname().toLowerCase(Locale.ROOT) + ' ' +
                    user.getFirstName().toLowerCase(Locale.ROOT) + ' ' +
                    user.getMiddleName().toLowerCase(Locale.ROOT) + ' ' +
                    user.getLastName().toLowerCase(Locale.ROOT)));

    /** UserRoles Comparator */
    Comparator<UserResponse> compareByRole = (userOne, userTwo) -> {
        String userOneRoles = userOne.getRolesValueList().toString();
        String userTwoRoles = userTwo.getRolesValueList().toString();
        return userOneRoles.compareTo(userTwoRoles);
    };

    private static final Model model = setMockModel();


    @BeforeEach
    void beforeAll() {
        expectedUsersList.clear();
        usersPerPage = 10;
        UserResponse.Builder user = UserResponse.newBuilder();
        user.setUsername("steve")
                .setFirstName("Steve")
                .setMiddleName("McSteve")
                .setLastName("Steveson")
                .setNickname("Stev")
                .setBio("kdsflkdjf")
                .setPersonalPronouns("Steve/Steve")
                .setEmail("steve@example.com")
                .setProfileImagePath("a");
        user.addRoles(UserRole.STUDENT);
        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), mockClientService)).thenReturn(user.build());
        addUsersToExpectedList(0, usersPerPage * 4 + 1);
        userPrefRepository.deleteAll();
        userListController.setPrefRepository(userPrefRepository);
    }


    /**
     * adds dummy users from a lower bound to an upper bound in order to test with multiple pages
     *
     * @param min the minimum number, used to ensure that there is no repeats of usernames
     * @param max the maximum number of users to be used for testing
     */
    private void addUsersToExpectedList(int min, int max) {
        for (int i = min; i < max; i++) {
            UserResponse.Builder user = UserResponse.newBuilder();
            user.setUsername("steve" + i)
                    .setFirstName("Steve" + i)
                    .setMiddleName("McSteve" + i)
                    .setLastName("Steveson" + i)
                    .setNickname("Stev" + i)
                    .setBio("kdsflkdjf")
                    .setPersonalPronouns("Steve/Steve")
                    .setEmail("steve@example.com");
            user.addRoles(UserRole.STUDENT);
            expectedUsersList.add(user.build());
        }
    }

    /**
     * Creates a mock response for a specific users per page limit and for an offset. Mocks the server side service of
     * retrieving the users from the repository
     *
     * @param offset the offset of where to start getting users from in the list, used for paging
     */
    private void createMockResponse(int offset, String sortOrder, String isAscending, String usersPerPage) {
        boolean boolAscending = Objects.equals(isAscending, "true");

        if (!(usersPerPage == null)){
            switch(usersPerPage){
                case "10" -> this.usersPerPage = 10;
                case "20" -> this.usersPerPage = 20;
                case "40" -> this.usersPerPage = 40;
                case "60" -> this.usersPerPage = 60;
                case "all" -> this.usersPerPage = 999999999;
            }
        }

        PaginationRequestOptions options = PaginationRequestOptions.newBuilder()
                                                                   .setOrderBy(sortOrder)
                                                                   .setOffset(offset)
                                                                   .setLimit(this.usersPerPage)
                                                                   .setIsAscendingOrder(boolAscending)
                                                                   .build();

        GetPaginatedUsersRequest request = GetPaginatedUsersRequest.newBuilder()
                .setPaginationRequestOptions(options)
                .build();

        PaginatedUsersResponse.Builder response = PaginatedUsersResponse.newBuilder();

        switch (sortOrder) {
            case "roles" -> expectedUsersList.sort(compareByRole);
            case "username" -> expectedUsersList.sort(compareByUsername);
            case "aliases" -> expectedUsersList.sort(compareByAlias);
            case "middlename" -> expectedUsersList.sort(compareByMiddleName);
            case "lastname" -> expectedUsersList.sort(compareByLastName);
            default -> expectedUsersList.sort(compareByFirstName);
        }

        if (!boolAscending) {
            Collections.reverse(expectedUsersList);
        }

        for (int i = offset; ((i - offset) < this.usersPerPage) && (i < expectedUsersList.size()); i++) {
            response.addUsers(expectedUsersList.get(i));
        }

        PaginationResponseOptions responseOptions = PaginationResponseOptions.newBuilder()
                                                                             .setResultSetSize(expectedUsersList.size())
                                                                             .build();

        response.setPaginationResponseOptions(responseOptions);
        when(mockClientService.getPaginatedUsers(request)).thenReturn(response.build());
    }


    @Test
    void contextLoads() {
        Assertions.assertNotNull(userListController);
    }


    @Test
    void loadFirstPage() {
        createMockResponse(0, "firstname", "true", null);
        userListController.getUserList(principal, model, 1, "firstname", "true", usersPerPage.toString());
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> userList = userListController.getUserResponseList();
        List<Integer> footerSequence = userListController.getFooterSequence();
        String sortOrder = userListController.getSortOrder();

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 1;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();
        String expectedSortOrder = "firstname";
        List<Integer> expectedFooterSequence = Arrays.asList(1, 2, 3, 4, 5);


        Assertions.assertEquals(expectedTotalPages, totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, userList.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), userList.toString());
        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
        Assertions.assertEquals(expectedSortOrder, sortOrder);
    }


    @Test
    void loadLastPage() {
        createMockResponse(usersPerPage * 4, "firstname", "true", null);
        userListController.getUserList(principal, model, 5, "firstname", "true", usersPerPage.toString());
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> userList = userListController.getUserResponseList();
        List<Integer> footerSequence = userListController.getFooterSequence();
        String sortOrder = userListController.getSortOrder();

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(usersPerPage * 4, usersPerPage * 4 + 1);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 5;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();
        String expectedSortOrder = "firstname";
        List<Integer> expectedFooterSequence = Arrays.asList(1, 2, 3, 4, 5);


        Assertions.assertEquals(expectedTotalPages, totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, userList.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), userList.toString());
        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
        Assertions.assertEquals(expectedSortOrder, sortOrder);
    }


    @Test
    void loadThirdPage() {
        createMockResponse(usersPerPage * 2, "firstname", "true", null);
        userListController.getUserList(principal, model, 3, "firstname", "true", usersPerPage.toString());
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> userList = userListController.getUserResponseList();
        List<Integer> footerSequence = userListController.getFooterSequence();
        String sortOrder = userListController.getSortOrder();

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(usersPerPage * 2, usersPerPage * 3);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 3;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();
        String expectedSortOrder = "firstname";
        List<Integer> expectedFooterSequence = Arrays.asList(1, 2, 3, 4, 5);


        Assertions.assertEquals(expectedTotalPages, totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, userList.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), userList.toString());
        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
        Assertions.assertEquals(expectedSortOrder, sortOrder);
    }


    @Test
    void loadLastPagePlusOne() {
        createMockResponse(usersPerPage * 5, "firstname", "true", null); //needed so controller can see the total pages amount
        createMockResponse(usersPerPage * 4, "firstname", "true", null);
        userListController.getUserList(principal, model, 6, "firstname", "true", usersPerPage.toString());
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> userList = userListController.getUserResponseList();
        List<Integer> footerSequence = userListController.getFooterSequence();
        String sortOrder = userListController.getSortOrder();

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(usersPerPage * 4, usersPerPage * 4 + 1);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 5;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();
        String expectedSortOrder = "firstname";
        List<Integer> expectedFooterSequence = Arrays.asList(1, 2, 3, 4, 5);


        Assertions.assertEquals(expectedTotalPages, totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, userList.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), userList.toString());
        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
        Assertions.assertEquals(expectedSortOrder, sortOrder);
    }


    @Test
    void loadZeroPageNumber() {
        createMockResponse(0, "firstname", "true", null);
        userListController.getUserList(principal, model, 0, "firstname", "true", usersPerPage.toString());
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> userList = userListController.getUserResponseList();
        List<Integer> footerSequence = userListController.getFooterSequence();
        String sortOrder = userListController.getSortOrder();

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 1;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();
        String expectedSortOrder = "firstname";
        List<Integer> expectedFooterSequence = Arrays.asList(1, 2, 3, 4, 5);


        Assertions.assertEquals(expectedTotalPages, totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, userList.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), userList.toString());
        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
        Assertions.assertEquals(expectedSortOrder, sortOrder);
    }


    @Test
    void loadNegativePageNumber() {
        createMockResponse(0, "firstname", "true", null);
        userListController.getUserList(principal, model, -1, "firstname", "true", usersPerPage.toString());
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> userList = userListController.getUserResponseList();
        List<Integer> footerSequence = userListController.getFooterSequence();
        String sortOrder = userListController.getSortOrder();

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 1;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();
        String expectedSortOrder = "firstname";
        List<Integer> expectedFooterSequence = Arrays.asList(1, 2, 3, 4, 5);

        Assertions.assertEquals(expectedTotalPages, totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, userList.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), userList.toString());
        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
        Assertions.assertEquals(expectedSortOrder, sortOrder);
    }


    @Test
    void footerNumberSequenceLessThanElevenPages() {
        createMockResponse(0, "firstname", "true", null);
        userListController.getUserList(principal, model, 1, null, null, usersPerPage.toString());
        List<Integer> footerSequence = userListController.getFooterSequence();
        List<Integer> expectedFooterSequence = Arrays.asList(1, 2, 3, 4, 5);

        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
    }


    @Test
    void footerNumberSequencePage10GreaterThan16Pages() {
        addUsersToExpectedList(usersPerPage  * 4 + 2, usersPerPage * 18);
        createMockResponse(usersPerPage * 9, "firstname", "true", null);
        userListController.getUserList(principal, model, 10, "firstname", "true", usersPerPage.toString());
        List<Integer> footerSequence = userListController.getFooterSequence();
        ArrayList<Integer> expectedFooterSequence = new ArrayList<>();
        for (int i = 5; i <= 15; i++) {
            expectedFooterSequence.add(i);
        }

        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
    }


    @Test
    void footerNumberSequencePage10LessThan16Pages() {
        addUsersToExpectedList(usersPerPage  * 4 + 2, usersPerPage * 13);
        createMockResponse(usersPerPage * 9, "firstname", "true", null);
        userListController.getUserList(principal, model, 10, "firstname", "true", usersPerPage.toString());
        List<Integer> footerSequence = userListController.getFooterSequence();
        ArrayList<Integer> expectedFooterSequence = new ArrayList<>();
        for (int i = 3; i <= 13; i++) {
            expectedFooterSequence.add(i);
        }

        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
    }


    @Test
    void sortByFirstNameIncreasing() {
        createMockResponse(0, "firstname", "true", null);
        userListController.getUserList(principal, model, 1, "firstname", "true", usersPerPage.toString());
        List<UserResponse> userList = userListController.getUserResponseList();
        expectedUsersList.sort(compareByFirstName);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, userList);
    }


    @Test
    void sortByFirstNameDecreasing() {
        createMockResponse(0, "firstname", "false", null);
        userListController.getUserList(principal, model, 1, "firstname", "false", usersPerPage.toString());
        List<UserResponse> userList = userListController.getUserResponseList();
        expectedUsersList.sort(compareByFirstName);
        Collections.reverse(expectedUsersList);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, userList);
    }


    @Test
    void sortByMiddleNameIncreasing() {
        createMockResponse(0, "middlename", "true", null);
        userListController.getUserList(principal, model, 1, "middlename", "true", usersPerPage.toString());
        List<UserResponse> userList = userListController.getUserResponseList();
        expectedUsersList.sort(compareByMiddleName);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, userList);
    }


    @Test
    void sortByMiddleNameDecreasing() {
        createMockResponse(0, "middlename", "false", null);
        userListController.getUserList(principal, model, 1, "middlename", "false", usersPerPage.toString());
        List<UserResponse> userList = userListController.getUserResponseList();
        expectedUsersList.sort(compareByMiddleName);
        Collections.reverse(expectedUsersList);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, userList);
    }


    @Test
    void sortByLastNameIncreasing() {
        createMockResponse(0, "lastname", "true", null);
        userListController.getUserList(principal, model, 1, "lastname", "true", usersPerPage.toString());
        List<UserResponse> userList = userListController.getUserResponseList();
        expectedUsersList.sort(compareByLastName);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, userList);
    }


    @Test
    void sortByLastNameDecreasing() {
        createMockResponse(0, "lastname", "false", null);
        userListController.getUserList(principal, model, 1, "lastname", "false", usersPerPage.toString());
        List<UserResponse> userList = userListController.getUserResponseList();
        expectedUsersList.sort(compareByLastName);
        Collections.reverse(expectedUsersList);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, userList);
    }


    @Test
    void sortByUsernameIncreasing() {
        createMockResponse(0, "username", "true", null);
        userListController.getUserList(principal, model, 1, "username", "true", usersPerPage.toString());
        List<UserResponse> userList = userListController.getUserResponseList();
        expectedUsersList.sort(compareByUsername);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, userList);
    }


    @Test
    void sortByUsernameDecreasing() {
        createMockResponse(0, "username", "false", null);
        userListController.getUserList(principal, model, 1, "username", "false", usersPerPage.toString());
        List<UserResponse> userList = userListController.getUserResponseList();
        expectedUsersList.sort(compareByUsername);
        Collections.reverse(expectedUsersList);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, userList);
    }


    @Test
    void sortByAliasIncreasing() {
        createMockResponse(0, "aliases", "true", null);
        userListController.getUserList(principal, model, 1, "aliases", "true", usersPerPage.toString());
        List<UserResponse> userList = userListController.getUserResponseList();
        expectedUsersList.sort(compareByAlias);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, userList);
    }


    @Test
    void sortByAliasDecreasing() {
        createMockResponse(0, "aliases", "false", null);
        userListController.getUserList(principal, model, 1, "aliases", "false", usersPerPage.toString());
        List<UserResponse> userList = userListController.getUserResponseList();
        expectedUsersList.sort(compareByAlias);
        Collections.reverse(expectedUsersList);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, userList);
    }


    @Test
    void sortByRolesIncreasing() {
        createMockResponse(0, "roles", "true", null);
        userListController.getUserList(principal, model, 1, "roles", "true", usersPerPage.toString());
        List<UserResponse> userList = userListController.getUserResponseList();
        expectedUsersList.sort(compareByRole);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, userList);
    }


    @Test
    void sortByRolesDecreasing() {
        createMockResponse(0, "roles", "false", null);
        userListController.getUserList(principal, model, 1, "roles", "false", usersPerPage.toString());
        List<UserResponse> userList = userListController.getUserResponseList();
        expectedUsersList.sort(compareByRole);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, userList);
    }


    @Test
    void sortOrderDefaultToNameIncreasing() {
        createMockResponse(0, "firstname", "true", null);
        String expectedDefaultSortOrder = "firstname";
        userListController.getUserList(principal, model, 1, null, null, usersPerPage.toString());
        String sortOrder = userListController.getSortOrder();
        boolean isAscending = userListController.getIsAscending();

        Assertions.assertEquals(expectedDefaultSortOrder, sortOrder);
        Assertions.assertTrue(isAscending);
    }


    @Test
    void getTenUsersPerPage(){
        createMockResponse(0, "firstname", "true", "10");
        userListController.getUserList(principal, model, 1, null, "true", "10");
        int usersPerPageLimit = userListController.getUsersPerPageLimit();

        Assertions.assertEquals(10, usersPerPageLimit);
    }


    @Test
    void getTwentyUsersPerPage(){
        createMockResponse(0, "firstname", "true", "20");
        userListController.getUserList(principal, model, 1, null, "true", "20");
        int usersPerPageLimit = userListController.getUsersPerPageLimit();

        Assertions.assertEquals(20, usersPerPageLimit);
    }


    @Test
    void getFortyUsersPerPage(){
        createMockResponse(0, "firstname", "true", "40");
        userListController.getUserList(principal, model, 1, null, "true", "40");
        int usersPerPageLimit = userListController.getUsersPerPageLimit();

        Assertions.assertEquals(40, usersPerPageLimit);
    }


    @Test
    void getSixtyUsersPerPage(){
        createMockResponse(0, "firstname", "true", "60");
        userListController.getUserList(principal, model, 1, null, "true", "60");
        int usersPerPageLimit = userListController.getUsersPerPageLimit();

        Assertions.assertEquals(60, usersPerPageLimit);
    }


    @Test
    void getAllUsersPerPage(){
        createMockResponse(0, "firstname", "true", "all");
        userListController.getUserList(principal, model, 1, null, "true", "all");
        int usersPerPageLimit = userListController.getUsersPerPageLimit();

        Assertions.assertEquals(999999999, usersPerPageLimit);
    }


    @Test
    void getInvalidNumberUsersPerPage(){
        createMockResponse(0, "firstname", "true", "50");
        userListController.getUserList(principal, model, 1, null, null, "50");
        int usersPerPageLimit = userListController.getUsersPerPageLimit();

        Assertions.assertEquals(usersPerPage, usersPerPageLimit);
    }


    @Test
    void sortOrderPersistence() {
        String expectedPersistedSortOrder = "role";
        createMockResponse(0, expectedPersistedSortOrder, "false", null);
        userListController.getUserList(principal, model, 1, expectedPersistedSortOrder, "false", usersPerPage.toString());
        String sortOrder = userListController.getSortOrder();
        boolean isAscending = userListController.getIsAscending();
        userListController.getUserList(principal, model, 1, null, null, usersPerPage.toString());

        Assertions.assertEquals(expectedPersistedSortOrder, sortOrder);
        Assertions.assertFalse(isAscending);
    }


    @SuppressWarnings("NullableProblems")
    private static Model setMockModel() {
        return new Model() {

            Object totalPages;
            Object currentPage;
            Object totalItems;
            Object userList;
            Object possibleRoles;
            Object isAscending;

            @Override
            public Model addAttribute(String attributeName, Object attributeValue) {
                switch (attributeName) {
                    case "totalPages" -> totalPages = attributeValue;
                    case "currentPage" -> currentPage = attributeValue;
                    case "totalItems" -> totalItems = attributeValue;
                    case "userList" -> userList = attributeValue;
                    case "possibleRoles" -> possibleRoles = attributeValue;
                    case "isAscending" -> isAscending = attributeValue;
                }
                return null;
            }

            @Override
            public Model addAttribute(Object attributeValue) {
                return null;
            }

            @Override
            public Model addAllAttributes(Collection<?> attributeValues) {
                return null;
            }

            @Override
            public Model addAllAttributes(Map<String, ?> attributes) {
                return null;
            }

            @Override
            public Model mergeAttributes(Map<String, ?> attributes) {
                return null;
            }

            @Override
            public boolean containsAttribute(String attributeName) {
                return false;
            }

            @Override
            public Object getAttribute(String attributeName) {
                Object toReturn = null;
                switch (attributeName) {
                    case "totalPages" -> toReturn = totalPages;
                    case "currentPage" -> toReturn = currentPage;
                    case "totalItems" -> toReturn = totalItems;
                    case "userList" -> toReturn = userList;
                    case "possibleRoles" -> toReturn = possibleRoles;
                    case "isAscending" -> toReturn = isAscending;
                }
                return toReturn;
            }

            @Override
            public Map<String, Object> asMap() {
                return null;
            }
        };
    }
}
