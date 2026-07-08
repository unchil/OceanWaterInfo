
document.addEventListener( "DOMContentLoaded", function() {

    const composeContainer = document.getElementById('webmain');
    const script = document.createElement('script');
    script.type = 'application/javascript';
    script.src = 'composeApp.js'; // 실제 파일 경로
    script.onload = function() {
        console.log("composeApp.js 로드 완료!");
    };
    composeContainer.appendChild(script);

}, { once: true });




