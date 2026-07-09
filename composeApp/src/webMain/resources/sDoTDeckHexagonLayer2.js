//import {valuesHexagon } from './valuesHexagonLayer.js';

import { qualityDetails } from './descriptions.js';

const {GoogleMapsOverlay, HexagonLayer } = deck;
const { load, JSONLoader } = loaders;
let cachedData = null; // 서버에서 받은 데이터를 저장할 변수
let dataLoadTime = null;
const DATA_URL = 'http://192.168.35.107:7788/sdot_env_info';

let map;
let overlay;
let zoomLevel = 12

let center = { lat: 37.385852, lng: 126.934515 };
let animationId; // 애니메이션 루프 ID를 저장할 변수 추가
let deckData = []; // 데이터를 전역으로 관리하여 루프에서 참조
let title;
let elevationBase = 0; // 기본 높이 배율
const animatedOpacity = 1.0 ;

let currentType = 'o3';



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


/**
 * 3단계(민감군 주의) ~ 6단계(위험)까지 레벨별 리스트를 생성 및 업데이트합니다.
 */
function updateLevelStatusLists() {
    const sidebarContent = parent.document.getElementById('description-text');
    if (!sidebarContent) return;

    // 단계별 정보 정의
    const levelInfo = {
        3: { label: "민감군 주의 (UNHEALTHY_FOR_SENSITIVE)", class: "level-3" },
        4: { label: "나쁨 (UNHEALTHY)", class: "level-4" },
        5: { label: "매우 나쁨 (VERY_UNHEALTHY)", class: "level-5" },
        6: { label: "위험 (HAZARDOUS)", class: "level-6" }
    };

    // 3단계부터 6단계까지 순회
    for (let level = 6; level >= 3; level--) {
        const containerId = `status-container-${level}`;
        let container = parent.document.getElementById(containerId);
        // 1. 기존 컨테이너가 있다면 아예 삭제하여 초기화
        if (container) {
            container.remove();
            container = null; // 참조 초기화
        }

        // 1. 해당 레벨 데이터 필터링 및 정렬


        const filteredEntries = cachedData.map(d => ({
                sensing_time: d.sensing_time,
                obs:d.obs,
                addr:d.addr,
                lng: Number(d.lng),
                lat: Number(d.lat),
                value: (parseFloat(d[currentType]) || 0 )
            })
        ).filter(d => {
          return getAirQualityLevel(d.value, currentType) === level;
      }).sort((a, b) => b.value - a.value);


        // 2. 데이터가 없으면 기존 UI 숨기고 다음 레벨로 이동
        if (filteredEntries.length === 0) {
            continue;
        }

        const html = `
            <div id="${containerId}" class="air-quality-list-container">
                <span class="air-quality-label ${levelInfo[level].class}">${levelInfo[level].label}</span>
                <textarea id="text-area-${level}" class="air-quality-textarea ${levelInfo[level].class}" readonly></textarea>
            </div>
        `;
        sidebarContent.insertAdjacentHTML('beforeend', html);
        container = parent.document.getElementById(containerId);


        // 4. 텍스트 내용 생성
        const listText = filteredEntries.map((d, index) => {
            const val = d.value.toFixed(3);
            const addr = d.addr || "주소 정보 없음";
            return `${index + 1}. [${val}] ${addr}`;
        }).join('\n');

        // 5. 값 업데이트 및 화면 표시
        const textArea = parent.document.getElementById(`text-area-${level}`);
        if (textArea) {
            textArea.value = listText;
            container.style.display = 'block';
        }
    }
}


/**
 * jvmMain의 sDoTEnvInfoStat.value 로직을 이식한 함수
 * 현재 로드된 데이터(cachedData)를 7단계로 분류하여 집계합니다.
 */
window.updateAirQualityStatistics = function() {

    let level = 0;

    // 1. 0~6단계 카운터를 0으로 초기화 (Map 역할)
    const stats = { 0: 0, 1: 0, 2: 0, 3: 0, 4: 0, 5: 0, 6: 0 };

    if (!cachedData || cachedData.length === 0) {
        console.warn("집계할 데이터가 없습니다.");
        return stats;
    }

    // 2. 전체 데이터를 순회하며 단계별 개수 계산
    cachedData.forEach(d => {
        // 데이터 정제 (공백 제거 및 숫자 변환)
        const rawValue = d[currentType]?.toString().trim() || "";
        const value = rawValue === "" ? -1 : (parseFloat(rawValue) || 0);

        // 레벨 계산 (이전에 정의한 getAirQualityLevel 함수 사용)
        // 값이 -1(공백)이면 0(UNKNOWN)으로 처리
        const level = value === -1 ? 0 : getAirQualityLevel(value, currentType);

        // 해당 단계 카운트 증가
        stats[level]++;
    });

    // 3. 계산된 통계를 사이드바 상황판 UI에 반영
    for (let i = 0; i < 7; i++) {

        if(stats[i] > 0) { level = i }

        let elementId = `status-level-${i}`
        const targetElement = parent.document.getElementById(elementId);
        if (targetElement) {
            // 이전에 만든 Column 구조 내의 .value 클래스를 찾음
            const valueDisplay = targetElement.querySelector('.value');
            if (valueDisplay) {
                // 부드러운 업데이트를 위해 텍스트 교체
                valueDisplay.innerText = stats[i];
            }
        }
    }

    updateSidebarStatusBoard(level);
    updateLevelStatusLists();
    updateSidebarInfoDisplay();
    console.log(`[통계 업데이트 완료] 타입: ${currentType}, 데이터수: ${cachedData.length}`);

}

/**
 * 특정 레벨(0~6)을 활성화 상태로 변경합니다.
 */
function updateSidebarStatusBoard(level) {
    // 모든 항목에서 active 제거
    parent.document.querySelectorAll('.status-item-h').forEach(item => {
        item.classList.remove('active');
    });

    // 해당되는 단계만 활성화
    const target = parent.document.getElementById(`status-level-${level}`);
    if (target) {
        target.classList.add('active');
    }
}


function updateSidebarInfoDisplay() {
    const container = parent.document.getElementById('description-text');
    if (!container) return;

    let elementId = 'stats-info-box'

// 1. 기존에 생성된 박스가 있다면 제거 (갱신을 위해)
    const oldBox = parent.document.getElementById(elementId);
    if (oldBox) oldBox.remove();

    const detail = qualityDetails[currentType];
    if (!detail) return;

    const labels = ["모름", "좋음", "보통", "민감군 영향", "나쁨", "매우 나쁨", "위험"];

    let detailHtml = `<div id=${elementId} class="info-display-box"> <div class="info-header">정보</div><p><strong>[특징]</strong><br>${detail.specialFeature}</p><p style="color: #d32f2f;"><strong>[민감군 영향]</strong><br>${detail.sensitiveInfo}</p><div style="margin: 10px 0;"><strong>[단계별 기준 및 코멘트]</strong><ul style="padding-left: 15px; margin-top: 5px;">`;

    for (let i = 1; i <= 6; i++) {
        detailHtml += `<li style="margin-bottom: 5px;"><span style="font-weight: bold; color: #555;">${labels[i]}</span>: ${detail.ranges[i-1]}<br><span style="font-size: 0.8rem; color: #777;">&nbsp;&nbsp;→ ${detail.comments[i-1]}</span> </li>`;
    }

    detailHtml += `</ul></div></div>`;



     container.insertAdjacentHTML('beforeend', detailHtml);

}

function updateData2(values){

    if (animationId) {
        cancelAnimationFrame(animationId);
    }

    deckData = values;
    renderLayer();
}


// 1. 데이터 업데이트 로직 (서버 요청)
async function updateData(type) {
    if (animationId) {
        cancelAnimationFrame(animationId);
    }
    currentType = type;
    let shouldFetch = true; // 기본적으로 서버 요청을 수행함

    // 2. 캐시 데이터가 존재하면 시간 체크 수행
    if (cachedData && cachedData.length > 0) {
        try {
            const now = new Date();
            const diffMinutes = (now - dataLoadTime) / (1000 * 60);
            console.log(`[데이터 선도 체크] 마지막 측정: ${dataLoadTime}, 경과: ${Math.round(diffMinutes)}분`);
            // 데이터가 5분 이내의 것이라면 서버 요청을 생략 (shouldFetch = false)
            if (diffMinutes >= 0 && diffMinutes < 5) {
                shouldFetch = false;
                console.log("[캐시 활용] 5분 이내의 최신 데이터가 존재하여 서버 요청을 건너뜁니다.");
            }
        } catch (e) {
            console.error("시간 계산 중 오류 발생, 서버에서 새로 로드합니다.", e);
            shouldFetch = true;
        }
    }

    // 3. 서버 요청이 필요한 경우에만 load 수행
    if (shouldFetch) {
        const dataUrl = `${DATA_URL}?t=${new Date().getTime()}`;
        console.log(`[서버 요청 실행] 타입: ${type}, URL: ${dataUrl}`);

        try {
            const rawData = await load(dataUrl, JSONLoader);
            const rows = rawData.sDoTEnv ? rawData.sDoTEnv.row : (Array.isArray(rawData) ? rawData : []);
            cachedData = rows.map(d => ({
                ...d,
                lng: Number(d.lng),
                lat: Number(d.lat)
                // 각 타입별 value는 renderLayer에서 동적으로 처리하거나
                // 여기서 미리 계산할 수 있습니다.
            }));
            dataLoadTime = new Date();
        } catch (error) {
            console.error("데이터 로드 실패:", error);
        }

    }


    deckData = cachedData.map(d => ({
            sensing_time: d.sensing_time,
            obs:d.obs,
            addr:d.addr,
            lng: Number(d.lng),
            lat: Number(d.lat),
            value: (parseFloat(d[currentType]) || 0 )
        })
    );



    // 4. 레이어 렌더링 (캐시든 새 데이터든 호출)
    renderLayer();
};


// 2. 레이어 렌더링 로직 (시각적 설정 및 애니메이션 시작)
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
};



async function initMap() {

    const { Map } = await google.maps.importLibrary("maps");
    const { AdvancedMarkerElement } = await google.maps.importLibrary("marker");

    map  = new Map(document.getElementById('un7map'), {
      mapId: "9038a0505ac4349baf8c6048",
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


    map.addListener('zoom_changed', () => {
        renderLayer();
    });

    // [최초 로드] 데이터 요청 포함
    google.maps.event.addListenerOnce(map, 'idle', () => {
       // updateData(currentType);
    });


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


    if (data.action === 'CHANGE_TYPE') {
        currentType = data.type;
        updateData(data.type);
      //  updateAirQualityStatistics()
    }

    if(data.action == 'CHANGE_DATA'){
        currentType = data.type;
        updateData2(data.values)
    }

    if (data.action === 'UPDATE-STATE') {
   //     updateAirQualityStatistics()
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


