const BasePage = require('./BasePage');

class ExercisePage extends BasePage {
    constructor() {
        super();
        this.titleHeader = '//*[contains(@text, "Exercise Recommendations") or contains(@text, "Fitness Plan")]';
        this.cardWorkout = '//*[contains(@text, "Cardio") or contains(@text, "Yoga") or contains(@text, "Stretching")]';
        this.btnStartWorkout = '//android.widget.Button[contains(@text, "Start Workout") or contains(@text, "Play")]';
    }

    async isExercisePageVisible() {
        return await this.isDisplayed(this.titleHeader);
    }
}

module.exports = new ExercisePage();
