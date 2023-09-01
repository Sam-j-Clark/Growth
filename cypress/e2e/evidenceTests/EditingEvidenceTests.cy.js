describe('Edit evidence', () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.viewport(1024, 1200)
        cy.visit('/evidence')
    })

    it("Edit button show on my own evidence", () => {
        cy.get("#editEvidenceButton").should("be.visible")
    })

    it("Edit page pre-fill with info", () => {
        cy.get("#editEvidenceButton").click()
        cy.get("#editEvidenceButton").click()
        cy.get("#editEvidenceButton").click()
        cy.get("#linkUsersToEvidenceButton").click()
        cy.get("#linkUsersInput").type("Joh").wait(2000).type('{enter}')
        cy.get("#linkUsersInput").type("Jos").wait(2000).type('{enter}')
        cy.get("#addOrEditEvidenceModal").find(".skillChip").should('have.length', 4)
        cy.get("#addOrEditEvidenceModal").find(".webLinkElement").should('have.length', 1)
        cy.get("#addOrEditEvidenceModal").find(".linkedUser").should('have.length', 2)
        cy.get(".evidenceCategoryTickIcon").should('be.visible')
    })

    it("User can be deleted", () => {
        cy.get("#editEvidenceButton").click()
        cy.get("#editEvidenceButton").click()
        cy.get("#editEvidenceButton").click()
        cy.get("#linkUsersToEvidenceButton").click()
        cy.get("#linkUsersInput").type("Joh").wait(2000).type('{enter}')
        cy.get("#linkUsersInput").type("Jos").wait(2000).type('{enter}')
        cy.get("#addOrEditEvidenceModal").find("#deleteLinkedUser").first().click("center")
        cy.get("#addOrEditEvidenceModal").find("#deleteLinkedUser").click("center")
        cy.get("#addOrEditEvidenceModal").find('.linkedUser').should('not.exist');
    })
})