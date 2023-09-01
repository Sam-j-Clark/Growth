describe("Create new restricted skill tag", () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.viewport(1024, 1200)
        cy.visit('/evidence')
    })

    it("Gives an error message when you attempt to manually create a 'No Skill' tag", () => {
        cy.get('#createEvidenceButton').click();
        cy.get('#skillsInput').type("No_Skill");
        cy.get(".skillChipInvalid").invoke('attr', 'data-bs-original-title')
            .should("eq", "This is a reserved tag and cannot be manually created")
    })
})
