package com.unchil.oceanwaterinfo


fun List<CoastalFloodingGeo>.toGeoJsonObject(info:Pair<String, String> ):String {


    if (this.isEmpty()) return """{"type": "FeatureCollection", "features": []}"""

    // 1. 결과값의 크기를 예측하여 StringBuilder 초기 용량 설정 (메모리 재할당 방지)
    // 100MB 데이터 기준, 대략적인 크기 지정
    val sb = StringBuilder(this.size * 256)

    sb.append("""{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"MultiPolygon","coordinates":[""")

    var isFirstPolygon = true

    for (item in this) {
        val rawGeom = item.geom
        if (rawGeom.isBlank()) continue

        // 2. 정규식(Regex) 대신 단순 Index 조작으로 데이터 추출
        // "MULTIPOLYGON ((...))" 에서 숫자 부분만 찾기 위해 괄호 밖 제거
        val startIdx = rawGeom.indexOf('(')
        val endIdx = rawGeom.lastIndexOf(')')
        if (startIdx == -1 || endIdx == -1) continue

        val content = rawGeom.substring(startIdx, endIdx)
            .replace("(", "")
            .replace(")", "")
            .trim()

        // 3. split(",") 대신 문자열을 직접 순회하며 숫자 추출 (GC 압력 감소)
        val coordinatePairs = content.split(",")
        val ring = mutableListOf<String>() // [lng,lat] 문자열 형태로 임시 저장

        for (pair in coordinatePairs) {
            val trimmedPair = pair.trim()
            val spaceIdx = trimmedPair.indexOf(' ')
            if (spaceIdx != -1) {
                val lng = trimmedPair.substring(0, spaceIdx)
                val lat = trimmedPair.substring(spaceIdx + 1)
                ring.add("[$lng,$lat]")
            }
        }

        // --- 검증 로직 ---
        if (ring.size < 3) continue

        // 폐쇄성 보장 (첫 점과 끝 점이 다르면 추가)
        if (ring.first() != ring.last()) {
            ring.add(ring.first())
        }

        if (ring.size < 4) continue

        // 4. StringBuilder에 직접 쓰기 (중간 리스트 구조화 생략)
        if (!isFirstPolygon) sb.append(",")

        sb.append("[[") // MultiPolygon의 하위 Polygon 시작
        sb.append(ring.joinToString(","))
        sb.append("]]")

        isFirstPolygon = false
    }

    sb.append("""]},"properties":{"name":"MultiPolygon ${info.first}_${info.second}"}}]}""")

    return sb.toString()
}