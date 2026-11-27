/* =========================================================
   GYMLINK — INTERACTIVE VANILLA JS ENGINE
   ========================================================= */

let weeklyChartInstance = null;

document.addEventListener('DOMContentLoaded', () => {
    initThemeToggle();
    initRewardFilters();
    initPostImagePreview();
    initFeedTags();
    initAthleteProfileModal();
    initRedeemModal();
    initQrWalletModal();
    initAdminCharts();
    initTableSearchFilters();
    initCheckinQuickFilter();
});

/**
 * Modo Claro / Oscuro con persistencia en localStorage
 */
function initThemeToggle() {
    const savedTheme = localStorage.getItem('gymlink-theme') || 'dark';
    applyTheme(savedTheme, false);

    const toggleButtons = document.querySelectorAll('[data-theme-toggle]');
    toggleButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const current = document.documentElement.getAttribute('data-bs-theme') || 'dark';
            const next = current === 'dark' ? 'light' : 'dark';
            applyTheme(next, true);
        });
    });
}

function applyTheme(theme, notifyChart) {
    document.documentElement.setAttribute('data-bs-theme', theme);
    localStorage.setItem('gymlink-theme', theme);

    document.querySelectorAll('[data-theme-toggle]').forEach(btn => {
        const icon = btn.querySelector('i');
        const label = btn.querySelector('[data-theme-label]');
        if (icon) {
            icon.className = theme === 'dark' ? 'bi bi-sun-fill text-warning' : 'bi bi-moon-stars-fill text-primary';
        }
        if (label) {
            label.textContent = theme === 'dark' ? 'Claro' : 'Oscuro';
        }
        btn.setAttribute('title', theme === 'dark' ? 'Cambiar a Modo Claro' : 'Cambiar a Modo Oscuro');
    });

    if (notifyChart && weeklyChartInstance) {
        weeklyChartInstance.destroy();
        weeklyChartInstance = null;
        initAdminCharts();
    }
}

function showGymToast(message, type = 'success') {
    const container = document.getElementById('gymlinkToastContainer');
    if (!container) return;

    const iconMap = {
        success: 'bi-check-circle-fill text-neon',
        fire: 'bi-fire text-fire',
        info: 'bi-info-circle-fill text-info',
        danger: 'bi-exclamation-triangle-fill text-danger'
    };

    const toastEl = document.createElement('div');
    toastEl.className = 'toast align-items-center bg-dark border border-secondary border-opacity-50 rounded-4 shadow-lg';
    toastEl.setAttribute('role', 'alert');
    toastEl.setAttribute('aria-live', 'assertive');
    toastEl.setAttribute('aria-atomic', 'true');

    toastEl.innerHTML = `
        <div class="d-flex align-items-center px-3 py-2">
            <i class="bi ${iconMap[type] || iconMap.success} fs-5 me-2"></i>
            <div class="toast-body fw-medium px-1">${message}</div>
            <button type="button" class="btn-close ms-auto me-1" data-bs-dismiss="toast" aria-label="Cerrar"></button>
        </div>
    `;

    container.appendChild(toastEl);
    const bsToast = new bootstrap.Toast(toastEl, { delay: 3200 });
    bsToast.show();
    toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
}

function copyPinToClipboard(pinCode, buttonEl) {
    if (!pinCode) return;

    const onSuccess = () => {
        showGymToast(`PIN <strong>${pinCode}</strong> copiado al portapapeles`, 'success');
        if (buttonEl) {
            const originalHtml = buttonEl.innerHTML;
            buttonEl.innerHTML = '<i class="bi bi-check2-all me-1"></i>¡Copiado!';
            buttonEl.classList.add('btn-neon');
            buttonEl.classList.remove('btn-ghost-dark');
            setTimeout(() => {
                buttonEl.innerHTML = originalHtml;
                buttonEl.classList.remove('btn-neon');
                buttonEl.classList.add('btn-ghost-dark');
            }, 2000);
        }
    };

    if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(pinCode).then(onSuccess).catch(() => fallbackCopy(pinCode, onSuccess));
    } else {
        fallbackCopy(pinCode, onSuccess);
    }
}

function fallbackCopy(text, callback) {
    const tempInput = document.createElement('input');
    tempInput.value = text;
    document.body.appendChild(tempInput);
    tempInput.select();
    document.execCommand('copy');
    document.body.removeChild(tempInput);
    callback();
}

function initRewardFilters() {
    const filterButtons = document.querySelectorAll('[data-reward-filter]');
    const rewardCards = document.querySelectorAll('[data-reward-category]');
    const searchInput = document.getElementById('rewardSearchInput');
    const emptyState = document.getElementById('rewardsEmptyState');

    if (!filterButtons.length && !rewardCards.length) return;

    let currentCategory = 'ALL';

    const applyFilter = () => {
        const query = searchInput ? searchInput.value.trim().toLowerCase() : '';
        let visibleCount = 0;

        rewardCards.forEach(card => {
            const cat = (card.getAttribute('data-reward-category') || '').toUpperCase();
            const title = (card.getAttribute('data-reward-title') || '').toLowerCase();
            const matchesCat = currentCategory === 'ALL' || cat === currentCategory;
            const matchesText = !query || title.includes(query);

            if (matchesCat && matchesText) {
                card.classList.remove('d-none');
                visibleCount++;
            } else {
                card.classList.add('d-none');
            }
        });

        if (emptyState) {
            emptyState.classList.toggle('d-none', visibleCount > 0);
        }
    };

    filterButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            filterButtons.forEach(b => {
                b.classList.remove('btn-neon', 'active');
                b.classList.add('btn-ghost-dark');
            });
            btn.classList.remove('btn-ghost-dark');
            btn.classList.add('btn-neon', 'active');
            currentCategory = btn.getAttribute('data-reward-filter').toUpperCase();
            applyFilter();
        });
    });

    if (searchInput) {
        searchInput.addEventListener('input', applyFilter);
    }
}

function initPostImagePreview() {
    const imageInput = document.getElementById('postImageUrlInput');
    const fileInput = document.getElementById('postImageFileInput');
    const previewWrapper = document.getElementById('postImagePreviewWrapper');
    const previewImg = document.getElementById('postImagePreview');
    const clearBtn = document.getElementById('clearPostPreviewBtn');

    if (!previewWrapper || !previewImg) return;

    if (imageInput) {
        imageInput.addEventListener('input', () => {
            const val = imageInput.value.trim();
            if (val) {
                previewImg.src = val;
                previewWrapper.classList.remove('d-none');
            } else {
                previewWrapper.classList.add('d-none');
            }
        });
    }

    if (fileInput) {
        fileInput.addEventListener('change', (e) => {
            const file = e.target.files && e.target.files[0];
            if (!file) return;
            const reader = new FileReader();
            reader.onload = (ev) => {
                previewImg.src = ev.target.result;
                previewWrapper.classList.remove('d-none');
            };
            reader.readAsDataURL(file);
        });
    }

    if (clearBtn) {
        clearBtn.addEventListener('click', () => {
            if (imageInput) imageInput.value = '';
            if (fileInput) fileInput.value = '';
            previewImg.src = '';
            previewWrapper.classList.add('d-none');
        });
    }
}

function initFeedTags() {
    const tagButtons = document.querySelectorAll('[data-feed-tag]');
    const hiddenTagInput = document.getElementById('selectedPostTagInput');
    const textarea = document.getElementById('postContentInput');

    if (!tagButtons.length) return;

    tagButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const tag = btn.getAttribute('data-feed-tag');
            tagButtons.forEach(b => b.classList.remove('btn-neon', 'active'));
            btn.classList.add('btn-neon', 'active');
            if (hiddenTagInput) hiddenTagInput.value = tag;
            if (textarea && !textarea.value.includes(tag)) {
                textarea.focus();
            }
        });
    });
}

function initAthleteProfileModal() {
    const modalEl = document.getElementById('athleteProfileModal');
    if (!modalEl) return;

    modalEl.addEventListener('show.bs.modal', (event) => {
        const trigger = event.relatedTarget;
        if (!trigger) return;

        const name = trigger.getAttribute('data-athlete-name') || 'Atleta GymLink';
        const initials = trigger.getAttribute('data-athlete-initials') || 'GL';
        const rank = trigger.getAttribute('data-athlete-rank') || 'BRONCE';
        const xp = trigger.getAttribute('data-athlete-xp') || '0';
        const streak = trigger.getAttribute('data-athlete-streak') || '0';
        const checkins = trigger.getAttribute('data-athlete-checkins') || '0';
        const bio = trigger.getAttribute('data-athlete-bio') || 'Entrenando constante en GymLink Club.';

        document.getElementById('modalAthleteName').textContent = name;
        document.getElementById('modalAthleteInitials').textContent = initials;
        document.getElementById('modalAthleteXp').textContent = xp;
        document.getElementById('modalAthleteStreak').textContent = `${streak} días`;
        document.getElementById('modalAthleteCheckins').textContent = checkins;
        document.getElementById('modalAthleteBio').textContent = bio;

        const rankBadge = document.getElementById('modalAthleteRank');
        rankBadge.textContent = rank;
        rankBadge.className = `vip-badge vip-${rank.toUpperCase()}`;
    });
}

function initRedeemModal() {
    const modalEl = document.getElementById('redeemConfirmModal');
    if (!modalEl) return;

    modalEl.addEventListener('show.bs.modal', (event) => {
        const trigger = event.relatedTarget;
        if (!trigger) return;

        const rewardId = trigger.getAttribute('data-reward-id');
        const title = trigger.getAttribute('data-reward-title');
        const cost = trigger.getAttribute('data-reward-cost');
        const type = trigger.getAttribute('data-reward-type');

        document.getElementById('redeemRewardIdInput').value = rewardId || '';
        document.getElementById('redeemModalTitle').textContent = title || 'Premio VIP';
        document.getElementById('redeemModalCost').textContent = `${cost || 0} PTS`;
        document.getElementById('redeemModalType').textContent = type === 'DIGITAL'
            ? 'Entrega instantánea en tu Billetera Digital con PIN único'
            : 'Genera pase QR para reclamar en recepción del gimnasio';
    });
}

function initQrWalletModal() {
    const modalEl = document.getElementById('walletQrModal');
    if (!modalEl) return;

    modalEl.addEventListener('show.bs.modal', (event) => {
        const trigger = event.relatedTarget;
        if (!trigger) return;

        const code = trigger.getAttribute('data-pass-code') || 'GL-0000';
        const title = trigger.getAttribute('data-pass-title') || 'Pase VIP';
        const status = trigger.getAttribute('data-pass-status') || 'ACTIVO';

        document.getElementById('qrModalRewardTitle').textContent = title;
        document.getElementById('qrModalCodeText').textContent = code;
        document.getElementById('qrModalStatus').textContent = status;

        const qrImage = document.getElementById('qrModalImg');
        if (qrImage) {
            qrImage.src = `https://api.qrserver.com/v1/create-qr-code/?size=220x220&bgcolor=121215&color=00f59b&data=${encodeURIComponent(code)}`;
        }
    });
}

function initAdminCharts() {
    const canvas = document.getElementById('weeklyAttendanceChart');
    if (!canvas || typeof Chart === 'undefined') return;

    let labels = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];
    let values = [64, 78, 92, 85, 110, 74, 48];

    try {
        if (canvas.dataset.labels) labels = JSON.parse(canvas.dataset.labels);
        if (canvas.dataset.values) values = JSON.parse(canvas.dataset.values);
    } catch (e) {
        console.warn('Usando datos por defecto para gráfica semanal');
    }

    const isLight = document.documentElement.getAttribute('data-bs-theme') === 'light';
    const accent = isLight ? '#00a86b' : '#00f59b';
    const tickColor = isLight ? '#475569' : '#a1a1aa';
    const gridColor = isLight ? 'rgba(15, 23, 42, 0.08)' : 'rgba(255,255,255,0.06)';

    const ctx = canvas.getContext('2d');
    const gradient = ctx.createLinearGradient(0, 0, 0, 280);
    gradient.addColorStop(0, isLight ? 'rgba(0, 168, 107, 0.42)' : 'rgba(0, 245, 155, 0.42)');
    gradient.addColorStop(1, 'rgba(0, 245, 155, 0.02)');

    weeklyChartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Asistencias',
                data: values,
                backgroundColor: gradient,
                borderColor: accent,
                borderWidth: 2,
                borderRadius: 10
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: isLight ? '#0f172a' : '#18181d',
                    titleColor: '#fafafa',
                    bodyColor: '#00f59b',
                    padding: 10
                }
            },
            scales: {
                x: {
                    grid: { display: false },
                    ticks: { color: tickColor, font: { family: 'Inter', weight: '600' } }
                },
                y: {
                    beginAtZero: true,
                    grid: { color: gridColor },
                    ticks: { color: tickColor, font: { family: 'JetBrains Mono' } }
                }
            }
        }
    });
}

function initTableSearchFilters() {
    const searchInputs = document.querySelectorAll('[data-table-search]');
    searchInputs.forEach(input => {
        const targetId = input.getAttribute('data-table-search');
        const table = document.getElementById(targetId);
        if (!table) return;

        input.addEventListener('input', () => {
            const term = input.value.trim().toLowerCase();
            const rows = table.querySelectorAll('tbody tr');
            rows.forEach(row => {
                const text = row.textContent.toLowerCase();
                row.classList.toggle('d-none', term !== '' && !text.includes(term));
            });
        });
    });

    const statusFilterBtns = document.querySelectorAll('[data-redemption-status]');
    const redemptionsTable = document.getElementById('redemptionsTable');
    if (statusFilterBtns.length && redemptionsTable) {
        statusFilterBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                statusFilterBtns.forEach(b => {
                    b.classList.remove('btn-neon');
                    b.classList.add('btn-ghost-dark');
                });
                btn.classList.remove('btn-ghost-dark');
                btn.classList.add('btn-neon');

                const status = btn.getAttribute('data-redemption-status');
                const rows = redemptionsTable.querySelectorAll('tbody tr');
                rows.forEach(row => {
                    const rowStatus = row.getAttribute('data-status') || '';
                    row.classList.toggle('d-none', status !== 'ALL' && rowStatus !== status);
                });
            });
        });
    }
}

function initCheckinQuickFilter() {
    const chips = document.querySelectorAll('[data-quick-doc]');
    const input = document.getElementById('checkinSearchInput');
    if (!chips.length || !input) return;

    chips.forEach(chip => {
        chip.addEventListener('click', () => {
            input.value = chip.getAttribute('data-quick-doc');
            input.closest('form').submit();
        });
    });
}

function fillDemoLogin(email, password) {
    const emailInput = document.getElementById('loginEmail');
    const passInput = document.getElementById('loginPassword');
    if (emailInput && passInput) {
        emailInput.value = email;
        passInput.value = password;
        showGymToast('Credenciales demo cargadas. Pulsa Entrar.', 'info');
    }
}

function openRewardFormModal(btn) {
    const modal = document.getElementById('rewardFormModal');
    if (!modal) return;
    document.getElementById('adminRewardId').value = btn?.dataset.id || '';
    document.getElementById('adminRewardName').value = btn?.dataset.name || '';
    document.getElementById('adminRewardDescription').value = btn?.dataset.description || '';
    document.getElementById('adminRewardCategory').value = btn?.dataset.category || 'DIGITAL';
    document.getElementById('adminRewardType').value = btn?.dataset.type || 'DIGITAL';
    document.getElementById('adminRewardCost').value = btn?.dataset.cost || 500;
    document.getElementById('adminRewardStock').value = btn?.dataset.stock || 20;
    document.getElementById('rewardModalLabel').textContent = btn?.dataset.id ? 'Editar Premio VIP' : 'Nuevo Premio VIP';
}