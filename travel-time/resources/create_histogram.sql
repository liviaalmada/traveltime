-- Function: public.create_histogram(integer, timestamp without time zone, timestamp without time zone)

-- DROP FUNCTION public.create_histogram(integer, timestamp without time zone, timestamp without time zone);

-- Function to create statistics per interval time with lenght interval_size from start_dt to end_dt in each edge.
CREATE OR REPLACE FUNCTION public.create_histogram(
    interval_size integer,
    start_dt timestamp without time zone,
    end_dt timestamp without time zone)
  RETURNS integer AS
$BODY$
DECLARE
	date_time timestamp;
	hist_id integer;
	offset_int varchar;   
BEGIN
	insert into histogram (interval_length) values(interval_size);
	select max(id) from histogram into hist_id;
	date_time = start_dt;
	offset_int = '' || interval_size || ' minutes';
	while date_time < end_dt loop
		
		insert into tt_histograms (  start_dt, end_dt, avg, std, count, sum, id_hist, edge_id) 
			select date_time, 
			date_time + offset_int::interval, 
			avg(travel_time) as avg, 
			sqrt(avg(travel_time*travel_time) - avg(travel_time)*avg(travel_time)) as std, 
			count(*) as count,
			sum(travel_time) as sum,
			hist_id,
			edge_id
			from trajectory_edges
			where start_time >= date_time and start_time < date_time + offset_int::interval
			group by edge_id;


		 
		date_time = date_time + offset_int::interval;
		RAISE NOTICE ' %', date_time;  
	end loop;
	
	RETURN 0;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.create_histogram(integer, timestamp without time zone, timestamp without time zone)
  OWNER TO postgres;
