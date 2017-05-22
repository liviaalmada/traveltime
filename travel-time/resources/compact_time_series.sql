--Includ all edges -- OLD
CREATE TABLE COMPACT_TIME_SERIES AS 
SELECT EDGE_ID, floor((DATE_PART('HOUR', START_TIME)* 60 + DATE_PART('MINUTES', START_TIME))/15) AS TIME_INTERVAL, TRAVEL_TIME, avg_speed_ms
FROM TRAJECTORY_EDGES, roads_experiments
where avg_speed_ms <=  maxspeed_forward and id=edge_id
ORDER BY 1,2;

-- Import map-matching to speed_observations table
psql -U postgres 
-h localhost  
-d taxisimples 
-c "COPY speed_observations FROM '/home/livia/git/speed-estimation/speed.estimation/map-matching-junho' delimiter ',' csv;"

-- Insert edges - NEW
insert into COMPACT_TIME_SERIES 
SELECT edge_id_gh, floor((DATE_PART('HOUR', START_TIME)* 60 + DATE_PART('MINUTES', START_TIME))/15) AS TIME_INTERVAL, avg_speed_ms, from_node_gh, to_node_gh
FROM speed_observations
ORDER BY 1,2;

select count(*), edge_id, time_interval from COMPACT_TIME_SERIES group by edge_id, time_interval order by count(*) desc limit 100;

-- Include top 1000

create table MOST_USED_TIME_SERIES as select * from COMPACT_TIME_SERIES WHERE edge_id in 
(select edge_id from COMPACT_TIME_SERIES group by edge_id order by count(*) desc limit 1000)