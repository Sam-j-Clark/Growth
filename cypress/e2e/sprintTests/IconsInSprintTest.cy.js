describe("Icons Appear In Sprints ", () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/portfolio?projectId=1')
    })

    it('Event icons should display in sprint', () => {
        cy.get('#eventIconInSprint').should('be.visible');
    })

    it('Deadline icons should display in sprint', () => {
        cy.get('#deadlineIconInSprint').should('be.visible');
    })

    it('Milestone icons should display in sprint', () => {
        cy.get('#milestoneIconInSprint').should('be.visible');
    })
})