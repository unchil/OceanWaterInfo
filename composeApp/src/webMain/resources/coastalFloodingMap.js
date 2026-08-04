//import {geojsonObject } from './geom.js';

const {GoogleMapsOverlay, GeoJsonLayer } = deck;


let map;
let overlay;
let zoomLevel = 10
let center = { lat: 37.385852, lng: 126.934515 };


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


 //   initMapWithData(geojsonObject, grade);

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

window.initMapWithData = function( geojsonObject, grade) {
    const strokeColor = getGradeColor(grade);
    // 2. 기존 데이터 레이어 초기화
    map.data.forEach((feature) => {
        map.data.remove(feature);
    });
    map.data.addGeoJson(geojsonObject);
    // 4. 스타일 설정
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

function initDeckWithData(data){

    const layerId = `geojson-layer-${data.grade}`;
    const geojsonObject = data.geoJson

    // 1. grade에 따른 Hex 색상 가져오기
    const hexColor = getGradeColor(data.grade);

    // 2. deck.gl 형식([R, G, B, A])으로 변환
    // 채우기 색상 (투명도 150 적용)
    const fillColor = hexToRgbArray(hexColor, 200);
    // 선 색상 (불투명 255 적용)
    const strokeColor = hexToRgbArray(hexColor, 255);

    // 2. GeoJsonLayer 생성
    const geoJsonLayer = new GeoJsonLayer({
      id: layerId,
      data: geojsonObject, // GeoJSON 객체 또는 URL

      // 스타일 설정
      pickable: true,            // 마우스 호버/클릭 이벤트 활성화
      stroked: true,             // 외곽선 그리기 여부
      filled: true,              // 내부 채우기 여부
      extruded: true,            // 3D 입체 높이 표현 여부
      wireframe: false,          // 3D 와이어프레임 표시 여부
      // 색상 지정 (RGBA 배열: 0~255)
      getFillColor:fillColor,     // 채우기 색상 (주황색, 투명도)
      getLineColor: strokeColor,   // 외곽선 색상 (흰색)
      getLineWidth: 2,                     // 외곽선 두께 (미터 또는 픽셀 단위)
      lineWidthMinPixels: 1,               // 축소 시 최저 픽셀 두께
      getElevation: f => f.properties.height || 10, // 3D 높이 값 지정
      // 이벤트 핸들러
      onHover: info => {
        if (info.object) {
       //   console.log('Hovered Feature:', info.object.properties);
        }
      },
      onClick: info => {
        if (info.object) {
    //      alert(`클릭된 영역: ${info.object.properties.name || '이름 없음'}`);
        }
      }
    });

    if (overlay) {

        overlay.setProps({
          layers: [geoJsonLayer],
        });

        if (geojsonObject) {
            const bounds = getBoundsFromGeoJson(geojsonObject);

            // bounds가 유효한 경우(데이터가 있는 경우)에만 실행
            if (!bounds.isEmpty()) {
              // padding을 주어 가장자리 여유를 둠
              map.fitBounds(bounds, 50);
              // 필요 시 최대 줌 레벨 제한 (너무 확대되는 것 방지)
              const listener = google.maps.event.addListener(map, 'idle', function() {
               //   if (map.getZoom() > 18) map.setZoom(18);
                  google.maps.event.removeListener(listener);
              });
            }
        }

    }


}

// 지도에 데이터를 렌더링하는 함수 예시
function renderOnMap(data) {
    // Google Maps Data Layer 사용 시
    initMapWithData(data.geoJson, data.grade);

    // deck.gl WebGL Layer 사용 시
    //initDeckWithData(data)

}


// 1. Web Worker 생성
const mapWorker = new Worker("./coastalFloodingMapWoker.js");

// 2. Worker로부터 파싱 완료 결과를 수신하는 리스너
mapWorker.onmessage = function (e) {

    const { status, data, error } = e.data;

    if (status === "success") {
        console.log("Worker 파싱 완료! 지도 렌더링을 시작합니다.");
        // 파싱된 GeoJSON 데이터를 지도에 그리기 (예: Google Maps Data Layer 또는 deck.gl)
        //  data = { geoJson: geoJsonData, grade:grade}
        renderOnMap(data);
    } else {
        console.error("Worker 내 파싱 에러:", error);
    }
};


window.addEventListener("message", (event) => {

   // let data ;    // 1. 데이터가 문자열(String)인 경우 JSON으로 파싱 시도

    // 1. 전달된 데이터가 Transferable(ArrayBuffer) 형태인지 확인
    if ( event.data &&  event.data.type === 'TRANSFER_DATA' && event.data.grade &&  event.data.buffer  instanceof ArrayBuffer) {
        try {
        // ArrayBuffer를 Worker로 넘기면서 소유권 이전 (Transferable List 활용)
            // 3번째 인자 [arrayBuffer]를 넘겨 메인 스레드 메모리 복사 없이 전달
            mapWorker.postMessage({ grade: event.data.grade,  buffer: event.data.buffer },   [event.data.buffer]);
            console.log("Worker로 ArrayBuffer 이관 완료 (메인 스레드 블로킹 없음)");
        } catch (e) {
            console.error("Transferable 데이터 디코딩 실패:", e.message, "Content:", data.buffer);
            return;
        }
    }
    else if (typeof event.data === 'string') {
        try {
            const data = JSON.parse(event.data);

            if(data.action == 'CHANGE_DATA'){
                initMapWithData(data.values, data.type)
            }

            if (data.action === "FLY_TO") {
                console.log("FLY_TO:", data.target.lat, data.target.lng);
                smoothFlyTo({lat:data.target.lat, lng:data.target.lng})
            }

        } catch (e) {
            console.error("메시지 데이터 파싱 중 오류 발생:", e);
            return; // 파싱 실패 시 함수 종료
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


