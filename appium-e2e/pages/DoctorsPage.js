const BasePage = require('./BasePage');

class DoctorsPage extends BasePage {
    constructor() {
        super();
        this.titleHeader = '//*[contains(@text, "Nearby Doctors") or contains(@text, "Find Specialists")]';
        this.filterCardiology = '//*[contains(@text, "Cardiologist") or contains(@text, "General")]';
        this.doctorCard = '//*[contains(@text, "Dr.") or contains(@text, "Hospital")]';
        this.btnBookAppointment = '//android.widget.Button[contains(@text, "Book Appointment") or contains(@text, "Call")]';
    }

    async isDoctorsPageVisible() {
        return await this.isDisplayed(this.titleHeader);
    }
}

module.exports = new DoctorsPage();
