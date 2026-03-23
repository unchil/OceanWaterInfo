//import {keys } from './keys.js';
//import {values } from './values.js';


let map;
let zoomLevel = 6
let infoWindow;
let toggleBtn = false;
let observatoryDesc;

let step = 0; // 애니메이션 진행 상태를 추적할 변수
let center = { lat: 37.385852, lng: 126.934515 };

let screenPoints = [];
let animationId; // 애니메이션 루프 ID를 저장할 변수 추가


const canvas = document.getElementById('animation-overlay');
const mapDiv = document.getElementById('un7map');
canvas.width = mapDiv.offsetWidth;
canvas.height = mapDiv.offsetHeight;


async function initMap() {

    const { Map } = await google.maps.importLibrary("maps");
    const { AdvancedMarkerElement } = await google.maps.importLibrary("marker");


    map  = new Map(document.getElementById('un7map'), {
        mapId: "YOUR MAP ID",
        center: center,
        zoom: zoomLevel,
        renderingType: google.maps.RenderingType.VECTOR,

    });



    // Set map options.
    map.setOptions({
        scaleControl: true,
        mapTypeId: google.maps.MapTypeId.SATELLITE,

    });



      infoWindow = new google.maps.InfoWindow({
          content: "",
          disableAutoPan: true,
          headerDisabled: true
      });

      google.maps.event.addListener(map, "click", (event) => {
          if(infoWindow != null){
              toggleBtn = false
              infoWindow.close()
          }
      });




    const marker =new google.maps.Marker({
        position: center,
        map: map,
    });



 //  addMarkerClusterer(keys, [], [])
  //  initMapWithData(keys, values)

}


function updateAndDraw(keys, values){

    screenPoints = [];

     const projection = map.getProjection();

    if (!projection) {
        console.error("프로젝션을 가져올 수 없습니다.");
        return;
    }

    console.log("프로젝션 준비 완료");


   // 현재 지도의 줌과 중심점을 고려한 픽셀 변환 공식
   const scale = Math.pow(2, map.getZoom());
   const centerWorld = projection.fromLatLngToPoint(map.getCenter());

    keys.forEach((coord, index) => {

        let particles = [];
        const worldPoint = projection.fromLatLngToPoint(new google.maps.LatLng(coord.lat, coord.lng));
        let x = (worldPoint.x - centerWorld.x) * scale + (canvas.width / 2);
        let y = (worldPoint.y - centerWorld.y) * scale + (canvas.height / 2);

        particles.push({x:x, y:y, speed:0.0});

        values[index].forEach( (particle) => {

            const worldPoint = projection.fromLatLngToPoint(new google.maps.LatLng(particle.lat, particle.lng));
            let x = (worldPoint.x - centerWorld.x) * scale + (canvas.width / 2);
            let y = (worldPoint.y - centerWorld.y) * scale + (canvas.height / 2);

            particles.push({x:x, y:y, speed: particle.speed});
        });

        screenPoints.push(particles);

    });

    console.log("입자 초기화 완료:", screenPoints.length);
    startAnimation();

}



window.initMapWithData = function(keys, values) {

    // 지도가 준비되었는지 확인하는 이벤트 리스너 등록
    google.maps.event.addListenerOnce(map, 'idle', function() {
        updateAndDraw(keys, values);
    });

    map.addListener("zoom_changed", () => {
        canvas.width = mapDiv.offsetWidth;
        canvas.height = mapDiv.offsetHeight;
        updateAndDraw(keys, values);
    });

      // 3. 지도를 드래그해서 옮길 때도 좌표를 맞춰야 한다면 'dragend' 추가
      map.addListener("dragend", () => {
          updateAndDraw(keys, values);
      });
}




function startAnimation() {

    const ctx = canvas.getContext('2d');

    function frame() {
        // 1. 잔상 효과: 완전히 지우지 않고 반투명하게 덮음 (꼬리 효과)
        ctx.globalCompositeOperation = 'source-over';
        ctx.fillStyle = "rgba(0, 0, 0, 0.1)"; // 배경이 검은색 계열일 때 (위성지도면 투명도 조절 필요)

        // 만약 지도를 가리지 않고 선만 깔끔하게 움직이게 하려면 clearRect를 유지하되
        // 로직을 '부분 그리기'로 바꿔야 합니다. 여기서는 '흐르는 효과'를 위해 clear를 유지합니다.
        ctx.clearRect(0, 0, canvas.width, canvas.height);


        // 2. 그리기 설정
        ctx.lineWidth = 1.0; // 10.0은 너무 두껍습니다.
        ctx.lineJoin = 'round';
        ctx.lineCap = 'round';


        // 3. 애니메이션 속도 제어 (숫자가 클수록 빠름)
        step += 0.005;
        if (step > 100) step = 0; // 0~100 사이 반복


        screenPoints.forEach( (particles, index) => {

            if (particles.length < 2) return;

            // --- [핵심] 입자가 이동하는 효과를 위한 점선 설정 ---
            // 20px의 선을 그리고 40px을 띄웁니다.
            ctx.setLineDash([6, 6]);
            // step 값에 따라 점선의 시작 위치를 밀어내어 움직이는 효과를 줌
            ctx.lineDashOffset = -step;

/*
            ctx.beginPath();
            ctx.moveTo(particles[0].x, particles[0].y);
      */

            for (let i = 1; i < particles.length; i++) {


                ctx.beginPath();
                ctx.moveTo(particles[i-1].x, particles[i-1].y);

                      // [핵심] 인덱스에 따라 두께를 0.2씩 증가시킴 (기본 두께 1.0부터 시작한다고 가정)
                        ctx.lineWidth = 1.0 + (i * 0.15);


                if (particles[i].speed > 100.0) {
                    ctx.strokeStyle = 'red';
                } else if (particles[i].speed > 30.0) {
                    ctx.strokeStyle = 'yellow';
                } else {
                    ctx.strokeStyle = 'cyan';
                };



                 ctx.lineTo(particles[i].x, particles[i].y);
                ctx.stroke(); // 즉시 그리기
            }
         //   ctx.stroke();
     // 2. 화살표 머리 그리기
     // 화살표는 점선과 가변 두께의 영향을 받지 않도록 초기화
     ctx.setLineDash([]);
     ctx.lineWidth = 2; // 화살표 촉의 일정한 두께

            // 2. 화살표 머리 그리기 (마지막 지점)
            const lastPoint = particles[particles.length - 1];
            const prevPoint = particles[particles.length - 2];

            // 두 점 사이의 각도 계산 (라디안)
            const angle = Math.atan2(lastPoint.y - prevPoint.y, lastPoint.x - prevPoint.x);

            const headLength =  map.getZoom() * 0.5;
            const headAngle = Math.PI / 6; // 화살표 날개의 각도 (30도)

            // 화살표 날개 1

            ctx.beginPath();
            ctx.moveTo(lastPoint.x, lastPoint.y);
            ctx.lineTo(
                lastPoint.x - headLength * Math.cos(angle - headAngle),
                lastPoint.y - headLength * Math.sin(angle - headAngle)
            );

            // 화살표 날개 2
            ctx.moveTo(lastPoint.x, lastPoint.y);
            ctx.lineTo(
                lastPoint.x - headLength * Math.cos(angle + headAngle),
                lastPoint.y - headLength * Math.sin(angle + headAngle)
            );

            ctx.stroke();

//ctx.setLineDash([]);


        });


requestAnimationFrame(frame);

   //     animationId =  requestAnimationFrame(frame);
    // [추가] 기존에 실행 중인 애니메이션이 있다면 중단시킴
    if (animationId) {
     //   cancelAnimationFrame(animationId);
    }else{

    }


    }

    frame();
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
        const marker =new google.maps.Marker({
          position,
          map: map
        });

        return marker;

    });

    new markerClusterer.MarkerClusterer({ map, markers });

}


initMap();


