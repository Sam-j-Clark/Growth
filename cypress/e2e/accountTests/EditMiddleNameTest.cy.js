describe('Editing User Account Middle Name', () => {
    const INVALID_BORDER_COLOUR = "rgb(220, 53, 69)"
    const INVALID_MIDDLE_NAME_MESSAGE = "Middle name can only contain unicode letters, punctuation and whitespace"
    const SUCCESSFUL_UPDATE_MESSAGE = "Updated details successfully!"

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

    it("Allows a blank field", () => {
        cy.contains("Edit Account").click()
        cy.get("#middlename").invoke("val", "")
        cy.contains("Submit").click().wait(500)

        cy.get("#firstname").should('have.value','John')
        cy.get("#middlename").should('have.value','')
        cy.get("#lastname").should('have.value','Wayne')
        cy.get("#nickname").should('have.value','Stev')
        cy.get("#email").should('have.value','steve@gmail.com')
        cy.get("#bio").should('have.value','Hello! my name is John and I am your course administrator!')
        cy.get("#personalPronouns").should('have.value','He/Him')
        cy.contains(SUCCESSFUL_UPDATE_MESSAGE).should("be.visible")

    })

    it("Allows a single letter", () => {
        cy.contains("Edit Account").click()
        cy.get("#middlename").invoke("val", "a")
        cy.contains("Submit").click().wait(500)

        cy.get("#firstname").should('have.value','John')
        cy.get("#middlename").should('have.value','a')
        cy.get("#lastname").should('have.value','Wayne')
        cy.get("#nickname").should('have.value','Stev')
        cy.get("#email").should('have.value','steve@gmail.com')
        cy.get("#bio").should('have.value','Hello! my name is John and I am your course administrator!')
        cy.get("#personalPronouns").should('have.value','He/Him')
        cy.contains(SUCCESSFUL_UPDATE_MESSAGE).should("be.visible")
    })

    it("Allows spaces", () => {
        cy.contains("Edit Account").click()
        cy.get("#middlename").invoke("val", "spacey name")
        cy.contains("Submit").click().wait(500)

        cy.get("#firstname").should('have.value','John')
        cy.get("#middlename").should('have.value','spacey name')
        cy.get("#lastname").should('have.value','Wayne')
        cy.get("#nickname").should('have.value','Stev')
        cy.get("#email").should('have.value','steve@gmail.com')
        cy.get("#bio").should('have.value','Hello! my name is John and I am your course administrator!')
        cy.get("#personalPronouns").should('have.value','He/Him')
        cy.contains(SUCCESSFUL_UPDATE_MESSAGE).should("be.visible")
    })

    it("Allows punctuation", () => {
        cy.contains("Edit Account").click()
        cy.get("#middlename").invoke("val", ",._-a")
        cy.contains("Submit").click().wait(500)

        cy.get("#firstname").should('have.value','John')
        cy.get("#middlename").should('have.value',',._-a')
        cy.get("#lastname").should('have.value','Wayne')
        cy.get("#nickname").should('have.value','Stev')
        cy.get("#email").should('have.value','steve@gmail.com')
        cy.get("#bio").should('have.value','Hello! my name is John and I am your course administrator!')
        cy.get("#personalPronouns").should('have.value','He/Him')
        cy.contains(SUCCESSFUL_UPDATE_MESSAGE).should("be.visible")
    })

    it("Disallows only punctuation", () => {
        cy.contains("Edit Account").click()
        cy.get("#middlename").invoke("val", "<>,._-")
        cy.contains("Submit").click().wait(500)

        cy.get("#middlename").should("have.css", "border-color", INVALID_BORDER_COLOUR)
        cy.contains(INVALID_MIDDLE_NAME_MESSAGE).should("be.visible")
        cy.contains(SUCCESSFUL_UPDATE_MESSAGE).should("not.exist")
    })

    it("Disallows symbols", () => {
        cy.contains("Edit Account").click()
        cy.get("#middlename").invoke("val", "%")
        cy.contains("Submit").click().wait(500)

        cy.get("#middlename").should("have.css", "border-color", INVALID_BORDER_COLOUR)
        cy.contains(INVALID_MIDDLE_NAME_MESSAGE).should("be.visible")
        cy.contains(SUCCESSFUL_UPDATE_MESSAGE).should("not.exist")
    })

    it("Disallows emoji", () => {
        cy.contains("Edit Account").click()
        cy.get("#middlename").invoke("val", "😂 😃 🧘🏻️")
        cy.contains("Submit").click().wait(500)

        cy.get("#middlename").should("have.css", "border-color", INVALID_BORDER_COLOUR)
        cy.contains(INVALID_MIDDLE_NAME_MESSAGE).should("be.visible")
        cy.contains(SUCCESSFUL_UPDATE_MESSAGE).should("not.exist")
    })
})