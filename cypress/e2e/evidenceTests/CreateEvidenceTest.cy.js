describe('Create new piece of evidence', () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.viewport(1024, 1200)
        cy.visit('/evidence')
    })

    it("Modal should display when button is clicked", () => {
        cy.get("#addEvidenceModal").should("not.be.visible")
        cy.get('#createEvidenceButton').click()
        cy.get("#addEvidenceModal").should("be.visible")
    })

    it("Should submit with just name and description", () => {
        cy.get('#createEvidenceButton').click()
        cy.get("#evidenceSaveButton").should("be.disabled")
        cy.get("#evidenceName").type("Writing Cypress Tests")
        cy.get("#evidenceSaveButton").should("be.disabled")
        cy.get("#evidenceDescription").type("This is all automatically written and should pass!")
        cy.get("#evidenceSaveButton").should("not.be.disabled")
        cy.get("#evidenceSaveButton").click()
        cy.get("#addEvidenceModal").should("not.be.visible")
    })
})


describe("Closing evidence creation form without save", () => {
    beforeEach(() => {
        cy.adminLogin().wait(100)
        cy.visit('/evidence')
    })

    it("Clicking outside the add evidence modal closes the modal", () => {
        cy.get("#createEvidenceButton").click({force: true})
        cy.get(".headerBar").click({force: true})
        cy.get("#addEvidenceModal").should("not.be.visible")
    })

    it("Closing alert does not close evidence creation modal", () => {
        cy.get("#createEvidenceButton").click({force: true})
        cy.window().then(win => win.createAlert("alert"))  // manually create an alert from the browser console
        cy.get("#alertPopUpCloseButton").click()
        cy.get("#addEvidenceModal").should("be.visible")
    })

    it("Clicking cancel closes the evidence creation modal", () => {
        cy.get("#createEvidenceButton").click({force: true})
        cy.get("#evidenceCancelButton").click()
        cy.get("#addEvidenceModal").should("not.be.visible")
    })

    it("Cancelling evidence creation does not clear form", () => {
        cy.get("#createEvidenceButton").click({force: true})
        cy.get("#evidenceName").type("tests")
        cy.get("#evidenceDescription").type("desc")
        cy.get("#skillsInput").type("skill")
        cy.get("#addWeblinkButton").click()
        cy.get("#webLinkUrl").type("http://www.a.ac.nz")
        cy.get("#webLinkName").type("name")
        cy.get("#addWeblinkButton").click()
        cy.get("#evidenceCancelButton").click().wait(500)

        cy.get("#createEvidenceButton").click({force: true}).wait(500)
        cy.get("#evidenceName").should("have.value", "test")
        cy.get("#evidenceDescription").should("have.value","desc")
        cy.get(".skillChipDisplay").contains("skill")
        cy.get("#addedWebLinks").contains("name")
    })
})
