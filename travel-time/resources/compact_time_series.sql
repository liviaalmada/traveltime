-- Import map-matching to speed_observations table 
psql -U postgres 
-h localhost  
-d taxisimples 
-c "COPY speed_observations FROM '/home/livia/git/speed-estimation/speed.estimation/map-matching-junho' delimiter ',' csv;"
-- Importar pra tabel onde o tempo é um bigint e depois copiar para uma outra onde o tempo é um timestamp
-- Insert edges - NEW
create table COMPACT_TIME_SERIES_june_hour as
SELECT edge_id_gh as edge_id, floor((DATE_PART('HOUR', START_TIME)* 60 + DATE_PART('MINUTES', START_TIME))/60) AS TIME_INTERVAL, avg_speed_ms
FROM speed_observations
ORDER BY 1,2;

select count(*), edge_id, time_interval from COMPACT_TIME_SERIES group by edge_id, time_interval order by count(*) desc limit 100;

-- Include top 1000

create table MOST_USED_TIME_SERIES as select * from COMPACT_TIME_SERIES WHERE edge_id in 
(select edge_id from COMPACT_TIME_SERIES group by edge_id order by count(*) desc limit 1000)