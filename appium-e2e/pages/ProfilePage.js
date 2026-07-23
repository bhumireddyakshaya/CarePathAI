const BasePage = require('./BasePage');

class ProfilePage extends BasePage {
    constructor() {
        super();
        this.titleHeader = '//*[contains(@text, "User Profile") or contains(@text, "Account Settings")]';
        this.userNameInput = '//android.widget.EditText[contains(@text, "Name") or contains(@text, "User")]';
        this.emergencyContactInput = '//android.widget.EditText[contains(@text, "Emergency Contact") or contains(@text, "Phone")]';
        this.toggleDarkMode = '//*[contains(@text, "Dark Mode") or contains(@text, "Theme")]';
        this.btnSaveProfile = '//android.widget.Button[contains(@text, "Save Profile") or contains(@text, "Update")]';
        this.btnLogout = '//android.widget.Button[contains(@text, "Logout") or contains(@text, "Sign Out")]';
    }

    async isProfilePageVisible() {
        return await this.isDisplayed(this.titleHeader);
    }
}

module.exports = new ProfilePage();
