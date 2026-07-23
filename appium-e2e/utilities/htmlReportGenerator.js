const fs = require('fs');
const path = require('path');

function generateHtmlReport(summaryData, testCases, loadTestData = null, customFilename = 'execution-report.html') {
    const reportDir = path.join(process.cwd(), 'reports');
    if (!fs.existsSync(reportDir)) {
        fs.mkdirSync(reportDir, { recursive: true });
    }

    const loadTestSection = loadTestData ? `
        <div class="k6-card">
            <h2>⚡ API Load Testing Performance Metrics (k6)</h2>
            <div class="k6-grid">
                <div class="k6-metric">
                    <span class="k6-label">Virtual Users (VUs)</span>
                    <span class="k6-value">${loadTestData.vus}</span>
                </div>
                <div class="k6-metric">
                    <span class="k6-label">Test Duration</span>
                    <span class="k6-value">${loadTestData.duration}</span>
                </div>
                <div class="k6-metric">
                    <span class="k6-label">Requests Per Second (RPS)</span>
                    <span class="k6-value highlight-green">${loadTestData.rps} req/sec</span>
                </div>
                <div class="k6-metric">
                    <span class="k6-label">Total Requests Sent</span>
                    <span class="k6-value">${loadTestData.totalRequests}</span>
                </div>
                <div class="k6-metric">
                    <span class="k6-label">Avg Response Time</span>
                    <span class="k6-value">${loadTestData.avgResponseTime}</span>
                </div>
                <div class="k6-metric">
                    <span class="k6-label">Min Response Time</span>
                    <span class="k6-value">${loadTestData.minResponseTime}</span>
                </div>
                <div class="k6-metric">
                    <span class="k6-label">Max Response Time</span>
                    <span class="k6-value">${loadTestData.maxResponseTime}</span>
                </div>
                <div class="k6-metric">
                    <span class="k6-label">p95 Response Time</span>
                    <span class="k6-value highlight-blue">${loadTestData.p95ResponseTime}</span>
                </div>
            </div>
        </div>
    ` : '';

    const htmlContent = `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CarePathAI - E2E Selenium & Load Test Execution Report</title>
    <style>
        :root {
            --bg-color: #0f172a;
            --card-bg: #1e293b;
            --text-primary: #f8fafc;
            --text-secondary: #94a3b8;
            --accent-green: #22c55e;
            --accent-blue: #3b82f6;
            --accent-purple: #a855f7;
            --border-color: #334155;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
            background-color: var(--bg-color);
            color: var(--text-primary);
            margin: 0;
            padding: 24px;
        }

        .container {
            max-width: 1280px;
            margin: 0 auto;
        }

        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding-bottom: 20px;
            border-bottom: 1px solid var(--border-color);
            margin-bottom: 24px;
        }

        .header h1 {
            font-size: 26px;
            margin: 0;
            background: linear-gradient(90deg, #60a5fa, #a855f7);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 16px;
            margin-bottom: 24px;
        }

        .stat-card {
            background: var(--card-bg);
            padding: 20px;
            border-radius: 12px;
            border: 1px solid var(--border-color);
            text-align: center;
        }

        .stat-card .number {
            font-size: 32px;
            font-weight: 700;
            margin-top: 8px;
        }

        .stat-card.passed .number { color: var(--accent-green); }
        .stat-card.rate .number { color: var(--accent-blue); }

        ${loadTestSection ? `
        .k6-card {
            background: #1e1b4b;
            border: 1px solid #4338ca;
            border-radius: 12px;
            padding: 20px;
            margin-bottom: 24px;
        }

        .k6-card h2 {
            margin-top: 0;
            font-size: 18px;
            color: #c7d2fe;
        }

        .k6-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
            gap: 12px;
        }

        .k6-metric {
            background: rgba(15, 23, 42, 0.6);
            padding: 12px;
            border-radius: 8px;
            text-align: center;
        }

        .k6-label {
            display: block;
            font-size: 12px;
            color: var(--text-secondary);
            margin-bottom: 4px;
        }

        .k6-value {
            font-size: 18px;
            font-weight: 600;
        }

        .highlight-green { color: #4ade80; }
        .highlight-blue { color: #38bdf8; }
        ` : ''}

        .table-container {
            background: var(--card-bg);
            border-radius: 12px;
            border: 1px solid var(--border-color);
            overflow: hidden;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;
        }

        th {
            background: #0f172a;
            color: var(--text-secondary);
            padding: 14px 16px;
            text-align: left;
            font-weight: 600;
            border-bottom: 1px solid var(--border-color);
        }

        td {
            padding: 12px 16px;
            border-bottom: 1px solid var(--border-color);
        }

        tr:hover {
            background: rgba(255, 255, 255, 0.02);
        }

        .badge-passed {
            background: rgba(34, 197, 94, 0.15);
            color: #4ade80;
            padding: 4px 10px;
            border-radius: 9999px;
            font-size: 12px;
            font-weight: 600;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <div>
                <h1>CarePathAI E2E & Load Testing Execution Report</h1>
                <p style="color: var(--text-secondary); margin: 4px 0 0 0;">Generated on ${summaryData.date || new Date().toISOString().split('T')[0]} | Device: ${summaryData.device || 'Android Emulator / Chrome'}</p>
            </div>
            <div>
                <span class="badge-passed" style="font-size: 14px; padding: 8px 16px;">Pass Rate: ${summaryData.percentage}</span>
            </div>
        </div>

        <div class="stats-grid">
            <div class="stat-card">
                <div style="color: var(--text-secondary);">Total Test Cases</div>
                <div class="number">${summaryData.total}</div>
            </div>
            <div class="stat-card passed">
                <div style="color: var(--text-secondary);">Passed Test Cases</div>
                <div class="number">${summaryData.passed}</div>
            </div>
            <div class="stat-card">
                <div style="color: var(--text-secondary);">Failed Test Cases</div>
                <div class="number" style="color: #ef4444;">${summaryData.failed || 0}</div>
            </div>
            <div class="stat-card rate">
                <div style="color: var(--text-secondary);">Execution Duration</div>
                <div class="number">${summaryData.duration}</div>
            </div>
        </div>

        ${loadTestSection}

        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th>Test ID</th>
                        <th>Category / Module</th>
                        <th>Test Scenario / Objective</th>
                        <th>Status</th>
                        <th>Duration</th>
                    </tr>
                </thead>
                <tbody>
                    ${testCases.slice(0, 150).map(tc => `
                        <tr>
                            <td style="font-family: monospace; color: var(--accent-blue);">${tc.id}</td>
                            <td style="font-weight: 500;">${tc.module}</td>
                            <td style="color: var(--text-secondary);">${tc.scenario}</td>
                            <td><span class="badge-passed">${tc.status}</span></td>
                            <td style="font-family: monospace; color: var(--text-secondary);">${tc.duration}</td>
                        </tr>
                    `).join('')}
                    ${testCases.length > 150 ? `
                        <tr>
                            <td colspan="5" style="text-align: center; padding: 16px; color: var(--text-secondary);">
                                ... and ${testCases.length - 150} more passing test cases in complete report suite ...
                            </td>
                        </tr>
                    ` : ''}
                </tbody>
            </table>
        </div>
    </div>
</body>
</html>`;

    const outputPath = path.join(reportDir, customFilename);
    fs.writeFileSync(outputPath, htmlContent, 'utf-8');
    
    // Also save directly to root reports/execution-report.html
    const rootReportPath = path.join(process.cwd(), 'execution-report.html');
    fs.writeFileSync(rootReportPath, htmlContent, 'utf-8');

    return outputPath;
}

module.exports = { generateHtmlReport };
