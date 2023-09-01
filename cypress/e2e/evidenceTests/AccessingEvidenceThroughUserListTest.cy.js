Cypress.on('uncaught:exception', (err, runnable) => {
  // returning false here prevents Cypress from failing the test
  return false
})

describe('Viewing evidence through the user list', () => {
    beforeEach(() => {
     cy.adminLogin()
    })

    it("can view other users evidence through the user list", () => {
        cy.visit('/user-list?sortField=aliases&isAscending=true')
        cy.get(".userRoleRow").first().click({force: true})
        cy.get("#nameHolder").contains("Viewing evidence for Aaron A")
        cy.get("#createEvidenceButton").should("not.exist")
    })

    it("can view my own evidence and still edit", () => {
        cy.visit('/user-list?sortField=roles&isAscending=true')
        cy.get(".userRoleRow").first().click({force: true})
        cy.get("#nameHolder").should('not.visible')
        cy.get("#createEvidenceButton").should("exist")
    })
})
