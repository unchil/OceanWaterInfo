// mapWorker.js
self.onmessage = async function (e) {
  const arrayBuffer = e.data.buffer;
  const grade =  e.data.grade

  try {
    // 1. ArrayBuffer를 Text로 디코딩 (Worker 스레드에서 수행)
    const decoder = new TextDecoder("utf-8");
    const jsonString = decoder.decode(arrayBuffer);

    // 2. Heavy한 JSON.parse() 실행
    const geoJsonData = JSON.parse(jsonString);

    // (선택) 3. 필요시 여기서 좌표 전처리나 필터링 로직을 추가하여 데이터 크기를 추가로 줄일 수 있습니다.
    // const processedData = processCoordinates(geoJsonData);

    // 4. 파싱 완료된 데이터를 메인 스레드로 전달
    self.postMessage({
      status: "success",
      data: { geoJson: geoJsonData, grade:grade}
    });

  } catch (error) {
    self.postMessage({
      status: "error",
      error: error.message
    });
  }
};