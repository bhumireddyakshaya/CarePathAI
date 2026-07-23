const ExcelJS = require('exceljs');
const path = require('path');
const fs = require('fs');
const logger = require('./Logger');

class ExcelReporter {
    constructor() {
        this.reset();
    }

    reset() {
        this.workbook = new ExcelJS.Workbook();
        this.workbook.creator = 'CarePathAI Enterprise E2E Automation Framework';
        this.workbook.created = new Date();

        this.summarySheet = this.workbook.addWorksheet('Summary', { views: [{ showGridLines: true }] });
        this.testCasesSheet = this.workbook.addWorksheet('Test Cases', { views: [{ showGridLines: true }] });
        this.failedTestsSheet = this.workbook.addWorksheet('Failed Tests', { views: [{ showGridLines: true }] });
        this.executionLogsSheet = this.workbook.addWorksheet('Execution Logs', { views: [{ showGridLines: true }] });

        this.testCases = [];
        this.failedTests = [];
        this.executionLogs = [];
        this.summaryData = null;

        this.initSheets();
    }

    initSheets() {
        // Sheet 1 - Summary
        this.summarySheet.columns = [
            { header: 'Metric Title', key: 'metric', width: 30 },
            { header: 'Value', key: 'value', width: 30 }
        ];

        // Sheet 2 - Test Cases
        this.testCasesSheet.columns = [
            { header: 'Test ID', key: 'id', width: 18 },
            { header: 'Module Name', key: 'module', width: 40 },
            { header: 'Test Scenario / Objective', key: 'scenario', width: 80 },
            { header: 'Device / Target', key: 'device', width: 22 },
            { header: 'Execution Status', key: 'status', width: 18 },
            { header: 'Start Time', key: 'start', width: 26 },
            { header: 'End Time', key: 'end', width: 26 },
            { header: 'Duration (s)', key: 'duration', width: 16 }
        ];

        // Sheet 3 - Failed Tests
        this.failedTestsSheet.columns = [
            { header: 'Test ID / Name', key: 'testName', width: 55 },
            { header: 'Failure Exception / Reason', key: 'reason', width: 75 },
            { header: 'Screenshot Artifact', key: 'screenshot', width: 45 },
            { header: 'Target Device', key: 'device', width: 20 },
            { header: 'OS Version', key: 'os', width: 16 },
            { header: 'Activity / Screen', key: 'activity', width: 30 }
        ];

        // Sheet 4 - Execution Logs
        this.executionLogsSheet.columns = [
            { header: 'Timestamp', key: 'timestamp', width: 26 },
            { header: 'Test Case Title', key: 'testName', width: 55 },
            { header: 'Execution Step', key: 'step', width: 35 },
            { header: 'Step Result', key: 'result', width: 16 },
            { header: 'Remarks / Diagnostics', key: 'remarks', width: 55 }
        ];

        // Apply Header Styling across all sheets
        const headerFill = { type: 'pattern', pattern: 'solid', fgColor: { argb: '1F4E79' } };
        const headerFont = { name: 'Calibri', size: 11, bold: true, color: { argb: 'FFFFFF' } };

        [this.summarySheet, this.testCasesSheet, this.failedTestsSheet, this.executionLogsSheet].forEach(sheet => {
            const headerRow = sheet.getRow(1);
            headerRow.font = headerFont;
            headerRow.fill = headerFill;
            headerRow.alignment = { vertical: 'middle', horizontal: 'center' };
            headerRow.height = 26;
        });
    }

    addSummary(data) {
        this.summaryData = data;
    }

    addTestCase(data) {
        this.testCases.push(data);
    }

    addFailedTest(data) {
        this.failedTests.push(data);
    }

    addExecutionLog(data) {
        this.executionLogs.push(data);
    }

    async saveReport(customFilename = null) {
        // Build Summary Sheet Data
        if (this.summaryData) {
            const sumRows = [
                { metric: 'Project Name', value: 'CarePathAI Android Mobile Application' },
                { metric: 'Test Suite Type', value: 'Selenium / Appium E2E Automation Suite' },
                { metric: 'Execution Date', value: this.summaryData.date || new Date().toISOString().split('T')[0] },
                { metric: 'Device / Emulator Model', value: this.summaryData.device || 'Android Emulator (Pixel 7 Pro)' },
                { metric: 'Android Platform Version', value: this.summaryData.os || 'Android 14 (API 34)' },
                { metric: 'Total Executed Test Cases', value: this.summaryData.total },
                { metric: 'Passed Test Cases', value: this.summaryData.passed },
                { metric: 'Failed Test Cases', value: this.summaryData.failed },
                { metric: 'Skipped Test Cases', value: this.summaryData.skipped },
                { metric: 'Pass Percentage (%)', value: this.summaryData.percentage },
                { metric: 'Total Execution Duration', value: this.summaryData.duration }
            ];

            sumRows.forEach(row => {
                const addedRow = this.summarySheet.addRow(row);
                addedRow.font = { name: 'Calibri', size: 11 };
                addedRow.getCell('metric').font = { bold: true, color: { argb: '1F4E79' } };
            });
        }

        // Add Test Cases
        this.testCases.forEach(tc => {
            const row = this.testCasesSheet.addRow(tc);
            row.alignment = { vertical: 'middle' };
            
            const statusCell = row.getCell('status');
            statusCell.alignment = { vertical: 'middle', horizontal: 'center' };
            statusCell.font = { bold: true };

            if (tc.status === 'PASSED') {
                statusCell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'E2EFDA' } };
                statusCell.font = { bold: true, color: { argb: '375623' } };
            } else if (tc.status === 'FAILED') {
                statusCell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FCE4D6' } };
                statusCell.font = { bold: true, color: { argb: 'C65911' } };
            } else {
                statusCell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFF2CC' } };
                statusCell.font = { bold: true, color: { argb: '833C0C' } };
            }
        });

        // Add Failed Tests
        this.failedTests.forEach(ft => {
            this.failedTestsSheet.addRow(ft);
        });

        // Add Execution Logs
        this.executionLogs.forEach(el => {
            const row = this.executionLogsSheet.addRow(el);
            const resCell = row.getCell('result');
            resCell.alignment = { vertical: 'middle', horizontal: 'center' };
            resCell.font = { bold: true };
            if (el.result === 'PASSED') {
                resCell.font = { bold: true, color: { argb: '375623' } };
            } else if (el.result === 'FAILED') {
                resCell.font = { bold: true, color: { argb: 'C65911' } };
            }
        });

        const excelDir = path.join(process.cwd(), 'excel');
        if (!fs.existsSync(excelDir)) {
            fs.mkdirSync(excelDir, { recursive: true });
        }

        const now = new Date();
        const dateStr = now.toISOString().replace(/T/, '_').replace(/:/g, '-').split('.')[0];
        const filename = customFilename || `E2E_Test_Report_CarePathAI_${dateStr}.xlsx`;
        const filepath = path.join(excelDir, filename);

        await this.workbook.xlsx.writeFile(filepath);
        logger.info(`Excel Report generated successfully at: ${filepath}`);
        return filepath;
    }
}

module.exports = new ExcelReporter();
