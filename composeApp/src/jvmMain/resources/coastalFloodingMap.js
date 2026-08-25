import { showMapLoader, hideMapLoader } from './mapLoader.js';


let map;
let overlay;
let zoomLevel = 6
let center = { lat: 37.385852, lng: 126.934515 };


// [14:30:05.123] 형태의 타임스탬프 생성
function getTimestamp() {
    const now = new Date();
    const h = String(now.getHours()).padStart(2, '0');
    const m = String(now.getMinutes()).padStart(2, '0');
    const s = String(now.getSeconds()).padStart(2, '0');
    const ms = String(now.getMilliseconds()).padStart(3, '0');
    return `[${h}:${m}:${s}.${ms}]`;
}


async function initMap() {

    const { Map } = await google.maps.importLibrary("maps");
    const { AdvancedMarkerElement } = await google.maps.importLibrary("marker");

    map  = new Map(document.getElementById('un7map'), {
      mapId: "",
      center:{ lat: 37.385852, lng: 126.934515 },
       zoom: zoomLevel,
       tilt: 45,
      renderingType: google.maps.RenderingType.VECTOR,

    });

    // Set map options.
    map.setOptions({
      scaleControl: true,
      mapTypeId: google.maps.MapTypeId.SATELLITE,

    });

};





// 등급별 색상 매핑 함수
function getGradeColor(grade) {
    switch (grade) {
        case 'F': return '#FF0000'; // 가득참 (가장 위험) - 빨강
        case 'E': return '#FF4500'; // 매우 높음 - 주황빨강
        case 'D': return '#FF8C00'; // 높음 - 주황
        case 'C': return '#FFD700'; // 보통 - 노랑
        case 'B': return '#9ACD32'; // 낮음 - 연두
        case 'A': return '#008000'; // 매우 낮음 - 초록
        default: return '#808080';  // 알 수 없음 - 회색
    }
}

// Geometry에서 좌표를 추출하여 bounds를 확장하는 보조 함수
function processConfiguration(geometry, callback, thisArg) {
    if (geometry instanceof google.maps.LatLng) {
        callback.call(thisArg, geometry);
    } else if (geometry instanceof google.maps.Data.Point) {
        callback.call(thisArg, geometry.get());
    } else {
        geometry.getArray().forEach((g) => {
            processConfiguration(g, callback, thisArg);
        });
    }
}


window.removeMapFeature = function(){
    map.data.forEach((feature) => {
        map.data.remove(feature);
    });
}

window.emptyData = function(){
  //  window.alert("선택하신 지역 및 등급에 대한 침수 예상 데이터가 존재하지 않습니다.");
    smoothFlyTo(center);
    removeFeather();
}


window.renderingMap = function( geojsonObject, grade, msgKey){
    console.log(`${getTimestamp()} renderingMap Start`);
    // --- [데이터 검증 및 이동 로직 추가] ---

    /*
    let isEmpty = true;

    if (geojsonObject && geojsonObject.features && geojsonObject.features.length > 0) {
        const geometry = geojsonObject.features[0].geometry;
        // MultiPolygon 구조이므로 coordinates 배열의 길이를 확인
        if (geometry && geometry.coordinates && geometry.coordinates.length > 0) {
            isEmpty = false;
        }
    }

    if (isEmpty && msgKey === 'COASTAL_FLOODING') {
        window.alert("선택하신 지역 및 등급에 대한 침수 예상 데이터가 존재하지 않습니다.");
        smoothFlyTo(center);
        removeFeather();
        return; // 이후 렌더링 로직 중단
    }
    */

    showMapLoader("loading...");

    const strokeColor = getGradeColor(grade);

    map.data.addGeoJson(geojsonObject);

    map.data.setStyle({
        fillColor: strokeColor,   // 면 색상
        fillOpacity: 0.5,         // 투명도
        strokeColor: strokeColor, // 선 색상
        strokeWeight: 2,          // 선 굵기
        clickable: true
    });
    //  지도를 데이터 경계에 맞게 조정
    const bounds = new google.maps.LatLngBounds();
    map.data.forEach((feature) => {
        processConfiguration(feature.getGeometry(), bounds.extend, bounds);
    });
    map.fitBounds(bounds);

    console.log(`${getTimestamp()} renderingMap End`);

    // 최종 렌더링 종료 후 로더 숨김
    setTimeout(() => {
       hideMapLoader();
    }, 300); // 부드러운 전환을 위해 약간의 지연
}




function smoothZoom ( targetZoom, currentZoom) {
    if (currentZoom === targetZoom) return;
    // 줌을 확대할지 축소할지 결정
    let nextZoom = currentZoom < targetZoom ? currentZoom + 2 : currentZoom - 2;
    // 맵의 줌 설정 (소수점 단위 지원 여부는 버전 및 맵 유형에 따라 다름)
    map.setZoom(nextZoom);
    // 재귀적으로 호출하여 애니메이션 효과 생성
    setTimeout(function() {
        smoothZoom(map, targetZoom, nextZoom);
    }, 300);
}

// map 에서 현재의 위치를 파악하고 타켓위치까지의 직선걸이가 길면 줌 레벨도 비율이 낮아 지도록
window.smoothFlyTo = function(target) {
    if (!map) {
        return;
    }
    smoothZoom(10, map.getZoom())

    setTimeout(() => {
        map.panTo(target)
        setTimeout(() => {
             smoothZoom(16, map.getZoom())
        }, 500);
    }, 500)
};

window.addMarkerClusterer =  function(locations, labels, contents) {
    const markers = locations.map((position, i) => {
        const marker = new google.maps.Marker({
          position,
          map: map
        });
        return marker;
    });
    new markerClusterer.MarkerClusterer({ map, markers });
};


initMap();


