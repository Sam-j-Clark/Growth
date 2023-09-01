describe('Restricting the dates of the calendar', () => {
    beforeEach(() => {
        cy.adminLogin()
    })

    it("can't scroll past the start of the project", () => {
        cy.visit('/calendar?projectId=1')
        for (let i = 0; i < 12; i++) {
            cy.get('.fc-prev-button').click({force: true}).wait(100) //clicking too quickly prevents button update
        }
        cy.get('.fc-prev-button').should('be.disabled');
    })

    it("can't scroll past the end of the project", () => {
        cy.visit('/calendar?projectId=1')
        for (let i = 0; i < 12; i++) {
            cy.get('.fc-next-button').click({force: true}).wait(100) //clicking too quickly prevents button update
        }
        cy.get('.fc-next-button').should('be.disabled');
    })
})
