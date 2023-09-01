describe('Deleting Evidence', () => {

    it("Can delete evidence belonging to me", () => {
        cy.adminLogin()
        cy.visit('/evidence').wait(500)
        cy.get(".evidenceListItem").its('length').as('numRows')
        cy.get("#deleteEvidenceButton").click({force: true}).wait(500)
        cy.on('window:confirm', () => true);
        cy.get('#alertPopUpMessage').should('contain.text', "Successfully deleted")
        cy.get('@numRows').then((numRows) => {
            const size = numRows - 1
            cy.get(".evidenceListItem").should("have.length", size)
        })
    })

    it("Can not delete evidence that doesn't belong to me", () => {
        cy.studentLogin()
        cy.visit("/user-list?sortField=roles&isAscending=true").wait(500)
        cy.get(".userRoleRow").first().click({force: true}).wait(500)
        cy.get("#deleteEvidenceButton").should('not.be.visible')
    })
})
