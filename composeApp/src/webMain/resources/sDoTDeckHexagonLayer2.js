//import {valuesHexagon } from './valuesHexagonLayer.js';

const {GoogleMapsOverlay, HexagonLayer } = deck;
const { load, JSONLoader } = loaders;
const DATA_URL = 'http://192.168.35.107:7788/seoul/sdot_env_info';

let map;
let overlay;
let zoomLevel = 12

let center = { lat: 37.385852, lng: 126.934515 };
let animationId; // 애니메이션 루프 ID를 저장할 변수 추가
let deckData = []; // 데이터를 전역으로 관리하여 루프에서 참조

let title;
let colorRange = [[255, 255, 255],[0, 200, 255],[0, 255, 100],[0, 255, 100],[255, 116, 0], [255, 0, 0]];


let elevationBase = 0; // 기본 높이 배율
const animatedOpacity = 1.0 ;

//페이지 새로고침 없이 함수만 호출하고 싶을 때
window.addEventListener("message", (event) => {
    if (event.data.action === 'CHANGE_TYPE') {
        const type = event.data.type;
        initMapWithType(type);
        // 위에서 만든 loadDataByUrlParam과 유사한 로직 실행
        // (단, 여기서는 파라미터 대신 event.data.type 사용)
    }
});



async function initMap() {

    const { Map } = await google.maps.importLibrary("maps");
    const { AdvancedMarkerElement } = await google.maps.importLibrary("marker");

    map  = new Map(document.getElementById('un7map'), {
      mapId: "",
      center:{ lat: 37.55267, lng: 126.98136 },
       zoom: zoomLevel,
       tilt: 45,
       renderingType: google.maps.RenderingType.VECTOR,
    //   colorScheme: google.maps.ColorScheme.DARK
    });

    // Set map options.
    map.setOptions({
      scaleControl: true,
      mapTypeId: google.maps.MapTypeId.Terrain,
    });

    overlay = new GoogleMapsOverlay({layers:[]});
    overlay.setMap(map);

    google.maps.event.addListenerOnce(map, 'idle', () => {
        initMapWithType('max_o3');
    });

};

window.initMapWithType =  function( type) {
    let title;
    let maxDomain;

    // 1. 타입에 따른 타이틀 및 도메인 범위 설정
    switch(type) {
        case 'max_o3':
            title = "Ozone(O3)";
            maxDomain = 0.15;
            break;
        case 'max_no2':
            title = "Nitrogen dioxide(NO2)";
            maxDomain = 0.2;
            break;
        case 'max_co':
            title = "Carbon monoxide(CO)";
            maxDomain = 50;
            break;
        case 'max_so2':
            title = "Sulfur dioxide(SO2)";
            maxDomain = 0.15;
            break;
        case 'max_nh3':
            title = "Ammonia(NH3)";
            maxDomain = 0.15;
            break;
        case 'max_h2s':
            title = "Hydrogen sulfide(H2S)";
            maxDomain = 0.3;
            break;
        default:
            title = "Ozone(O3)";
            type = 'max_o3';
            maxDomain = 0.15;
    }

    let props = {
      id: 'hexagon-layer-' + type,
      data: DATA_URL,
         loaders: [JSONLoader],
           //  [중요] 모든 데이터(...d)를 유지해야 툴팁에서 addr, serial을 쓸 수 있음
         dataTransform: (rawData) => {
            // SDoT API 특유의 계층 구조 대응
         const rows = rawData.sDoTEnv ? rawData.sDoTEnv.row : (Array.isArray(rawData) ? rawData : []);
         return rows.map(d => ({
           ...d,
              lng: Number(d.lng),
              lat: Number(d.lat),
         // d[type]을 통해 'max_o3', 'max_no2' 등의 필드값에 동적 접근
                value: Number(d[type] || 0)
         }));

     },

      gpuAggregation: true,
      extruded: true,
      getPosition: d => [d.lng, d.lat],

        colorRange:colorRange,
        colorDomain: [0, maxDomain],
        getColorWeight: d => Math.min(d.value, maxDomain),
        colorAggregation: 'MAX',

        elevationRange: [0, 100],
        elevationDomain: [0, maxDomain],
        getElevationWeight: d => Math.min(d.value, maxDomain),
        elevationAggregation: 'MAX',
/*
updateTriggers: {
    getColorWeight: [type],
    getElevationWeight: [type]
},
*/


      radius: 90,
      pickable: true,
      onHover: info => {
          const tooltip = document.getElementById('tooltip');
          if (info.object) {
              const count = info.object.points.length;
              const source = info.object.points[0].source;
              const maxValue = Math.max(...info.object.points.map(p => p.source.value));

              tooltip.innerHTML = `
                  <div style="font-weight:bold; margin-bottom:5px;">${source.addr}</div>
                  <div>${source.sensing_time}</div>
                  <div>SerialID: ${source.serial}</div>
                  <div>${title}:${source.value}</div>
                  <div>측정 지점 수: ${count}개</div>
                  <div>최대값: ${maxValue}</div>
              `;
              tooltip.style.display = 'block';
              tooltip.style.left = `${info.x + 15}px`;
              tooltip.style.top = `${info.y + 15}px`;
          } else {
              tooltip.style.display = 'none';
          }
       },
  }
    startHexagonAnimation(props);
}


function startHexagonAnimation(props) {
    let currentTime = 0

    if (animationId) {
        cancelAnimationFrame(animationId);
    }

    function animate() {
        // currentTime을 이용하여 사인(Sine) 곡선 생성 (0.02는 속도 조절)
        currentTime += 0.06;

        // [핵심 수정] 매 프레임마다 높이와 투명도를 새로 계산합니다.
        const animatedScale =  currentTime  * 3;
        const currentOpacity = currentTime * 0.1;

        if (currentTime >= 10) {
            // 정점에서 멈출 때 마지막으로 레이어를 깨끗하게(정확히 20으로) 업데이트
            const finalLayer = new HexagonLayer({
                ...props,
                elevationScale: animatedScale, // 최고점 값 고정
                opacity: animatedOpacity      // 최고점 투명도 고정
            });

            if (overlay) {
                overlay.setProps({
                    layers: [finalLayer],
                });
            }

            // 애니메이션 루프 중단
            cancelAnimationFrame(animationId);
            console.log("최고점에 도달하여 애니메이션을 중단합니다.");
            return;
        }


        const hexagonLayer = new HexagonLayer({
            ...props,
            elevationScale: animatedScale,
            opacity: currentOpacity
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
        smoothZoom( targetZoom, nextZoom);
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


