const BasePage = require('./BasePage');

class ChatbotPage extends BasePage {
    constructor() {
        super();
        this.titleHeader = '//*[contains(@text, "AI Health Assistant") or contains(@text, "Chatbot")]';
        this.chatInput = '//android.widget.EditText[contains(@text, "Ask") or contains(@text, "Message")]';
        this.btnSend = '//android.widget.Button[contains(@text, "Send") or @content-desc="Send"]';
        this.quickPromptChip = '//*[contains(@text, "Fever tips") or contains(@text, "Diet plan")]';
        this.lastBotMessage = '(//android.widget.TextView)[last()]';
    }

    async isChatbotVisible() {
        return await this.isDisplayed(this.titleHeader);
    }

    async sendMessage(msg) {
        await this.setValue(this.chatInput, msg);
        await this.click(this.btnSend);
    }
}

module.exports = new ChatbotPage();
