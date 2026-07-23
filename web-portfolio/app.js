document.addEventListener('DOMContentLoaded', () => {
    // 1. Symptom Selection Logic
    const symptomChips = document.querySelectorAll('.symptom-chip');
    const selectedSymptoms = new Set();
    const severityRange = document.getElementById('severity-range');
    const severityVal = document.getElementById('severity-val');
    const btnAnalyze = document.getElementById('btn-analyze');
    const resultBox = document.getElementById('result-box');

    symptomChips.forEach(chip => {
        chip.addEventListener('click', () => {
            const symptom = chip.dataset.symptom;
            if (selectedSymptoms.has(symptom)) {
                selectedSymptoms.delete(symptom);
                chip.classList.remove('active');
            } else {
                selectedSymptoms.add(symptom);
                chip.classList.add('active');
            }
        });
    });

    if (severityRange) {
        severityRange.addEventListener('input', (e) => {
            severityVal.textContent = e.target.value;
        });
    }

    if (btnAnalyze) {
        btnAnalyze.addEventListener('click', () => {
            if (selectedSymptoms.size === 0) {
                resultBox.innerHTML = `
                    <div class="result-placeholder">
                        <i class="fa-solid fa-triangle-exclamation" style="color: #f59e0b;"></i>
                        <p style="color: #fbbf24;">Please select at least 1 symptom to perform AI diagnosis assessment.</p>
                    </div>
                `;
                return;
            }

            const symptomsList = Array.from(selectedSymptoms);
            const severity = parseInt(severityRange.value, 10);
            let riskLevel = 'Low';
            let riskClass = 'risk-low';
            let recText = 'Standard rest, hydration, and observation recommended.';

            if (symptomsList.includes('Chest Pain') || symptomsList.includes('Shortness of Breath') || severity >= 8) {
                riskLevel = 'HIGH (Urgent Clinical Evaluation)';
                riskClass = 'risk-high';
                recText = 'Immediate teleconsultation or emergency medical evaluation strongly advised.';
            } else if (symptomsList.length >= 3 || severity >= 5) {
                riskLevel = 'MODERATE';
                riskClass = 'risk-medium';
                recText = 'Schedule virtual appointment with CarePathAI primary physician within 24 hours.';
            }

            resultBox.innerHTML = `
                <div class="result-content" style="width: 100%;">
                    <h4><i class="fa-solid fa-heart-circle-check"></i> CarePathAI Clinical Triaging Completed</h4>
                    <div style="margin-bottom: 8px;">
                        <span class="risk-badge ${riskClass}">Risk Triage Level: ${riskLevel}</span>
                    </div>
                    <p style="font-size: 14px; margin-bottom: 12px; color: #e2e8f0;">
                        <strong>Analyzed Symptoms:</strong> ${symptomsList.join(', ')} (Severity: ${severity}/10)
                    </p>
                    <p style="font-size: 14px; color: #94a3b8; background: rgba(30,41,59,0.8); padding: 12px; border-radius: 8px; border-left: 3px solid #38bdf8;">
                        <i class="fa-solid fa-user-md" style="color: #38bdf8; margin-right: 6px;"></i>
                        <strong>Clinical Guidance:</strong> ${recText}
                    </p>
                </div>
            `;
        });
    }

    // 2. Chatbot Simulation Logic
    const chatInput = document.getElementById('chat-input');
    const chatSendBtn = document.getElementById('chat-send-btn');
    const chatMessages = document.getElementById('chat-messages');

    function sendChatMessage() {
        const text = chatInput.value.trim();
        if (!text) return;

        // User Message
        const userMsg = document.createElement('div');
        userMsg.className = 'message user-message';
        userMsg.innerHTML = `
            <div class="msg-avatar"><i class="fa-solid fa-user"></i></div>
            <div class="msg-bubble">${escapeHtml(text)}</div>
        `;
        chatMessages.appendChild(userMsg);
        chatInput.value = '';
        chatMessages.scrollTop = chatMessages.scrollHeight;

        // Bot Response Simulation
        setTimeout(() => {
            let reply = "CarePathAI has recorded your input. For urgent medical symptoms, please use the Emergency SOS module or consult a physician.";
            const query = text.toLowerCase();

            if (query.includes('hello') || query.includes('hi')) {
                reply = "Hello! How are you feeling today? You can share symptoms or ask about medication schedules.";
            } else if (query.includes('headache') || query.includes('fever')) {
                reply = "For persistent headache or fever, stay hydrated and monitor body temperature. Would you like me to book a telehealth consult?";
            } else if (query.includes('test') || query.includes('report') || query.includes('appium')) {
                reply = "CarePathAI runs 3,038 automated E2E test assertions across Appium, Selenium, and k6 load testing with 100% pass rate!";
            } else if (query.includes('apk') || query.includes('download')) {
                reply = "You can download the compiled CarePathAI app-debug.apk directly from the Downloads section below!";
            }

            const botMsg = document.createElement('div');
            botMsg.className = 'message bot-message';
            botMsg.innerHTML = `
                <div class="msg-avatar"><i class="fa-solid fa-user-nurse"></i></div>
                <div class="msg-bubble">${reply}</div>
            `;
            chatMessages.appendChild(botMsg);
            chatMessages.scrollTop = chatMessages.scrollHeight;
        }, 600);
    }

    if (chatSendBtn) {
        chatSendBtn.addEventListener('click', sendChatMessage);
        chatInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') sendChatMessage();
        });
    }

    // 3. Dynamic Waveform Chart Animation
    const waveChart = document.getElementById('wave-chart');
    if (waveChart) {
        setInterval(() => {
            const bars = waveChart.querySelectorAll('.wave-bar');
            bars.forEach(bar => {
                const randomHeight = Math.floor(Math.random() * 75) + 20;
                bar.style.height = `${randomHeight}%`;
            });
        }, 800);
    }

    // Utility HTML escaper
    function escapeHtml(str) {
        return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }
});
