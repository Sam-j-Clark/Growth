
describe("Adding Invalid Weblinks to Evidence", () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit("/evidence").wait(100)
        cy.viewport(1200, 1024)
        cy.get('#createEvidenceButton').click();
    })

    let WEBLINKS_TO_ADD

    function assertFails() {
        WEBLINKS_TO_ADD.forEach((linkName) => {
            cy.get('#addWeblinkButton').click().wait(100)
            cy.get('#webLinkUrl').wait(100)
                .invoke('val', linkName)
            cy.get('#webLinkName').wait(100)
                .invoke('val', "link name")
            cy.get('#addWeblinkButton').click().wait(100)
            cy.contains("Weblink address").should("be.visible")
        })
    }


    it('Protocol tests for invalid weblinks', () => {
        WEBLINKS_TO_ADD = [
            ".",
            "urn:oasis:names:specification:docbook:dtd:xml:4.1.2",
            "ftp://ftp.is.co.za/rfc/rfc1808.txt",
            "tel:+1-816-555-1212",
            "telnet://192.0.2.16:80/",
            "mailto:John.Doe@example.com",
            "news:comp.infosystems.www.servers.unix",
            "://example",
            "htt://example",
            "htp://example",
            "http:/example",
            "http:///example",
            "https:///example",
            "hps://",
            "https:/example"
        ]

        assertFails()
    })

    it('Domain tests for invalid weblinks', () => {
        WEBLINKS_TO_ADD = [
            ".example",
            "i am a website.com",
            "'quote'.com",
            "$$$money$$$.nz",
            "@.com",
            "!.com",
            "♨️.com"
        ]

        assertFails()
    })

    it('Port tests for invalid weblinks', () => {
        WEBLINKS_TO_ADD = [
            "example:800000",
            "http://example:800000",
            "example.com:port",
            "example.com:@#$%^&"
        ]

        assertFails()
    })


    it('Path tests for invalid weblinks', () => {
        WEBLINKS_TO_ADD = [
            "https://",
            "https://example?"
        ]

        assertFails()
    })

    it('Query tests for invalid weblinks', () => {
        WEBLINKS_TO_ADD = [
            "http://example?a=",
            "http://example?",
            "http://example?=biggerThanFrogs"
        ]

        assertFails()
    })
})