describe('Testing evidence creation form', () => {
    beforeEach(() => {
        cy.adminLogin()
    })

    it("Everything is refreshed on submit", () => {
        cy.viewport(1200,1024)
        cy.visit('/evidence')
        cy.get('#createEvidenceButton').click()
        cy.get('.countCharName').should("have.text", "50 characters remaining")
        cy.get("#evidenceName").type("Created Cypress Test")
        cy.get("#evidenceDescription").type("Created this to make it run automatically")
        cy.get("#evidenceSaveButton").click()
        cy.get('#createEvidenceButton').click()
        cy.get('.countCharName').should("have.text", "50 characters remaining")
        cy.get("#evidenceName").should("be.empty")
        cy.get("#evidenceDescription").should("be.empty")
        cy.get("#skillsInput").should("be.empty")
    })
})
