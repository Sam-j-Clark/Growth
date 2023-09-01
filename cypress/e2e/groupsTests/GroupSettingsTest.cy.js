context("Repo settings page", () => {
    beforeEach(() => {
        cy.viewport(1920, 1024)
        cy.adminLogin()
        cy.visit('/groups')
        cy.get('#3').click()
        cy.get("#pillsSettingsTab").click();
        cy.get(".editRepo").click();
    })

    it('Counts alias characters correctly', () => {
        cy.get("#repoName").clear();
        cy.get(".countChar").should("contain.text", "100");

        for (let i = 1; i < 5; i++) {
            cy.get("#repoName").type("a");
            cy.get(".countChar").should("contain.text", (100 - i).toString(10));
        }
    })

    it("Displays error when alias length bypassed", () => {
        cy.get("#repoName").invoke("val", "w".repeat(10000));
        cy.get("#editRepoForm").submit();
        cy.get("#alertPopUpMessage").should("contain.text", "The repository name must be no longer than 100 characters")

    })

})

