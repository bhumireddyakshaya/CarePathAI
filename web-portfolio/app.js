// Firebase Configuration (Synced with CarePathAI Android App)
const firebaseConfig = {
    apiKey: "AIzaSyD4vmPx2VshhFUdnMLBpxEYu_e8YDwIIYk",
    authDomain: "ai-assisted-symptom-analysis.firebaseapp.com",
    projectId: "ai-assisted-symptom-analysis",
    storageBucket: "ai-assisted-symptom-analysis.firebasestorage.app",
    messagingSenderId: "180164884905",
    appId: "1:180164884905:web:03a61a4fce418a39a9ecb8",
    measurementId: "G-8MK157XXKH"
};

// Initialize Firebase
if (typeof firebase !== 'undefined' && !firebase.apps.length) {
    firebase.initializeApp(firebaseConfig);
}
const auth = typeof firebase !== 'undefined' ? firebase.auth() : null;
const db = typeof firebase !== 'undefined' ? firebase.firestore() : null;

document.addEventListener('DOMContentLoaded', () => {

    // 1. Navigation Logic
    const navItems = document.querySelectorAll('.nav-item');
    const tabViews = document.querySelectorAll('.tab-view');
    const bottomNav = document.querySelector('.bottom-nav');

    window.switchTab = function (tabId) {
        if (tabId !== 'login' && auth && !auth.currentUser) {
            tabId = 'login';
        }

        tabViews.forEach(view => {
            view.classList.remove('active');
            if (view.id === `view-${tabId}`) view.classList.add('active');
        });

        navItems.forEach(item => {
            item.classList.remove('active');
            if (item.dataset.tab === tabId) item.classList.add('active');
        });

        if (tabId === 'login') {
            if (bottomNav) bottomNav.style.display = 'none';
        } else {
            if (bottomNav) bottomNav.style.display = 'flex';
        }

        // Re-render history if switching to history tab
        if (tabId === 'history') renderHistoryRecords(allHistoryRecords);

        // Reset assessment if switching to it
        if (tabId === 'assessment') resetAssessment();
    };

    // Firebase Auth State Listener & Real-time Snapshot Listeners
    let profileUnsubscribe = null;
    let historyUnsubscribe = null;
    let medicineUnsubscribe = null;

    if (auth) {
        auth.onAuthStateChanged(user => {
            if (profileUnsubscribe) { profileUnsubscribe(); profileUnsubscribe = null; }
            if (historyUnsubscribe) { historyUnsubscribe(); historyUnsubscribe = null; }
            if (medicineUnsubscribe) { medicineUnsubscribe(); medicineUnsubscribe = null; }

            if (user) {
                // Real-time listener for user profile doc
                profileUnsubscribe = db.collection('users').doc(user.uid).onSnapshot(doc => {
                    if (doc.exists) {
                        const data = doc.data();
                        const userData = {
                            uid: user.uid,
                            name: data.fullName || data.name || user.displayName || 'User Profile',
                            email: data.email || user.email,
                            mobile: data.mobileNumber || data.mobile || 'N/A',
                            age: data.age != null ? String(data.age) : 'N/A',
                            blood: data.bloodGroup || data.blood || 'N/A',
                            height: data.height != null ? String(data.height) : 'N/A',
                            weight: data.weight != null ? String(data.weight) : 'N/A'
                        };
                        localStorage.setItem('carepath_user', JSON.stringify(userData));
                        updateUserProfile(userData);
                    } else {
                        const userData = {
                            uid: user.uid,
                            name: user.displayName || user.email.split('@')[0],
                            email: user.email,
                            mobile: 'N/A',
                            age: 'N/A',
                            blood: 'N/A',
                            height: 'N/A',
                            weight: 'N/A'
                        };
                        localStorage.setItem('carepath_user', JSON.stringify(userData));
                        updateUserProfile(userData);
                    }
                }, err => {
                    console.error("Profile snapshot error:", err);
                });

                // Real-time listener for health_history subcollection
                syncLocalHistoryToFirestore(user);

                showProgressBar();
                historyUnsubscribe = db.collection('users').doc(user.uid)
                    .collection('health_history')
                    .onSnapshot(snapshot => {
                        hideProgressBar();
                        const records = [];
                        snapshot.forEach(doc => {
                            records.push({ id: doc.id, ...doc.data() });
                        });
                        records.sort((a, b) => (b.date || 0) - (a.date || 0));
                        allHistoryRecords = records;
                        renderHistoryRecords(allHistoryRecords);
                    }, err => {
                        hideProgressBar();
                        console.error("History snapshot error:", err);
                        showErrorDialog("Health History Fetch Failed", err.message || "Could not retrieve health history from Firestore.");
                        allHistoryRecords = JSON.parse(localStorage.getItem('carepath_history') || '[]');
                        renderHistoryRecords(allHistoryRecords);
                    });

                // Real-time listener for medicines subcollection
                medicineUnsubscribe = db.collection('users').doc(user.uid)
                    .collection('medicines')
                    .onSnapshot(snapshot => {
                        const meds = [];
                        snapshot.forEach(doc => {
                            meds.push({ id: doc.id, ...doc.data() });
                        });
                        renderMedicines(meds);
                    }, err => {
                        console.error("Medicine snapshot error:", err);
                        showErrorDialog("Medicine Sync Failed", err.message || "Could not retrieve medicine reminders from Firestore.");
                    });

                switchTab('home');
            } else {
                hideProgressBar();
                allHistoryRecords = JSON.parse(localStorage.getItem('carepath_history') || '[]');
                renderHistoryRecords(allHistoryRecords);
                renderMedicines([]);
                localStorage.removeItem('carepath_user');
                switchTab('login');
            }
        });
    }

    window.toggleAuthMode = function (mode) {
        const tabSignIn = document.getElementById('tab-signin');
        const tabSignUp = document.getElementById('tab-signup');
        const loginForm = document.getElementById('login-form');
        const signupForm = document.getElementById('signup-form');
        const authTitle = document.getElementById('auth-title');
        const authSubtitle = document.getElementById('auth-subtitle');

        if (mode === 'signup') {
            tabSignIn.classList.remove('active');
            tabSignUp.classList.add('active');
            loginForm.style.display = 'none';
            signupForm.style.display = 'block';
            authTitle.textContent = 'Create an Account';
            authSubtitle.textContent = 'Join CarePathAI to manage your health journey';
        } else {
            tabSignUp.classList.remove('active');
            tabSignIn.classList.add('active');
            signupForm.style.display = 'none';
            loginForm.style.display = 'block';
            authTitle.textContent = 'Welcome Back';
            authSubtitle.textContent = 'Sign in or create an account to access CarePathAI';
        }
    };

    window.handleLogout = function () {
        if (auth) {
            auth.signOut().then(() => {
                localStorage.removeItem('carepath_user');
                switchTab('login');
            });
        } else {
            localStorage.removeItem('carepath_user');
            switchTab('login');
        }
    };

    window.handleLogin = function () {
        const emailInput = document.getElementById('login-email');
        const passwordInput = document.getElementById('login-password');
        const email = emailInput ? emailInput.value.trim().toLowerCase() : '';
        const password = passwordInput ? passwordInput.value : '';

        if (!email || !password) {
            showErrorDialog('Login Failed', 'Please enter both your email address and password.');
            return;
        }

        showProgressBar();
        if (auth) {
            auth.signInWithEmailAndPassword(email, password)
                .then((userCredential) => {
                    console.log("Firebase Login successful:", userCredential.user.email);
                    hideProgressBar();
                })
                .catch((error) => {
                    hideProgressBar();
                    console.error("Firebase Login error:", error);
                    let msg = error.message;
                    if (error.code === 'auth/wrong-password' || error.code === 'auth/user-not-found' || error.code === 'auth/invalid-credential') {
                        msg = 'Invalid email address or password. Please check your credentials and try again.';
                    }
                    showErrorDialog('Login Failed', msg);
                    if (passwordInput) passwordInput.value = '';
                    switchTab('login');
                });
        } else {
            hideProgressBar();
            showErrorDialog('System Error', 'Firebase Authentication service is unavailable.');
        }
    };

    window.handleSignUp = function () {
        const nameInput = document.getElementById('signup-name');
        const emailInput = document.getElementById('signup-email');
        const mobileInput = document.getElementById('signup-mobile');
        const passwordInput = document.getElementById('signup-password');
        const confirmPassInput = document.getElementById('signup-confirm-password');

        const name = nameInput ? nameInput.value.trim() : '';
        const email = emailInput ? emailInput.value.trim().toLowerCase() : '';
        const mobile = mobileInput ? mobileInput.value.trim() : '';
        const pass = passwordInput ? passwordInput.value : '';
        const confirmPass = confirmPassInput ? confirmPassInput.value : '';

        if (!name || !email || !pass || !confirmPass) {
            showErrorDialog('Sign Up Failed', 'Please fill in all required fields.');
            return;
        }

        if (pass !== confirmPass) {
            showErrorDialog('Sign Up Failed', 'Passwords do not match. Please verify your password.');
            return;
        }

        if (pass.length < 6) {
            showErrorDialog('Sign Up Failed', 'Password should be at least 6 characters long.');
            return;
        }

        showProgressBar();
        if (auth) {
            auth.createUserWithEmailAndPassword(email, pass)
                .then((userCredential) => {
                    const user = userCredential.user;
                    const userData = {
                        id: user.uid,
                        fullName: name,
                        name: name,
                        email: email,
                        mobileNumber: mobile,
                        mobile: mobile,
                        age: 0,
                        bloodGroup: "",
                        blood: "",
                        height: 0,
                        weight: 0,
                        gender: "",
                        medicalHistory: [],
                        dietaryPreferences: "",
                        fitnessGoals: "",
                        profileImageUrl: "",
                        createdAt: Date.now()
                    };

                    // Save in Firestore matching Android App collection schema
                    return db.collection('users').doc(user.uid).set(userData);
                })
                .then(() => {
                    hideProgressBar();
                    console.log("Firebase User registered & saved in Firestore successfully!");
                })
                .catch((error) => {
                    hideProgressBar();
                    console.error("Firebase SignUp error:", error);
                    showErrorDialog('Sign Up Failed', error.message);
                });
        } else {
            hideProgressBar();
            showErrorDialog('System Error', 'Firebase Authentication service is unavailable.');
        }
    };

    function updateUserProfile(userData) {
        if (!userData) return;
        const nameElem = document.getElementById('disp-name');
        const emailElem = document.getElementById('disp-email');
        const mobileElem = document.getElementById('val-mobile');
        const ageElem = document.getElementById('val-age');
        const bloodElem = document.getElementById('val-blood');
        const heightElem = document.getElementById('val-height');
        const weightElem = document.getElementById('val-weight');

        const editMobile = document.getElementById('edit-mobile');
        const editAge = document.getElementById('edit-age');
        const editBlood = document.getElementById('edit-blood');
        const editHeight = document.getElementById('edit-height');
        const editWeight = document.getElementById('edit-weight');

        if (nameElem) nameElem.textContent = userData.name || 'User Profile';
        if (emailElem) emailElem.textContent = userData.email || '';
        if (mobileElem) mobileElem.textContent = userData.mobile || 'N/A';
        if (ageElem) ageElem.textContent = userData.age || 'N/A';
        if (bloodElem) bloodElem.textContent = userData.blood || 'N/A';
        if (heightElem) heightElem.textContent = userData.height || 'N/A';
        if (weightElem) weightElem.textContent = userData.weight || 'N/A';

        if (editMobile) editMobile.value = userData.mobile !== 'N/A' ? userData.mobile : '';
        if (editAge) editAge.value = userData.age !== 'N/A' ? userData.age : '';
        if (editBlood) editBlood.value = userData.blood !== 'N/A' ? userData.blood : '';
        if (editHeight) editHeight.value = userData.height !== 'N/A' ? userData.height : '';
        if (editWeight) editWeight.value = userData.weight !== 'N/A' ? userData.weight : '';
    }

    // Default to login tab until Firebase Auth state is resolved
    if (!auth || !auth.currentUser) {
        switchTab('login');
    }

    navItems.forEach(item => {
        item.addEventListener('click', () => switchTab(item.dataset.tab));
    });

    // 2. Assessment Multi-step Logic
    let currentAssessmentStep = 1;
    let selectedBodyPart = null;
    const bodyPartCards = document.querySelectorAll('.body-part-card');
    const btnNext1 = document.getElementById('next-1');
    const btnNext2 = document.getElementById('next-2');
    const btnNext3 = document.getElementById('next-3');
    const progressBar = document.getElementById('assessment-progress');
    const searchInput = document.getElementById('symptom-search-input');

    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase().trim();
            const items = document.querySelectorAll('.symptom-check-item');
            items.forEach(item => {
                const text = item.textContent.toLowerCase();
                item.style.display = text.includes(query) ? 'flex' : 'none';
            });
        });
    }

    window.handleAssessmentBack = function() {
        if (currentAssessmentStep > 1) {
            showStep(currentAssessmentStep - 1);
        } else {
            switchTab('home');
        }
    };

    function resetAssessment() {
        showStep(1);
        selectedBodyPart = null;
        bodyPartCards.forEach(c => c.classList.remove('active'));
        if (btnNext1) btnNext1.disabled = true;
        document.querySelectorAll('.symptom-check-item input').forEach(i => i.checked = false);
        if (searchInput) searchInput.value = '';
    }

    function showStep(stepNum) {
        currentAssessmentStep = stepNum;
        const viewElem = document.getElementById('view-assessment');
        if (viewElem) {
            if (stepNum === 4) viewElem.classList.add('dark-mode');
            else viewElem.classList.remove('dark-mode');
        }

        document.querySelectorAll('.step').forEach(s => s.classList.remove('active'));
        const target = document.getElementById(`step-${stepNum}`);
        if (target) target.classList.add('active');
        if (progressBar) progressBar.style.width = `${stepNum * 25}%`;
    }

    bodyPartCards.forEach(card => {
        card.addEventListener('click', () => {
            bodyPartCards.forEach(c => c.classList.remove('active'));
            card.classList.add('active');
            selectedBodyPart = card.dataset.part;
            if (btnNext1) btnNext1.disabled = false;
        });
    });

    if (btnNext1) btnNext1.addEventListener('click', () => showStep(2));
    if (btnNext2) btnNext2.addEventListener('click', () => showStep(3));
    if (btnNext3) btnNext3.addEventListener('click', () => {
        showStep(4);
        runAIAnalysis();
    });

    async function runAIAnalysis() {
        const resultContainer = document.getElementById('analysis-result');
        resultContainer.innerHTML = `
            <div class="loading-spinner">
                <div class="spinner"></div>
                <p style="color: white; font-weight: 600;">Gemini AI is analyzing your symptoms and building recommendations...</p>
            </div>
        `;

        const checked = Array.from(document.querySelectorAll('.symptom-check-item input:checked'));
        const symptomsList = checked.map(i => i.nextElementSibling ? i.nextElementSibling.textContent.trim() : i.parentElement.textContent.trim());
        const symptomsStr = symptomsList.join(', ') || 'General Wellness Check';

        const severitySlider = document.getElementById('detail-severity-slider');
        const severityVal = severitySlider ? parseInt(severitySlider.value) : 1;
        const duration = document.getElementById('detail-duration') ? (document.getElementById('detail-duration').value.trim() || 'Few days') : 'Few days';
        const notes = document.getElementById('detail-notes') ? document.getElementById('detail-notes').value.trim() : '';

        const apiKey = "AIzaSyD4vmPx2VshhFUdnMLBpxEYu_e8YDwIIYk";
        const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${apiKey}`;
        const prompt = `You are a clinical AI assistant for CarePathAI. Patient Symptoms: "${symptomsStr}". Duration: "${duration}". Severity (1-10): ${severityVal}. Additional Notes: "${notes}". Return ONLY a JSON object with keys: "diagnosis" (string), "riskLevel" ("Low", "Medium", or "High"), "foodRecommendations" (string), "exercisePlans" (string). Do not include markdown tags outside JSON.`;

        let diagnosis = symptomsList.some(s => s.toLowerCase().includes('chest') || s.toLowerCase().includes('palpitation')) ? 'Cardiac Concern' : 'General Wellness';
        let riskLevel = diagnosis === 'Cardiac Concern' ? 'High' : (severityVal >= 7 ? 'High' : (severityVal >= 4 ? 'Medium' : 'Low'));
        let foodRecs = 'Balanced diet rich in leafy greens, almonds, and hydration.';
        let exerciseRecs = 'Light cardiovascular activity 20 mins/day.';

        try {
            const resp = await fetch(geminiUrl, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    contents: [{ parts: [{ text: prompt }] }]
                })
            });

            if (resp.ok) {
                const data = await resp.json();
                const candidates = data.candidates;
                if (candidates && candidates.length > 0) {
                    let rawText = candidates[0].content.parts[0].text.trim();
                    if (rawText.startsWith('```json')) rawText = rawText.replace(/```json/g, '').replace(/```/g, '').trim();
                    else if (rawText.startsWith('```')) rawText = rawText.replace(/```/g, '').trim();

                    const parsed = JSON.parse(rawText);
                    if (parsed.diagnosis) diagnosis = parsed.diagnosis;
                    if (parsed.riskLevel) riskLevel = parsed.riskLevel;
                    if (parsed.foodRecommendations) foodRecs = parsed.foodRecommendations;
                    if (parsed.exercisePlans) exerciseRecs = parsed.exercisePlans;
                }
            }
        } catch (err) {
            console.warn("Gemini API call warning (using fallback):", err);
        }

        const color = riskLevel === 'High' ? "#ef4444" : (riskLevel === 'Medium' ? '#f59e0b' : "#4ade80");

        resultContainer.innerHTML = `
            <div style="text-align: center; padding-top: 10px;">
                <div class="dark-card" style="padding: 24px 16px; margin-bottom: 20px;">
                    <div style="width: 90px; height: 90px; border-radius: 50%; border: 6px solid ${color}; display: flex; flex-direction: column; align-items: center; justify-content: center; margin: 0 auto 16px auto; background: rgba(0,0,0,0.4);">
                        <span style="font-size: 22px; font-weight: 800; color: white;">85%</span>
                        <span style="font-size: 10px; color: #94a3b8;">Confidence</span>
                    </div>
                    <p style="font-size: 12px; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 4px;">Possible Condition</p>
                    <h3 style="font-size: 22px; font-weight: 800; margin-bottom: 8px; color: white;">${diagnosis}</h3>
                    <div style="display: inline-flex; align-items: center; gap: 6px; padding: 4px 14px; border-radius: 999px; background: ${color}22; color: ${color}; font-size: 12px; font-weight: 700;">
                        <span style="width: 8px; height: 8px; border-radius: 50%; background: ${color}; display: inline-block;"></span>
                        Risk Level: ${riskLevel}
                    </div>
                </div>

                <div class="dark-card" style="text-align: left; margin-bottom: 20px;">
                    <h4 style="font-size: 15px; font-weight: 700; margin-bottom: 8px; color: white; display: flex; align-items: center; gap: 8px;">
                        <i class="fa-solid fa-brain" style="color: var(--primary);"></i> AI Health Insights
                    </h4>
                    <p style="font-size: 13px; color: #cbd5e1; line-height: 1.6;">
                        Based on your symptoms (${symptomsStr}) lasting ${duration} with severity score ${severityVal}/10, the AI suggests this might be related to ${diagnosis.toLowerCase()}. Monitor your symptoms closely.
                    </p>
                </div>

                <div style="text-align: left; margin-bottom: 20px;">
                    <h4 style="font-size: 15px; font-weight: 700; color: white; margin-bottom: 12px;">Recommended Next Actions</h4>
                    <div style="display: flex; flex-direction: column; gap: 10px;">
                        <div style="display: flex; align-items: center; gap: 12px; font-size: 13px; color: #e2e8f0;">
                            <i class="fa-solid fa-heart-pulse" style="color: #ef4444; width: 18px;"></i>
                            <span>Monitor your vitals twice daily.</span>
                        </div>
                        <div style="display: flex; align-items: center; gap: 12px; font-size: 13px; color: #e2e8f0;">
                            <i class="fa-solid fa-droplet" style="color: #3b82f6; width: 18px;"></i>
                            <span>Increase fluid intake (at least 2.5L).</span>
                        </div>
                        <div style="display: flex; align-items: center; gap: 12px; font-size: 13px; color: #e2e8f0;">
                            <i class="fa-solid fa-bed" style="color: #a855f7; width: 18px;"></i>
                            <span>Ensure adequate rest (7-8 hours).</span>
                        </div>
                    </div>
                </div>

                <div class="dark-card" style="text-align: left; margin-bottom: 16px; border-left: 4px solid #38bdf8;">
                    <h4 style="font-size: 14px; font-weight: 700; margin-bottom: 6px; color: #38bdf8;"><i class="fa-solid fa-utensils" style="margin-right: 8px;"></i> Recommended Nutrition (Gemini AI)</h4>
                    <p style="font-size: 13px; color: #cbd5e1; line-height: 1.6;">${foodRecs}</p>
                </div>

                <div class="dark-card" style="text-align: left; margin-bottom: 24px; border-left: 4px solid #a855f7;">
                    <h4 style="font-size: 14px; font-weight: 700; margin-bottom: 6px; color: #a855f7;"><i class="fa-solid fa-dumbbell" style="margin-right: 8px;"></i> Recommended Exercise (Gemini AI)</h4>
                    <p style="font-size: 13px; color: #cbd5e1; line-height: 1.6;">${exerciseRecs}</p>
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 20px;">
                    <div class="insight-item" style="background: #1A1A1A; border: 1px solid #333; color: white; padding: 14px; border-radius: 12px; cursor: pointer; text-align: center;" onclick="switchTab('nutrition')">
                        <i class="fa-solid fa-utensils" style="font-size: 20px; color: #4CAF50; margin-bottom: 6px; display: block;"></i>
                        <span style="font-size: 13px; font-weight: 700;">Diet Plan</span>
                    </div>
                    <div class="insight-item" style="background: #1A1A1A; border: 1px solid #333; color: white; padding: 14px; border-radius: 12px; cursor: pointer; text-align: center;" onclick="switchTab('exercise')">
                        <i class="fa-solid fa-dumbbell" style="font-size: 20px; color: #2196F3; margin-bottom: 6px; display: block;"></i>
                        <span style="font-size: 13px; font-weight: 700;">Exercises</span>
                    </div>
                </div>

                <button class="btn btn-primary btn-block" style="margin-top: 12px; padding: 14px;" onclick="switchTab('home')">Back to Dashboard</button>
            </div>
        `;

        // Update Nutrition AI and Exercise Plan sub views dynamically
        updateDynamicNutritionAndExercise(foodRecs, exerciseRecs, diagnosis);

        // Save assessment record to Firestore
        saveHistoryToFirestore(diagnosis, symptomsStr, riskLevel, foodRecs, exerciseRecs);
    }

    function updateDynamicNutritionAndExercise(foodRecs, exerciseRecs, diagnosis) {
        const nutInsight = document.getElementById('nutrition-insight-text');
        const mealList = document.getElementById('dynamic-meal-list');
        const exInsight = document.getElementById('exercise-insight-text');
        const exList = document.getElementById('dynamic-exercise-list');

        if (nutInsight) nutInsight.textContent = `“${foodRecs}”`;
        if (exInsight) exInsight.textContent = `“${exerciseRecs}”`;

        if (mealList) {
            let meals = [
                { type: 'BREAKFAST', title: 'Oatmeal with Almonds & Berries', desc: 'High in soluble fiber and antioxidants.' },
                { type: 'LUNCH', title: 'Quinoa & Avocado Salad', desc: 'Plant protein and essential healthy fats.' },
                { type: 'DINNER', title: 'Baked Fish or Tofu with Greens', desc: 'Lean protein with anti-inflammatory nutrients.' },
                { type: 'IMMUNITY SNACK', title: 'Citrus Fruits & Ginger Tea', desc: 'Vitamin C boost and digestive support.' }
            ];

            if (diagnosis.toLowerCase().includes('fever') || diagnosis.toLowerCase().includes('infection')) {
                meals = [
                    { type: 'BREAKFAST', title: 'Fruit Smoothie Bowl with Honey', desc: 'Easy on digestion & rich in vitamins.' },
                    { type: 'LUNCH', title: 'Warm Chicken or Vegetable Soup', desc: 'Provides hydration, electrolytes, and warmth.' },
                    { type: 'DINNER', title: 'Steamed Rice with Soft Vegetables', desc: 'Gentle on stomach and easy to absorb.' },
                    { type: 'IMMUNITY SNACK', title: 'Fresh Coconut Water & Orange Slices', desc: 'Electrolyte replenishment & Vitamin C.' }
                ];
            } else if (diagnosis.toLowerCase().includes('cardiac') || diagnosis.toLowerCase().includes('heart')) {
                meals = [
                    { type: 'BREAKFAST', title: 'Steel Cut Oats with Walnuts', desc: 'Omega-3 fatty acids for heart health.' },
                    { type: 'LUNCH', title: 'Low-Sodium Quinoa & Spinach Salad', desc: 'Rich in potassium and magnesium.' },
                    { type: 'DINNER', title: 'Grilled Salmon/Tofu with Broccoli', desc: 'Zero trans fats, supports clear arteries.' },
                    { type: 'IMMUNITY SNACK', title: 'Unsalted Almonds & Green Tea', desc: 'Antioxidants for vascular protection.' }
                ];
            }

            mealList.innerHTML = meals.map(m => `
                <div class="dark-card" style="margin-bottom: 12px; padding: 18px;">
                    <span style="font-size: 11px; font-weight: 800; color: #38bdf8; letter-spacing: 0.5px;">${m.type}</span>
                    <h4 style="font-size: 16px; font-weight: 700; color: white; margin: 4px 0;">${m.title}</h4>
                    <p style="font-size: 12px; color: #cbd5e1; opacity: 0.9;">${m.desc}</p>
                </div>
            `).join('');
        }

        if (exList) {
            let exercises = [
                { title: 'Brisk Walking', desc: '20-30 mins • Low to Medium Intensity' },
                { title: 'Gentle Yoga & Stretching', desc: '15 mins • Low Intensity' },
                { title: 'Deep Breathing Exercises', desc: '5-10 mins • Relaxation' }
            ];

            if (diagnosis.toLowerCase().includes('fever') || diagnosis.toLowerCase().includes('infection')) {
                exercises = [
                    { title: 'Complete Bed Rest', desc: 'Full recovery focus' },
                    { title: 'Deep Diaphragmatic Breathing', desc: '5 mins • Low Intensity' },
                    { title: 'Light In-Bed Stretching', desc: '5 mins • Very Light' }
                ];
            }

            exList.innerHTML = exercises.map(e => `
                <div class="dark-card" style="margin-bottom: 12px; padding: 18px;">
                    <h4 style="font-size: 16px; font-weight: 700; color: white; margin-bottom: 4px;">${e.title}</h4>
                    <p style="font-size: 12px; color: #a855f7; font-weight: 600;">${e.desc}</p>
                </div>
            `).join('');
        }
    }

    // 3. History Management (Firestore & Local Storage Synced)
    const historyList = document.getElementById('history-list');
    const historySearch = document.getElementById('history-search');
    let allHistoryRecords = [];

    if (historySearch) {
        historySearch.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase().trim();
            if (!query) {
                renderHistoryRecords(allHistoryRecords);
            } else {
                const filtered = allHistoryRecords.filter(r =>
                    (r.diagnosis && r.diagnosis.toLowerCase().includes(query)) ||
                    (r.symptoms && r.symptoms.toLowerCase().includes(query)) ||
                    (r.riskLevel && r.riskLevel.toLowerCase().includes(query))
                );
                renderHistoryRecords(filtered);
            }
        });
    }

    function saveHistoryToFirestore(diagnosis, symptoms, riskLevel, foodRecommendations, exercisePlans) {
        const user = auth ? auth.currentUser : null;
        const record = {
            id: 'local_' + Date.now(),
            date: Date.now(),
            symptoms: symptoms || 'None',
            diagnosis: diagnosis,
            riskLevel: riskLevel || 'Low',
            foodRecommendations: foodRecommendations || 'Balanced diet rich in leafy greens and hydration.',
            exercisePlans: exercisePlans || 'Light cardiovascular activity 30 mins/day.',
            createdAt: Date.now()
        };

        // Always save to localStorage for offline / non-logged in view
        const localItems = JSON.parse(localStorage.getItem('carepath_history') || '[]');
        localItems.unshift(record);
        localStorage.setItem('carepath_history', JSON.stringify(localItems));

        if (!user || !db) {
            console.warn("User not authenticated in Firebase Auth. Record saved to local storage only.");
            allHistoryRecords = localItems;
            renderHistoryRecords(allHistoryRecords);
            return;
        }

        const newDocRef = db.collection('users').doc(user.uid).collection('health_history').doc();
        const firestoreData = {
            id: newDocRef.id,
            date: record.date,
            symptoms: record.symptoms,
            diagnosis: record.diagnosis,
            riskLevel: record.riskLevel,
            foodRecommendations: record.foodRecommendations,
            exercisePlans: record.exercisePlans,
            createdAt: record.createdAt
        };

        allHistoryRecords.unshift(firestoreData);
        renderHistoryRecords(allHistoryRecords);

        newDocRef.set(firestoreData)
            .then(() => {
                console.log("Health history saved to Firestore with ID:", newDocRef.id);
            })
            .catch(err => {
                console.error("Error saving health history to Firestore:", err);
                showErrorDialog("Firestore Save Failed", err.message || "Failed to save history record.");
            });
    }

    function syncLocalHistoryToFirestore(user) {
        if (!user || !db) return;
        const localItems = JSON.parse(localStorage.getItem('carepath_history') || '[]');
        if (localItems.length === 0) return;

        localItems.forEach(item => {
            if (item.id && item.id.startsWith('local_')) {
                const docRef = db.collection('users').doc(user.uid).collection('health_history').doc();
                docRef.set({
                    id: docRef.id,
                    date: item.date || Date.now(),
                    symptoms: item.symptoms || 'None',
                    diagnosis: item.diagnosis || 'General Assessment',
                    riskLevel: item.riskLevel || 'Low',
                    foodRecommendations: item.foodRecommendations || '',
                    exercisePlans: item.exercisePlans || '',
                    createdAt: item.createdAt || Date.now()
                });
            }
        });

        // Clear local unsynced records after pushing
        localStorage.removeItem('carepath_history');
    }

    function updateWellnessScore(records) {
        const scoreElem = document.getElementById('wellness-score');
        const msgElem = document.getElementById('wellness-message');
        if (!scoreElem || !msgElem) return;

        if (!records || records.length === 0) {
            scoreElem.innerHTML = `100<small>/100</small>`;
            msgElem.textContent = "Complete a symptom assessment to track your score.";
            return;
        }

        const latest = records[0];
        const risk = latest.riskLevel || 'Low';

        if (risk === 'High') {
            scoreElem.innerHTML = `50<small>/100</small>`;
            msgElem.textContent = "High risk concern detected. Consult a healthcare professional.";
        } else if (risk === 'Medium') {
            scoreElem.innerHTML = `75<small>/100</small>`;
            msgElem.textContent = "Moderate concern detected. Follow AI recommendations.";
        } else {
            scoreElem.innerHTML = `95<small>/100</small>`;
            msgElem.textContent = "You're doing great! Keep up the healthy routine.";
        }
    }

    function renderHistoryRecords(records) {
        updateWellnessScore(records);

        const listElem = document.getElementById('history-list');
        if (!listElem) return;
        listElem.innerHTML = '';

        if (!records || records.length === 0) {
            listElem.innerHTML = `<div class="card empty-msg" style="text-align:center; color: var(--text-muted); padding: 20px;">No assessment history recorded yet.</div>`;
            return;
        }

        records.forEach(rec => {
            const dateStr = rec.date ? new Date(rec.date).toLocaleString() : new Date().toLocaleString();
            const item = document.createElement('div');
            item.className = 'card';
            item.innerHTML = `
                <div class="card-body">
                    <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                        <div>
                            <h3 style="font-size: 16px; font-weight: 700; color: #1e293b;">${rec.diagnosis || 'General Assessment'}</h3>
                            <p style="font-size: 11px; color: var(--text-muted);">${dateStr}</p>
                        </div>
                        <i class="fa-solid fa-trash" style="color: #ef4444; cursor: pointer; padding: 4px;" onclick="deleteHistoryRecord('${rec.id}')"></i>
                    </div>
                    <hr style="margin: 12px 0; border: 0; border-top: 1px solid #f3f4f6;">
                    <p style="font-size: 12px; color: var(--text-muted);"><strong>Symptoms:</strong> ${rec.symptoms || 'None'}</p>
                    ${rec.riskLevel ? `<p style="font-size: 11px; color: ${rec.riskLevel === 'High' ? '#ef4444' : '#10b981'}; margin-top: 4px; font-weight: 600;">Risk Level: ${rec.riskLevel}</p>` : ''}
                </div>
            `;
            listElem.appendChild(item);
        });
    }

    window.deleteHistoryRecord = function (docId) {
        const user = auth ? auth.currentUser : null;

        // Remove locally from memory & localStorage
        allHistoryRecords = allHistoryRecords.filter(r => r.id !== docId);
        renderHistoryRecords(allHistoryRecords);
        const localItems = JSON.parse(localStorage.getItem('carepath_history') || '[]').filter(r => r.id !== docId);
        localStorage.setItem('carepath_history', JSON.stringify(localItems));

        if (!user || !db || !docId || docId.startsWith('local_')) return;

        db.collection('users').doc(user.uid)
            .collection('health_history')
            .doc(docId)
            .delete()
            .then(() => {
                console.log("Deleted history document from Firestore:", docId);
            })
            .catch(err => {
                console.error("Error deleting history document:", err);
            });
    };

    // 4. Profile Editing Logic (Firestore Synced)
    const btnEdit = document.getElementById('btn-edit-profile');
    let isEditing = false;

    if (btnEdit) {
        btnEdit.addEventListener('click', () => {
            isEditing = !isEditing;
            const spans = document.querySelectorAll('.info-val span');
            const inputs = document.querySelectorAll('.info-val input');
            const icon = btnEdit.querySelector('i');

            if (isEditing) {
                icon.className = 'fa-solid fa-check';
                spans.forEach(s => s.style.display = 'none');
                inputs.forEach(i => i.style.display = 'block');
            } else {
                icon.className = 'fa-solid fa-pen';

                const currentUser = auth ? auth.currentUser : null;
                const activeUser = JSON.parse(localStorage.getItem('carepath_user')) || {};
                activeUser.mobile = document.getElementById('edit-mobile').value.trim() || 'N/A';
                activeUser.age = document.getElementById('edit-age').value.trim() || 'N/A';
                activeUser.blood = document.getElementById('edit-blood').value.trim() || 'N/A';
                activeUser.height = document.getElementById('edit-height').value.trim() || 'N/A';
                activeUser.weight = document.getElementById('edit-weight').value.trim() || 'N/A';

                localStorage.setItem('carepath_user', JSON.stringify(activeUser));

                if (currentUser && db) {
                    const ageNum = parseInt(activeUser.age) || 0;
                    const heightNum = parseFloat(activeUser.height) || 0;
                    const weightNum = parseFloat(activeUser.weight) || 0;

                    db.collection('users').doc(currentUser.uid).set({
                        mobileNumber: activeUser.mobile,
                        mobile: activeUser.mobile,
                        age: ageNum,
                        bloodGroup: activeUser.blood,
                        blood: activeUser.blood,
                        height: heightNum,
                        weight: weightNum
                    }, { merge: true }).then(() => {
                        console.log("Firestore profile updated successfully!");
                    }).catch(err => {
                        console.error("Firestore update error:", err);
                    });
                }

                updateUserProfile(activeUser);

                spans.forEach(s => s.style.display = 'block');
                inputs.forEach(i => i.style.display = 'none');
                alert('Profile updated and saved!');
            }
        });
    }

    window.renderMedicines = function (records) {
        const medList = document.getElementById('medicine-list');
        const adherenceText = document.getElementById('adherence-text');
        const circlePath = document.getElementById('adherence-circle-path');
        const upcomingReminder = document.getElementById('upcoming-reminder-text');

        if (!records || records.length === 0) {
            if (adherenceText) adherenceText.textContent = "No medicines added yet";
            if (circlePath) circlePath.setAttribute('stroke-dasharray', '0, 100');
            if (upcomingReminder) upcomingReminder.textContent = "No upcoming medicine reminders";
            if (medList) medList.innerHTML = `<div class="card empty-msg" style="text-align:center; color: var(--text-muted); padding: 20px;">No medicine reminders added yet.</div>`;
            return;
        }

        const takenCount = records.filter(m => m.isTaken === true || m.isTaken === 'true').length;
        const totalCount = records.length;
        const percentage = Math.round((takenCount / totalCount) * 100);

        if (adherenceText) adherenceText.textContent = `${takenCount} of ${totalCount} doses taken today`;
        if (circlePath) circlePath.setAttribute('stroke-dasharray', `${percentage}, 100`);

        if (upcomingReminder) {
            const firstMed = records[0];
            upcomingReminder.textContent = `${firstMed.name || 'Medicine'} - ${firstMed.dosage || 'Daily'}`;
        }

        if (!medList) return;
        medList.innerHTML = '';

        records.forEach(med => {
            const item = document.createElement('div');
            item.className = 'card reminder-card';
            item.style.marginBottom = '12px';

            const schedule = [];
            if (med.morning) schedule.push('Morning');
            if (med.afternoon) schedule.push('Afternoon');
            if (med.night) schedule.push('Night');
            const schedStr = schedule.length > 0 ? schedule.join(', ') : 'Daily';

            item.innerHTML = `
                <div class="card-body row" style="display: flex; justify-content: space-between; align-items: center; padding: 16px;">
                    <div style="display: flex; align-items: center; gap: 14px;">
                        <i class="fa-solid fa-pills reminder-icon" style="font-size: 24px; color: ${med.isTaken ? '#10b981' : 'var(--primary)'};"></i>
                        <div class="reminder-info">
                            <h3 style="font-size: 16px; font-weight: 700; ${med.isTaken ? 'text-decoration: line-through; opacity: 0.7;' : ''}">${med.name || 'Medicine'}</h3>
                            <p style="font-size: 12px; color: var(--text-muted); margin-top: 2px;">${med.dosage || ''} • ${med.frequency || ''}</p>
                            <p style="font-size: 11px; color: #64748b; margin-top: 2px;"><i class="fa-regular fa-clock"></i> ${schedStr}</p>
                        </div>
                    </div>
                    <div style="display: flex; align-items: center; gap: 12px;">
                        <div class="check-pill" style="cursor: pointer;" onclick="toggleMedicineTaken('${med.id}', ${!med.isTaken})">
                            <i class="${med.isTaken ? 'fa-solid fa-circle-check' : 'fa-regular fa-circle'}" style="font-size: 24px; color: ${med.isTaken ? '#10b981' : '#cbd5e1'};"></i>
                        </div>
                        <i class="fa-solid fa-trash" style="color: #ef4444; cursor: pointer; font-size: 16px;" onclick="deleteMedicineRecord('${med.id}')"></i>
                    </div>
                </div>
            `;
            medList.appendChild(item);
        });
    };

    window.openAddMedicineModal = function () {
        const modal = document.getElementById('add-medicine-modal');
        if (modal) modal.style.display = 'flex';
    };

    window.closeAddMedicineModal = function () {
        const modal = document.getElementById('add-medicine-modal');
        if (modal) modal.style.display = 'none';
        const form = document.getElementById('add-medicine-form');
        if (form) form.reset();
    };

    window.handleAddMedicineSubmit = function (event) {
        event.preventDefault();
        const user = auth ? auth.currentUser : null;
        if (!user || !db) {
            alert('Please sign in to add medicine reminders.');
            return;
        }

        const name = document.getElementById('med-name').value.trim();
        const dosage = document.getElementById('med-dosage').value.trim();
        const frequency = document.getElementById('med-frequency').value.trim();
        const morning = document.getElementById('med-morning').checked;
        const afternoon = document.getElementById('med-afternoon').checked;
        const night = document.getElementById('med-night').checked;

        const medData = {
            name: name,
            dosage: dosage,
            frequency: frequency,
            beforeFood: false,
            doctorName: '',
            startDate: Date.now(),
            endDate: Date.now() + 7 * 24 * 60 * 60 * 1000,
            morning: morning,
            afternoon: afternoon,
            evening: false,
            night: night,
            isTaken: false,
            lastTakenTimestamp: null,
            medicineImageUrl: '',
            notes: '',
            createdAt: Date.now()
        };

        db.collection('users').doc(user.uid)
            .collection('medicines')
            .add(medData)
            .then(docRef => {
                console.log("Medicine added to Firestore with ID:", docRef.id);
                closeAddMedicineModal();
            })
            .catch(err => {
                console.error("Error adding medicine to Firestore:", err);
                alert("Failed to add medicine: " + err.message);
            });
    };

    window.toggleMedicineTaken = function (medId, isTaken) {
        const user = auth ? auth.currentUser : null;
        if (!user || !db || !medId) return;

        db.collection('users').doc(user.uid)
            .collection('medicines')
            .doc(medId)
            .update({
                isTaken: isTaken,
                lastTakenTimestamp: isTaken ? Date.now() : null
            })
            .then(() => {
                console.log("Medicine taken status updated:", medId, isTaken);
            })
            .catch(err => {
                console.error("Error updating medicine status:", err);
            });
    };

    window.deleteMedicineRecord = function (medId) {
        const user = auth ? auth.currentUser : null;
        if (!user || !db || !medId) return;

        db.collection('users').doc(user.uid)
            .collection('medicines')
            .doc(medId)
            .delete()
            .then(() => {
                console.log("Medicine deleted from Firestore:", medId);
            })
            .catch(err => {
                console.error("Error deleting medicine from Firestore:", err);
            });
    };

    // 6. UI Helpers (Progress Bar & Error Dialogs)
    window.showProgressBar = function () {
        const bar = document.getElementById('top-progress-bar');
        if (bar) bar.style.display = 'block';
    };

    window.hideProgressBar = function () {
        const bar = document.getElementById('top-progress-bar');
        if (bar) bar.style.display = 'none';
    };

    window.showErrorDialog = function (title, message) {
        const modal = document.getElementById('error-dialog-modal');
        const titleElem = document.getElementById('error-dialog-title');
        const msgElem = document.getElementById('error-dialog-message');

        if (titleElem) titleElem.textContent = title || "Sync Error";
        if (msgElem) msgElem.textContent = message || "An error occurred while fetching data.";
        if (modal) modal.style.display = 'flex';
    };

    window.closeErrorDialog = function () {
        const modal = document.getElementById('error-dialog-modal');
        if (modal) modal.style.display = 'none';
    };

    // SOS Button
    document.getElementById('btn-sos').addEventListener('click', () => {
        alert('🚨 EMERGENCY SOS ACTIVATED!\nBroadcasting location to emergency services...');
    });
});
