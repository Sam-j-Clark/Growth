describe('Link users to evidence', () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.viewport(1024, 1200)
        cy.visit('/evidence')
        cy.get("#createEvidenceButton").click()
    })

    it("can display linking users input", () => {
        cy.get("#linkUsersToEvidenceButton").click()
        cy.get("#linkUsersInput").should("be.visible")
    })

    it("can link users", () => {
        cy.get("#linkUsersToEvidenceButton").click()
        cy.get("#linkUsersInput").type("Joh").wait(2000).type('{enter}')
        cy.get("#linkedUsersTitle").should("be.visible")
        cy.get("#linkedUserId131").should("exist")
    })

    it("can see linked users", () => {
        cy.get("#linkUsersToEvidenceButton").click()
        cy.get("#linkUsersInput").type("Joh").wait(2000).type('{enter}')
        cy.get("#linkUsersInput").type("Jos").wait(2000).type('{enter}')
        cy.get("#evidenceName").type("test")
        cy.get("#evidenceDescription").type("test")
        cy.get("#evidenceSaveButton").click().wait(2000)
        cy.get(".evidenceListItem").last().click()
        cy.get("#evidenceDetailsLinkedUsersTitle").should("be.visible")
        cy.get("#evidenceDetailsLinkedUsers").contains("John Fletcher")
        cy.get("#evidenceDetailsLinkedUsers").contains("Joseph Haywood")
        cy.get("#evidenceDetailsLinkedUsers").contains("John Wayne")
    })
})