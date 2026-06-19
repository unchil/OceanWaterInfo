let map;
let infoWindow;
let toggleBtn = false;
let observatoryDesc;



let center = { lat: 37.385852, lng: 126.934515 };
let locations = [{ lat: 37.799, lng: 128.9492 },{ lat: 34.7851, lng: 128.5664 },{ lat: 34.8038, lng: 128.7094 },{ lat: 38.3681, lng: 128.5239 },{ lat: 34.5049, lng: 127.1228 },{ lat: 35.9607, lng: 129.5497 },{ lat: 35.8168, lng: 126.4422 },{ lat: 35.187, lng: 129.227 },{ lat: 34.8746, lng: 127.9522 },{ lat: 34.7255, lng: 128.0497 },{ lat: 34.7892, lng: 126.3653 },{ lat: 37.9505, lng: 124.7295 },{ lat: 37.3023, lng: 129.3127 },{ lat: 36.8935, lng: 126.3524 },{ lat: 36.6163, lng: 126.3717 },{ lat: 33.3104, lng: 126.164 },{ lat: 38.0808, lng: 128.6998 },{ lat: 34.687, lng: 127.708 },{ lat: 36.5737, lng: 129.437 },{ lat: 34.4347, lng: 126.8083 },{ lat: 34.342, lng: 127.0101 },{ lat: 34.3786, lng: 127.0654 },{ lat: 34.2214, lng: 126.5414 },{ lat: 34.3825, lng: 126.7364 },{ lat: 34.3275, lng: 127.035 },{ lat: 34.3013, lng: 126.768 },{ lat: 34.1636, lng: 126.6269 },{ lat: 34.3674, lng: 126.9941 },{ lat: 34.1698, lng: 126.8547 },{ lat: 36.4158, lng: 126.4333 },{ lat: 36.4808, lng: 126.4202 },{ lat: 36.7123, lng: 126.147 },{ lat: 34.8082, lng: 128.4951 },{ lat: 34.8022, lng: 128.2463 },{ lat: 34.8222, lng: 128.345 },{ lat: 34.7904, lng: 128.4293 },{ lat: 34.8348, lng: 128.3353 },{ lat: 34.7498, lng: 128.4151 },{ lat: 34.6069, lng: 126.2672 },{ lat: 34.4235, lng: 126.4216 }]
let labels = [ "강릉", "거제 가배", "거제 일운", "고성 가진", "고흥 소록도", "구룡포 하정", "군산 신시도", "기장", "남해 강진", "남해 미조", "목포", "백령도", "삼척", "서산 지곡", "서산 창리", "서제주", "양양", "여수 신월", "영덕", "완도 가교", "완도 감목", "완도 금일", "완도 노화도", "완도 대창", "완도 동백", "완도 망남", "완도 백도", "완도 일정", "완도 청산", "태안 고남", "태안 대야도", "태안 파도리", "통영 비산도", "통영 사량", "통영 수월", "통영 영운", "통영 풍화", "통영 학림", "해남 임하", "해남 화산" ]
let contents = [" build_data:2008-07-08<br>surface_depth: 5M<br>middle_depth: 20M<br>bottom_depth: 30M<br>desc: 강원도 강릉시 사천면 사천항"," build_data:2004-12-24<br>surface_depth: 5M<br>desc: 경남 거제시 동부면 가배리 해바라기수산"," build_data:2008-06-20<br>surface_depth: 5M<br>desc: 거제 일운"," build_data:2019-03-27<br>surface_depth: 3M<br>desc: "," build_data:2014-08-08<br>surface_depth: 3M<br>desc: 포항 구룡포 하정 양식장"," build_data:2018-04-03<br>surface_depth: 2M<br>desc: "," build_data:2008-11-04<br>surface_depth: 5M<br>middle_depth: 10M<br>bottom_depth: 15M<br>desc: "," build_data:2005-12-24<br>surface_depth: 3M<br>desc: 경상남도 남해군 이동면 양식장 바지선"," build_data:2005-11-23<br>surface_depth: 5M<br>middle_depth: 10M<br>desc: 경남 남해군 미조면"," build_data:2006-07-03<br>surface_depth: 5M<br>desc: 전라남도 목포시 죽교동 571-2번지"," build_data:2006-09-12<br>surface_depth: 5M<br>desc: 인천광역시 옹진군 백령도"," build_data:2008-07-22<br>surface_depth: 5M<br>middle_depth: 15M<br>bottom_depth: 25M<br>desc: 강원도 삼척시 근덕면 장호항"," build_data:2008-06-21<br>surface_depth: 3M<br>desc: 서산 지곡"," build_data:2017-06-13<br>surface_depth: 5M<br>middle_depth: 10M<br>desc: 충남 서산시 부석면 창리"," build_data:2004-07-30<br>surface_depth: 2M<br>desc: 제주시 북제주군 한경면 고산리 차귀도잠수함"," build_data:2008-07-13<br>surface_depth: 5M<br>middle_depth: 15M<br>bottom_depth: 25M<br>desc: 강원도 양양군 수산항"," build_data:2005-11-02<br>surface_depth: 5M<br>desc: 전남 여수시 가막만 지선"," build_data:2008-11-04<br>surface_depth: 5M<br>middle_depth: 20M<br>bottom_depth: 30M<br>desc: "," build_data:2014-12-30<br>surface_depth: 2M<br>middle_depth: 6M<br>desc: 완도 가교 완도군청 설치."," build_data:2024-05-09<br>surface_depth: 5M<br>desc: "," build_data:2009-04-16<br>surface_depth: 3.5M<br>desc: 전남 완도군 금일읍 충동리"," build_data:2008-07-24<br>surface_depth: 5M<br>desc: 완도 노화도"," build_data:2024-05-13<br>surface_depth: 3M<br>desc: "," build_data:2014-12-30<br>surface_depth: 2M<br>middle_depth: 6M<br>desc: 완도 동백 완도군청 설치."," build_data:2016-03-29<br>surface_depth: 2M<br>middle_depth: 6M<br>desc: 전라남도 완도군 완도군청 설치"," build_data:2024-05-13<br>surface_depth: 3M<br>desc: "," build_data:2017-06-13<br>surface_depth: 2.5M<br>desc: 완도군 금일읍 일정리"," build_data:2005-10-25<br>surface_depth: 3M<br>desc: 전라남도 완도군 청산면 도락리"," build_data:2005-10-05<br>surface_depth: 3M<br>desc: 관측소 위치에 맞는 이름현행화를 위하여 [보령 효자도]를 [태안 고남]으로 변경합니다."," build_data:2020-05-28<br>surface_depth: 6M<br>middle_depth: 12M<br>desc: 태안군청 신규관측소"," build_data:2020-05-28<br>surface_depth: 1.5M<br>middle_depth: 2.5M<br>desc: 태안군청 신규관측소"," build_data:2008-07-23<br>surface_depth: 5M<br>middle_depth: 8M<br>desc: 통영 비산도"," build_data:2005-11-21<br>surface_depth: 5M<br>desc: 경상남도 통영시 사량면 양지리"," build_data:2019-03-28<br>surface_depth: 5M<br>desc: "," build_data:2005-10-26<br>surface_depth: 5M<br>desc: 경남 통영시 산양면 영운리 양식환경연구소 ..."," build_data:2004-12-24<br>surface_depth: 4M<br>middle_depth: 9M<br>desc: 통영시 산양읍 풍화리 경은수산"," build_data:2005-09-23<br>surface_depth: 5M<br>middle_depth: 10M<br>bottom_depth: 15M<br>desc: 경남 통영시 산양읍 저림리 성지수산"," build_data:2005-10-07<br>surface_depth: 5M<br>desc: 관측소 위치에 맞는 이름현행화를 위하여 [진도 회동]을 [해남 임하]로 관측소명을 변경합니다."," build_data:2015-12-11<br>surface_depth: 1M<br>middle_depth: 3M<br>desc: 전라남도 해남군 전라남도 해양수산과학원 설치"]

let clusterer; // 기존 클러스터를 저장할 변수
let allMarkers = []; // 기존 마커들을 저장할 배열

async function initMap() {

    const { Map } = await google.maps.importLibrary("maps");
    const { AdvancedMarkerElement } = await google.maps.importLibrary("marker");


    map  = new Map(document.getElementById('un7map'), {
        mapId: "",
        center: center,
        zoom: 16,
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


//   addMarkerClusterer(locations, labels, contents)

}

//페이지 새로고침 없이 함수만 호출하고 싶을 때
window.addEventListener("message", (event) => {
    // 보안을 위해 event.origin 체크 권장
  //  if (event.origin !== "http://192.168.35.107:8080/") return;
    try {
        const data = JSON.parse(event.data);
        if (data.action === "FLY_TO") {
            console.log("좌표 이동:", data.target.lat, data.target.lng);
            // 여기에 지도 이동 로직 작성 (예: Leaflet, Google Maps 등)
            smoothFlyTo({lat:data.target.lat, lng:data.target.lng})
        }
    } catch (e) {
        console.error("메시지 파싱 에러:", e);
    }
}, false);


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

    // 1. 기존 클러스터가 있다면 지도에서 제거하고 마커 클리어
    if (clusterer) {
        clusterer.clearMarkers(); // 클러스터러 내부 마커 제거
    }

    // 2. 기존 마커들이 지도에 표시되고 있다면 모두 제거
    allMarkers.forEach(marker => marker.setMap(null));
    allMarkers = [];


    const markers = locations.map((position, i) => {

        const label = labels[i];
        const pinGlyph = new google.maps.marker.PinElement({
          glyph: label,
          glyphColor: "white",
        });

        const marker =new google.maps.Marker({
          position,
          map: map,
          content: pinGlyph.element,
        });

        marker.addListener("mouseover", () => {

         infoWindow.setContent(labels[i] + "<br>" + contents[i]);
         infoWindow.open(map, marker);

        });

        marker.addListener("mouseout", () => {
          infoWindow.close();
        });

        // markers can only be keyboard focusable when they have click listeners
        // open info window when marker is clicked
        marker.addListener("click", () => {

        });


        return marker;

    });

        // 4. 전역 배열에 저장 (나중에 지우기 위함)
    allMarkers = markers;

    // 5. 새로운 클러스터러 생성 및 전역 변수 할당
    clusterer = new markerClusterer.MarkerClusterer({ map, markers });

}

initMap();

