DELETE
FROM KHNP_RadioRate
WHERE collectionTime < strftime('%Y-%m-%d %H:%M', 'now', 'localtime', '-2 days');

DELETE
FROM KHNP_ThermalWasteWater
WHERE time < strftime('%Y-%m-%d %H:%M', 'now', 'localtime', '-2 days');

DELETE
FROM KHNP_WasteWater
WHERE time < strftime('%Y-%m-%d %H:%M', 'now', 'localtime', '-2 days');

DELETE
FROM Observation
WHERE obs_datetime < strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime', '-2 days');

DELETE
FROM ObservationKHOA
WHERE obsrvnDt < strftime('%Y-%m-%d %H:%M', 'now', 'localtime', '-2 days');

DELETE
FROM OWQInformation
WHERE rtmWqWtchDtlDt < strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime', '-2 days');


DELETE
FROM SDoT_EnvInfo
WHERE sensing_time < strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime', '-2 days');

DELETE
FROM SDoT_EnvInfo_Gyonggi
WHERE sensing_time < strftime('%Y-%m-%d %H:%M', 'now', 'localtime', '-2 days');

DELETE
FROM TidalCurrentInfoKHOA
WHERE sch_time < strftime('%Y-%m-%d %H:%M', 'now', 'localtime', '-2 days');

DELETE
FROM WaveInfo
WHERE obsrvnDt < strftime('%Y-%m-%d %H:%M', 'now', 'localtime', '-2 days');



VACUUM ;


-- /usr/bin/sqlite3 /Users/unchil/AndroidStudioProjects/OceanWaterInfo/oceanwater.sqlite < /Users/unchil/AndroidStudioProjects/OceanWaterInfo/clear_old_data.sql