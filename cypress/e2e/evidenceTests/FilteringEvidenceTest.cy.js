describe('filtering by categories and skills', () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/evidence')
    })

    it("can filter by 'Qualitative' category", () => {
        cy.get('#categoryList').find(".categoryChip").first().click({force: true}).wait(500)
        cy.get(".evidenceTitle").should("have.text", "Qualitative")
        cy.get(".evidenceListItem").each(($el) => {
            cy.wrap($el).find(".categoryChipDisplay").contains("Qualitative")
        })
    })

    it("can filter by 'debugging' skill", () => {
        cy.get('#skillList').find("#skillCalleddebugging").click({force: true}).wait(500)
        cy.get(".evidenceTitle").should("have.text", "debugging")
        cy.get(".evidenceListItem").each(($el) => {
            cy.wrap($el).find(".skillChipDisplay").contains("debugging")
        })
    })

    it("selected filter changes when 'Qualitative' category clicked in menu", () => {
        cy.get('#categoryList').find(".categoryChip").first().click({force: true}).wait(500)
        cy.get("#categoryCalledQualitative").should("have.class", "selected")
        cy.get(".selected").should("have.length", 1)
    })

    it("selected filter are removed when 'Show all Evidence' is clicked", () => {
        cy.get('#skillList').find("#skillCalleddebugging").click({force: true}).wait(500)
        cy.get('#showAllEvidence').click({force: true}).wait(500)
        cy.get(".evidenceTitle").should("have.text", "Evidence")
        cy.get(".selected").should("have.length", 0)
    })

    it("selected filter changes when 'Qualitative' category clicked in preview", () => {
        cy.get('#evidenceList').first().find(".categoryChip").first().click({force: true}).wait(500)
        cy.get("#categoryCalledQualitative").should("have.class", "selected")
        cy.get(".selected").should("have.length", 1)
    })
})
