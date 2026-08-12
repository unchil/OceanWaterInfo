import { showMapLoader, hideMapLoader } from './mapLoader.js';

const {GoogleMapsOverlay, GeoJsonLayer } = deck;


let map;
let overlay;
let zoomLevel = 10
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


    overlay = new GoogleMapsOverlay({layers:[]});
    overlay.setMap(map);


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

// Hex 색상을 [R, G, B, A] 배열로 변환하는 함수
function hexToRgbArray(hex, alpha = 255) {
    const r = parseInt(hex.slice(1, 3), 16);
    const g = parseInt(hex.slice(3, 5), 16);
    const b = parseInt(hex.slice(5, 7), 16);
    return [r, g, b, alpha];
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

window.renderingMap = function( geojsonObject, grade, msgKey){
    console.log(`${getTimestamp()} renderingMap Start`);

    // --- [데이터 검증 및 이동 로직 추가] ---
    let isEmpty = true;

    if (geojsonObject && geojsonObject.features && geojsonObject.features.length > 0) {
        const geometry = geojsonObject.features[0].geometry;
        // MultiPolygon 구조이므로 coordinates 배열의 길이를 확인
        if (geometry && geometry.coordinates && geometry.coordinates.length > 0) {
            isEmpty = false;
        }
    }

    if (isEmpty && msgKey === 'COASTAL_FLOODING') {
        console.log(`${getTimestamp()} GeoJSON coordinates are empty. Flying to default center.`);
        window.alert("선택하신 지역 및 등급에 대한 침수 예상 데이터가 존재하지 않습니다.");
        // 데이터가 없으므로 기존 데이터를 지우고 기본 센터로 이동
        removeFeather();
        smoothFlyTo(center);
        hideMapLoader();
        return; // 이후 렌더링 로직 중단
    }

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


/**
 * GeoJSON 객체로부터 google.maps.LatLngBounds를 추출하는 함수
 */
function getBoundsFromGeoJson(geojson) {
    const bounds = new google.maps.LatLngBounds();

    // 좌표 배열을 재귀적으로 탐색하여 bounds 확장
    const traverse = (coords) => {
        if (Array.isArray(coords) && typeof coords[0] === 'number') {
            // [longitude, latitude] 형태의 좌표 쌍을 찾은 경우
            // GeoJSON은 [lng, lat] 순서이므로 구글 맵 형식에 맞게 전달
            bounds.extend({ lng: coords[0], lat: coords[1] });
        } else if (Array.isArray(coords)) {
            // 더 깊은 배열이 있으면 재귀 호출
            coords.forEach(traverse);
        }
    };

    if (geojson.type === 'FeatureCollection') {
        geojson.features.forEach(f => traverse(f.geometry.coordinates));
    } else if (geojson.type === 'Feature') {
        traverse(geojson.geometry.coordinates);
    } else if (geojson.geometry && geojson.geometry.coordinates) {
        traverse(geojson.geometry.coordinates);
    }

    return bounds;
}

function removeFeather(){
    map.data.forEach((feature) => {
        map.data.remove(feature);
    });
}



// 1. Web Worker 생성
const mapWorker = new Worker("./coastalFloodingMapWoker.js");

// 2. Worker로부터 파싱 완료 결과를 수신하는 리스너
mapWorker.onmessage = function (e) {

    const { status, data, error } = e.data;

    if (status === "success") {
        console.log(`${getTimestamp()} Worker 파싱 완료! 지도 렌더링을 시작합니다.`);
        renderingMap(data.geoJson, data.grade, data.msgKey);
    } else {
        console.log(`${getTimestamp()} Worker 내 파싱 에러:`, error);
    }
};


window.addEventListener("message", async(event) => {

    // 1. 기초 검증
    if (!event.data) {
       console.log(`${getTimestamp()} EventListener Empty Data`);
       return;
    }

    console.log(`${getTimestamp()} EventListener Receive Message!`);

    if( event.data.msgKey === 'COASTAL_FLOODING'){
        // 단일 데이터 전송 (Main 스레드 디코딩)
        showMapLoader("loading...");
        const decoder = new TextDecoder("utf-8");
        const jsonString = decoder.decode(event.data.buffer);
        const geoJsonData = JSON.parse(jsonString);
        renderingMap(geoJsonData, event.data.grade, event.data.msgKey);

    }else if(event.data.msgKey === 'COASTAL_FLOODING_ALL'){
        // 대용량 데이터 전송 (Web Worker로 위임)
        showMapLoader("loading...");
        mapWorker.postMessage({
            grade: event.data.grade,
            buffer: event.data.buffer,
            msgKey: event.data.msgKey
        }, [event.data.buffer]);

        console.log(`${getTimestamp()} Data sent to Worker: ${event.data.msgKey}`);

    }else{

        if (typeof event.data === 'string') {
            try {
                let messageData = JSON.parse(event.data);
                switch (messageData.msgKey) {
                    case 'REMOVE_FEATHER':
                        removeFeather();
                        break;
                    default:
                        console.log(`${getTimestamp()} Unknown msgKey: ${messageData.msgKey}`);
                        break;
                }
            } catch (e) {
                console.error("JSON String 파싱 에러:", e);
                return;
            }
        }

    }


});



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


