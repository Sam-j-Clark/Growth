describe('Test Adding and Deleting Sprints', () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/portfolio?projectId=1')
    })

    it('Deleted sprint confirmation message appears after deleting', () => {
        cy.get(".sprintsContainer").find(".deleteSprint").first().click({force: true})
        cy.get("#alertPopUp").should('be.visible').contains("Sprint deleted!")
    })

    it('Events are auto added when new sprint is created', () => {
        cy.get(".sprintsContainer").find(".deleteSprint").last().click({force: true}).wait(1000)
        cy.get(".addSprint").click({force: true}).wait(1000)
        cy.get(".sprintsContainer").find(".sprint").last().find(".eventInSprint").should('be.visible')
    })
})

