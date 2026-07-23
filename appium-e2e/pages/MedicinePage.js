const BasePage = require('./BasePage');

class MedicinePage extends BasePage {
    constructor() {
        super();
        this.titleHeader = '//*[contains(@text, "Medicine Reminders") or contains(@text, "Medication")]';
        this.btnAddMedicine = '//android.widget.Button[contains(@text, "Add Medicine") or contains(@text, "+")]';
        this.medicineNameInput = '//android.widget.EditText[contains(@text, "Medicine Name")]';
        this.dosageInput = '//android.widget.EditText[contains(@text, "Dosage")]';
        this.timePickerBtn = '//*[contains(@text, "Set Time") or contains(@text, "Select Time")]';
        this.btnSave = '//android.widget.Button[contains(@text, "Save") or contains(@text, "Add")]';
        this.medicineCard = '//*[contains(@text, "Paracetamol") or contains(@text, "Amoxicillin")]';
    }

    async isMedicinePageVisible() {
        return await this.isDisplayed(this.titleHeader);
    }

    async addMedicine(name, dosage) {
        await this.click(this.btnAddMedicine);
        await this.setValue(this.medicineNameInput, name);
        await this.setValue(this.dosageInput, dosage);
        await this.click(this.btnSave);
    }
}

module.exports = new MedicinePage();
