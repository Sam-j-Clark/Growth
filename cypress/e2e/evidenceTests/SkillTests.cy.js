describe('Skill creation', () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.viewport(1024, 1200)
        cy.visit('/evidence').wait(100)
        cy.get('#createEvidenceButton').click()
    })

    const postNewEvidence = (callback) => {
        const projectId = 1

        let evidenceDTO = {
            title: "cypress test evidence",
            date: "2022-08-01",
            description: "description",
            projectId: projectId,
            webLinks: [],
            skills: ["test evidence", "MAKING tests", "ALLCAPS"],
            categories: [],
            associateIds: []
        }

        cy.request({
            method: 'POST',
            url: '/evidence',
            headers: {contentType: "application/json"},
            body: evidenceDTO,
        }).then(callback)
    }


    it("Should have skill chips display", () => {
        cy.get("#skillsInput").type("test test1 test2 ")
        cy.get("#tagInputChips").find(".skillChip").should("have.length", 3)
    })

    it("Should be able to select a skill from the dropdown - case-insensitive", () => {
        postNewEvidence(() => {
            cy.reload()
            cy.get('#createEvidenceButton').click()
            cy.get("#skillsInput").type("T")
            cy.get(".ui-menu-item-wrapper").should("contain.text", "test evidence")
        })
    })

    it("Should clear skills input after evidence submission", () => {
        cy.get("#evidenceName").type("Cypress Testing")
        cy.get("#evidenceDescription").type( "Description of the test")
        cy.get("#skillsInput").type("test test1 test2 ")
        cy.get("#evidenceSaveButton").click()
        cy.get('#createEvidenceButton').click()
        cy.get("#tagInputChips").should("be.empty")
    })

    it("Should allow special characters", () => {
        cy.get("#skillsInput").type("C# a@! ")
        cy.get("#tagInputChips").find(".skillChip").should("have.length", 2)
    })

    it("Should not allow only special characters", () => {
        cy.get("#skillsInput").type("@ ")
        cy.contains("Skill name must contain at least one letter.").should('be.visible')
        cy.get("#skillsInput").type("@%& ")
        cy.contains("Skill name must contain at least one letter.").should('be.visible')
    })

    it("Should display red and how error message for skill length > 30", () => {
        cy.get("#skillsInput").type("Definitely_more_than_30_characters")
        cy.get("#skillsInput").should("have.class", "skillChipInvalid")
        cy.contains("Maximum skill length is 30 characters").should('be.visible')
        cy.get("#skillsInput").type("https://en.wikipedia.org/wiki/Main_Page")
        cy.get("#skillsInput").should("have.class", "skillChipInvalid")
        cy.contains("Maximum skill length is 30 characters").should('be.visible')
    })

    it("Should make skill chip on Space key press", () => {
        cy.get("#skillsInput").type("fish &_chips ") // using invocation causes only one tab to be made
        cy.get("#tagInputChips").find(".skillChip").should("have.length", 2)
    })

    it("Should not allow 'no skill' tag to be created", () => {
        cy.get("#skillsInput").type("no_skill") // using invocation causes only one tab to be made
        cy.get("#skillsInput").should("have.class", "skillChipInvalid")
        cy.contains("This is a reserved tag and cannot be manually created").should('be.visible')
        cy.get("#skillsInput").type(" ") // using invocation causes only one tab to be made
        cy.contains("This is a reserved tag and cannot be manually created").should('be.visible')
        cy.get("#tagInputChips").should("be.empty")
    })

    it("Should accept multiple skills from copy-pasted skills list", () => {
        cy.get('#skillsInput').invoke('val', "wee woo waa wuu wyy ").trigger('paste')
        cy.get("#tagInputChips").find(".skillChip").should("have.length", 5)
    })

    it("Should not have duplicate auto-completions", () => {
        postNewEvidence(() => {
            cy.reload()
            cy.get('#createEvidenceButton').click()
            cy.get("#skillsInput").type("evi")
            cy.get(".ui-menu-item-wrapper").should("contain.text", "evidence")
            cy.get("#skillsInput").type("{Enter}")
            cy.get("#skillsInput").type("evi")
            cy.get(".ui-menu-item-wrapper").should("not.be.visible")
        })
    })

    it ("Should not allow a skill with all underscores to be made", () => {
        cy.get("#skillsInput").type("______ Normal_Skill ______ ")
        cy.get("#tagInputChips").find(".skillChip").should("have.length", 1)
    })

    it ("Should correct to the casing of saved skills", () => {
        postNewEvidence(() => {
            cy.reload()
            cy.get('#createEvidenceButton').click()
            cy.get("#skillsInput").type("MAKING_TESTS ")
            cy.get("#skillsInput").type("allcaps ")
            cy.get("#skillsInput").type("tEsT_eViDeNcE ")
            cy.get("#tagInputChips").should("contain", "MAKING tests")
            cy.get("#tagInputChips").should("contain", "ALLCAPS")
            cy.get("#tagInputChips").should("contain", "test evidence")
        })
    })

    it ("Should not create duplicate skill chips", () => {
        cy.get("#skillsInput").type("Normal_Skill Normal_Skill Normal_Skill strange________skill strange_skill ")
        cy.get("#tagInputChips").find(".skillChip").should("have.length", 2)
    })
})


