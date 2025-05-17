// transaction.js




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
            <td><a href="#" class="transaction-link" data-id="${item.id}">${item.id}</a></td>
            <td>${item.stationId || '-'}</td>
            <td>${item.evseId || '-'}</td>
            <td>${item.userId || '-'}</td>
            <td>${item.startTime || '-'}</td>
            <td>${item.endTime || '-'}</td>
            <td>${(item.energyWh / 1000).toFixed(1) || 0}</td>
            <td>${item.cost?.toLocaleString() || 0}</td>
            <td>${formatTransactionStatus(item.transactionStatus)}</td>
        `;
        tbody.appendChild(row);
    });

    // 충전 ID 클릭 이벤트
    document.querySelectorAll('.transaction-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const transactionId = e.target.dataset.id;
            openProfileModal(transactionId);
        });
    })
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

let profileChart = null;

async function openProfileModal(transactionId) {
    try {
        const res = await fetch(`/api/transactions/${transactionId}/charging-profile`);
        const data = await res.json();

        const baseTime = new Date(data.startSchedule);
        const snapshots = data.chargingProfileSnapshots;

        setProfileInfo(data); // 💡 1. 상단 정보 렌더링
        const { labels, values, backgroundColors, essAnnotations } = buildChartData(snapshots, baseTime);
        const options = buildChartOptions(data, baseTime, snapshots, essAnnotations); // 💡 3. 옵션 분리

        const modalEl = document.getElementById('profileModal');
        const modal = new bootstrap.Modal(modalEl);

        modalEl.addEventListener('shown.bs.modal', () => {
            const ctx = document.getElementById('profileChart').getContext('2d');
            if (profileChart) profileChart.destroy();

            profileChart = new Chart(ctx, {
                type: 'line',
                data: {
                    labels,
                    datasets: [{
                        label: '전력 제한 (W)',
                        data: values,
                        stepped: true,
                        fill: false,
                        borderColor: 'rgba(33, 150, 243, 1)',
                        backgroundColor: backgroundColors,
                        pointRadius: 3,
                        tension: 0
                    }]
                },
                options
            });
        }, { once: true });

        modal.show();
    } catch (err) {
        alert("충전 프로파일을 불러오는 중 오류 발생");
        console.error(err);
    }
}


function setProfileInfo(data) {
    document.getElementById("profile-id").textContent = data.transactionId;
    document.getElementById("profile-energy").textContent = data.energyWh?.toLocaleString();
    document.getElementById("profile-cost").textContent = data.cost?.toLocaleString();
    document.getElementById("profile-start-time").textContent = data.startTime;
    document.getElementById("profile-end-time").textContent = data.endTime;
    document.getElementById("profile-base-time").textContent = data.startSchedule;
}


function buildChartData(snapshots, baseTime) {
    const labels = [], values = [], backgroundColors = [], essAnnotations = {};

    for (let i = 0; i < snapshots.length; i++) {
        const s = snapshots[i];
        const currTime = new Date(baseTime.getTime() + s.startPeriod * 1000);
        const next = snapshots[i + 1];
        const nextTime = next
            ? new Date(baseTime.getTime() + next.startPeriod * 1000)
            : new Date(currTime.getTime() + 15 * 60 * 1000);

        labels.push(currTime.toISOString());
        values.push(s.limit);
        backgroundColors.push(s.useESS ? 'rgba(0, 230, 118, 0.6)' : 'rgba(33, 150, 243, 0.6)');

        if (s.useESS) {
            essAnnotations[`essBox${i}`] = {
                type: 'box',
                xMin: currTime.toISOString(),
                xMax: nextTime.toISOString(),
                backgroundColor: 'rgba(0, 230, 118, 0.12)',
                borderWidth: 0
            };
        }
    }

    return { labels, values, backgroundColors, essAnnotations };
}

function buildChartOptions(data, baseTime, snapshots, essAnnotations) {
    const startTime = new Date(data.startTime).toISOString();
    const endTime = new Date(data.endTime).toISOString();
    const base = baseTime.toISOString();

    return {
        responsive: true,
        plugins: {
            annotation: {
                annotations: {
                    ...essAnnotations,
                    start: {
                        type: 'line',
                        xMin: startTime,
                        xMax: startTime,
                        borderColor: 'green',
                        borderDash: [6, 6],
                        label: {
                            enabled: true,
                            content: '실제 충전 시작',
                            position: 'start'
                        }
                    },
                    end: {
                        type: 'line',
                        xMin: endTime,
                        xMax: endTime,
                        borderColor: 'red',
                        borderDash: [6, 6],
                        label: {
                            enabled: true,
                            content: '실제 충전 종료',
                            position: 'end'
                        }
                    },
                    base: {
                        type: 'line',
                        xMin: base,
                        xMax: base,
                        borderColor: 'blue',
                        borderDash: [4, 4],
                        label: {
                            enabled: true,
                            content: '충전 스케줄',
                            position: 'center'
                        }
                    }
                }
            },
            tooltip: {
                callbacks: {
                    label: ctx => {
                        const essUsed = snapshots[ctx.dataIndex].useESS;
                        return `제한: ${ctx.raw}Wh (${essUsed ? 'ESS 사용' : 'ESS 미사용'})`;
                    }
                }
            },
            legend: { display: false }
        },
        scales: {
            x: {
                type: 'time',
                time: {
                    tooltipFormat: 'HH:mm',
                    displayFormats: {
                        minute: 'HH:mm',
                        hour: 'HH:mm'
                    }
                },
                title: { display: true, text: '시간' }
            },
            y: {
                beginAtZero: true,
                title: { display: true, text: '전력 제한 (Wh)' }
            }
        }
    };
}










let monthlyChart = null;

async function fetchMonthlyChartData() {
    const firstStationId = window.chargingStations[0].stationId;
    const secondStationId = window.chargingStations[1].stationId;
    const metric = document.getElementById("stat-metric").value;

    const url1 = `/api/stations/${firstStationId}/monthly-revenue`;
    const url2 = `/api/stations/${secondStationId}/monthly-revenue`;

    const [firstRes, secondRes] = await Promise.all([fetch(url1), fetch(url2)]);
    const [firstData, secondData] = await Promise.all([firstRes.json(), secondRes.json()]);


    // 요약 정보 세팅
    document.getElementById("summary-first-title").textContent = firstStationId;
    document.getElementById("summary-first-revenue").textContent = firstData.totalRevenue.toLocaleString();
    const energy1 = firstData.totalEnergy;
    document.getElementById("summary-first-energy").textContent = energy1 === 0 ? "0" : `${(energy1 / 1000).toFixed(1)}`;
    document.getElementById("summary-first-count").textContent = firstData.totalCount.toLocaleString();

    document.getElementById("summary-second-title").textContent = secondStationId;
    document.getElementById("summary-second-revenue").textContent = secondData.totalRevenue.toLocaleString();
    const energy2 = firstData.totalEnergy;
    document.getElementById("summary-second-energy").textContent = energy2 === 0 ? "0" : `${(energy2 / 1000).toFixed(1)}`;
    document.getElementById("summary-second-count").textContent = secondData.totalCount.toLocaleString();

    const labels = firstData.dailyStats.map(d => d.date);
    const firstValues = firstData.dailyStats.map(d => {
        if (metric === "totalEnergy") {
            return +(d[metric] / 1000).toFixed(1);
        }
        return d[metric];
    });
    const secondValues = secondData.dailyStats.map(d => {
        if (metric === "totalEnergy") {
            return +(d[metric] / 1000).toFixed(1);
        }
        return d[metric];
    });


    const ctx = document.getElementById("monthlyChartCanvas").getContext("2d");
    if (monthlyChart) monthlyChart.destroy();

    monthlyChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels,
            datasets: [
                {
                    label: `${firstStationId}`,
                    data: firstValues,
                    borderColor: 'rgba(33, 150, 243, 1)',
                    backgroundColor: 'rgba(33, 150, 243, 0.2)',
                    fill: true,
                    tension: 0.2
                },
                {
                    label: `${secondStationId}`,
                    data: secondValues,
                    borderColor: 'rgba(76, 175, 80, 1)',
                    backgroundColor: 'rgba(76, 175, 80, 0.2)',
                    fill: true,
                    tension: 0.2
                }
            ]
        },
        options: {
            responsive: true,
            interaction: {
                mode: 'index',
                intersect: false
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: ctx => `${ctx.dataset.label}: ${ctx.raw.toLocaleString()}`
                    }
                },
                legend: {
                    display: true
                }
            },
            scales: {
                x: {
                    ticks: {
                        maxTicksLimit: 10
                    }
                },
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: metric === 'totalRevenue' ? '원' :
                            metric === 'totalEnergy' ? 'kWh' : '횟수'
                    }
                }
            }
        }
    });
}





window.addEventListener('DOMContentLoaded', () => {
    fetchTransactionData(0);
    fetchMonthlyChartData();

    // 검색 버튼 클릭 이벤트 등록
    document.querySelector("#search-button")
        .addEventListener("click", () => fetchTransactionData(0));
});
