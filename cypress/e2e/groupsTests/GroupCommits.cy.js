context("Groups can add commits", () => {
    beforeEach(() => {
        cy.viewport(1920, 1024)
        cy.adminLogin()
        cy.visit('/groups')
        cy.get('#3').click()
    })

    it('Create group and send notification, group should exist on page', () => {
        cy.get("#pillsSettingsTab").click()
        cy.get(".gitCommitInfo").should("contain", "7e319516")
        cy.get(".gitCommitInfo").should("contain", "2022-08-05")
        cy.get(".gitCommitInfo").should("contain", "00:40:58")
        cy.get(".gitCommitInfo").should("contain", "Aidan Campbell")
    })

})

