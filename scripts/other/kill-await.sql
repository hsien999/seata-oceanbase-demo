SELECT sid, serial#, username, osuser
FROM v$session
where sid = (select session_id from v$locked_object);
ALTER SYSTEM KILL SESSION '66, 7'