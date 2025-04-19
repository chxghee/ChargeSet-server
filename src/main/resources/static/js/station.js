//== station.js ==//

// 주간 통계 API 데이터로 차트 생성
async function fetchStationTodayRevenue() {
    const stationId = window.chargingStationInfo.stationId;

    try {
        const response = await fetch(`/api/stations/${stationId}/today-revenue`);
        const data = await response.json();

        document.getElementById("today-user-count").textContent = `${data.count}명`;
        document.getElementById("today-energy").textContent = `${data.totalEnergy.toLocaleString()} kWh`;
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
    const values = json.data.map(d => d[metric]);

    if (hourlyChart) hourlyChart.destroy();

    const labelMap = {
        count: "횟수",
        totalEnergy: "전력량 (Wh)",
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


// 초기화
window.addEventListener('DOMContentLoaded', () => {
    fetchStationTodayRevenue();
    document.getElementById("stat-date").valueAsDate = new Date();
    fetchHourlyChartData();
});
