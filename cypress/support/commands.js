/**
 * Logs into the admin account.
 *
 * This is more efficient than logging-in through the user interface.
 */
Cypress.Commands.add('adminLogin', () => {
    cy.request({
        method: 'POST',
        followRedirect: false, // need this otherwise the cookie won't be there
        url: '/login',
        form: true,
        body: {
        username: 'admin',
        password: 'password',
        },
    }).then(() => {
        cy.getCookie('lens-session-token')
            .should('exist')
            .then((c) => {
                // save cookie until we need it
                cy.setCookie('lens-session-token', c.value)
            })
    })
})


/**
 * Logs into a student account.
 *
 * This is more efficient than logging-in through the user interface.
 */
Cypress.Commands.add('studentLogin', () => {
    cy.request({
        method: 'POST',
        followRedirect: false, // need this otherwise the cookie won't be there
        url: '/login',
        form: true,
        body: {
        username: 'Walter.harber',
        password: 'doopoo2Ah',
        },
    }).then(() => {
        cy.getCookie('lens-session-token')
            .should('exist')
            .then((c) => {
                // save cookie until we need it
                cy.setCookie('lens-session-token', c.value)
            })
    })
})


// prevents tests from failing due to uncaught exceptions
Cypress.on('uncaught:exception', () => {
    return false
})