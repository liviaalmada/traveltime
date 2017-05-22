-- To see how many observation with incorrect speed
-- select  count(*) from trajectory_edges, roads_experiments 
-- where avg_speed_ms <=  maxspeed_forward and id=edge_id

update trajectory_edges 
set avg_speed_ms = r.length_m/(travel_time*1000+1)
from roads_experiments r 
where r.id=edge_id;