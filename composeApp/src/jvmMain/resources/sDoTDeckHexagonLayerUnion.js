//import {valuesHexagon } from './valuesHexagonLayer.js';

const {GoogleMapsOverlay, HexagonLayer } = deck;
const { load, JSONLoader } = loaders;
let cachedData = null; // 서버에서 받은 데이터를 저장할 변수
const DATA_URL = 'http://192.168.35.107:7788/sdot_env_info';

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

let currentType = 'o3';
let currentValue;


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
      mapTypeId: google.maps.MapTypeId.SATELLITE,
    });

    overlay = new GoogleMapsOverlay({layers:[]});
    overlay.setMap(map);

 // [줌 변경] 데이터 요청 없이 레이어 설정만 업데이트
    map.addListener('zoom_changed', () => {
       // renderLayer();
        initMapWithData(currentValue, currentType);
    });

/*
    google.maps.event.addListenerOnce(map, 'idle', () => {
        initMapWithData(valuesHexagon, "o3");
    });
    */

};


window.initMapWithData =  function( values, type) {
    currentValue = values
    currentType = type
    let title;
    let maxDomain;

    // 1. 타입에 따른 타이틀 및 도메인 범위 설정
    switch(type) {
        case 'o3':
            title = "Ozone(O3)";
            maxDomain = 0.15;
            break;
        case 'no2':
            title = "Nitrogen dioxide(NO2)";
            maxDomain = 0.2;
            break;
        case 'co':
            title = "Carbon monoxide(CO)";
            maxDomain = 50;
            break;
        case 'so2':
            title = "Sulfur dioxide(SO2)";
            maxDomain = 0.15;
            break;
        case 'nh3':
            title = "Ammonia(NH3)";
            maxDomain = 0.15;
            break;
        case 'h2s':
            title = "Hydrogen sulfide(H2S)";
            maxDomain = 0.3;
            break;
        case 'pm10':
            title = "Particulate Matter(PM10)";
            maxDomain = 81;
            break;
        case 'pm25':
            title = "Particulate Matter(PM2.5)";
            maxDomain = 36;
            break;
        default:
            title = "Ozone(O3)";
            type = 'o3';
            maxDomain = 0.15;
    }
    let id =  'hexagon-layer-' + type + '_' + Date.now();
    let dataUrl = DATA_URL + "?t=" + new Date().getTime();
    console.log(`[자동 갱신] ${id} - ${dataUrl} 데이터를 새로 고침합니다.`);

    /*
    공식 설명 (Math.pow 활용)
    •줌 레벨 12: 90 * Math.pow(2, 12 - 12) = 90 * 1 = 90m
    •줌 레벨 14 (확대): 90 * Math.pow(2, 12 - 14) = 90 * 0.25 = 22.5m (육각형이 너무 커지는 것을 방지)
    •줌 레벨 10 (축소): 90 * Math.pow(2, 12 - 10) = 90 * 4 = 360m (육각형이 너무 작아져서 안 보이는 것을 방지)
    */
    const currentZoom = map.getZoom();
    const dynamicRadius =   currentZoom >= zoomLevel ? 90 : (90 * Math.pow(2, zoomLevel - currentZoom))
    const dynamicMaxElevation =   currentZoom >= zoomLevel ? 20 : (100 * Math.pow(2, zoomLevel - currentZoom))
//  const dynamicRadius = 90 * Math.pow(2, 12 - currentZoom);
//  const dynamicMaxElevation = 100 * Math.pow(2, 12 - currentZoom);

    let props = {
        id: id,
        data: values,

        gpuAggregation: true,
        extruded: true,
        getPosition: d => [d.lng, d.lat],

        colorRange:colorRange,
        colorDomain: [0, maxDomain],
        getColorWeight: d => Math.min(d.value, maxDomain),
        colorAggregation: 'MAX',
        // <selection> [수정] 고정값 100 대신 계산된 dynamicMaxElevation 적용 </selection>
        elevationRange: [0, dynamicMaxElevation],
        elevationDomain: [0, maxDomain],
        getElevationWeight: d => Math.min(d.value, maxDomain),
        elevationAggregation: 'MAX',
        // [수정] 고정값 90 대신 계산된 dynamicRadius 적용
        radius: dynamicRadius,
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
                  <div>SerialID: ${source.obs}</div>
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


