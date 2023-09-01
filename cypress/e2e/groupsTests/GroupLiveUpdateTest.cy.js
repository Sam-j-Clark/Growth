context("Group creation live update notification", () => {
    beforeEach(() => {
        cy.viewport(1920, 1024)
        cy.adminLogin()
        cy.visit('/groups')
        cy.get('#2').click()
    })

    it('Create group and send notification, group should exist on page', () => {
        let randomName = "aaa" + Math.floor(Math.random() * 100)
        cy.get(".alert").should("not.exist");
        cy.get(".groupShortName").invoke("text").should("not.contain", randomName)
        cy.request({
            method: "POST",
            url: `/groups/edit`,
            form: true,
            body: {
                "shortName": randomName,
                "longName": randomName
            }
        }).wait(1000)
        cy.window().then(win => win.sendNotification("group", 0, "newGroup"))
        cy.get(".groupShortName").should("contain", randomName)
        cy.get(".alert").should("be.visible");
    })

})

