// reservation.js

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
            <td>${(item.targetEnergyWh / 1000).toFixed(1) || 0}</td>
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

// 충전소 미이행률 조회 API 호출
async function fetchNoShowData() {

    const url = '/api/reservations/no-show';
    const res = await fetch(url);
    const data = await res.json();

    const [station1, station2] = data.data;

    // 카드 제목
    document.getElementById("summary-first-title").textContent = station1.stationId;
    document.getElementById("summary-second-title").textContent = station2.stationId;

    // 카드 1
    document.getElementById('summary-first-complete').textContent = station1.completeCount;
    document.getElementById('summary-first-no-show').textContent = station1.expiredCount;
    document.getElementById('summary-first-no-show-rate').textContent = calculateRate(station1.expiredCount, station1.completeCount + station1.expiredCount);

    // 카드 2
    document.getElementById('summary-second-complete').textContent = station2.completeCount;
    document.getElementById('summary-second-no-show').textContent = station2.expiredCount;
    document.getElementById('summary-second-no-show-rate').textContent = calculateRate(station2.expiredCount, station2.completeCount + station2.expiredCount);

    setSummaryComments(data);
}

// 퍼센트 계산 유틸
function calculateRate(part, total) {
    if (total === 0) return '0.0';
    return ((part / total) * 100).toFixed(1);
}

// 충전소 미이행 관련 코멘트
function setSummaryComments(data) {

    const currentRate = (data.totalNoShowRate * 100).toFixed(1);
    const prevRate = (data.previousNoShowRate * 100).toFixed(1);
    const rateDiff = (currentRate - prevRate).toFixed(1);

    let compareText = '';
    if (rateDiff > 0) {
        compareText = `<span class="text-danger fw-bold">지난달(${prevRate}%) 대비 ${rateDiff}% 증가했습니다</span>`;
    } else if (rateDiff < 0) {
        compareText = `<span class="text-success fw-bold">지난달(${prevRate}%) 대비 ${Math.abs(rateDiff)}% 감소했습니다.</span>`;
    } else {
        compareText = `<span class="text-muted fw-bold">지난달과(${prevRate}%) 동일합니다.</span>`;
    }

    const message = `
        <div class="mb-1">
           ${data.fromDate} ~ ${data.toDate}</strong> 동안 전체 충전소의 
            <br> <strong class="text-primary">${data.totalFinishedReservationCount}</strong>건의 종료된 예약 중 
            <strong class="text-danger">${data.totalNoShowCount}</strong>건이 미이행되어<br>
            <span class="text-dark">노쇼율은 <strong>${currentRate}%</strong>입니다.</span>
        </div>
        <div>${compareText}</div>
    `;

    document.getElementById('summary-result-message').innerHTML = message;
}


window.addEventListener('DOMContentLoaded', () => {
    fetchReservationData(0);
    fetchNoShowData();

    // 검색 버튼 클릭 이벤트 등록
    document.querySelector("#search-button")
        .addEventListener("click", () => fetchReservationData(0));
});
