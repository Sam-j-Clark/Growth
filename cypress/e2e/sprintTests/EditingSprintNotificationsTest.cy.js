describe('Notifications While Editing Sprints', () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/portfolio?projectId=1')
    })

    it('Deletion notification is received while editing the the same sprint', () => {
        cy.get('[data-bs-original-title="Edit Sprint"]').first().click({force: true})

        cy.get('#sprintId').invoke('val').as('sprintId')

        cy.get('@sprintId').then((sprintId) => {
                cy.request({
                    method: "DELETE",
                    url: `/deleteSprint?sprintId=${sprintId}`
                })
            }
        )

        cy.contains("Submit").click({force: true}).wait(100)
        cy.contains("Sprint id doesn't correspond to existing sprint").should("be.visible")
    })
})