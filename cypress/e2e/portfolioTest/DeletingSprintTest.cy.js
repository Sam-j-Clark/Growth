describe('Test Deleting Sprints', () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/portfolio?projectId=1').wait(500)
    })

    it('Trying to delete a sprint prompts confirmation', () => {
        cy.get(".sprintsContainer").find(".deleteSprint").first().click({force: true})

        cy.on('window:confirm', (text) => {
            expect(text).to.contains('Are you sure you want to delete:')
            return false; // This is cypress pressing the cancel button
        })
    })
})