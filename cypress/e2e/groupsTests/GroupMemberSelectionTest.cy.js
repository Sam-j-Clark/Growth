context("Ctrl Selecting Group Members", () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/groups')
        cy.get('#2').click() // selects non-group
    })

    it('ctrl clicking 2 adjacent rows', () => {
        cy.get('.userRow').first()
            .click({ctrlKey: true})
        cy.get('.userRow:not(.ui-selected)').first()
            .click({ctrlKey: true})
        cy.get('.ui-selected').should('have.length', 2)
    })

    it('ctrl clicking 2 non-adjacent rows', () => {
        cy.get('.userRow').first()
            .click({ctrlKey: true})
        cy.get('.userRow:not(.ui-selected)').last()
            .click({ctrlKey: true})
        cy.get('.ui-selected').should('have.length', 2)
    })

    it('ctrl clicking 2 adjacent rows, then deselecting them', () => {
        cy.get('.userRow').first()
            .click({ctrlKey: true})
        cy.get('.userRow:not(.ui-selected)').first()
            .click({ctrlKey: true})
        cy.get('.ui-selected').first().click()
        cy.get('.ui-selected').first().click({ctrlKey: true, force: true})

        cy.get('.ui-selected').should('have.length', 0)
    })

    it('ctrl clicking 2 non-adjacent rows, press again to deselect', () => {
        cy.get('.userRow').first()
            .click({ctrlKey: true})
        cy.get('.userRow:not(.ui-selected)').last()
            .click({ctrlKey: true})
        cy.get('.ui-selected').first().click()
        cy.get('.ui-selected').first().click({ctrlKey: true, force: true})

        cy.get('.ui-selected').should('have.length', 0)
    })
})


context("Shift Selecting Group Members", () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/groups')
        cy.get('#2').click('top');
    })

    it('shift selecting the entire group members list', () => {
        const numRows = Cypress.$('.userRow').length

        cy.get('.userRow').first()
            .click({shiftKey: true})
        cy.get('.userRow').last()
            .click({shiftKey: true})

        cy.get('.ui-selected').should('have.length', numRows)
    })

    it('shift selecting and deselecting the whole group members list except one', () => {
        cy.get('.userRow').first()
            .click({shiftKey: true})
        cy.get('.userRow').last()
            .click({shiftKey: true})
        cy.contains('Shirley').click()

        cy.get('.ui-selected')
            .should('have.length', 1)
            .find('td').first().should('contain.text', '15')
    })
})


context("Shift and Ctrl Selecting Group Members", () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/groups')
        cy.get('#2').click('top');
    })

    it('shift selecting and deselecting the whole group members list except one', () => {
        const numRows = Cypress.$('.userRow').length - 1

        cy.get('.userRow').first()
            .click({shiftKey: true})
        cy.get('.userRow').last()
            .click({shiftKey: true})
        cy.contains('Shirley')
            .click({ctrlKey: true, force: true})

        cy.get('.ui-selected')
            .should('have.length', numRows)
        cy.get('.userRow:not(.ui-selected)').first()
            .should('contain.text', '15')
    })
})
