describe("Clicking Linked Users", () => {

    const STEVE_USER_ID = 110;


    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/evidence')
    })

    const postNewEvidence = (callback) => {
        const projectId = 1

        let evidenceDTO = {
            title: "cypress test evidence",
            date: "2022-08-01",
            description: "description",
            projectId: projectId,
            webLinks: [],
            skills: [],
            categories: [],
            associateIds: [STEVE_USER_ID]
        }

        cy.request({
            method: 'POST',
            url: '/evidence',
            headers: {contentType: "application/json"},
            body: evidenceDTO,
        }).then(callback)
    }


    it("can view other users evidence through the user list", () => {
        postNewEvidence(() => {
            cy.reload()
            cy.contains("cypress test evidence").click()
            cy.contains("Steve Steveson (steve)").click()
            cy.get("#nameHolder").contains("Viewing evidence for Steve Steveson")
            cy.get("#editEvidenceButton").should("not.exist")
        })
    })

    it("can view own evidence through the user list", () => {
        postNewEvidence(() => {
            cy.reload()
            cy.contains("cypress test evidence").click()
            cy.contains("John Wayne (admin)").click()
            cy.get("#createEvidenceButton").should("be.visible");
            cy.get("#editEvidenceButton").should("be.visible")
        })
    })
})