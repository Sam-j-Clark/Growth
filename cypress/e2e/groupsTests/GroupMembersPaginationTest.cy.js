context("Test group pagination", () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/groups')
        cy.viewport(1200, 1024)
        cy.get('.scrollableGroupOverview').scrollTo('bottom')
        cy.get('#2').click() // selects non-group
    })

    function checkButtons(position) {
        cy.get("#groupMembersAmountOptions" + position).contains('[class="page-link"]', "1").click()
        cy.get("#groupMembersAmountOptions" + position).find(".groupFooterFirst").should("have.class", "disabled")
        cy.get("#groupMembersAmountOptions" + position).find(".groupFooterPrevious").should("have.class", "disabled")
        cy.get("#groupMembersAmountOptions" + position).find(".groupFooterNext").should("not.have.class", "disabled")
        cy.get("#groupMembersAmountOptions" + position).find(".groupFooterLast").should("not.have.class", "disabled")

        cy.get("#groupMembersAmountOptions" + position).contains('[class="page-link"]', "Last").click()
        cy.get("#groupMembersAmountOptions" + position).find(".groupFooterFirst").should("not.have.class", "disabled")
        cy.get("#groupMembersAmountOptions" + position).find(".groupFooterPrevious").should("not.have.class", "disabled")
        cy.get("#groupMembersAmountOptions" + position).find(".groupFooterNext").should("have.class", "disabled")
        cy.get("#groupMembersAmountOptions" + position).find(".groupFooterLast").should("have.class", "disabled")
    }


    it('There should be a selector for group members', () => {
        cy.get('#membersPerPageSelect').should("be.visible")
    })

    it('Check buttons disable themselves', () => {
        checkButtons("Top");
        checkButtons("Bottom");
    })

    it('Check amount of group members to display changes', () => {
        cy.get(".scrollableGroupDetails").scrollTo("top")
        cy.get("#membersPerPageSelect").select("10")
        cy.get(".userRow").should("have.length", 10)
        cy.get(".scrollableGroupDetails").scrollTo("top")
        cy.get("#membersPerPageSelect").select("20")
        cy.get("#membersPerPageSelect").select("20")
        cy.contains('[class="page-link"]', "1").click()
        cy.get(".userRow").should("have.length", 20)
        cy.get("#membersPerPageSelect").select("All")
        cy.get("#membersPerPageSelect").select("All")
        cy.get(".userRow").should("have.length.at.least", 21)
    })

})