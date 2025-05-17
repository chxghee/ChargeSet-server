//== station.js ==//

// 주간 통계 API 데이터로 차트 생성
async function fetchStationTodayRevenue() {
    const stationId = window.chargingStationInfo.stationId;

    try {
        const response = await fetch(`/api/stations/${stationId}/today-revenue`);
        const data = await response.json();

        document.getElementById("today-user-count").textContent = `${data.count}명`;
        const energy = data.totalEnergy;
        document.getElementById("today-energy").textContent = energy === 0 ? "0 kWh" : `${(energy / 1000).toFixed(1)} kWh`;
        document.getElementById("today-revenue").textContent = `${data.totalRevenue.toLocaleString()}원`;
    } catch (error) {
        console.error("금일 수익 데이터 로딩 실패:", error);
    }
}

let hourlyChart = null;

async function fetchHourlyChartData() {
    const stationId = window.chargingStationInfo.stationId;
    const date = document.getElementById("stat-date").value;
    const metric = document.getElementById("stat-metric").value;

    const url = `/api/stations/${stationId}/hourly-revenue?searchingDate=${date}`;
    const res = await fetch(url);
    const json = await res.json();

    const labels = json.data.map(d => `${d.hour}시`);
    const values = json.data.map(d => {
        if (metric === "totalEnergy") {
            return +(d[metric] / 1000).toFixed(1);
        }
        return d[metric];
    });


    if (hourlyChart) hourlyChart.destroy();

    const labelMap = {
        count: "횟수",
        totalEnergy: "전력량 (kWh)",
        totalRevenue: "매출 (원)"
    };

    const backgroundColors = {
        count: "#42a5f5",
        totalEnergy: "#66bb6a",
        totalRevenue: "#ffa726"
    };

    hourlyChart = new Chart(document.getElementById("hourly-chart"), {
        type: "bar",
        data: {
            labels: labels,
            datasets: [{
                label: labelMap[metric],
                data: values,
                backgroundColor: backgroundColors[metric]
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: { beginAtZero: true },
                x: { grid: { display: false } }
            },
            plugins: {
                legend: { display: true }
            }
        }
    });
}

async function fetchUsageChartData() {
    const stationId = window.chargingStationInfo.stationId;
    const res = await fetch(`/api/stations/${stationId}/user-usage-summary`);
    const data = await res.json();

    const labels = ['1회 이용', '2회 이용', '3~4회 이용', '5회 이상 이용'];
    const values = [data.once, data.twice, data.thirdAndFourth, data.fifthOrMore];
    const colors = ['#4caf50', '#2196f3', '#ff9800', '#f44336'];

    const total = values.reduce((acc, val) => acc + val, 0);

    // 중앙 라벨 텍스트 삽입
    const centerLabel = document.getElementById("user-usage-center-label");
    centerLabel.innerHTML = `
    <div>${total}명</div>
    <div style="font-size: 12px; font-weight: normal;">전체 이용자</div>
  `;

    // 차트 아래 텍스트 표시
    document.getElementById("user-usage-total-text").textContent =
        `재방문률: ${(100 - (data.once / total * 100 || 0)).toFixed(1)}% (2회 이상)`;

    new Chart(document.getElementById('user-usage-summary'), {
        type: 'doughnut',
        data: {
            labels,
            datasets: [{
                data: values,
                backgroundColor: colors,
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            cutout: '70%',
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        font: { size: 12 }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            const val = context.raw;
                            const percentage = total ? ((val / total) * 100).toFixed(1) : 0;
                            return `${context.label}: ${val}명 (${percentage}%)`;
                        }
                    }
                }
            }
        }
    });
}



// 초기화
window.addEventListener('DOMContentLoaded', () => {
    fetchStationTodayRevenue();
    document.getElementById("stat-date").valueAsDate = new Date();
    fetchHourlyChartData();
    fetchUsageChartData();
});
