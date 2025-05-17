let currentPosition = null;

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

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(position => {
            const lat = position.coords.latitude;
            const lng = position.coords.longitude;
            currentPosition = new kakao.maps.LatLng(lat, lng);

            new kakao.maps.Marker({
                position: currentPosition,
                map,
                image: new kakao.maps.MarkerImage(
                    'https://cdn-icons-png.flaticon.com/128/1476/1476737.png',
                    new kakao.maps.Size(36, 36),
                    { offset: new kakao.maps.Point(18, 36) }
                )
            });

            map.setCenter(currentPosition);
        });
    }

    window.chargingStations.forEach(station => {
        const position = new kakao.maps.LatLng(station.location.lat, station.location.lng);
        const marker = new kakao.maps.Marker({ position, image: markerImage, map });

        kakao.maps.event.addListener(marker, 'click', () => {
            fetch(`/api/evses/${station.stationId}/status-summary`)
                .then(res => res.json())
                .then(status => {
                    const stationPos = new kakao.maps.LatLng(station.location.lat, station.location.lng);
                    let distanceText = '위치 정보 없음';

                    if (currentPosition && kakao.maps.geometry?.spherical) {
                        const distance = kakao.maps.geometry.spherical.computeDistanceBetween(currentPosition, stationPos);
                        distanceText = `${(distance / 1000).toFixed(2)} km`;
                    }
                    if (kakao.maps.geometry == null) {
                        const myLat = currentPosition.getLat();
                        const myLng = currentPosition.getLng();
                        const stationLat = station.location.lat;
                        const stationLng = station.location.lng;

                        const distance = computeDistance(myLat, myLng, stationLat, stationLng);
                        distanceText = `${distance.toFixed(2)} km`;
                    }

                    document.getElementById('station-name').textContent = station.name;
                    document.getElementById('station-address').textContent = station.location.address;
                    document.getElementById('station-available').textContent = status.available;
                    document.getElementById('station-charging').textContent = status.charging;
                    document.getElementById('station-distance').textContent = distanceText;

                    document.getElementById('station-detail-modal').classList.add('show');
                });
        });
    });
}

function computeDistance(lat1, lon1, lat2, lon2) {
    const R = 6371; // 지구 반지름 (km)
    const toRad = angle => angle * (Math.PI / 180);

    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);

    const a = Math.sin(dLat / 2) ** 2 +
        Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
        Math.sin(dLon / 2) ** 2;

    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c; // 단위: km
}

// ✅ 딱 한 번만 호출
window.addEventListener('load', () => {
    if (window.kakao && kakao.maps && kakao.maps.load) {
        kakao.maps.load(loadMapWithStations);
    } else {
        console.error('카카오맵 SDK가 로드되지 않았습니다.');
    }
});

function closeModal() {
    document.getElementById('station-detail-modal').classList.remove('show');
}
