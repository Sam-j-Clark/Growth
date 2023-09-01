describe('Deleting Users Role', () => {
    beforeEach(() => {
         cy.adminLogin()
    })

    it('Checks if users last role is deletable', () => {
        cy.visit('/user-list?sortField=roles&isAscending=false')
        cy.get(".roleButtonsContainer").first().find(".tagDiv").find(".roleDeleteButton").should("not.exist")
    })
})