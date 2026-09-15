let map;
let infoWindow;
let toggleBtn = false;
let observatoryDesc;



let center = { lat: 37.385852, lng: 126.934515 };


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
       //     console.log("FLY_TO:", data.target.lat, data.target.lng);
            smoothFlyTo({lat:data.target.lat, lng:data.target.lng})
        }else if (data.action === "ADD_Marker_Clusterer") {
        //    console.log("ADD_Marker_Clusterer:");
            addMarkerClusterer(data.target.locations, data.target.labels, data.target.content)
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

