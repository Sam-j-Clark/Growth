describe('Login page', () => {
   beforeEach(() => {
     cy.visit('http://localhost:9000/anything')
   })

   it('fail login to admin account invalid password', () => {
     cy.get('#username')
         .type('admin')
     cy.get('#password')
         .type(`invalidate password {enter}`)
     cy.get(".errorMessage").should('have.text', 'Log in attempt failed: username or password incorrect')
   })

   it('successful log in to admin account', () => {
     cy.get('#username')
         .type("admin")
     cy.get('#password')
         .type(`password{enter}`) //add password and submit form
     cy.url().should('include', 'account')
   })
 })


describe("logging out", () => {
  beforeEach(() => {
    cy.studentLogin()
  })

  it("logging out", () => {
    cy.get('.profileDropdown').click()
    cy.get('.logout').click()
    cy.url().should('include', 'login')
  })
})