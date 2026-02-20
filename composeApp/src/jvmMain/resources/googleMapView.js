let map;
let infoWindow;
let toggleBtn = false;

let center = { lat: 37.385852, lng: 126.934515 };
//let locations = [{ lat: 37.799, lng: 128.9492 },{ lat: 34.7851, lng: 128.5664 },{ lat: 34.8038, lng: 128.7094 },{ lat: 38.3681, lng: 128.5239 },{ lat: 34.5049, lng: 127.1228 },{ lat: 35.9607, lng: 129.5497 },{ lat: 35.8168, lng: 126.4422 },{ lat: 35.187, lng: 129.227 },{ lat: 34.8746, lng: 127.9522 },{ lat: 34.7255, lng: 128.0497 },{ lat: 34.7892, lng: 126.3653 },{ lat: 37.9505, lng: 124.7295 },{ lat: 37.3023, lng: 129.3127 },{ lat: 36.8935, lng: 126.3524 },{ lat: 36.6163, lng: 126.3717 },{ lat: 33.3104, lng: 126.164 },{ lat: 38.0808, lng: 128.6998 },{ lat: 34.687, lng: 127.708 },{ lat: 36.5737, lng: 129.437 },{ lat: 34.4347, lng: 126.8083 },{ lat: 34.342, lng: 127.0101 },{ lat: 34.3786, lng: 127.0654 },{ lat: 34.2214, lng: 126.5414 },{ lat: 34.3825, lng: 126.7364 },{ lat: 34.3275, lng: 127.035 },{ lat: 34.3013, lng: 126.768 },{ lat: 34.1636, lng: 126.6269 },{ lat: 34.3674, lng: 126.9941 },{ lat: 34.1698, lng: 126.8547 },{ lat: 36.4158, lng: 126.4333 },{ lat: 36.4808, lng: 126.4202 },{ lat: 36.7123, lng: 126.147 },{ lat: 34.8082, lng: 128.4951 },{ lat: 34.8022, lng: 128.2463 },{ lat: 34.8222, lng: 128.345 },{ lat: 34.7904, lng: 128.4293 },{ lat: 34.8348, lng: 128.3353 },{ lat: 34.7498, lng: 128.4151 },{ lat: 34.6069, lng: 126.2672 },{ lat: 34.4235, lng: 126.4216 }]
//let labels = [ "강릉", "거제 가배", "거제 일운", "고성 가진", "고흥 소록도", "구룡포 하정", "군산 신시도", "기장", "남해 강진", "남해 미조", "목포", "백령도", "삼척", "서산 지곡", "서산 창리", "서제주", "양양", "여수 신월", "영덕", "완도 가교", "완도 감목", "완도 금일", "완도 노화도", "완도 대창", "완도 동백", "완도 망남", "완도 백도", "완도 일정", "완도 청산", "태안 고남", "태안 대야도", "태안 파도리", "통영 비산도", "통영 사량", "통영 수월", "통영 영운", "통영 풍화", "통영 학림", "해남 임하", "해남 화산" ]

async function initMap() {

    const { Map } = await google.maps.importLibrary("maps");
    const { AdvancedMarkerElement } = await google.maps.importLibrary("marker");


//AdvancedMarkerElement를 사용하려면 Map 객체를 생성할 때 반드시 mapId 옵션이 포함되어야 합니다.
    map  = new Map(document.getElementById('un7map'), {
        center: center,
        zoom: 10,
        mapId: "UN7_MAP_ID"
    });

    // Set map options.
    map.setOptions({
        mapTypeControl: true,
        mapTypeId: google.maps.MapTypeId.SATELLITE
    });


    infoWindow = new google.maps.InfoWindow({
        content: "",
        disableAutoPan: true,
        headerDisabled: true // 헤더와 닫기 버튼을 숨김
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


//addMarkerClusterer(locations, labels)

}

async function addMarkerClusterer(locations, labels) {

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
          title:labels[i],
        });

        marker.addListener("mouseover", () => {
         infoWindow.setContent(labels[i] + " " + position.lat + ", " + position.lng);
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




    new markerClusterer.MarkerClusterer({ map, markers });


}

initMap();

