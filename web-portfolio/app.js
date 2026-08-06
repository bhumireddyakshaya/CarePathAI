// Firebase Configuration (Synced with CarePathAI Android App)
const firebaseConfig = {
    apiKey: "AIzaSyB-mximOd6pSfN2pc4qVng7fX8BvsPFzLs",
    authDomain: "carepathai-5714b.firebaseapp.com",
    projectId: "carepathai-5714b",
    storageBucket: "carepathai-5714b.firebasestorage.app",
    messagingSenderId: "444560361811",
    appId: "1:444560361811:web:carepathaiweb"
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

    window.switchTab = function(tabId) {
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

    window.toggleAuthMode = function(mode) {
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

    window.handleLogout = function() {
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

    window.handleLogin = function() {
        const email = document.getElementById('login-email').value.trim();
        const password = document.getElementById('login-password').value;

        if (!email || !password) {
            alert('Please enter both email and password.');
            return;
        }

        if (auth) {
            auth.signInWithEmailAndPassword(email, password)
                .then((userCredential) => {
                    console.log("Firebase Login successful:", userCredential.user.email);
                })
                .catch((error) => {
                    console.error("Firebase Login error:", error);
                    let userMsg = error.message;
                    if (error.code === 'auth/user-not-found' || error.code === 'auth/invalid-credential') {
                        userMsg = "Account not found or password incorrect. If you haven't registered this account yet, please click the 'Sign Up' tab to create your account.";
                    } else if (error.code === 'auth/wrong-password') {
                        userMsg = "Incorrect password. Please double-check your password or create a new account in 'Sign Up'.";
                    }
                    alert("Login Failed: " + userMsg);
                });
        }
    };

    window.handleSignUp = function() {
        const name = document.getElementById('signup-name').value.trim();
        const email = document.getElementById('signup-email').value.trim();
        const mobile = document.getElementById('signup-mobile').value.trim() || 'N/A';
        const age = document.getElementById('signup-age').value.trim() || 'N/A';
        const blood = document.getElementById('signup-blood').value.trim() || 'N/A';
        const height = document.getElementById('signup-height').value.trim() || 'N/A';
        const weight = document.getElementById('signup-weight').value.trim() || 'N/A';
        const pass = document.getElementById('signup-password').value;
        const confirmPass = document.getElementById('signup-confirm-password').value;

        if (pass !== confirmPass) {
            alert('Passwords do not match. Please verify your password.');
            return;
        }

        if (auth) {
            auth.createUserWithEmailAndPassword(email, pass)
                .then((userCredential) => {
                    const user = userCredential.user;
                    const ageNum = parseInt(age) || 0;
                    const heightNum = parseFloat(height) || 0;
                    const weightNum = parseFloat(weight) || 0;

                    const userData = {
                        id: user.uid,
                        fullName: name,
                        name: name,
                        email: email,
                        mobileNumber: mobile,
                        mobile: mobile,
                        age: ageNum,
                        bloodGroup: blood,
                        blood: blood,
                        height: heightNum,
                        weight: weightNum,
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
                    console.log("Firebase User registered & saved in Firestore successfully!");
                })
                .catch((error) => {
                    console.error("Firebase SignUp error:", error);
                    alert("Sign Up Failed: " + error.message);
                });
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

    // Initial Authentication Check
    const savedUser = localStorage.getItem('carepath_user');
    if (!savedUser) {
        switchTab('login');
    } else {
        updateUserProfile(JSON.parse(savedUser));
    }

    navItems.forEach(item => {
        item.addEventListener('click', () => switchTab(item.dataset.tab));
    });

    // 2. Assessment Multi-step Logic
    let selectedBodyPart = null;
    const bodyPartCards = document.querySelectorAll('.body-part-card');
    const btnNext1 = document.getElementById('next-1');
    const btnNext2 = document.getElementById('next-2');
    const progressBar = document.getElementById('assessment-progress');

    function resetAssessment() {
        showStep(1);
        selectedBodyPart = null;
        bodyPartCards.forEach(c => c.classList.remove('active'));
        btnNext1.disabled = true;
        document.querySelectorAll('.symptom-check-item input').forEach(i => i.checked = false);
    }

    function showStep(stepNum) {
        document.querySelectorAll('.step').forEach(s => s.classList.remove('active'));
        document.getElementById(`step-${stepNum}`).classList.add('active');
        progressBar.style.width = `${stepNum * 33.3}%`;
    }

    bodyPartCards.forEach(card => {
        card.addEventListener('click', () => {
            bodyPartCards.forEach(c => c.classList.remove('active'));
            card.classList.add('active');
            selectedBodyPart = card.dataset.part;
            btnNext1.disabled = false;
        });
    });

    btnNext1.addEventListener('click', () => showStep(2));

    btnNext2.addEventListener('click', () => {
        showStep(3);
        runAIAnalysis();
    });

    function runAIAnalysis() {
        const resultContainer = document.getElementById('analysis-result');
        resultContainer.innerHTML = `
            <div class="loading-spinner">
                <div class="spinner"></div>
                <p>AI is analyzing your symptoms...</p>
            </div>
        `;

        setTimeout(() => {
            const checked = Array.from(document.querySelectorAll('.symptom-check-item input:checked'));
            const symptoms = checked.map(i => i.nextElementSibling.textContent);
            const isHighRisk = symptoms.some(s => s.toLowerCase().includes('chest') || s.toLowerCase().includes('palpitation'));

            const riskLevel = isHighRisk ? "High" : "Low";
            const color = isHighRisk ? "#ef4444" : "#4ade80";

            resultContainer.innerHTML = `
                <div style="text-align: center; padding-top: 20px;">
                    <div style="width: 100px; height: 100px; border-radius: 50%; border: 8px solid ${color}; display: flex; align-items: center; justify-content: center; margin: 0 auto 24px auto;">
                        <span style="font-size: 24px; font-weight: 800; color: white;">85%</span>
                    </div>
                    <h3 style="font-size: 24px; margin-bottom: 8px; color: ${color};">${isHighRisk ? 'Cardiac Concern' : 'General Wellness'}</h3>
                    <div style="display: inline-block; padding: 6px 16px; border-radius: 999px; background: ${color}33; color: ${color}; font-size: 12px; font-weight: 700; margin-bottom: 30px;">
                        Risk Level: ${riskLevel}
                    </div>

                    <div class="dark-card" style="text-align: left;">
                        <h4 style="font-size: 16px; margin-bottom: 12px;"><i class="fa-solid fa-brain" style="margin-right: 10px;"></i> AI Health Insights</h4>
                        <p style="font-size: 14px; color: #94a3b8; line-height: 1.6;">
                            Based on your symptoms (${symptoms.join(', ') || 'None'}), the AI suggests monitoring your condition.
                            ${isHighRisk ? 'Immediate attention is recommended.' : 'Maintain hydration and rest.'}
                        </p>
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-top: 20px;">
                        <div class="insight-item" style="background: #1A1A1A; border: 1px solid #333; color: white;" onclick="switchTab('nutrition')">
                            <i class="fa-solid fa-utensils"></i>
                            <span>Diet Plan</span>
                        </div>
                        <div class="insight-item" style="background: #1A1A1A; border: 1px solid #333; color: white;" onclick="switchTab('exercise')">
                            <i class="fa-solid fa-dumbbell"></i>
                            <span>Exercise Plan</span>
                        </div>
                    </div>

                    <button class="btn btn-primary btn-block" style="margin-top: 24px;" onclick="switchTab('home')">Back to Dashboard</button>
                </div>
            `;

            // Save assessment record to Firestore
            saveHistoryToFirestore(isHighRisk ? 'Cardiac Concern' : 'General Wellness', symptoms.join(', '), isHighRisk ? 'High' : 'Low');
        }, 2000);
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

    function saveHistoryToFirestore(diagnosis, symptoms, riskLevel) {
        const user = auth ? auth.currentUser : null;
        const record = {
            id: 'local_' + Date.now(),
            date: Date.now(),
            symptoms: symptoms || 'None',
            diagnosis: diagnosis,
            riskLevel: riskLevel || 'Low',
            foodRecommendations: 'Balanced diet rich in leafy greens and hydration.',
            exercisePlans: 'Light cardiovascular activity 30 mins/day.',
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

        newDocRef.set(firestoreData)
            .then(() => {
                console.log("Health history saved to Firestore with ID:", newDocRef.id);
            })
            .catch(err => {
                console.error("Error saving health history to Firestore:", err);
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

    function renderHistoryRecords(records) {
        if (!historyList) return;
        historyList.innerHTML = '';

        if (!records || records.length === 0) {
            historyList.innerHTML = `<div class="card empty-msg" style="text-align:center; color: var(--text-muted); padding: 20px;">No assessment history recorded yet.</div>`;
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
                            <h3 style="font-size: 16px; font-weight: 700;">${rec.diagnosis || 'General Assessment'}</h3>
                            <p style="font-size: 11px; color: var(--text-muted);">${dateStr}</p>
                        </div>
                        <i class="fa-solid fa-trash" style="color: #ef4444; cursor: pointer;" onclick="deleteHistoryRecord('${rec.id}')"></i>
                    </div>
                    <hr style="margin: 12px 0; border: 0; border-top: 1px solid #f3f4f6;">
                    <p style="font-size: 12px; color: var(--text-muted);"><strong>Symptoms:</strong> ${rec.symptoms || 'None'}</p>
                    ${rec.riskLevel ? `<p style="font-size: 11px; color: ${rec.riskLevel === 'High' ? '#ef4444' : '#4ade80'}; margin-top: 4px; font-weight: 600;">Risk Level: ${rec.riskLevel}</p>` : ''}
                </div>
            `;
            historyList.appendChild(item);
        });
    }

    window.deleteHistoryRecord = function(docId) {
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

    // 5. Medicine Reminders Logic (Firestore Real-time Synced)
    window.renderMedicines = function(records) {
        const medList = document.getElementById('medicine-list');
        if (!medList) return;
        medList.innerHTML = '';

        if (!records || records.length === 0) {
            medList.innerHTML = `<div class="card empty-msg" style="text-align:center; color: var(--text-muted); padding: 20px;">No medicine reminders added yet.</div>`;
            return;
        }

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

    window.openAddMedicineModal = function() {
        const modal = document.getElementById('add-medicine-modal');
        if (modal) modal.style.display = 'flex';
    };

    window.closeAddMedicineModal = function() {
        const modal = document.getElementById('add-medicine-modal');
        if (modal) modal.style.display = 'none';
        const form = document.getElementById('add-medicine-form');
        if (form) form.reset();
    };

    window.handleAddMedicineSubmit = function(event) {
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

    window.toggleMedicineTaken = function(medId, isTaken) {
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

    window.deleteMedicineRecord = function(medId) {
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
    window.showProgressBar = function() {
        const bar = document.getElementById('top-progress-bar');
        if (bar) bar.style.display = 'block';
    };

    window.hideProgressBar = function() {
        const bar = document.getElementById('top-progress-bar');
        if (bar) bar.style.display = 'none';
    };

    window.showErrorDialog = function(title, message) {
        const modal = document.getElementById('error-dialog-modal');
        const titleElem = document.getElementById('error-dialog-title');
        const msgElem = document.getElementById('error-dialog-message');

        if (titleElem) titleElem.textContent = title || "Sync Error";
        if (msgElem) msgElem.textContent = message || "An error occurred while fetching data.";
        if (modal) modal.style.display = 'flex';
    };

    window.closeErrorDialog = function() {
        const modal = document.getElementById('error-dialog-modal');
        if (modal) modal.style.display = 'none';
    };

    // SOS Button
    document.getElementById('btn-sos').addEventListener('click', () => {
        alert('🚨 EMERGENCY SOS ACTIVATED!\nBroadcasting location to emergency services...');
    });
});
