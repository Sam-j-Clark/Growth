describe('Editing user account info', () => {

    let INVALID_BORDER_COLOUR = 'rgb(220, 53, 69)';

    beforeEach(() => {
        cy.adminLogin()
        cy.visit("/account")
        cy.request({
            method: 'POST',
            url: 'edit/details',
            form: true,
            body: {
                "firstname": "John",
                "middlename": "McSteves",
                "lastname": "Wayne",
                "nickname": "Stev",
                "bio": "Hello! my name is John and I am your course administrator!",
                "personalPronouns": "He/Him",
                "email": "steve@gmail.com"
            },
        })
    })

    it("resets account data on cancel edit", () => {
        cy.get(".editUserButton").click()
        cy.get("#firstname").invoke('val', "hello")
        cy.get("#middlename").invoke('val', "hello")
        cy.get("#lastname").invoke('val', "hello")
        cy.get("#nickname").invoke('val', "hello")
        cy.get("#email").invoke('val', "hello")
        cy.get("#bio").invoke('val', "hello")
        cy.get("#personalPronouns").invoke('val', "hello")
        cy.get(".editUserButton").click().wait(500)

        cy.get("#firstname").should('have.value','John')
        cy.get("#middlename").should('have.value','McSteves')
        cy.get("#lastname").should('have.value','Wayne')
        cy.get("#nickname").should('have.value','Stev')
        cy.get("#email").should('have.value','steve@gmail.com')
        cy.get("#bio").should('have.value','Hello! my name is John and I am your course administrator!')
        cy.get("#personalPronouns").should('have.value','He/Him')
    })

    it("Displays multiple errors without submission", () => {
        cy.get(".editUserButton").click()
        cy.get("#firstname").invoke('val', "99")
        cy.get("#middlename").invoke('val', "99")
        cy.get("#lastname").invoke('val', "99")
        cy.get("#nickname").invoke('val', "Ⅵ")
        cy.get("#email").invoke('val', "99")
        cy.get("#bio").type("Ⅵ")
        cy.get("#personalPronouns").invoke('val', "Ⅴ")

        cy.get("#firstname").should("have.css", "border-color", );
        cy.get("#middlename").should("have.css", "border-color", INVALID_BORDER_COLOUR);
        cy.get("#lastname").should("have.css", "border-color", INVALID_BORDER_COLOUR);
        cy.get("#nickname").should("have.css", "border-color", INVALID_BORDER_COLOUR);
        cy.get("#email").should("have.css", "border-color", INVALID_BORDER_COLOUR);
        cy.get("#bio").should("have.css", "border-color", INVALID_BORDER_COLOUR);
        cy.get("#personalPronouns").should("have.css", "border-color", INVALID_BORDER_COLOUR);

        cy.get("#firstNameError").should("be.visible").should('contain.text', "at least one letter. Can also include punctuation and whitespace.")
        cy.get("#middleNameError").should("be.visible").should('contain.text', "at least one letter. Can also include punctuation and whitespace.")
        cy.get("#lastNameError").should("be.visible").should('contain.text', "at least one letter. Can also include punctuation and whitespace.")
        cy.get("#nickNameError").should("be.visible").should('contain.text', "can only contain unicode letters, numbers, punctuation, symbols (but not emojis) and whitespace.")
        cy.get("#emailError").should("be.visible").should('contain.text', " must be of a valid email format, e.g. example@email.com.")
        cy.get("#bioError").should("be.visible").should('contain.text', "can only contain unicode letters, numbers, punctuation, symbols (but not emojis) and whitespace.")
        cy.get("#pronounsError").should("be.visible").should('contain.text', "can only contain unicode letters, numbers, punctuation, symbols (but not emojis) and whitespace.")
    })


    it("names must have one alphanumeric character", () => {
        cy.get(".editUserButton").click()
        cy.get("#firstname").invoke('val', "!@#&*()_-[{]}\\'\";:,./?")
        cy.get("#middlename").invoke('val', "!@#&*()_-[{]}\\'\";:,./")
        cy.get("#lastname").invoke('val', "!@#&*()_-[{]}\\'\";:,./")

        cy.get("#firstname").should("have.css", "border-color", INVALID_BORDER_COLOUR);
        cy.get("#middlename").should("have.css", "border-color", INVALID_BORDER_COLOUR);
        cy.get("#lastname").should("have.css", "border-color", INVALID_BORDER_COLOUR);
        cy.get("#firstNameError").should("be.visible")
        cy.get("#middleNameError").should("be.visible")
        cy.get("#lastNameError").should("be.visible")

        cy.get("#firstname").type('A', {force: true})
        cy.get("#middlename").type('A', {force: true})
        cy.get("#lastname").type('A', {force: true})

        cy.get("#firstname").should("not.have.css", "border-color", INVALID_BORDER_COLOUR);
        cy.get("#middlename").should("not.have.css", "border-color", INVALID_BORDER_COLOUR);
        cy.get("#lastname").should("not.have.css", "border-color", INVALID_BORDER_COLOUR);
        cy.get("#firstNameError").should("not.be.visible")
        cy.get("#middleNameError").should("not.be.visible")
        cy.get("#lastNameError").should("not.be.visible")
    })
})