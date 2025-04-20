// transaction.js

import { renderPagination } from '/js/pagination.js';

const pageSize = 10;

async function fetchTransactionData(page) {
    const stationId = document.getElementById('station-select').value;
    const status = document.getElementById('status-select').value;
    const fromDate = document.getElementById('from-date').value;
    const toDate = document.getElementById('to-date').value;

    const params = new URLSearchParams();
    if (fromDate) params.append('from', fromDate);
    if (toDate) params.append('to', toDate);
    if (stationId) params.append('stationId', stationId);
    if (status) params.append('status', status);
    params.append('page', page);
    params.append('size', pageSize);

    const url = `/api/transactions/all?${params.toString()}`;

    try {
        const res = await fetch(url);
        if (res.ok) {
            const data = await res.json();
            renderTransactionData(data.content);
            renderPagination(data.page, page, fetchTransactionData);
        } else {
            const errorData = await res.json(); // JSON 응답을 파싱
            alert(`오류: ${errorData.message || '알 수 없는 오류 관리자에게 문의하세요.'}`);
        }
    } catch (error) {
        alert("에러!!!");
    }
}


function renderTransactionData(transactions) {
    const tbody = document.getElementById("transaction-table-body");
    tbody.innerHTML = '';

    if (!transactions || transactions.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9">충전 이력 정보가 없습니다.</td></tr>`;
        return;
    }

    transactions.forEach(item => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${item.id || '-'}</td>
            <td>${item.stationId || '-'}</td>
            <td>${item.evseId || '-'}</td>
            <td>${item.userId || '-'}</td>
            <td>${item.startTime || '-'}</td>
            <td>${item.endTime || '-'}</td>
            <td>${item.energyWh?.toLocaleString() || 0}</td>
            <td>${item.cost?.toLocaleString() || 0}</td>
            <td>${formatTransactionStatus(item.transactionStatus)}</td>
        `;
        tbody.appendChild(row);
    });
}

function formatTransactionStatus(status) {
    switch (status) {
        case 'COMPLETED': return '정상 완료';
        case 'ABORTED': return '서버 강제종료';
        case 'FAILED': return '충전 실패';
        case 'INTERRUPTED': return '충전 중 중단';
        default: return status;
    }
}

// 초기화
window.addEventListener('DOMContentLoaded', () => {
    fetchTransactionData(0);

    // 검색 버튼 클릭 이벤트 등록
    document.querySelector("#search-button")
        .addEventListener("click", () => fetchTransactionData(0));
});
