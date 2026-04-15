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


// 1. 데이터 업데이트 로직 (서버 요청)
window.updateData = async function(type) {
    currentType = type;
    const dataUrl = `${DATA_URL}?t=${new Date().getTime()}`;
    console.log(`[데이터 갱신] 타입: ${type}, URL: ${dataUrl}`);

    try {
        // loaders.gl의 load 함수를 사용하여 데이터를 미리 가져옵니다.
        const rawData = await load(dataUrl, JSONLoader);

        // 데이터 구조 변환 (기존 dataTransform 로직을 여기로 이동)
        const rows = rawData.sDoTEnv ? rawData.sDoTEnv.row : (Array.isArray(rawData) ? rawData : []);
        cachedData = rows.map(d => ({
            ...d,
            lng: Number(d.lng),
            lat: Number(d.lat)
            // 각 타입별 value는 renderLayer에서 동적으로 처리하거나
            // 여기서 미리 계산할 수 있습니다.
        }));

        // 데이터 로드 완료 후 화면 그리기
        renderLayer();
    } catch (error) {
        console.error("데이터 로드 실패:", error);
    }
};


// 2. 레이어 렌더링 로직 (시각적 설정 및 애니메이션 시작)
window.renderLayer = function() {
    if (!cachedData) return;

    let title;
    let maxDomain;

    // 타입에 따른 설정값 결정
    switch(currentType) {
        case 'o3': title = "Ozone(O3)"; maxDomain = 0.15; break;
        case 'no2': title = "Nitrogen dioxide(NO2)"; maxDomain = 0.2; break;
        case 'co': title = "Carbon monoxide(CO)"; maxDomain = 50; break;
        case 'so2': title = "Sulfur dioxide(SO2)"; maxDomain = 0.15; break;
        case 'nh3': title = "Ammonia(NH3)"; maxDomain = 0.15; break;
        case 'h2s': title = "Hydrogen sulfide(H2S)"; maxDomain = 0.3; break;
        case 'pm10': title = "Particulate Matter(PM10)"; maxDomain = 81; break;
        case 'pm25': title = "Particulate Matter(PM2.5)"; maxDomain = 36; break;
        default: title = "Ozone(O3)"; maxDomain = 0.15;
    }
    /*
    공식 설명 (Math.pow 활용)
    •줌 레벨 12: 90 * Math.pow(2, 12 - 12) = 90 * 1 = 90m
    •줌 레벨 14 (확대): 90 * Math.pow(2, 12 - 14) = 90 * 0.25 = 22.5m (육각형이 너무 커지는 것을 방지)
    •줌 레벨 10 (축소): 90 * Math.pow(2, 12 - 10) = 90 * 4 = 360m (육각형이 너무 작아져서 안 보이는 것을 방지)
    */
    const currentZoom = map.getZoom();
    const dynamicRadius = 90 * Math.pow(2, 12 - currentZoom);
    const dynamicMaxElevation = 100 * Math.pow(2, 12 - currentZoom);

    // ID는 타입이 바뀔 때만 변경되도록 하여 줌 변경 시 불필요한 전체 리렌더링 방지
    const layerId = `hexagon-layer-${currentType}`;

    const props = {
        id: layerId,
        data: cachedData, // 미리 로드된 데이터(Array)를 직접 전달 (네트워크 요청 없음)
        dataTransform: (data) => data.map(d => ({
            ...d,
            value: Number(d[currentType] || 0) // 선택된 타입에 따른 가중치 설정
        })),
        gpuAggregation: true,
        extruded: true,
        getPosition: d => [d.lng, d.lat],
        colorRange: colorRange,
        colorDomain: [0, maxDomain],
        getColorWeight: d => Math.min(d.value, maxDomain),
        colorAggregation: 'MAX',
        elevationRange: [0, dynamicMaxElevation],
        elevationDomain: [0, maxDomain],
        getElevationWeight: d => Math.min(d.value, maxDomain),
        elevationAggregation: 'MAX',
        radius: dynamicRadius,
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
};



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

 // [줌 변경] 데이터 요청 없이 레이어 설정만 업데이트
    map.addListener('zoom_changed', () => {
       // renderLayer();
        initMapWithType(currentType);
    });

    // [최초 로드] 데이터 요청 포함
    google.maps.event.addListenerOnce(map, 'idle', () => {
        //updateData(currentType);
        initMapWithType(currentType);
    });


};

//페이지 새로고침 없이 함수만 호출하고 싶을 때
window.addEventListener("message", (event) => {
    if (event.data.action === 'CHANGE_TYPE') {
        currentType = event.data.type;
        initMapWithType(currentType);
      //  updateData(event.data.type);
    }
});


window.initMapWithType =  function( type) {
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

    // [추가] 줌 레벨에 따른 동적 radius 계산
    // 공식: 기준반경 * 2^(기준줌 - 현재줌)
    // 줌이 커지면(확대) radius는 작아지고, 줌이 작아지면(축소) radius는 커집니다.
    //공식 설명 (Math.pow 활용)
    //•줌 레벨 12: 90 * Math.pow(2, 12 - 12) = 90 * 1 = 90m
    //•줌 레벨 14 (확대): 90 * Math.pow(2, 12 - 14) = 90 * 0.25 = 22.5m (육각형이 너무 커지는 것을 방지)
    //•줌 레벨 10 (축소): 90 * Math.pow(2, 12 - 10) = 90 * 4 = 360m (육각형이 너무 작아져서 안 보이는 것을 방지)

    const currentZoom = map.getZoom();
    const dynamicRadius = 90 * Math.pow(2, 12 - currentZoom);

    // [추가] 줌 레벨에 따른 동적 elevationRange 최대값 계산
    // 기준 줌 12에서 최대 높이를 100으로 설정하고, 줌 레벨에 따라 지수적으로 변경합니다.
    // 줌 확대(숫자 커짐) -> 최대 높이 감소 (세밀하게 보기 위함)
    // 줌 축소(숫자 작아짐) -> 최대 높이 증가 (멀리서도 잘 보이기 위함)

    const dynamicMaxElevation = 100 * Math.pow(2, 12 - currentZoom);

    let props = {
        id: id,
        data: dataUrl,
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


