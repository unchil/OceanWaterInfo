// mapLoader.js

// 1. 스타일 설정
const style = document.createElement('style');
style.innerHTML = `
  #map-loader-container {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    z-index: 2000;
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

// 2. DOM 생성 및 초기화
let loaderDiv;

function initLoader() {
    if (document.getElementById('map-loader-container')) return;

    document.head.appendChild(style);

    loaderDiv = document.createElement('div');
    loaderDiv.id = 'map-loader-container';
    loaderDiv.innerHTML = `
        <div class="map-spinner"></div>
        <div class="loader-text">loading...</div>
    `;
    document.body.appendChild(loaderDiv);
}

// 3. 외부에서 사용할 함수 내보내기 (Export)
export function showMapLoader(text = "loading...") {
    if (!loaderDiv) initLoader(); // 초기화 확인
    document.querySelector('.loader-text').textContent = text;
    loaderDiv.style.display = 'block';
}

export function hideMapLoader() {
    if (loaderDiv) {
        loaderDiv.style.display = 'none';
    }
}

// 파일 로드 시 자동으로 초기화 실행
initLoader();

