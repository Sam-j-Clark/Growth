describe('Test Registration', () => {
  beforeEach('Visits the local web address for registration', () => {
    cy.visit('/register')
  })

  it('Fills in the registration details with correct information', () => {
    cy.get("#firstname").type("Aaron")
    cy.get("#middlename").type("A test")
    cy.get("#lastname").type("Ai")
    cy.get("#username").type("User" + Math.floor(Math.random() * 100))
    cy.get("#password").type("password")
    cy.get("#email").type("test@test.com")
    cy.contains("Submit").click()
    cy.url().should("include", '/account')
  })

  it('Fills in the registration details with correct information no middle name', () => {
    cy.get("#firstname").type("Aaron")
    cy.get("#middlename").invoke("val", "")
    cy.get("#lastname").type("Ai")
    cy.get("#username").type("User" + Math.floor(Math.random() * 100))
    cy.get("#password").type("password")
    cy.get("#email").type("test@test.com")
    cy.contains("Submit").click()
    cy.url().should("include", '/account')
  })

  it('Fills in the registration details with bad username', () => {
    cy.get("#firstname").type("test")
    cy.get("#middlename").type("test")
    cy.get("#lastname").type("test")
    cy.get("#password").type("password")
    cy.get("#email").type("test@test.com")
    cy.contains("Submit").click()
    cy.get('#username:invalid')
        .invoke('prop', 'validationMessage')
        .should('contain', 'Please fill ')
  })

  it('Fills in the registration details with bad firstname', () => {
    cy.get("#middlename").type("test")
    cy.get("#lastname").type("test")
    cy.get("#password").type("password")
    cy.get("#username").type("User" + Math.floor(Math.random() * 100))
    cy.get("#email").type("test@test.com")
    cy.contains("Submit").click()
    cy.get('#firstname:invalid')
        .invoke('prop', 'validationMessage')
        .should('contain', 'Please fill ')
  })

  it('Fills in the registration details with bad lastname', () => {
    cy.get("#firstname").type("test")
    cy.get("#middlename").type("test")
    cy.get("#password").type("password")
    cy.get("#username").type("User" + Math.floor(Math.random() * 100))
    cy.get("#email").type("test@test.com")
    cy.contains("Submit").click()
    cy.get('#lastname:invalid')
        .invoke('prop', 'validationMessage')
        .should('contain', 'Please fill ')
  })

  it('Fills in the registration details with invalid username', () => {
    cy.get("#firstname").type("test")
    cy.get("#middlename").type("test")
    cy.get("#lastname").type("test")
    cy.get("#username").type('\u0000' + Math.floor(Math.random() * 10))
    cy.get("#password").type("password")
    cy.get("#email").type("test@test.com")
    cy.contains("Submit").click()
    cy.get('#username:invalid')
        .invoke('prop', 'validationMessage')
        .should('exist')
  })

  it('Fills in the registration details with bio emoji', () => {
    cy.get("#firstname").type("test")
    cy.get("#middlename").type("test")
    cy.get("#lastname").type("test")
    cy.get("#username").type("test" + Math.floor(Math.random() * 10))
    cy.get("#password").type("password")
    cy.get("#email").type("test@test.com")
    cy.get("#bio").type("😀")
    cy.get("#alertPopUp").should('be.visible').contains("Invalid character")
  })
})

