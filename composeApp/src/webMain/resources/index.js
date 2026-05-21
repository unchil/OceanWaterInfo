// 현재 선택된 타입을 추적하기 위한 변수 (초기값 o3)
let currentSelectedType = 'o3';

document.addEventListener( "DOMContentLoaded", function() {

    const mapIframe = document.getElementById('map-iframe');
    const oceanSection = document.getElementById('section-ocean');
    const composeContainer = document.getElementById('webmain');

    mapIframe.addEventListener('load', function() {
        oceanSection.style.display = 'flex';
        oceanSection.style.visibility = 'hidden';
        oceanSection.style.position = 'absolute';
        const script = document.createElement('script');
        script.type = 'application/javascript';
        script.src = 'composeApp.js'; // 실제 파일 경로
        script.onload = function() {
            console.log("composeApp.js 로드 완료!");
        };
        composeContainer.appendChild(script);
    }, { once: true });

    const textElem = document.getElementById('description-text');
    const sidebarTitle = document.querySelector('.sidebar-header h3');

    if (qualityDescriptions['o3']) {
        textElem.innerText = qualityDescriptions['o3'];
        if (sidebarTitle) sidebarTitle.innerText = "Ozone(O3)";
    }

    startAutoRefresh();

});


function toggleSidebar() {
    const sidebar = document.getElementById('info-sidebar');
    const icon = document.getElementById('handle-icon');

    sidebar.classList.toggle('active');

    // 열리고 닫힐 때 아이콘 변경 (선택 사항)
    if (sidebar.classList.contains('active')) {
        icon.innerText = '◀';
    } else {
        icon.innerText = 'ⓘ';
    }
}


function switchMainTab(element, index) {
    // 1. 모든 메인 탭 활성화 해제
    const mainTabs = document.querySelectorAll('.main-tab');
    mainTabs.forEach(tab => tab.classList.remove('active'));

    // 2. 클릭한 탭 활성화
    element.classList.add('active');

    // 3. 섹션 전환
    const sdotSection = document.getElementById('section-sdot');
    const oceanSection = document.getElementById('section-ocean');

    if (index === 0) { // Seoul/Gyonggi 선택 시
        sdotSection.style.display = 'flex';
        sdotSection.style.visibility = 'visible';
        sdotSection.style.position = 'static';
        oceanSection.style.display = 'flex'; // flex 유지
        oceanSection.style.visibility = 'hidden'; // 숨김
        oceanSection.style.position = 'absolute'; // 공간 차지 방지
    } else {   // Korea Ocean 선택 시
        // Ocean은 보이고 SDoT은 숨김
        oceanSection.style.display = 'flex';
        oceanSection.style.visibility = 'visible';
        oceanSection.style.position = 'static';
        sdotSection.style.display = 'flex'; // flex 유지
        sdotSection.style.visibility = 'hidden'; // 숨김
        sdotSection.style.position = 'absolute'; // 공간 차지 방지
    }

    // 필요 시 Compose 측에 상태 변경을 알리는 로직을 추가할 수 있습니다.
    console.log("Main Tab Switched to:", index);
}


function changeTab(element, type) {
        // 현재 선택된 타입 업데이트
    currentSelectedType = type;

    // 1. 모든 탭에서 'active' 클래스 제거
    const tabs = document.querySelectorAll('.tab-item');
    tabs.forEach(tab => tab.classList.remove('active'));

    // 2. 클릭된 탭에 'active' 클래스 추가
    element.classList.add('active');

        // 클릭된 버튼(element)의 텍스트를 가져와서 사이드바의 h3에 할당합니다.
    const sidebarTitle = document.querySelector('.sidebar-header h3');

    if (sidebarTitle) {
        sidebarTitle.innerText = element.innerText;
    }

    const textElem = document.getElementById('description-text');

    if (qualityDescriptions[type]) {
        textElem.innerText = qualityDescriptions[type];
    }

    // 3. iframe의 ID를 가져와서 src 변경
    const iframe = document.getElementById('map-iframe');

    //페이지 새로고침 없이 함수만 호출하고 싶을 때
    //src를 바꾸는 대신 메시지를 보냄
    iframe.contentWindow.postMessage({ action: 'CHANGE_TYPE', type: type }, '*');

}

// --- [추가] 30분 간격 자동 리프레시 로직 ---
function startAutoRefresh() {
    //const THIRTY_MINUTES = 5 * 60 * 1000; // 5분을 밀리초로 계산
    const THIRTY_MINUTES = 1 * 60 * 1000; // 1분을 밀리초로 계산
    setInterval(() => {
        const iframe = document.getElementById('map-iframe');
        if (iframe) {
            console.log(`[자동 갱신] ${new Date().toLocaleTimeString()} - ${currentSelectedType} 데이터를 새로 고침합니다.`);

            // 방법 A: postMessage로 내부 함수 호출 (깜빡임 없음)
            iframe.contentWindow.postMessage({ action: 'CHANGE_TYPE', type: currentSelectedType }, '*');

            /*
            방법 B: 만약 페이지 전체를 새로고침하고 싶다면 아래 주석 해제 (깜빡임 있음)
            const currentSrc = iframe.src.split('?')[0];
            iframe.src = currentSrc + "?t=" + new Date().getTime();
            */
        }
    }, THIRTY_MINUTES);
}


