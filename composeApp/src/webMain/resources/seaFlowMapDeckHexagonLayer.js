import { showMapLoader, hideMapLoader } from './mapLoader.js';

const {GoogleMapsOverlay, HexagonLayer} = deck;

let map;
let overlay;
let zoomLevel = 8
let currentTime = 0; // 애니메이션 진행 상태를 추적할 변수//
let center = { lat: 37.385852, lng: 126.934515 };
let animationId; // 애니메이션 루프 ID를 저장할 변수 추가
let deckData = []; // 데이터를 전역으로 관리하여 루프에서 참조
let props;
let colorRange = [
  [1, 152, 189],
  [73, 227, 206],
  [216, 254, 181],
  [254, 237, 177],
  [254, 173, 84],
  [209, 55, 78]
];

let elevationBase = 10; // 기본 높이 배율
const animatedOpacity = 0.5 + ((Math.sin(currentTime) + 1) *0.25); // 0.5 ~ 1.0 사이 왕복


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


    google.maps.event.addListenerOnce(map, 'idle', function() {
        overlay = new GoogleMapsOverlay({layers:[]});
       overlay.setMap(map);
    });



};


window.initMapWithData = function( values) {
    showMapLoader("loading..."); // 로더 표시

    props = {
      id: 'hexagon-layer',
      data: values,
      gpuAggregation: true,
      colorRange,
      extruded: true,
      getPosition: d => [d.lng, d.lat],
      getColorWeight: d => d.speed,
      getElevationWeight: d => d.speed,
      elevationScale: elevationBase,
      radius: 4500,
      pickable: true

  }


   // 최종 렌더링 종료 후 로더 숨김
   setTimeout(() => {
       hideMapLoader();
   }, 300); // 부드러운 전환을 위해 약간의 지연

  startHexagonAnimation();

}

/**
 * Hexagon의 높이를 실시간으로 변화시키는 애니메이션 루프
 */
function startHexagonAnimation() {
    if (animationId) {
        cancelAnimationFrame(animationId);
    }

    function animate() {
        // currentTime을 이용하여 사인(Sine) 곡선 생성 (0.02는 속도 조절)
        currentTime += 0.02;

        // [핵심 수정] 매 프레임마다 높이와 투명도를 새로 계산합니다.
        const animatedScale =  ((Math.sin(currentTime) + 1) * 30);
        const currentOpacity = ((Math.sin(currentTime) + 1) * 0.5);

        const hexagonLayer = new HexagonLayer({
            ...props,
            elevationScale: animatedScale,
            opacity: animatedOpacity
        });

        if (overlay) {
            overlay.setProps({
                layers: [hexagonLayer],
            });
        }

        animationId = requestAnimationFrame(animate);
    }

    animationId = requestAnimationFrame(animate);
}


function smoothZoom ( targetZoom, currentZoom) {
    if (currentZoom === targetZoom) return;
    // 줌을 확대할지 축소할지 결정


    let nextZoom = currentZoom < targetZoom
        ? Math.min(currentZoom + 2, targetZoom)
        : Math.max(currentZoom - 2, targetZoom);

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


window.addEventListener("message", (event) => {

    let data = event.data;    // 1. 데이터가 문자열(String)인 경우 JSON으로 파싱 시도
    if (typeof data === 'string') {
        try {
            data = JSON.parse(data);
        } catch (e) {
            console.error("메시지 데이터 파싱 중 오류 발생:", e);
            return; // 파싱 실패 시 함수 종료
        }
    }

    if(data.action == 'INIT_DATA'){
        initMapWithData(data.values)
    }



});


initMap();


