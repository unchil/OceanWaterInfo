import {valuesHexagon } from './output2.js';

const {GoogleMapsOverlay, HexagonLayer} = deck;

let map;
let overlay;
let zoomLevel = 12
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

let elevationBase = 0; // 기본 높이 배율
const animatedOpacity = 1.0 ;


async function initMap() {

    const { Map } = await google.maps.importLibrary("maps");
    const { AdvancedMarkerElement } = await google.maps.importLibrary("marker");

    map  = new Map(document.getElementById('un7map'), {
      mapId: "",
      center:{ lat: 37.55267, lng: 126.98136 },
       zoom: zoomLevel,
       tilt: 45,
       renderingType: google.maps.RenderingType.VECTOR,
       colorScheme: google.maps.ColorScheme.DARK

    });

    // Set map options.
    map.setOptions({
      scaleControl: true,

     mapTypeId: google.maps.MapTypeId.Terrain,

    });

    overlay = new GoogleMapsOverlay({layers:[]});
    overlay.setMap(map);

    initMapWithData(valuesHexagon)

};


window.initMapWithData = function( values) {

    props = {
      id: 'hexagon-layer',
      data: values,
      elevationRange: [0, 100],
      gpuAggregation: true,
      colorRange,
      extruded: true,
      getPosition: d => [d.lng, d.lat],
      getColorWeight: d => d.value,
      getElevationWeight: d => d.value,
      elevationScale: elevationBase,
      radius: 90,
      pickable: true,

       // --- 툴팁 로직 추가 ---
      onHover: info => {
          const tooltip = document.getElementById('tooltip');
          if (info.object) {
              // info.object.points에는 해당 격자에 포함된 원본 데이터 리스트가 들어있음
              const count = info.object.points.length;
              const source = info.object.points[0].source;

              // 해당 격자에 포함된 데이터들의 value 합계 계산
              const totalValue = info.object.points.reduce((sum, p) => sum + p.source.value, 0);
              const avgValue = (totalValue / count).toFixed(2);

              tooltip.innerHTML = `
                  <div style="font-weight:bold; margin-bottom:5px;">${source.addr}</div>
                  <div>SerialID: ${source.serial}</div>
                  <div>측정 값: ${source.value}</div>
                  <div>측정 지점 수: ${count}개</div>
                  <div>평균 수치: ${avgValue}</div>
              `;
              tooltip.style.display = 'block';
              tooltip.style.left = `${info.x + 15}px`;
              tooltip.style.top = `${info.y + 15}px`;
          } else {
              tooltip.style.display = 'none';
          }
       },



  }


      // [개선] 지도가 완전히 로드된 후 데이터를 주입합니다.
      google.maps.event.addListenerOnce(map, 'idle', () => {
        startHexagonAnimation();

      });
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
        currentTime += 0.06;

        // [핵심 수정] 매 프레임마다 높이와 투명도를 새로 계산합니다.
        const animatedScale =  currentTime  * 1.5;
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


