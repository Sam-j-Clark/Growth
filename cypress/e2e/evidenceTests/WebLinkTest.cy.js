Cypress.on('uncaught:exception', () => {
    // returning false here prevents Cypress from failing the test due to being unable to read the document
    return false
})

describe("Adding Weblinks to Evidence", () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit("/evidence")
        cy.viewport(1200, 1024)
        cy.get('#createEvidenceButton').click();
    })

    it('Can add max 10 weblinks', () => {
        for (let i = 1; i < 11; i++) {
            cy.get('#addWeblinkButton').click()
            cy.get('#webLinkUrl').wait(100)
                .invoke('val', 'http://www.a.ac.nz')
            cy.get('#webLinkName').wait(100)
                .invoke('val', 'Wl ' + i.toString())
            cy.get('#addWeblinkButton').click()
        }

        cy.get('#webLinkFull').should('be.visible');
    })

    it("Can cancel adding a weblink", () => {
        cy.get("#addWeblinkButton").click()
        cy.get("#cancelWeblinkButton").click()
        cy.get("#addWeblinkButton").should("have.text", "Add Web Link")
        cy.get("#cancelWeblinkButton").should("be.hidden")
    })

    it("Weblink form does not empty on close", () => {
        cy.get("#addWeblinkButton").click();
        cy.get("#webLinkUrl").wait(500)
            .type("https")
        cy.get("#webLinkName").wait(500)
            .type("name")
        cy.get("#cancelWeblinkButton").click()
        cy.get("#addWeblinkButton").click()
        cy.get("#webLinkUrl").should("have.value", "https")
        cy.get("#webLinkName").should("have.value", "name")
    })

    it("Saved weblinks remain on form cancel", () => {
        cy.get("#addWeblinkButton").click();
        cy.get("#webLinkUrl").wait(500)
            .type("https://www.a.ac.nz/")
        cy.get("#webLinkName").wait(500)
            .type("name")
        cy.get("#addWeblinkButton").click()
        cy.get("#addWeblinkButton").click()
        cy.get("#cancelWeblinkButton").click()
        cy.get("#addedWebLinks").should("contain.text", "name")
    })

    it("Warning displayed on invalid address that is too long", () => {
        cy.get("#addWeblinkButton").click();
        cy.get("#webLinkUrl").should("have.attr", "maxlength")
        cy.get("#webLinkUrl").invoke('removeAttr', 'maxlength')
        cy.get("#webLinkUrl").wait(500).invoke('val', "https://www." + "a".repeat(2000) + ".com")
        cy.get("#webLinkName").wait(500)
            .type("Test")
        cy.get("#addWeblinkButton").click()
        cy.get("#evidenceWeblinkAddressFeedback").should("contain", "Weblink address cannot be longer than 2000 characters")
    })

    it("Warning displayed on address in form [something]:/[something]", () => {
        cy.get("#addWeblinkButton").click();
        cy.get("#webLinkUrl").should("have.attr", "maxlength")
        cy.get("#webLinkUrl").invoke('removeAttr', 'maxlength')
        cy.get("#webLinkUrl").wait(500).invoke('val', "http:/www.google.com")
        cy.get("#webLinkName").wait(500)
            .type("Test")
        cy.get("#addWeblinkButton").click()
        cy.get("#evidenceWeblinkAddressFeedback").should("contain", "Weblink address must be a valid URL")
    })

    it("Warning displayed on invalid name", () => {
        cy.get("#addWeblinkButton").click();
        cy.get("#webLinkUrl").wait(500)
            .type("https://www.a.ac.nz/")
        cy.get("#addWeblinkButton").click()
        cy.get("#evidenceWeblinkAddressFeedback").should("not.be.visible")
        cy.get("#evidenceWeblinkNameFeedback").should("contain", "Weblink name must not be empty")
        cy.get('#webLinkName:invalid').invoke('prop', 'validationMessage')
            .should('contain', 'Please fill ')
    })

    it("Weblink alerts are removed on cancel", () => {
        cy.get("#addWeblinkButton").click()
        cy.get("#addWeblinkButton").click()
        cy.get("#cancelWeblinkButton").click()
        cy.get("#addWeblinkButton").click()
        cy.get("#evidenceWeblinkAddressFeedback").should("not.be.visible")
        cy.get("#evidenceWeblinkNameFeedback").should("not.be.visible")
    })

    it('Can add web addresses', () => {
        cy.get('#addWeblinkButton').click()
        cy.get('#webLinkUrl').type('www.a.ac.nz')
        cy.get('#webLinkName').type('Wl 1').wait(100)
        cy.get('#addWeblinkButton').click({force:true})

        cy.get("#webLinkTitle").should("be.visible")
        cy.get("#addedWebLinks").contains("Wl 1")
    })

    it('Can save evidence with web addresses', () => {
        cy.get("#evidenceName").invoke('val', "namee")
        cy.get("#evidenceDescription").invoke('val', "description")
        cy.get('#addWeblinkButton').click()
        cy.get('#webLinkUrl').type('www.a.ac.nz')
        cy.get('#webLinkName').type('Wl 1')
        cy.get('#addWeblinkButton').click()
        cy.get("#evidenceSaveButton").click().wait(1000)

        cy.get(".evidenceListItem").last().click({force:true})
        cy.get("#evidenceWebLinks").contains("Wl 1")
    })

    it('Can delete weblinks', () => {
        for (let i = 1; i < 5; i++) {
            cy.get('#addWeblinkButton').click()
            cy.get('#webLinkUrl').wait(100)
                .invoke('val', 'http://www.a' + i.toString() + '.ac.nz')
            cy.get('#webLinkName').wait(100)
                .invoke('val', 'Wl ' + i.toString())
            cy.get('#addWeblinkButton').click()
        }
        cy.get('[data-value="http://www.a1.ac.nz"]').find(".deleteWeblink").click()
        cy.get('[data-value="http://www.a2.ac.nz"]').find(".deleteWeblink").click()

        cy.get('[data-value="http://www.a3.ac.nz"]').should('exist');
        cy.get('[data-value="http://www.a4.ac.nz"]').should('exist');
        cy.get('[data-value="http://www.a1.ac.nz"]').should('not.exist');
        cy.get('[data-value="http://www.a2.ac.nz"]').should('not.exist');
    })

    it('Can add max 10 weblinks, delete 1, then add another', () => {
        for (let i = 1; i < 11; i++) {
            cy.get('#addWeblinkButton').click()
            cy.get('#webLinkUrl').wait(100)
                .invoke('val', 'http://www.a.ac.nz')
            cy.get('#webLinkName').wait(100)
                .invoke('val', 'Wl ' + i.toString())
            cy.get('#addWeblinkButton').click()
        }

        cy.get('#webLinkFull').should('be.visible');
        cy.get(".deleteWeblink").first().click()
        cy.get('#webLinkFull').should('not.be.visible');

        cy.get('#addWeblinkButton').click()
        cy.get('#webLinkUrl').wait(100)
            .invoke('val', 'http://www.a.ac.nz')
        cy.get('#webLinkName').wait(100)
            .invoke('val', 'Wlfinal')
        cy.get('#addWeblinkButton').click()

        cy.get('#webLinkFull').should('be.visible');
    })
})