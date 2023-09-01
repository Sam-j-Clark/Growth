/// <reference types="cypress" />

/*
 * Required for naming custom functions.
 * Allows strings as names.
 */
declare namespace Cypress {
    interface Chainable<Subject = any> {
        /**
         * Programmatically logs in to the student account "Walter.harber".
         * Sets the session token cookie from login.
         */
        studentLogin(): Chainable;

        /**
         * Programmatically logs in to the admin account "admin".
         * Sets the session token cookie from login.
         */
        adminLogin(): Chainable;
    }
}