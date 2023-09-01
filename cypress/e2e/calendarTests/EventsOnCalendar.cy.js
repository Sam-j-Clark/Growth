describe('Displaying Events on the Calendar', () => {
    let eventId

    beforeEach(() => {
        cy.adminLogin()
        eventId = ""
    })


    afterEach(() => {
        cy.request({
            method: "DELETE",
            url: `/deleteEvent?eventId=${eventId}`
        })
    })


    it("One day events display on the calendar", () => {
        cy.visit("/calendar?projectId=1")
        let projectId = 1
        let name = "test event"
        let start = "2022-02-25T11:38:00.01"
        let end = "2022-02-25T12:38:00.01"

        cy.request({
            method: "PUT",
            url: `/addEvent?projectId=${projectId}&eventName=${name}&eventStart=${start}&eventEnd=${end}`
        }).then( (response) => {
            eventId = response.body.id

            for (let i = 0; i < 12; i++) {
                cy.get('.fc-prev-button').click({force: true}).wait(100) //clicking too quickly prevents button update
            }

            cy.get("[data-date='2022-02-24']").should("not.exist")
            cy.get("[data-date='2022-02-25']").find(".eventCalendar").should("exist")
            cy.get("[data-date='2022-02-26']").find(".eventCalendar").should("not.exist")
        })
    })


    it("An event that goes from midnight to midnight two days later", () => {
        cy.visit("/calendar?projectId=1")
        let projectId = 1
        let name = "midnight test event"
        let start = "2022-02-25T00:00:00.00"
        let end = "2022-02-27T00:00:00.00"

        cy.request({
            method: "PUT",
            url: `/addEvent?projectId=${projectId}&eventName=${name}&eventStart=${start}&eventEnd=${end}`
        }).then( (response) => {
            eventId = response.body.id
            for (let i = 0; i < 12; i++) {
                cy.get('.fc-prev-button').click({force: true}).wait(100) //clicking too quickly prevents button update
            }

            cy.get("[data-date='2022-02-24']").should("not.exist")
            cy.get("[data-date='2022-02-25']").find(".eventCalendar").should("exist")
            cy.get("[data-date='2022-02-26']").find(".eventCalendar").should("exist")
            cy.get("[data-date='2022-02-27']").find(".eventCalendar").should("exist")
            cy.get("[data-date='2022-02-28']").find(".eventCalendar").should("not.exist")
        })
    })


    it("An event that goes from 6pm to 6pm the next day", () => {
        cy.visit("/calendar?projectId=1")
        let projectId = 1
        let name = "6pm test event"
        let start = "2022-02-27T18:00:00.00"
        let end = "2022-02-28T18:00:00.00"

        cy.request({
            method: "PUT",
            url: `/addEvent?projectId=${projectId}&eventName=${name}&eventStart=${start}&eventEnd=${end}`
        }).then( (response) => {
            eventId = response.body.id
            for (let i = 0; i < 12; i++) {
                cy.get('.fc-prev-button').click({force: true}).wait(100) //clicking too quickly prevents button update
            }

            cy.get("[data-date='2022-02-26']").find(".eventCalendar").should("not.exist")
            cy.get("[data-date='2022-02-27']").find(".eventCalendar").should("exist")
            cy.get("[data-date='2022-02-28']").find(".eventCalendar").should("exist")
            cy.get("[data-date='2022-03-01']").find(".eventCalendar").should("not.exist")
        })
    })
})