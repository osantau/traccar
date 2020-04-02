-- tc_lots only_one_running
CREATE OR REPLACE FUNCTION public.tc_lots_only_one_active()
  RETURNS trigger AS
$BODY$
declare 
  vId integer;
BEGIN
 SELECT id from tc_lots WHERE running='Y' and started is null and ended is null into vId  ;
 UPDATE TC_LOTS SET running = 'N', updated=now(), started=(SELECT min(created) FROM tc_labels WHERE lotid = vId), ended =(SELECT max(created) FROM tc_labels WHERE lotid = vId)  WHERE id != NEW.id AND running !='N' and started is null and ended is null;
 NEW.running='Y';
RETURN NEW;
END;$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
-- ALTER FUNCTION public.tc_lots_only_one_active()  OWNER TO traccar;
  
  CREATE TRIGGER trg_lots
  BEFORE INSERT
  ON public.tc_lots
  FOR EACH ROW
  EXECUTE PROCEDURE public.tc_lots_only_one_active();

-- Function: public.mark_if_there_are_many()

-- DROP FUNCTION public.mark_if_there_are_many();

CREATE OR REPLACE FUNCTION public.mark_if_there_are_many()
  RETURNS trigger AS
$BODY$
declare 
	v_count integer=0;
	v_total integer=0;
	v_errors integer=0;
	v_repeats integer=0;
	v_viables integer=0;
begin  
SELECT count(*) FROM tc_labels where label = NEW.label into v_count; 
if (v_count >= 1)  then 
   update tc_labels set repeated='Y' where label=NEW.label;
   NEW.repeated='Y';
end if;
if (NEW.label = 'ERROR') then 
 NEW.error ='Y';
end if; 

--udpate lot information
SELECT count(*)+1 FROM tc_labels where lotid= NEW.lotid into v_total;
SELECT count(*) FROM tc_labels where lotid= NEW.lotid AND error='Y' into v_errors; IF (NEW.error='Y') THEN v_errors = v_errors  + 1; END IF;
SELECT count(*)  FROM tc_labels where lotid= NEW.lotid AND repeated='Y'  into v_repeats; IF (NEW.repeated='Y') THEN v_repeats = v_repeats  + 1; END IF;
SELECT count(*)  FROM tc_labels where lotid= NEW.lotid AND repeated in ('N') and error='N'  into v_viables; IF (NEW.repeated='N' AND NEW.error ='N') THEN v_viables = v_viables  + 1; END IF;


UPDATE tc_lots set  attributes = '{ "total" : '||v_total||', "errors" : '||v_errors||', "repeats" : '||v_repeats||', "viables" : '||v_viables||' }' where id = NEW.lotid;
return new;
end;$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

CREATE TRIGGER trg_labels
  BEFORE INSERT
  ON public.tc_labels
  FOR EACH ROW
  EXECUTE PROCEDURE public.mark_if_there_are_many();
--adaugare device care reprezinta camera care transmite eticheta
INSERT INTO public.tc_devices( name, uniqueid) VALUES ('label_cam','label_cam') ON CONFLICT DO NOTHING;
INSERT INTO public.tc_user_device(
            userid, deviceid)
    VALUES (1,(select id from tc_devices where uniqueid = 'label_cam'));
