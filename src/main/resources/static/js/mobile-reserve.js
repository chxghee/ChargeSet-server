let currentPosition = null;

document.addEventListener('DOMContentLoaded', () => {

    navigator.geolocation.getCurrentPosition(position => {
        currentPosition = {
            lat: position.coords.latitude,
            lng: position.coords.longitude
        };
    }, () => {
        alert("위치 정보를 가져올 수 없습니다.");
    });

    const form = document.getElementById('reserve-form');
    form.addEventListener('submit', handleReservationSubmit);
    loadMyUpcomingReservation()
});


function handleReservationSubmit(e) {
    e.preventDefault();

    const startDate = document.getElementById('startDate').value;
    const startTime = document.getElementById('startTime').value;
    const chargingMinute = parseInt(document.getElementById('chargingMinute').value);
    const targetEnergyWh = parseInt(document.getElementById('targetEnergy').value);

    // 일단 유저 토큰 하드코딩으로 넣어놓음 -> 추후 로그인이나 그런걸로 대체예정
    const idToken = "token-6789";

    if (!startDate || !startTime || !chargingMinute || !targetEnergyWh) {
        alert("모든 항목을 입력해 주세요.");
        return;
    }

    const requestBody = {
        startDate,
        startTime,
        chargingMinute,
        targetEnergyWh
    };

    Promise.allSettled(
        stationIds.map(stationId =>
            fetch(`/api/members/${idToken}/reservations/${stationId}/recommend`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(requestBody)
            }).then(res => {
                if (!res.ok) throw new Error(`${stationId} 예약 실패`);
                return res.json().then(data => ({ stationId, data }));
            })
        )
    ).then(results => {
        const contentBox = document.getElementById('reservation-result-content');
        const modal = document.getElementById('available-reservation-modal');

        const success = results.filter(r => r.status === 'fulfilled').map(r => r.value);
        const failed = results.filter(r => r.status === 'rejected');

        if (success.length === 0) {
            contentBox.innerHTML = "<div class='text-center'>❌ 예약 가능한 충전소가 없습니다.</div>";
            openReservationModal();
            return;
        }



        let html = success.map((r) => {
            const reservation = r.data;
            const json = encodeURIComponent(JSON.stringify(reservation));
            const station = stationMap[r.stationId];
            const stationName = station.name || r.stationId;
            const energyKWh = (reservation.targetEnergyWh / 1000).toFixed(1);

            var distance;
            if (r.stationId == "ST-001") {
                distance = 0.56;
            } else {
                distance = 8.24;
            }

            let distanceText = "거리 계산 불가";
            if (currentPosition) {
                distance = computeDistanceKm(
                    currentPosition.lat, currentPosition.lng,
                    station.lat, station.lng
                );
                distanceText = `${distance.toFixed(2)} km`;
            } else {
                distanceText = `${distance.toFixed(2)} km`;
            }

            return `
                    <div class="mb-3 border-bottom pb-3">
                        <strong>📍 ${stationName}</strong><br>
                        예상 요금: <span class="text-primary fw-bold">${reservation.cost.toLocaleString()}원</span><br>
                        충전량: ${energyKWh}kWh<br>
                        시작: ${convertUTCToKST(reservation.startTime)}<br>
                        종료: ${convertUTCToKST(reservation.endTime)}<br>
                        거리: ${distanceText}<br>
                        <button class="btn btn-sm btn-success mt-2" onclick="confirmReservation('${json}')">예약하기</button>
                    </div>
                `;
        }).join('');

        if (failed.length == 2) {
            html += `<div class="text-danger mt-3">⚠️ 해당 시간에 예약 가능한 충전소가 없습니다</div>`;
        }

        contentBox.innerHTML = html;
        openReservationModal();
    }).catch(err => {
        alert("오류 발생: " + err.message);
        console.error(err);
    });
}

function convertUTCToKST(utcString) {
    const date = new Date(utcString);
    const pad = n => n.toString().padStart(2, '0');

    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ` +
        `${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function openReservationModal() {
    document.getElementById('available-reservation-modal').classList.add('show');
}

function closeReservationModal() {
    document.getElementById('available-reservation-modal').classList.remove('show');
}

function confirmReservation(encodedJson) {
    const reservation = JSON.parse(decodeURIComponent(encodedJson));

    fetch("/api/members/reservations/confirm", {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(reservation)
    })
        .then(res => {
            if (!res.ok) throw new Error("예약 실패");
            alert("✅ 예약이 완료되었습니다!");
            closeReservationModal();
            loadMyUpcomingReservation(); // 다시 불러오기
        })
        .catch(err => {
            alert("❌ 예약 중 오류가 발생했습니다.");
            console.error(err);
        });
}


function loadMyUpcomingReservation() {
    const userId = "682d6f8a3792904b5c987afe"; // 실제 로그인된 사용자 ID 또는 토큰 기반 추출

    fetch(`/api/members/${userId}/reservations`)
        .then(res => {
            if (!res.ok) {
                return res.json().then(err => {
                    // 서버가 JSON 형식의 에러 메시지를 응답했다고 가정
                    throw new Error(err.message || "예약 정보를 불러오지 못했습니다.");
                });
            }
            return res.json();
        })
        .then(reservation => {
            const container = document.getElementById("my-reservation-content");

            const start = convertUTCToKST(reservation.startTime);
            const end = convertUTCToKST(reservation.endTime);

            container.innerHTML = `
                        <div class="card p-3 mb-3 shadow-sm border rounded d-flex flex-row align-items-center justify-content-between">
                            <div>
                                <div><strong>📍 충전소:</strong> ${stationMap[reservation.stationId].name}</div>
                                <div><strong>시작:</strong> ${start}</div>
                                <div><strong>종료:</strong> ${end}</div>
                                <div><strong>충전량:</strong> ${(reservation.targetEnergyWh / 1000).toFixed(1)} kWh</div>
                            </div>
                            <div>
                                <button class="btn btn-outline-danger btn-sm" onclick="cancelReservation('${reservation.id}')">
                                    취소
                                </button>
                            </div>
                        </div>
                    `;

        })
        .catch(err => {
            document.getElementById("my-reservation-content").textContent = "예정된 예약이 없습니다.";
            console.error(err);
        });
}

function cancelReservation(reservationId) {
    if (!confirm("정말 예약을 취소하시겠습니까?")) return;

    fetch(`/api/reservations/${reservationId}/cancel`, {
        method: 'POST'
    })
        .then(res => {
            if (!res.ok) throw new Error("예약 취소에 실패했습니다.");
            alert("예약이 취소되었습니다.");
            loadMyUpcomingReservation(); // 다시 불러오기
        })
        .catch(err => {
            alert("오류 발생: " + err.message);
            console.error(err);
        });
}

function computeDistanceKm(lat1, lng1, lat2, lng2) {
    const R = 6371; // 지구 반지름 km
    const toRad = deg => deg * Math.PI / 180;

    const dLat = toRad(lat2 - lat1);
    const dLng = toRad(lng2 - lng1);
    const a = Math.sin(dLat / 2) ** 2 +
        Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
        Math.sin(dLng / 2) ** 2;
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}
