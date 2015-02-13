select 'insert into user values(' || ID || ',''' ||
FORMATDATETIME(CREATED, 'yyyy-MM-dd HH:mm:ss') || ''',''' || EMAIL || ''',''' ||
FORMATDATETIME(LAST_LOGIN, 'yyyy-MM-dd HH:mm:ss') ||''','''||
FORMATDATETIME(LAST_MOVIES_FEED_GENERATED, 'yyyy-MM-dd HH:mm:ss') ||''','''||
FORMATDATETIME(LAST_SHOWS_FEED_GENERATED, 'yyyy-MM-dd HH:mm:ss') ||''','''|| t.PASSWORD ||''','''||
SHOW_RSS_PASSWORD ||''','''|| SHOW_RSS_USERNAME ||''',null)'
from user t


insert into user values(1,'2012-12-11 21:15:29','archmisha@gmail.com',1, 'Michael','2012-12-24 21:48:10', '2012-12-26 23:30:27','Dikman','2012-12-26 23:36:08','123456',0, null)

insert into user values(1,'2012-12-11 21:15:29','archmisha@gmail.com','2012-12-24 21:48:10','2012-12-26 23:30:27','2012-12-26 23:36:08','123456','84ad17ad','archmisha',null)
insert into user values(1,'2012-12-11 21:15:29','archmisha@gmail.com',1,'Michael','2012-12-24 21:48:10','2012-12-26 23:30:27','Dikman','2012-12-26 23:36:08','123456',0,null)

insert into user values(1,'2012-12-11 21:15:29','archmisha@gmail.com',1,'2013-01-05 18:09:45','2013-01-05 18:03:55','2013-01-05 18:21:59','123456','84ad17ad','archmisha',null)
insert into user values(2,'2012-12-11 21:26:25','dim4iksh@gmail.com',2,'2012-12-14 19:21:13','2012-12-14 19:22:51','2013-01-05 18:03:13','123456','zrqqmac2','dim4iksh',null)



indexes to be created manually:
@org.hibernate.annotations.Table(appliesTo = "movie_torrentids", indexes = {
		@Index(name = "movie_torrentids_torrentIds_idx", columnNames = {"torrentids"})
})