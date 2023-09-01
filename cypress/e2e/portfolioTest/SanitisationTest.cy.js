context("The browser sanitises sprints effectively", () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/portfolio?projectId=1')
        cy.get(".sprintsContainer").find(".editSprint").first().click({force: true})
    })

    it('Sanitises sprint names', () => {
        cy.get("#sprintName").clear().type("<h1>Test</h1>")
        cy.get(".submitButton").click()
        cy.get(".sprint").first().find(".name").should("have.text", "<h1>Test</h1>")
    })

    it('Sanitises sprint descriptions', () => {
        cy.get("#sprintDescription").clear().type("<script>console.log('test');</script>Description");
        cy.get(".submitButton").click()
        cy.get(".sprint").first().find(".description")
            .should("have.text", "<script>console.log('test');</script>Description")
    })

})

context("The browser sanitises projects effectively", () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/portfolio?projectId=1')
        cy.get("#editProject").click()
    })

    it('Sanitises project names', () => {
        cy.get("#projectName").clear().type("<h1>Test</h1>")
        cy.get(".submitButton").click()
        cy.get("#projectName").should("have.text", "<h1>Test</h1>")
    })

    it('Sanitises sprint descriptions', () => {
        cy.get("#projectDescription").clear().type("<script>console.log('test');</script>Description");
        cy.get(".submitButton").click()
        cy.get("#projectDescription")
            .should("have.text", "<script>console.log('test');</script>Description")
    })

})
