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

 //   initMapWithData(geojsonObject, grade);

};

// 1. Web Worker 생성
const mapWorker = new Worker("./coastalFloodingMapWoker.js");

// 2. Worker로부터 파싱 완료 결과를 수신하는 리스너
mapWorker.onmessage = function (e) {

    const { status, data, error } = e.data;

    if (status === "success") {
        console.log(`${getTimestamp()} Worker 파싱 완료! 지도 렌더링을 시작합니다.`);
        // 파싱된 GeoJSON 데이터를 지도에 그리기 (예: Google Maps Data Layer 또는 deck.gl)
        //  data = { geoJson: geoJsonData, grade:grade}
        initMapWithData(data.geoJson, data.grade);
    } else {
        console.log(`${getTimestamp()} Worker 내 파싱 에러:`, error);
    }
};



window.addEventListener("message", async(event) => {

   // let data ;    // 1. 데이터가 문자열(String)인 경우 JSON으로 파싱 시도

   console.log(`${getTimestamp()} EventListener Receive Message!`);


    // 1. 전달된 데이터가 Transferable(ArrayBuffer) 형태인지 확인
    if ( event.data &&  event.data.type === 'TRANSFER_DATA' && event.data.grade &&  event.data.buffer  instanceof ArrayBuffer) {
        try {
        // ArrayBuffer를 Worker로 넘기면서 소유권 이전 (Transferable List 활용)
            // 3번째 인자 [arrayBuffer]를 넘겨 메인 스레드 메모리 복사 없이 전달
            mapWorker.postMessage({ grade: event.data.grade,  buffer: event.data.buffer },   [event.data.buffer]);
            console.log(`${getTimestamp()} Receive Data Send to Worker `);
        } catch (e) {
            console.error("Transferable 데이터 디코딩 실패:", e.message, "Content:", data.buffer);
            return;
        }
    }
    else if (event.data &&  event.data.type === 'COMPRESSED_TRANSFER_DATA') {
        try {
            console.log(`${getTimestamp()} 압축 데이터 수신 완료. 해제 시작...`);

            // 1. DecompressionStream 생성 (gzip)
            const ds = new DecompressionStream('gzip');

            // 2. 데이터를 스트림으로 변환 후 해제
            const response = new Response(event.data.buffer);
            const decompressedStream = response.body.pipeThrough(ds);

            // 3. 텍스트로 읽기
            const resultText = await new Response(decompressedStream).text();

            // 4. JSON 파싱
            const json = JSON.parse(resultText);

            console.log(`${getTimestamp()} 해제 완료. 데이터 처리 시작.`);

            initMapWithData(json, event.data.grade)

        } catch (e) {
            console.error(`${getTimestamp()} 압축 해제 실패:` , e);
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

window.initMapWithData = function( geojsonObject, grade) {
 showMapLoader("loading..."); // 로더 표시
    // --- [데이터 검증 및 이동 로직 추가] ---
    let isEmpty = true;

    if (geojsonObject && geojsonObject.features && geojsonObject.features.length > 0) {
        const geometry = geojsonObject.features[0].geometry;
        // MultiPolygon 구조이므로 coordinates 배열의 길이를 확인
        if (geometry && geometry.coordinates && geometry.coordinates.length > 0) {
            isEmpty = false;
        }
    }

    if (isEmpty) {

        console.log(`${getTimestamp()} GeoJSON coordinates are empty. Flying to default center.`);
        window.alert("선택하신 지역 및 등급에 대한 침수 예상 데이터가 존재하지 않습니다.");
        // 데이터가 없으므로 기존 데이터를 지우고 기본 센터로 이동
        map.data.forEach((feature) => {
            map.data.remove(feature);
        });

        // 전역 변수로 정의된 center ({ lat: 37.385852, lng: 126.934515 }) 사용
        smoothFlyTo(center);

            hideMapLoader(); // 에러 시 로더 숨김
        return; // 이후 렌더링 로직 중단
    }
    // ------------------------------------------

    const strokeColor = getGradeColor(grade);

    // 2. 기존 데이터 레이어 초기화 (필요시)
    map.data.forEach((feature) => {
        map.data.remove(feature);
    });

    map.data.addGeoJson(geojsonObject);

    // 4. 스타일 설정 (전체 레이어에 적용하거나 조건부 적용)
    map.data.setStyle({
        fillColor: strokeColor,   // 면 색상
        fillOpacity: 0.5,         // 투명도
        strokeColor: strokeColor, // 선 색상
        strokeWeight: 2,          // 선 굵기
        clickable: true
    });

    // (선택 사항) 지도를 데이터 경계에 맞게 조정
    const bounds = new google.maps.LatLngBounds();
    map.data.forEach((feature) => {
        processConfiguration(feature.getGeometry(), bounds.extend, bounds);
    });
    map.fitBounds(bounds);


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


