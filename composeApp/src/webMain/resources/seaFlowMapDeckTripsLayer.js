//import {values } from './values.js';

const {GoogleMapsOverlay, TripsLayer} = deck;

let map;
let overlay;
let zoomLevel = 8
let currentTime = 0; // 애니메이션 진행 상태를 추적할 변수//
let center = { lat: 37.385852, lng: 126.934515 };
let animationId; // 애니메이션 루프 ID를 저장할 변수 추가
let deckData = []; // 데이터를 전역으로 관리하여 루프에서 참조
let props;


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

    overlay = new GoogleMapsOverlay({layers:[]});
    overlay.setMap(map);
  //  initMapWithData(values)

};


window.initMapWithData = function( values) {

    deckData = values.map((particle) => {
        const path = particle.map(p => [p.lng, p.lat]);
        const timestamps = particle.map( (_, index) =>  index  *  10  );
        const speed = particle[0].speed;
        return {
            speed: speed,
            path: path,
            timestamps: timestamps
        };
    });

    props = {
      id: 'trips-layer',
      data: deckData,
      getPath: d => d.path,
      getTimestamps: d => d.timestamps,
      getColor: d => {
          if (d.speed > 100) return [255, 0, 0];      // Red
          if (d.speed > 30) return [255, 165, 0];    // Orange
          return [0, 255, 255];                      // Cyan
      },
      opacity: 1.0,
      widthMinPixels: 3,   // 최소 선 두께
      trailLength: 360,     // 입자 꼬리의 길이
      currentTime: currentTime,   // 현재 애니메이션 시간
      shadowEnabled: true
  }

     console.log("deckData 초기화 완료:", deckData.length);

    google.maps.event.addListenerOnce(map, 'idle', function() {
       startAnimation();
    });

}


function startAnimation() {

    if (animationId) {
        cancelAnimationFrame(animationId);
    }
    // 데이터가 있는지 확인
    if (deckData.length === 0) return;

    // 데이터의 마지막 타임스탬프 값 계산 (예: 데이터 36개 * 간격 30 )
    const maxTime = ( deckData[0].timestamps.length - 1 ) * 10 ;

    console.log("Animation Started with maxTime:", maxTime);

    function animate() {

        currentTime += 3.0; // 애니메이션 속도 조절
        if (currentTime > maxTime) currentTime = 0;

        const tripsLayer = new TripsLayer({
            ...props,
            currentTime
        });

        overlay.setProps({
          layers: [tripsLayer],
        });

        animationId = requestAnimationFrame(animate);
    }

    animationId = requestAnimationFrame(animate);
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


