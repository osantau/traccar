--adaugare device care reprezinta camera wenglor
INSERT INTO public.tc_devices( name, uniqueid) VALUES ('wenglor_cam','wenglor_cam') ON CONFLICT DO NOTHING;
update public.tc_user_device set deviceid = (select id from tc_devices where uniqueid = 'wenglor_cam') WHERE userid = 1;
