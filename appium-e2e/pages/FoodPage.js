const BasePage = require('./BasePage');

class FoodPage extends BasePage {
    constructor() {
        super();
        this.titleHeader = '//*[contains(@text, "Food Recommendations") or contains(@text, "Diet & Nutrition")]';
        this.dietChipKeto = '//*[contains(@text, "Keto") or contains(@text, "Low Carb")]';
        this.dietChipBalanced = '//*[contains(@text, "Balanced") or contains(@text, "Vegan")]';
        this.mealCard = '//*[contains(@text, "Calories") or contains(@text, "Protein")]';
        this.btnGeneratePlan = '//android.widget.Button[contains(@text, "Generate Meal Plan") or contains(@text, "Refresh")]';
    }

    async isFoodPageVisible() {
        return await this.isDisplayed(this.titleHeader);
    }
}

module.exports = new FoodPage();
