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
            const locPosition = new kakao.maps.LatLng(lat, lng);

            const myMarker = new kakao.maps.Marker({
                position: locPosition,
                map,
                title: '내 위치',
                image: new kakao.maps.MarkerImage(
                    'https://cdn-icons-png.flaticon.com/128/1476/1476737.png',
                    new kakao.maps.Size(36, 36),
                    { offset: new kakao.maps.Point(18, 36) }
                )
            });

            // 지도 중심을 내 위치로 이동
            map.setCenter(locPosition);
        }, () => {
            alert('위치 정보를 가져올 수 없습니다.');
        });
    } else {
        alert('이 브라우저는 위치 정보를 지원하지 않습니다.');
    }

    window.chargingStations.forEach(station => {
        const position = new kakao.maps.LatLng(station.location.lat, station.location.lng);
        const marker = new kakao.maps.Marker({ position, image: markerImage, map });

        kakao.maps.event.addListener(marker, 'click', function () {
            fetch(`/api/evses/${station.stationId}/status-summary`)
                .then(res => res.json())
                .then(status => {
                    document.getElementById('station-name').textContent = station.name;
                    document.getElementById('station-address').textContent = station.location.address;
                    document.getElementById('station-available').textContent = status.available;
                    document.getElementById('station-charging').textContent = status.charging;
                    document.getElementById('station-reserved').textContent = status.reserved;
                    document.getElementById('station-faulted').textContent = status.faulted;
                    document.getElementById('station-detail-modal').classList.add('show');
                });
        });
    });
}

function closeModal() {
    document.getElementById('station-detail-modal').classList.remove('show');
}

window.addEventListener('DOMContentLoaded', () => {
    loadMapWithStations();
});
