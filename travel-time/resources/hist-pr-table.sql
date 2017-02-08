-- Create table with counts of trajectories per edge grouped by hour. 
-- The count do not consider the day, but group by hour in different days.

create table count_by_edges as
select sum(count) as count, tt.edge_id, date_part('hour', tt.start_dt)
from tt_histograms tt
group by tt.edge_id, date_part('hour', tt.start_dt)
order by  tt.edge_id;

-- Group values of the same travel time and counts the percentage they appears in a interval of one hour.  
-- The count do not consider the day, but group by hour in different days.

﻿create table histogram_pr as select tt.avg, tt.edge_id, date_part('hour', tt.start_dt), sum(tt.count)/c.count as prob
from tt_histograms tt, count_by_edges c
where c.edge_id = tt.edge_id and c.start_dt = date_part('hour', tt.start_dt)
group by tt.edge_id, date_part('hour', tt.start_dt), tt.avg, c.count
order by  date_part('hour', tt.start_dt);
