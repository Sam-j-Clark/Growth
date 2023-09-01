context("WebLinkTest", () => {
        beforeEach(() => {
            cy.adminLogin()
            cy.visit('/groups')
        })

        it('option and button display for groups pagination', () => {
            cy.get('.scrollableGroupOverview').first().click()
            cy.get('#lang').should('be.visible');
        })
    }
)