'use strict';

const $ = (id) => document.getElementById(id);

let currentRequest = null;

function getInputs() {
    const games = Math.min(50, Math.max(1, parseInt($('games-input').value, 10) || 5));
    const manual = Math.min(games, Math.max(0, parseInt($('manual-input').value, 10) || 0));
    return { games, manual };
}

async function fetchTicket(games, manual) {
    if (currentRequest) {
        currentRequest.abort();
    }
    currentRequest = new AbortController();

    const params = new URLSearchParams({ games, manual, skipHistory: 'true' });
    const res = await fetch('/api/lotto/ticket?' + params, {
        headers: { Accept: 'application/json' },
        signal: currentRequest.signal,
    });

    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.detail || 'HTTP ' + res.status);
    }
    return res.json();
}

function renderGames(games) {
    const ul = $('games');
    ul.innerHTML = '';
    for (const g of games) {
        const li = document.createElement('li');
        li.className = 'game';

        const label = document.createElement('span');
        label.className = 'game__label';
        label.textContent = g.label;

        const badge = document.createElement('small');
        badge.textContent = g.modeLabel;
        label.appendChild(badge);
        li.appendChild(label);

        for (const n of g.numbers) {
            const cell = document.createElement('span');
            cell.className = 'game__num';
            cell.textContent = n;
            li.appendChild(cell);
        }
        ul.appendChild(li);
    }
}

function render(t) {
    $('round').textContent = (t.round > 0 ? t.round : '-') + '회';
    $('issuedAt').textContent = t.issuedAt;
    $('drawDate').textContent = t.drawDate;
    $('claimDeadline').textContent = t.claimDeadline;
    $('receiptTop').textContent = t.receiptNumber;
    $('receiptBottom').textContent = t.receiptNumber;
    $('totalPrice').textContent = t.price.total.toLocaleString('ko-KR');
    $('currency').textContent = t.price.currency;
    renderGames(t.games);
}

function setLoading(on) {
    const btn = $('btn-reload');
    const section = $('ticket');
    btn.disabled = on;
    btn.setAttribute('aria-busy', String(on));
    btn.textContent = on ? '발권 중…' : '새로 발권';
    section.setAttribute('aria-busy', String(on));
}

function showError(msg) {
    const el = $('error-msg');
    el.textContent = msg;
    el.hidden = false;
}

function clearError() {
    const el = $('error-msg');
    el.textContent = '';
    el.hidden = true;
}

async function load() {
    clearError();
    setLoading(true);
    try {
        const { games, manual } = getInputs();
        const t = await fetchTicket(games, manual);
        render(t);
    } catch (e) {
        if (e.name !== 'AbortError') {
            showError('티켓 발급 실패: ' + e.message);
            console.error(e);
        }
    } finally {
        setLoading(false);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    $('btn-reload').addEventListener('click', load);
    $('btn-close').addEventListener('click', () => window.close());
    $('btn-print').addEventListener('click', () => window.print());
    load();
});
