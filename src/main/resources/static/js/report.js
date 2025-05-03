// report.js

document.addEventListener('DOMContentLoaded', () => {
    fetchTotalStats();
});


async function fetchTotalStats() {
    try {
        const res = await fetch('/api/report/total-stats');
        const data = await res.json();

        // ✅ kWh 변환
        const totalEnergyKWh = (data.totalEnergy / 1000).toFixed(1);
        const station1 = data.stationChargingStats[0];
        const station2 = data.stationChargingStats[1];
        const station1EnergyKWh = (station1.totalEnergy / 1000).toFixed(1);
        const station2EnergyKWh = (station2.totalEnergy / 1000).toFixed(1);

        const totalGrowthRate = Number(data.revenueGrowthRate.toFixed(1));


        const date = new Date(data.fromDate);
        const formatted = `${date.getFullYear()}년 ${date.getMonth() + 1}월`;
        document.getElementById('report-title').textContent = `${formatted} 보고서`;

        // ✅ 상단 통계
        document.getElementById('total-station-charging-usage').textContent = `${data.totalCount.toLocaleString()}회`;
        document.getElementById('total-station-energy-usage').textContent = `${totalEnergyKWh.toLocaleString()} kWh`;
        document.getElementById('total-station-revenue').textContent = `${data.totalRevenue.toLocaleString()}원`;


        let growthText = '';
        if (totalGrowthRate > 0) {
            growthText = `<span class="text-danger">${totalGrowthRate}%</span>`;
        } else if (totalGrowthRate < 0) {
            growthText = `<span class="text-primary">${totalGrowthRate}%</span>`;
        } else {
            growthText = `<span class="text-muted">${totalGrowthRate}%</span>`;
        }

        document.getElementById('total-station-revenue-growth-rate').innerHTML = growthText;


        // ✅ 충전소별 통계 카드
        document.getElementById('summary-first-title').textContent = `${station1.stationId}`;
        document.getElementById('summary-first-revenue').textContent = station1.totalRevenue.toLocaleString();
        document.getElementById('summary-first-energy').textContent = station1EnergyKWh.toLocaleString();
        document.getElementById('summary-first-count').textContent = station1.totalCount.toLocaleString();
        document.getElementById('summary-first-growth-rate').textContent = `${station1.revenueGrowthRate.toFixed(1)}%`;

        document.getElementById('summary-second-title').textContent = `${station2.stationId}`;
        document.getElementById('summary-second-revenue').textContent = station2.totalRevenue.toLocaleString();
        document.getElementById('summary-second-energy').textContent = station2EnergyKWh.toLocaleString();
        document.getElementById('summary-second-count').textContent = station2.totalCount.toLocaleString();
        document.getElementById('summary-second-growth-rate').textContent = `${station2.revenueGrowthRate.toFixed(1)}%`;

        // ✅ 요약 멘트
        getGrowthRateSummaryComments(data, totalEnergyKWh);

        renderRevenueGrowthCharts(data);
        renderComparisonPieCharts(data);

    } catch (err) {
        console.error('보고서 데이터 로딩 실패:', err);
    }
}



// 충전소 미이행 관련 코멘트
function getGrowthRateSummaryComments(data, totalEnergyKWh) {

    const growthRate = data.revenueGrowthRate.toFixed(1);

    let summaryText = '';
    if (growthRate > 0) {
        summaryText = `<span class="text-danger fw-bold">전월 대비 ${growthRate}% 의 매출 성장이 있었습니다.</span>`;
    } else if (growthRate < 0) {
        summaryText = `<span class="text-primary fw-bold">전월 대비 ${growthRate}% 의 매출 감소가 있었습니다.</span>`;
    } else {
        summaryText = `<span class="text-muted fw-bold">전월 대비 ${growthRate}% 의 성장률로, 매출의 변동이 없었습니다.</span>`;
    }

    const message = `
        <div class="mb-1">
              <strong>${data.fromDate}</strong>부터 <strong>${data.toDate}</strong>까지 <br> 
              총 <strong class="text-dark">${data.totalCount}</strong>건의 충전이 진행되었고,<br>
              총 매출은 <strong class="text-primary">${data.totalRevenue.toLocaleString()}원</strong>, 
              전력 사용량은 <strong class="text-success">${totalEnergyKWh.toLocaleString()} kWh</strong>입니다.
        </div>
        <div class="mt-2">${summaryText}</div>
    `;

    document.getElementById('summary-result-message').innerHTML = message;
}



let revenueDonutChart, countDonutChart, energyDonutChart;
let totalRevenueChart;

function renderRevenueGrowthCharts(data) {
    const station1 = data.stationChargingStats[0];
    const station2 = data.stationChargingStats[1];

    // ✅ 기준 날짜 설정
    const toDate = new Date(data.toDate);
    const currentYearMonth = `${toDate.getFullYear()}-${String(toDate.getMonth() + 1).padStart(2, '0')}`;

    // ✅ 데이터
    const labels = [
        '전체 충전소',
        `${station1.stationId}`,
        `${station2.stationId}`
    ];

    const prevData = [
        data.totalRevenueLastMonth,
        station1.totalRevenueLastMonth,
        station2.totalRevenueLastMonth
    ];

    const currentData = [
        data.totalRevenue,
        station1.totalRevenue,
        station2.totalRevenue
    ];

    const prevColor = '#cfd8dc'; // 연회색 (지난달)
    const upColor = '#ef5350';   // 빨간색 (성장)
    const downColor = '#42a5f5'; // 파란색 (감소)

    if (totalRevenueChart) totalRevenueChart.destroy();
    totalRevenueChart = new Chart(document.getElementById('total-revenue-growth-chart'), {
        type: 'bar',
        data: {
            labels,
            datasets: [
                {
                    label: `이전달 매출`,
                    data: prevData,
                    backgroundColor: prevColor,
                    categoryPercentage: 0.6,
                    barPercentage: 0.6
                },
                {
                    label: `${currentYearMonth} 매출`,
                    data: currentData,
                    backgroundColor: currentData.map((v, i) =>
                        v >= prevData[i] ? upColor : downColor
                    ),
                    categoryPercentage: 0.6,
                    barPercentage: 0.6
                }
            ]
        },
        options: {
            responsive: true,
            plugins: {
                tooltip: {
                    callbacks: {
                        label: ctx => `${ctx.dataset.label}: ${ctx.raw.toLocaleString()}원`
                    }
                },
                legend: {
                    display: true,
                    position: 'bottom'
                }
            },
            scales: {
                x: {
                    stacked: false
                },
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: val => `${val.toLocaleString()}`
                    }
                }
            }
        }
    });
}




function renderComparisonPieCharts(data) {
    const station1 = data.stationChargingStats[0];
    const station2 = data.stationChargingStats[1];

    // ✅ 매출 비교
    if (revenueDonutChart) revenueDonutChart.destroy();
    revenueDonutChart = new Chart(document.getElementById('revenue-donut-chart'), {
        type: 'pie', // ✅ 도넛 → 파이로 변경
        data: {
            labels: [station1.stationId, station2.stationId],
            datasets: [{
                data: [station1.totalRevenue, station2.totalRevenue],
                backgroundColor: ['#42a5f5', '#66bb6a'],
                borderWidth: 1
            }]
        },
        options: basePieChartOptions('원')
    });

    // ✅ 충전 횟수 비교
    if (countDonutChart) countDonutChart.destroy();
    countDonutChart = new Chart(document.getElementById('count-donut-chart'), {
        type: 'pie',
        data: {
            labels: [station1.stationId, station2.stationId],
            datasets: [{
                data: [station1.totalCount, station2.totalCount],
                backgroundColor: ['#ffa726', '#ab47bc'],
                borderWidth: 1
            }]
        },
        options: basePieChartOptions('회')
    });

    // ✅ 전력 사용량 비교
    if (energyDonutChart) energyDonutChart.destroy();
    energyDonutChart = new Chart(document.getElementById('energy-donut-chart'), {
        type: 'pie',
        data: {
            labels: [station1.stationId, station2.stationId],
            datasets: [{
                data: [station1.totalEnergy / 1000, station2.totalEnergy / 1000], // ✅ kWh 변환
                backgroundColor: ['#26c6da', '#ec407a'],
                borderWidth: 1
            }]
        },
        options: basePieChartOptions('kWh')
    });
}

function basePieChartOptions(unit) {
    return {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            tooltip: {
                callbacks: {
                    label: ctx => `${ctx.label}: ${ctx.raw.toLocaleString()} ${unit}`
                }
            },
            legend: {
                position: 'bottom',
                labels: {
                    boxWidth: 15,
                    font: {
                        size: 12
                    }
                }
            }
        }
    };
}


async function downloadExcel() {
    try {
        const res = await fetch('/api/report/download-excel');

        if (!res.ok) {
            throw new Error('엑셀 파일 다운로드 실패');
        }

        const blob = await res.blob();
        const disposition = res.headers.get('Content-Disposition') || '';
        const fileNameMatch = disposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
        let fileName = 'report.xlsx';

        if (fileNameMatch && fileNameMatch[1]) {
            fileName = fileNameMatch[1].replace(/['"]/g, ''); // 따옴표 제거
        }

        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
    } catch (error) {
        alert('엑셀 다운로드 중 오류가 발생했습니다.');
        console.error(error);
    }
}

