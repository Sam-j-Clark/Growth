
describe("Adding Valid Weblinks to Evidence", () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit("/evidence").wait(100)
        cy.viewport(1200, 1024)
        cy.get('#createEvidenceButton').click();
    })

    let WEBLINKS_TO_ADD

    function assertPasses() {
        WEBLINKS_TO_ADD.forEach((linkName) => {
            cy.get('#addWeblinkButton').click().wait(100)
            cy.get('#webLinkUrl').wait(100)
                .invoke('val', linkName)
            cy.get('#webLinkName').wait(100)
                .invoke('val', "link name")
            cy.get('#addWeblinkButton').click().wait(100)
            cy.contains("must be a valid").should("not.exist")
        })
    }


    it('Protocol tests for valid weblinks', () => {
        WEBLINKS_TO_ADD = [
            "example","www.google.com",
            "http://example",
            "http://example.com",
            "http://example.com/",
            "http://example.com:/",
            "http://example.com:80/",
            "https://www.google.com"
        ]

        assertPasses()
    })

    it('Domain tests for valid weblinks', () => {
        WEBLINKS_TO_ADD = [
            "i_am_a_website.com",
            "CAPITALLETTERS.com",
            "www.google.com",
            "xn--j6h.com",
            "www.canterbury.ac.nz",
            "csse.canterbury.ac.nz",
            "http://132.181.106.9",
            "http://www.w3.org",
        ]

        assertPasses()
    })

    it('Port tests for valid weblinks', () => {
        WEBLINKS_TO_ADD = [
            "http://example:",
            "http://example:80",
            "http://example:80000"
        ]

        assertPasses()
    })


    it('Path tests for valid weblinks', () => {
        WEBLINKS_TO_ADD = [
            "https://example/",
            "http://example/path",
            "http://example:80000/path",
            "http://www.w3.org/Addressing/",
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "http://localhost:9000/portfolio?projectId=1",
            "http://hello/path/lots-of_chars_T0~.-()test",
            "https://learn.canterbury.ac.nz/login/index.php",
            "https://stackoverflow.com/questions/13009670/prolog-recursive-list-construction",
        ]

        assertPasses()
    })

    it('Query tests for valid weblinks', () => {
        WEBLINKS_TO_ADD = [
            "http://example?a=a",
            "http://example?sam=nerd",
            "http://example?llamas=biggerThanFrogs",
            "http://example?sam=nerd&april=100TimesAsSwag&harrison=27",
        ]

        assertPasses()
    })

    it('Fragment tests for valid weblinks', () => {
        WEBLINKS_TO_ADD = [
            "https://example#",
            "https://example#letters",
            "https://example#numb3ers",
            "https://example#34sdfg-';",
            "http://www.ics.uci.edu/pub/ietf/uri/historical.html#WARNING"
        ]

        assertPasses()
    })
})