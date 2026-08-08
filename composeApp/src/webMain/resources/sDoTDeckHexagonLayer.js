const {GoogleMapsOverlay, HexagonLayer } = deck;

let center = { lat: 37.55267, lng: 126.98136 };
let map;
let overlay;
let zoomLevel = 12
let animationId; // 애니메이션 루프 ID를 저장할 변수 추가
let deckData = []; // 데이터를 전역으로 관리하여 루프에서 참조
let title;
let elevationBase = 0; // 기본 높이 배율
const animatedOpacity = 1.0 ;
let currentType = 'o3';


// --- 로딩 스피너 설정 ---
const style = document.createElement('style');
style.innerHTML = `
  #map-loader-container {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    z-index: 2000; /* 지도보다 위에 표시 */
    display: none;
    text-align: center;
    background: rgba(255, 255, 255, 0.8);
    padding: 20px;
    border-radius: 10px;
    box-shadow: 0 2px 10px rgba(0,0,0,0.2);
  }
  .map-spinner {
    border: 6px solid #f3f3f3;
    border-top: 6px solid #3498db;
    border-radius: 50%;
    width: 50px;
    height: 50px;
    animation: map-spin 1s linear infinite;
    margin-bottom: 10px;
  }
  @keyframes map-spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
  .loader-text {
    font-family: Arial, sans-serif;
    font-size: 14px;
    font-weight: bold;
    color: #333;
  }
`;
document.head.appendChild(style);

// 로더 DOM 생성
const loaderDiv = document.createElement('div');
loaderDiv.id = 'map-loader-container';
loaderDiv.innerHTML = `
    <div class="map-spinner"></div>
    <div class="loader-text">loading...</div>
`;
document.body.appendChild(loaderDiv);

// 제어 함수
function showMapLoader(text = "loading...") {
    document.querySelector('.loader-text').textContent = text;
    loaderDiv.style.display = 'block';
}

function hideMapLoader() {
    loaderDiv.style.display = 'none';
}


// Data.kt의 AirQualityStage 색상과 매칭 (RGB 형식)
const airQualityColorRange = [
    [0, 228, 0],    // 1: GOOD (Green)
    [255, 255, 0],  // 2: MODERATE (Yellow)
    [255, 126, 0],  // 3: UNHEALTHY_FOR_SENSITIVE (Orange)
    [255, 0, 0],    // 4: UNHEALTHY (Red)
    [143, 63, 151], // 5: VERY_UNHEALTHY (Purple)
    [126, 0, 35]    // 6: HAZARDOUS (Maroon)
];

/**
 * Data.kt의 get...Stage 함수들과 동일한 로직의 JS 구현
 */
function getAirQualityLevel(value, type) {
    if (value <= 0) return 0;

    const thresholds = {
        pm25: [0, 9.0, 35.4, 55.4, 125.4, 225.4],
        pm10: [0, 54.0, 154.0, 254.0, 354.0, 424.0],
        o3:   [0, 0.054, 0.070, 0.085, 0.105, 0.200],
        no2:  [0, 0.053, 0.100, 0.360, 0.649, 1.249],
        co:   [0, 4.4, 9.4, 12.4, 15.4, 30.4],
        so2:  [0, 0.035, 0.075, 0.185, 0.304, 0.604],
        nh3:  [0, 0.25, 0.70, 1.50, 5.00, 25.00],
        h2s:  [0, 0.01, 0.05, 0.10, 1.00, 10.00]
    };

    const limits = thresholds[type] || thresholds['pm10']; // 기본값 pm10

    if (value <= limits[1]) return 1;
    if (value <= limits[2]) return 2;
    if (value <= limits[3]) return 3;
    if (value <= limits[4]) return 4;
    if (value <= limits[5]) return 5;
    return 6;
}

function updateData(values){

    if (animationId) {
        cancelAnimationFrame(animationId);
    }

    deckData = values;
    renderLayer();
};

function renderLayer() {
    if (!deckData) return;

    let title;

    // 타입에 따른 설정값 결정
    switch(currentType) {
        case 'o3': title = "Ozone(O3)"; break;
        case 'no2': title = "Nitrogen dioxide(NO2)";  break;
        case 'co': title = "Carbon monoxide(CO)";  break;
        case 'so2': title = "Sulfur dioxide(SO2)";  break;
        case 'nh3': title = "Ammonia(NH3)";  break;
        case 'h2s': title = "Hydrogen sulfide(H2S)";  break;
        case 'pm10': title = "Particulate Matter(PM10)"; break;
        case 'pm25': title = "Particulate Matter(PM2.5)";  break;
        default: title = "Ozone(O3)";
    }
    /*
    공식 설명 (Math.pow 활용)
    •줌 레벨 12: 90 * Math.pow(2, 12 - 12) = 90 * 1 = 90m
    •줌 레벨 14 (확대): 90 * Math.pow(2, 12 - 14) = 90 * 0.25 = 22.5m (육각형이 너무 커지는 것을 방지)
    •줌 레벨 10 (축소): 90 * Math.pow(2, 12 - 10) = 90 * 4 = 360m (육각형이 너무 작아져서 안 보이는 것을 방지)
    */
    const currentZoom = map.getZoom();
    const dynamicRadius =   currentZoom >= zoomLevel ? 90 : (90 * Math.pow(2, zoomLevel - currentZoom))
    const dynamicMaxElevation =   currentZoom >= zoomLevel ? 50 : (200 * Math.pow(2, zoomLevel - currentZoom))
    let values = deckData.map(d => d.value || 0 )
    const maxDomain = Math.max(...values);

    // ID는 타입이 바뀔 때만 변경되도록 하여 줌 변경 시 불필요한 전체 리렌더링 방지
    const layerId = `hexagon-layer-${currentType}`;

    const props = {
        id: layerId,
        data: deckData, // 미리 로드된 데이터(Array)를 직접 전달 (네트워크 요청 없음)
        dataTransform: (data) => data.map(d => ({
            ...d,
            value: d.value
        })),
        gpuAggregation: true,
        getPosition: d => [d.lng, d.lat],
        colorRange: airQualityColorRange,
        colorDomain: [1, 6],
        getColorWeight: d => getAirQualityLevel(Number(d.value), currentType),
        colorAggregation: 'MAX',
        elevationRange: [0, dynamicMaxElevation],
        elevationDomain: [1, 6],
        getElevationWeight: d => getAirQualityLevel(Number(d.value), currentType),
        elevationAggregation: 'MAX',
        radius: dynamicRadius,
        extruded: true,
        pickable: true,
        onHover: info => {
            const tooltip = document.getElementById('tooltip');
            if (info.object) {
                const source = info.object.points[0].source;
                const maxValue = Math.max(...info.object.points.map(p => p.source.value));
                tooltip.innerHTML = `
                    <div style="font-weight:bold; margin-bottom:5px;">${source.addr}</div>
                    <div>${source.sensing_time}</div>
                    <div>SerialID: ${source.obs}</div>
                    <div>${title}:${source.value}</div>
                    <div>최대값: ${maxValue}</div>
                `;
                tooltip.style.display = 'block';
                tooltip.style.left = `${info.x + 15}px`;
                tooltip.style.top = `${info.y + 15}px`;
            } else {
                tooltip.style.display = 'none';
            }
        }
    };

    startHexagonAnimation(props);

           // 최종 렌더링 종료 후 로더 숨김
           setTimeout(() => {
               hideMapLoader();
           }, 300); // 부드러운 전환을 위해 약간의 지연
};


async function initMap() {

    const { Map } = await google.maps.importLibrary("maps");
    const { AdvancedMarkerElement } = await google.maps.importLibrary("marker");

    map  = new Map(document.getElementById('un7map'), {
      mapId: "",
      center: center,
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


    map.addListener('zoom_changed', () => {
        renderLayer();
    });

    // [최초 로드] 데이터 요청 포함
    google.maps.event.addListenerOnce(map, 'idle', () => {

    });


};


window.addEventListener("message", (event) => {
 showMapLoader("loading..."); // 로더 표시
    let data = event.data;    // 1. 데이터가 문자열(String)인 경우 JSON으로 파싱 시도
    if (typeof data === 'string') {
        try {
            data = JSON.parse(data);
        } catch (e) {
            console.error("메시지 데이터 파싱 중 오류 발생:", e);
            hideMapLoader();
            return; // 파싱 실패 시 함수 종료
        }
    }

    if(data.action == 'CHANGE_DATA'){

        currentType = data.type;
        updateData(data.values)
    }

});



function startHexagonAnimation(props) {
    let currentTime = 0

    if (animationId) {
        cancelAnimationFrame(animationId);
    }

    function animate() {
        // currentTime을 이용하여 사인(Sine) 곡선 생성 (0.02는 속도 조절)
        currentTime += 0.2;
      //  currentTime += 0.06;
        // [핵심 수정] 매 프레임마다 높이와 투명도를 새로 계산합니다.
        const animatedScale =  currentTime  * 5;
        //        const animatedScale =  currentTime  * 3;
        const currentOpacity = currentTime * 0.1;
       //         const currentOpacity = 1;

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

