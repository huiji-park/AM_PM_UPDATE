create database am_pm;

create user 'phoenix'@'localhost' identified by 'phoenix';

grant all privileges on am_pm.* to 'phoenix'@'localhost';

grant all on am_pm.* to 'phoenix'@'localhost';

flush privileges;