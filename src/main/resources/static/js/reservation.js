// reservation.js
import { renderPagination } from '/js/pagination.js';

const pageSize = 10;

async function fetchReservationData(page) {
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


    const url = `/api/reservations/all?${params.toString()}`;

    try {
        const res = await fetch(url);
        if (res.ok) {
            const data = await res.json();
            renderReservationData(data.content);
            renderPagination(data.page, page, fetchReservationData)

        } else {
            const errorData = await res.json();
            alert(`오류: ${errorData.message || '알 수 없는 오류 관리자에게 문의하세요.'}`);
        }
    } catch (error) {
        alert("에러!!")
    }
}

function renderReservationData(reservation) {
    const tbody = document.getElementById("reservation-table-body");
    tbody.innerHTML = '';

    if (!reservation || reservation.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9">예약 정보가 없습니다.</td></tr>`;
        return;
    }

    reservation.forEach(item => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${item.id || '-'}</td>
            <td>${item.stationId || '-'}</td>
            <td>${item.evseId || '-'}</td>
            <td>${item.userId || '-'}</td>
            <td>${item.startTime || '-'}</td>
            <td>${item.endTime || '-'}</td>
            <td>${item.targetEnergyWh?.toLocaleString() || 0}</td>
            <td>${item.createdAt || '-'}</td>
            <td>${formatReservationStatus(item.reservationStatus)}</td>
        `;
        tbody.appendChild(row);
    });
}

function formatReservationStatus(status) {
    switch (status) {
        case 'ACTIVE': return '예약됨';
        case 'WAITING': return '충전 대기 중';
        case 'ONGOING': return '충전 중인 예약';
        case 'EXPIRED': return '예약 만료(노쇼)';
        case 'COMPLETED': return '충전 완료된 예약';
        case 'CANCELED': return '최소된 예약';
        default: return status;
    }
}


window.addEventListener('DOMContentLoaded', () => {
   fetchReservationData(0);

    // 검색 버튼 클릭 이벤트 등록
    document.querySelector("#search-button")
        .addEventListener("click", () => fetchReservationData(0));
});
