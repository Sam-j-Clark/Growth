const MIN_BRIGHTNESS_FACTOR = 0.5
const MAX_BRIGHTNESS_FACTOR = 1.5

/**
 * Creates HTMl for a skill chip with the given skill name and frequency
 *
 * @param skillName The name to be displayed in the skill chip.
 * @param skillId The ID of the skill saved in the database.
 * @param frequency A float representing how much a skill has been used. It will be used to colour the skill chip
 * @param deletable Optional parameter determining if the chip is deletable, false by default
 *
 * @returns {string} The string of HTMl representing the skill chip.
 */
function createSkillChip(skillName, skillId, frequency, deletable=false) {

    const scaledFrequency = frequency * (MAX_BRIGHTNESS_FACTOR - MIN_BRIGHTNESS_FACTOR) + MIN_BRIGHTNESS_FACTOR

    const deleteIcon = deletable ? `
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-x-circle chipDelete" viewBox="0 0 16 16">
            <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
            <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
        </svg>` : ""

    return `
        <div class="chip skillChip 
            ${deletable ? "editableChip" : "sortableChip"}" 
            ${skillId !== undefined ? "data-id=" + skillId: ""}
            ${deletable ? "" : "style=\"filter: brightness(" + scaledFrequency + "\")"}>
            <span class="chipText noDisplayInput focus" role="textbox">${sanitise(skillName)}</span>
            ${deleteIcon}
        </div>`
}