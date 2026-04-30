'use strict';

// ─── Constants ────────────────────────────────────────────────────────────────
const DEBOUNCE_MS       = 600;
const SKELETON_MAX_ROWS = 8;

// ─── JSDoc Typedefs ───────────────────────────────────────────────────────────

/**
 * @typedef {{ label: string, mode: string, modeLabel: string, numbers: number[] }} GameLine
 * @typedef {{ unit: number, total: number, currency: string }} TicketPrice
 * @typedef {{
 *   round: number,
 *   issuedAt: string,
 *   drawDate: string,
 *   claimDeadline: string,
 *   receiptNumber: string,
 *   games: GameLine[],
 *   price: TicketPrice
 * }} TicketResponse
 * @typedef {{ games: number, manual: number, manualNumbers: string[], skipHistory: boolean }} TicketInputs
 */

// ─── Typed DOM Helpers ────────────────────────────────────────────────────────

/** @param {string} id @returns {HTMLElement} */
const $ = (id) => /** @type {HTMLElement} */ (document.getElementById(id));

/** input / textarea / checkbox 등 폼 요소 전용 — .value .checked .max .disabled 접근 */
/** @param {string} id @returns {HTMLInputElement} */
const $input = (id) => /** @type {HTMLInputElement} */ (document.getElementById(id));

// ─── Utilities ────────────────────────────────────────────────────────────────

/** @param {number} n @returns {string} */
function ballClass(n) {
    if (n <=  9) return 'ball-y';
    if (n <= 19) return 'ball-b';
    if (n <= 29) return 'ball-r';
    if (n <= 39) return 'ball-s';
    return 'ball-g';
}

/** @param {string} text @returns {string[]} */
function parseManualLines(text) {
    if (!text) return [];
    return text.split(/\r?\n/).map(l => l.trim()).filter(Boolean);
}

// ─── Request State ────────────────────────────────────────────────────────────
let currentRequest = null;
let debounceTimer   = null;

// ─── Input Reading ────────────────────────────────────────────────────────────

/** @returns {TicketInputs} */
function getInputs() {
    const games         = Math.min(50, Math.max(1, parseInt($input('games-input').value,  10) || 5));
    const manual        = Math.min(games, Math.max(0, parseInt($input('manual-input').value, 10) || 0));
    const manualNumbers = parseManualLines($input('manual-numbers').value);
    const skipHistory   = !$input('skip-history').checked; // checked="역대 제외" → skipHistory=false
    return { games, manual, manualNumbers, skipHistory };
}

// ─── API ─────────────────────────────────────────────────────────────────────

/** @param {TicketInputs} inputs @returns {Promise<TicketResponse>} */
async function fetchTicket({ games, manual, manualNumbers, skipHistory }) {
    if (currentRequest) currentRequest.abort();
    currentRequest = new AbortController();

    const params = new URLSearchParams({
        games:       String(games),
        skipHistory: String(skipHistory),
    });
    if (manualNumbers.length > 0) {
        // manualNumbers 제공 시 백엔드가 manual 파라미터를 무시하므로 전송하지 않음
        for (const line of manualNumbers) params.append('manualNumbers', line);
    } else if (manual > 0) {
        params.append('manual', String(manual));
    }

    const res = await fetch('/api/lotto/ticket?' + params, {
        headers: { Accept: 'application/json' },
        signal: currentRequest.signal,
    });

    if (!res.ok) {
        // RFC 7807 ProblemDetail: detail > title > 상태코드 순으로 메시지 추출
        const body = await res.json().catch(() => ({}));
        throw new Error(body.detail || body.title || ('HTTP ' + res.status));
    }
    return res.json();
}

// ─── Rendering ────────────────────────────────────────────────────────────────

/** @param {number} count */
function renderSkeleton(count) {
    const ul = $('games');
    ul.innerHTML = '';
    const fragment = document.createDocumentFragment();
    const rows = Math.max(1, Math.min(SKELETON_MAX_ROWS, count || 5));
    for (let i = 0; i < rows; i++) {
        const li = document.createElement('li');
        li.className = 'game game--skeleton';
        const label = document.createElement('span');
        label.className = 'game__label skeleton-block';
        li.appendChild(label);
        for (let j = 0; j < 6; j++) {
            const cell = document.createElement('span');
            cell.className = 'game__num skeleton-circle';
            li.appendChild(cell);
        }
        fragment.appendChild(li);
    }
    ul.appendChild(fragment);
}

/** @param {GameLine[]} games */
function renderGames(games) {
    const ul = $('games');
    ul.innerHTML = '';
    const fragment = document.createDocumentFragment();
    for (const g of games) {
        const li = document.createElement('li');
        li.className = 'game';

        const label = document.createElement('span');
        label.className = 'game__label';
        label.textContent = g.label;

        const badge = document.createElement('small');
        badge.textContent = g.modeLabel; // 백엔드 PickMode.label() → "수동" | "자동"
        label.appendChild(badge);
        li.appendChild(label);

        for (const n of g.numbers) {
            const cell = document.createElement('span');
            cell.className = 'game__num ' + ballClass(n);
            cell.textContent = String(n); // number → string (textContent는 string 타입)
            li.appendChild(cell);
        }
        fragment.appendChild(li);
    }
    ul.appendChild(fragment);
}

/** @param {TicketResponse} t */
function render(t) {
    // round=0 은 skipHistory=true(빠른 모드) 응답 → "-회" 표시
    $('round').textContent         = (t.round > 0 ? String(t.round) : '-') + '회';
    $('issuedAt').textContent      = t.issuedAt;
    $('drawDate').textContent      = t.drawDate;
    $('claimDeadline').textContent = t.claimDeadline;
    $('receiptTop').textContent    = t.receiptNumber;
    $('receiptBottom').textContent = t.receiptNumber;
    $('totalPrice').textContent    = t.price.total.toLocaleString('ko-KR');
    $('currency').textContent      = t.price.currency;
    renderGames(t.games);
}

// ─── UI State ─────────────────────────────────────────────────────────────────

/** @param {boolean} on */
function setLoading(on) {
    const btn = /** @type {HTMLButtonElement} */ ($('btn-reload'));
    btn.disabled = on;
    btn.setAttribute('aria-busy', String(on));
    btn.textContent = on ? '발권 중…' : '새로 발권';
    $('ticket').setAttribute('aria-busy', String(on));
}

/** @param {string} msg */
function showError(msg) {
    $('error-text').textContent = msg;
    $('error-msg').hidden = false;
}

function clearError() {
    $('error-text').textContent = '';
    $('error-msg').hidden = true;
}

// ─── Load ─────────────────────────────────────────────────────────────────────
async function load() {
    clearError();
    const inputs = getInputs();
    setLoading(true);
    renderSkeleton(inputs.games);
    try {
        render(await fetchTicket(inputs));
    } catch (e) {
        if (/** @type {Error} */ (e).name !== 'AbortError') {
            showError('티켓 발급 실패: ' + /** @type {Error} */ (e).message);
            console.error(e);
        }
    } finally {
        setLoading(false);
    }
}

function scheduleReload() {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(load, DEBOUNCE_MS);
}

// ─── Input Sync ───────────────────────────────────────────────────────────────
function syncManualMax() {
    const games       = Math.min(50, Math.max(1, parseInt($input('games-input').value, 10) || 5));
    const manualInput = $input('manual-input');
    manualInput.max   = String(games);  // HTMLInputElement.max 는 string
    if (parseInt(manualInput.value, 10) > games) manualInput.value = String(games);
}

function syncManualFromTextarea() {
    // textarea에 번호가 있으면 manual-input 비활성화 후 줄 수로 자동 채움
    // (백엔드는 manualNumbers 제공 시 manual 파라미터를 무시하고 크기로 결정)
    const lines       = parseManualLines($input('manual-numbers').value);
    const manualInput = $input('manual-input');
    const hasContent  = lines.length > 0;
    manualInput.disabled = hasContent;
    if (hasContent) manualInput.value = String(lines.length); // number → string
}

// ─── Events ───────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    $('btn-reload').addEventListener('click', load);
    $('btn-retry').addEventListener('click', load);
    $('btn-print').addEventListener('click', () => window.print());
    $('btn-close').addEventListener('click', () => { $('ticket').hidden = true; });

    $('games-input').addEventListener('input', () => { syncManualMax(); scheduleReload(); });
    $('manual-input').addEventListener('input', scheduleReload);
    $('manual-numbers').addEventListener('input', () => { syncManualFromTextarea(); scheduleReload(); });
    $('skip-history').addEventListener('change', load); // 즉시 재발권 (디바운스 불필요)

    syncManualMax();
    syncManualFromTextarea();
    load().catch(error => console.error('초기 로드 중 오류 발생:', error)).then(() => { /* 초기 로드 완료 */ });
});
