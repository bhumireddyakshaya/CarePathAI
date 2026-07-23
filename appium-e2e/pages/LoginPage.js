const BasePage = require('./BasePage');

class LoginPage extends BasePage {
    
    // Selectors
    get inputUsername() { 
        // Finds the first EditText on the screen (Email)
        return '//android.widget.EditText[contains(@text, "Email") or @index=0]'; 
    }
    get inputPassword() { 
        // Finds the password EditText (contains password label or second EditText)
        return '//android.widget.EditText[contains(@text, "Password") or @index=1]'; 
    }
    get btnLogin() { 
        // Finds the Login button by text
        return '//android.widget.Button[@text="Login" or contains(@content-desc, "Login")]'; 
    }
    get btnSignUpLink() {
        // Sign Up button to go to registration screen
        return '//android.widget.Button[@text="Sign Up" or contains(@text, "Sign Up")]';
    }
    get msgError() { 
        // Compose uses Toast, or we can check if there's any text matching common error alerts
        return '//android.widget.Toast'; 
    }

    async login(username, password) {
        if (username) {
            await this.setValue(this.inputUsername, username);
        }
        if (password) {
            await this.setValue(this.inputPassword, password);
        }
        await this.hideKeyboard();
        await this.click(this.btnLogin);
    }

    async navigateToSignUp() {
        await this.click(this.btnSignUpLink);
    }

    async isLoginScreenVisible() {
        return await this.isDisplayed(this.btnLogin);
    }
}

module.exports = new LoginPage();

