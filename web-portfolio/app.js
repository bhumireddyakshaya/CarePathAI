document.addEventListener('DOMContentLoaded', () => {
    // 1. Navigation Tab Switcher Logic
    const navBtns = document.querySelectorAll('.nav-btn');
    const featureViews = document.querySelectorAll('.feature-view');

    window.switchTab = function(tabName) {
        navBtns.forEach(btn => {
            if (btn.dataset.tab === tabName) btn.classList.add('active');
            else btn.classList.remove('active');
        });

        featureViews.forEach(view => {
            if (view.id === `view-${tabName}`) view.classList.add('active');
            else view.classList.remove('active');
        });

        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    navBtns.forEach(btn => {
        btn.addEventListener('click', () => switchTab(btn.dataset.tab));
    });

    // 2. Symptom Selection & AI Clinical Triage
    const chipItems = document.querySelectorAll('.chip-item');
    const selectedSymptoms = new Set();
    const symSeverity = document.getElementById('sym-severity');
    const symSeverityVal = document.getElementById('sym-severity-val');
    const btnRunTriage = document.getElementById('btn-run-triage');
    const symptomOutputBox = document.getElementById('symptom-output-box');

    chipItems.forEach(chip => {
        chip.addEventListener('click', () => {
            const sym = chip.dataset.sym;
            if (selectedSymptoms.has(sym)) {
                selectedSymptoms.delete(sym);
                chip.classList.remove('active');
            } else {
                selectedSymptoms.add(sym);
                chip.classList.add('active');
            }
        });
    });

    if (symSeverity) {
        symSeverity.addEventListener('input', (e) => {
            symSeverityVal.textContent = e.target.value;
        });
    }

    if (btnRunTriage) {
        btnRunTriage.addEventListener('click', () => {
            if (selectedSymptoms.size === 0) {
                symptomOutputBox.innerHTML = `
                    <div style="text-align: center; color: #fbbf24; padding: 20px;">
                        <i class="fa-solid fa-triangle-exclamation" style="font-size: 36px; margin-bottom: 10px;"></i>
                        <p style="font-weight: 600;">Please select at least 1 symptom chip before executing triage.</p>
                    </div>
                `;
                return;
            }

            const symptomsList = Array.from(selectedSymptoms);
            const severity = parseInt(symSeverity.value, 10);
            let riskTitle = 'LOW RISK TRIAGE';
            let riskClass = 'status-tag green';
            let color = '#4ade80';
            let guidance = 'Standard rest, hydration, and 48-hour monitoring recommended.';

            if (symptomsList.includes('Chest Pain') || symptomsList.includes('Shortness of Breath') || severity >= 8) {
                riskTitle = 'HIGH RISK (URGENT CARE)';
                riskClass = 'status-tag red';
                color = '#f87171';
                guidance = 'Immediate emergency department or urgent teleconsultation advised.';
            } else if (symptomsList.length >= 3 || severity >= 5) {
                riskTitle = 'MODERATE RISK';
                riskClass = 'status-tag orange';
                color = '#fbbf24';
                guidance = 'Schedule a telehealth virtual consult with CarePathAI primary physician within 24 hours.';
            }

            symptomOutputBox.innerHTML = `
                <div style="width: 100%;">
                    <h3 style="font-family: var(--font-heading); font-size: 22px; color: ${color}; margin-bottom: 10px;">
                        <i class="fa-solid fa-square-poll-vertical"></i> ${riskTitle}
                    </h3>
                    <div style="font-size: 14px; margin-bottom: 12px; color: #f8fafc;">
                        <strong>Evaluated Symptoms:</strong> ${symptomsList.join(', ')} (Severity Index: ${severity}/10)
                    </div>
                    <div style="font-size: 13px; color: #cbd5e1; background: rgba(15, 23, 42, 0.8); padding: 14px; border-radius: 10px; border-left: 4px solid ${color}; margin-bottom: 16px;">
                        <i class="fa-solid fa-user-md" style="color: ${color}; margin-right: 8px;"></i>
                        <strong>Clinical Recommendation:</strong> ${guidance}
                    </div>
                    <button class="btn btn-primary btn-block" onclick="switchTab('doctors')">
                        <i class="fa-solid fa-user-doctor"></i> Book Doctor Consultation Now
                    </button>
                </div>
            `;
        });
    }

    // 3. AI Chatbot Conversation
    const chatUserInput = document.getElementById('chat-user-input');
    const btnSendChat = document.getElementById('btn-send-chat');
    const chatMessagesArea = document.getElementById('chat-messages-area');
    const promptBtns = document.querySelectorAll('.prompt-btn');

    function sendChatMessage(textOverride = null) {
        const text = textOverride || (chatUserInput ? chatUserInput.value.trim() : '');
        if (!text) return;

        const userMsg = document.createElement('div');
        userMsg.className = 'chat-message user';
        userMsg.innerHTML = `
            <div class="msg-avatar"><i class="fa-solid fa-user"></i></div>
            <div class="msg-content">${escapeHtml(text)}</div>
        `;
        chatMessagesArea.appendChild(userMsg);
        if (chatUserInput) chatUserInput.value = '';
        chatMessagesArea.scrollTop = chatMessagesArea.scrollHeight;

        setTimeout(() => {
            let responseText = "CarePathAI virtual nurse has recorded your inquiry. For urgent symptoms, use the SOS tab.";
            const q = text.toLowerCase();

            if (q.includes('fever')) {
                reply = "For high fever, maintain adequate oral hydration, apply cool compresses, and monitor body temperature every 4 hours.";
            } else if (q.includes('paracetamol')) {
                reply = "Standard adult Paracetamol dosage is 500mg - 1000mg every 4-6 hours as needed (maximum 4000mg per 24 hours).";
            } else if (q.includes('doctor') || q.includes('consultation')) {
                reply = "You can instantly browse accredited cardiologists and general physicians in the 'Doctors' tab above!";
            } else {
                reply = "Thank you for reaching out. CarePathAI offers automated symptom triaging, pill alarms, and encrypted FHIR health records.";
            }

            const botMsg = document.createElement('div');
            botMsg.className = 'chat-message bot';
            botMsg.innerHTML = `
                <div class="msg-avatar"><i class="fa-solid fa-user-nurse"></i></div>
                <div class="msg-content">${reply}</div>
            `;
            chatMessagesArea.appendChild(botMsg);
            chatMessagesArea.scrollTop = chatMessagesArea.scrollHeight;
        }, 500);
    }

    if (btnSendChat) {
        btnSendChat.addEventListener('click', () => sendChatMessage());
        chatUserInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') sendChatMessage();
        });
    }

    promptBtns.forEach(p => {
        p.addEventListener('click', () => sendChatMessage(p.dataset.text));
    });

    // 4. Medicine Status Toggle
    const statusBtns = document.querySelectorAll('.btn-status');
    statusBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            if (btn.classList.contains('pending')) {
                btn.classList.remove('pending');
                btn.classList.add('taken');
                btn.textContent = '✔ TAKEN';
            } else {
                btn.classList.remove('taken');
                btn.classList.add('pending');
                btn.textContent = '⏳ PENDING';
            }
        });
    });

    // 5. Emergency SOS Trigger
    document.getElementById('btn-trigger-sos')?.addEventListener('click', () => {
        alert('🚨 EMERGENCY SOS BEACON ACTIVATED!\nBroadcasting GPS coordinates & CarePathAI encrypted medical vault profile to emergency contacts & dispatchers.');
    });

    // 6. FHIR R4 JSON Export Simulation
    document.getElementById('btn-export-fhir')?.addEventListener('click', () => {
        const fhirJson = {
            resourceType: "Patient",
            id: "carepath-patient-001",
            name: [{ family: "Bhumireddy", given: ["Akshaya"] }],
            gender: "female",
            birthDate: "2000-01-01",
            address: [{ city: "Bangalore", country: "IN" }],
            observation: [
                { code: "Daily Wellness Score", value: "85/100" },
                { code: "Medicine Adherence", value: "75%" }
            ]
        };

        const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(fhirJson, null, 2));
        const dlAnchor = document.createElement('a');
        dlAnchor.setAttribute("href", dataStr);
        dlAnchor.setAttribute("download", "CarePathAI_FHIR_Patient_Record.json");
        document.body.appendChild(dlAnchor);
        dlAnchor.click();
        dlAnchor.remove();
    });

    function escapeHtml(str) {
        return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }
});
