package nz.ac.canterbury.seng302.identityprovider.demodata;

import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.model.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.service.PasswordEncryptionException;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestUserData {

    /** Enables us to directly inject test users into the database*/
    @Autowired
    UserRepository repository;

    /** Logs the applications' initialisation process */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * Adds the default admin user
     */
    public void addAdminAccount() throws PasswordEncryptionException {
        logger.info("Initialising Admin user");
        User admin = new User(
                "admin",
                "password",
                "John",
                "McSteves",
                "Wayne",
                "Stev",
                "Hello! my name is John and I am your course administrator!",
                "He/Him",
                "steve@gmail.com"
        );
        admin.addRole(UserRole.COURSE_ADMINISTRATOR);
        repository.save(admin);
    }


    /**
     * Adds the 30 default test users
     */
    public void addTestUsers() throws PasswordEncryptionException {
        String malePronouns = "He/Him";
        String femalePronouns = "She/Her";
        String defaultPassword = "password";

        logger.info("Initialising test users");
        User tempUser = new User(
                "steve",
                defaultPassword,
                "Steve",
                "",
                "Steveson",
                "Stev",
                "My name is Steve. I am a teacher",
                malePronouns,
                "steve@gmail.com"
        );
        tempUser.addRole(UserRole.TEACHER);
        repository.save(tempUser);

        tempUser = new User(
                "spaghetti",
                defaultPassword,
                "Anna",
                "",
                "Steveson",
                "Anne",
                "",
                femalePronouns,
                "anna@gmail.com"
        );
        tempUser.addRole(UserRole.TEACHER);
        repository.save(tempUser);

        tempUser = new User(
                "Brenda.Bren",
                defaultPassword,
                "Brenda",
                "",
                "Bren",
                "Brenny",
                "Big fan of teaching. Love hiking over the weekend",
                femalePronouns,
                "brenda@hotmail.com"
        );
        tempUser.addRole(UserRole.TEACHER);
        repository.save(tempUser);

        tempUser = new User(
                "G-Man",
                defaultPassword,
                "Greg",
                "",
                "Whitelock",
                "greggs",
                "Everything the Greg way",
                malePronouns,
                "greggs@gmail.com"
        );
        tempUser.addRole(UserRole.TEACHER);
        repository.save(tempUser);

        tempUser = new User(
                "Emma",
                defaultPassword,
                "Emma",
                "",
                "Johnson",
                "ems",
                "",
                femalePronouns,
                "emma.johnson@gmail.com"
        );
        tempUser.addRole(UserRole.TEACHER);
        repository.save(tempUser);

        tempUser = new User(
                "Robert.abe1989",
                "thib2eCuTh",
                "Robert",
                "Martin",
                "Lawrence",
                "Rob",
                "Musicaholic. Proud problem solver. Travel practitioner. Writer. Internet trailblazer.",
                malePronouns,
                "kale.kovace6@yahoo.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "finagle",
                defaultPassword,
                "Pamela",
                "Johnathon",
                "North",
                "Pam",
                "Wannabe internet fanatic. Entrepreneur. Evil troublemaker. Coffee guru. Freelance communicator. Total beer fan.",
                femalePronouns,
                "johnathon2006@gmail.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "Walter.harber",
                "doopoo2Ah",
                "Walter",
                "Dave",
                "Nightingale",
                "Walt",
                "Social media specialist. Amateur creator. Avid twitter fan. Friendly coffee buff. Proud explorer.",
                malePronouns,
                "clark1996@yahoo.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "RonnieNick",
                "mobahz4Nae",
                "Ronnie",
                "Liam",
                "Hughes",
                "Ron",
                "Alcohol geek. Total communicator. Problem solver. Analyst. Incurable zombie fanatic.",
                malePronouns,
                "Ronnie1972@yahoo.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "Shirley.reilly",
                "mobahz4Nae",
                "Shirley",
                "Jade",
                "Snyder",
                "Shir",
                "Student. Hipster-friendly food buff. Incurable music nerd. Internet practitioner. Tv scholar.",
                femalePronouns,
                "arch2001@hotmail.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "Katrina.Crawford",
                "gieheQui2",
                "Katrina",
                "Betty",
                "Crawford",
                "Kat",
                "Food ninja. Typical explorer. Award-winning coffee maven. Social media trailblazer. Freelance zombie scholar. Beer nerd. Introvert.",
                femalePronouns,
                "keyon.moscis@yahoo.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "Eric.feest",
                "ruYahf0Oo",
                "Eric",
                "Matthew",
                "Brown",
                "Matt",
                "Food expert. Extreme internet aficionado. Typical problem solver. Web guru.",
                malePronouns,
                "robin_lin8@gmail.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "elia_Shirley",
                "joogh1Eyei",
                "Shirley",
                "Betty",
                "Swanson",
                "Shir",
                "Beer fanatic. Twitter enthusiast. Internet expert. Unapologetic web evangelist. Tv practitioner. Food fan.",
                femalePronouns,
                "jarred1996@hotmail.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "Blackgod47",
                "joogh09Eyei",
                "Casey",
                "Dale",
                "Arroyo",
                "Case",
                "Introvert. Internet junkie. Hardcore food maven. Problem solver. Typical thinker.",
                malePronouns,
                "marianna2009@hotmail.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "adrienne_m1987",
                "joogh0Eyei",
                "Irvin",
                "John",
                "Stuart",
                "Irv",
                "Avid writer. Social media guru. Web geek. Pop culture fan. Problem solver. Wannabe twitter junkie. Student.",
                malePronouns,
                "christ.bosc6@yahoo.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "gileadite",
                "vee4eiTaeph",
                "Joseph",
                "Noah",
                "Haywood",
                "Jo",
                "Total zombieaholic. Lifelong beer lover. Food fan. Travel enthusiast. Alcohol evangelist. Incurable tv scholar. Amateur social media nerd.",
                malePronouns,
                "kenyon.volkm@yahoo.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "maximo.bec1970",
                "kiboMoh6doo",
                "Debra",
                "Olivia",
                "Jones",
                "Deb",
                "Creator. Tv evangelist. Hardcore alcohol enthusiast. Avid web advocate. Entrepreneur. Award-winning twitter fanatic.",
                femalePronouns,
                "alexys1979@gmail.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "marjory_kl1981",
                "Gah4leech8",
                "Lucille",
                "Emma",
                "Hurt",
                "Lucy",
                "Prone to fits of apathy. Certified internet maven. Zombie fanatic. Typical creator. Troublemaker. Travel lover.",
                femalePronouns,
                "dolly.vander@hotmail.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "giles",
                "po6ohth8Xi0",
                "Amy",
                "Charlotte",
                "Smith",
                "Ames",
                "Introvert. Friendly tv lover. Music enthusiast. Communicator. Incurable problem solver.",
                femalePronouns,
                "gunner_croo@yahoo.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "wendy_mohr",
                "pij7Eegahshei",
                "Stephen",
                "Oliver",
                "Acosta",
                "Steve",
                "Subtly charming troublemaker. Devoted student. Certified web enthusiast. Avid reader.",
                malePronouns,
                "baby_osinsk4@yahoo.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "verda",
                "aFeivee2ae",
                "Curtis",
                "Elijah",
                "Cheney",
                "Curt",
                "Web specialist. Infuriatingly humble beer buff. Entrepreneur. Bacon maven. Food junkie. Certified organizer",
                malePronouns,
                "gwen_klock2@hotmail.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "jamarcus",
                "eeKie1ooP",
                "Anthony",
                "James",
                "Look",
                "Tony",
                "Food scholar. Internet aficionado. Typical twitter enthusiast. Devoted student. Beer advocate.",
                malePronouns,
                "fern_kutc6@gmail.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "fidel",
                "ro1Hei2aet",
                "Michelle",
                "Amelia",
                "Mahaney",
                "Micky",
                "Subtly charming pop culture junkie. Certified twitter ninja. Student. Web fanatic.",
                femalePronouns,
                "maynard.gaylo@gmail.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "abdullah",
                "Iengoh0bu",
                "Kenneth",
                "William",
                "Tillman",
                "Ken",
                "Pop culture junkie. Tv fanatic. Award-winning music lover. Problem solver. Coffee practitioner.",
                malePronouns,
                "kaden1973@yahoo.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "immanuel.z1983",
                "ip0Aefai",
                "James",
                "Benjamin",
                "Rosa",
                "JJ",
                "Coffee fanatic. Incurable explorer. Future teen idol. Troublemaker. Tv evangelist. Proud beer maven.",
                malePronouns,
                "lewis1973@yahoo.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "godfrey",
                "ungeeD6foch5",
                "John",
                "Lucas",
                "Fletcher",
                "Johnny",
                "Prone to fits of apathy. Passionate student. Professional beer buff. Unapologetic internet fanatic.",
                malePronouns,
                "kristian2014@gmail.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "tyler88",
                "jo4airie8Ie",
                "Michael",
                "Henry",
                "Caldwell",
                "Mike",
                "Thinker. Freelance zombie fanatic. Tv trailblazer. Writer. Infuriatingly humble troublemaker.",
                malePronouns,
                "julian2013@gmail.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "rubie.ocon2003",
                "lohBeRio3",
                "William",
                "Theodore",
                "Meier",
                "Will",
                "Travel maven. Music fanatic. Hardcore writer. Analyst. Friendly coffee junkie. Food guru.",
                malePronouns,
                "ernest_mill0@gmail.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "Josette",
                "eth2faaHief",
                "Josette",
                "Amelia",
                "Schrum",
                "Jo",
                "Organizer. Incurable troublemaker. Typical internetaholic. Explorer. Introvert. Social media trailblazer.",
                femalePronouns,
                "candace.herz@hotmail.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "milan_stol1996",
                "waiKai8u",
                "Ray",
                "Liam",
                "Anderson",
                "Andy",
                "Introvert. Beer enthusiast. Falls down a lot. Pop culture scholar. Hipster-friendly music advocate.",
                malePronouns,
                "madonna.pri@yahoo.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "murl",
                "aiFahxei9gah",
                "Diane",
                "Isabella",
                "Bishop",
                "Dee",
                "Proud beeraholic. Unapologetic pop culture advocate. Tv lover. Hardcore zombie enthusiast. Problem solver. Creator.",
                femalePronouns,
                "antwan.herm@yahoo.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "sagedaniel",
                "iPeeW7iemae",
                "Matthew",
                "Noah",
                "Richard",
                "Matt",
                "Bacon specialist. Coffee ninja. Internet guru. Friendly tv fan. Twitter fanatic. Subtly charming social media advocate. Pop culture geek.",
                malePronouns,
                "roberto.boe@hotmail.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "singlehandedly",
                "eigie3Tue",
                "Dorothy",
                "Sophia",
                "Smallwood",
                "Sophie",
                "Infuriatingly humble music evangelist. Evil web trailblazer. Explorer. Social media nerd.",
                femalePronouns,
                "king1987@yahoo.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);

        tempUser = new User(
                "lavonne.do1975",
                "uNe7naing",
                "Alexander",
                "Oliver",
                "Fuller",
                "Alex",
                "Friendly food junkie. Lifelong introvert. Student. Avid coffee scholar. Unapologetic travel specialist. Zombie buff.",
                malePronouns,
                "rosalia1975@gmail.com"
        );
        tempUser.addRole(UserRole.STUDENT);
        repository.save(tempUser);
    }

}
