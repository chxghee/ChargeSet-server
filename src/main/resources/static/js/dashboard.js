// dashboard.js

let revenueChart, energyChart, countChart;

async function fetchWeeklyStats() {
    const res = await fetch('/api/transactions/weekly-revenue');
    const result = await res.json();
    const data = result.dailyStats;

    const labels = data.map(d =>
        new Date(d.date).toLocaleDateString('ko-KR', { month: '2-digit', day: '2-digit' })
    );
    const revenue = data.map(d => d.totalRevenue);
    const energy = data.map(d => (d.totalEnergy / 1000).toFixed(1));
    const count = data.map(d => d.count);

    if (!revenueChart) {
        const commonOptions = {
            responsive: true,
            plugins: { legend: { display: false } },
            scales: {
                y: { beginAtZero: true },
                x: { grid: { display: false } }
            }
        };

        revenueChart = new Chart(document.getElementById('revenueChart'), {
            type: 'line',
            data: {
                labels,
                datasets: [{
                    label: '일일 수익 (원)',
                    data: revenue,
                    borderColor: '#fbbf24',
                    backgroundColor: 'transparent',
                    tension: 0.4
                }]
            },
            options: commonOptions
        });

        energyChart = new Chart(document.getElementById('energyChart'), {
            type: 'line',
            data: {
                labels,
                datasets: [{
                    label: '전력량 (kWh)',
                    data: energy,
                    borderColor: '#4dabf7',
                    backgroundColor: 'transparent',
                    tension: 0.4
                }]
            },
            options: commonOptions
        });

        countChart = new Chart(document.getElementById('countChart'), {
            type: 'line',
            data: {
                labels,
                datasets: [{
                    label: '충전 횟수',
                    data: count,
                    borderColor: '#81c784',
                    backgroundColor: 'transparent',
                    tension: 0.4
                }]
            },
            options: commonOptions
        });
    } else {
        // 기존 차트에 데이터만 갱신
        revenueChart.data.labels = labels;
        revenueChart.data.datasets[0].data = revenue;
        revenueChart.update();

        energyChart.data.labels = labels;
        energyChart.data.datasets[0].data = energy;
        energyChart.update();

        countChart.data.labels = labels;
        countChart.data.datasets[0].data = count;
        countChart.update();
    }
}



let evseStatusChartInstance = null;

async function fetchEvseStatus() {
    const res = await fetch("/api/evses/status-summary");
    const data = await res.json();

    const labels = ['사용 가능', '충전 중', '예약됨', '고장', '오프라인'];
    const values = [data.available, data.charging, data.reserved, data.faulted, data.offline];
    const colors = ['#2196f3', '#4caf50', '#ff9800', '#f44336', '#9E9E9E'];

    if (!evseStatusChartInstance) {
        // 최초 1회 차트 생성
        const ctx = document.getElementById('evseStatusChart').getContext('2d');
        evseStatusChartInstance = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: values,
                    backgroundColor: colors,
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { position: 'bottom' },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                return `${context.label}: ${context.raw}대`;
                            }
                        }
                    }
                }
            }
        });
    } else {
        // 이후는 데이터만 갱신
        evseStatusChartInstance.data.datasets[0].data = values;
        evseStatusChartInstance.update();
    }
}
// 지도에 충전소 마커 및 오버레이 표시
function loadMapWithStations() {
    const mapContainer = document.getElementById('map');
    const map = new kakao.maps.Map(mapContainer, {
        center: new kakao.maps.LatLng(37.5596942, 127.028838),
        level: 8
    });

    const markerImage = new kakao.maps.MarkerImage(
        'https://cdn.iconscout.com/icon/premium/png-512-thumb/charging-station-location-4982614-4140308.png?f=webp&w=512',
        new kakao.maps.Size(48, 52),
        { offset: new kakao.maps.Point(24, 52) }
    );

    const openOverlays = new Map();

    window.closeOverlay = function (stationId) {
        const overlay = openOverlays.get(stationId);
        if (overlay) {
            overlay.setMap(null);
            openOverlays.delete(stationId);
        }
    };

    window.chargingStations.forEach(station => {
        const position = new kakao.maps.LatLng(station.location.lat, station.location.lng);
        const marker = new kakao.maps.Marker({ position, image: markerImage, map });

        kakao.maps.event.addListener(marker, 'click', function () {
            if (openOverlays.has(station.stationId)) return;

            fetch(`/api/evses/${station.stationId}/status-summary`)
                .then(res => res.json())
                .then(status => {
                    const content = `
                        <div class="customoverlay-box">
                            <div class="customoverlay-header">
                                <strong>${station.name}</strong>
                                <button class="overlay-close" onclick="window.closeOverlay('${station.stationId}')">×</button>
                            </div>
                            <div class="customoverlay-body">
                                <div>주소: ${station.location.address}</div>
                                <div>사용 가능: ${status.available}대</div>
                                <div>충전 중: ${status.charging}대</div>
                                <div>예약됨: ${status.reserved}대</div>
                                <div>고장: ${status.faulted}대</div>
                                <div>오프라인: ${status.offline}대</div>
                            </div>
                        </div>`;

                    const overlay = new kakao.maps.CustomOverlay({
                        content,
                        position,
                        yAnchor: 1.4,
                        zIndex: 3
                    });

                    overlay.setMap(map);
                    openOverlays.set(station.stationId, overlay);
                });
        });
    });
}

// 예약 데이터 불러오기 + 렌더링
const pageSize = 5;

async function fetchReservations(page) {
    const res = await fetch(`/api/reservations/today-stat?page=${page}&size=${pageSize}`);
    const data = await res.json();

    renderReservations(data.content);
    renderPagination(data.page, page, fetchReservations);
}

function renderReservations(reservations) {
    const tbody = document.getElementById("reservation-table-body");
    tbody.innerHTML = '';

    if (!reservations || reservations.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7">오늘 예약된 정보가 없습니다.</td></tr>`;
        return;
    }

    reservations.forEach(item => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${item.id || '-'}</td>
            <td>${item.stationId || '-'}</td>
            <td>${item.evseId || '-'}</td>
            <td>${item.startTime || '-'}</td>
            <td>${item.endTime || '-'}</td>
            <td>${(item.targetEnergyWh / 1000).toFixed(1) || 0}</td>
            <td>${formatReservationStatus(item.reservationStatus)}</td>
        `;
        tbody.appendChild(row);
    });
}

function formatReservationStatus(status) {
    switch (status) {
        case 'ACTIVE': return '예약됨';
        case 'WAITING': return '대기 중';
        case 'ONGOING': return '충전 중';
        case 'EXPIRED': return '만료됨(no show)';
        case 'COMPLETED': return '완료됨';
        case 'CANCELED': return '취소됨';
        default: return status;
    }
}

// 주간 통계 API 데이터로 차트 생성
async function fetchStationTodayRevenue() {

    try {
        const response = await fetch(`/api/transactions/today-revenue`);
        const data = await response.json();

        document.getElementById("today-user-count").textContent = `${data.count}명`;
        const energy = data.totalEnergy;
        document.getElementById("today-energy").textContent = energy === 0 ? "0 kWh" : `${(energy / 1000).toFixed(1)} kWh`;
        document.getElementById("today-revenue").textContent = `${data.totalRevenue.toLocaleString()}원`;
    } catch (error) {
        console.error("금일 수익 데이터 로딩 실패:", error);
    }
}

// 초기화
window.addEventListener('DOMContentLoaded', () => {
    fetchStationTodayRevenue();
    fetchWeeklyStats();
    fetchEvseStatus();                         // 초기 1회 호출
    setInterval(fetchEvseStatus, 1500);        // 1.5초마다 상태 갱신
    setInterval(fetchStationTodayRevenue, 7000) // 6초마다 갱신
//    setInterval(fetchWeeklyStats, 10000);

    loadMapWithStations();
    fetchReservations(0);
});
