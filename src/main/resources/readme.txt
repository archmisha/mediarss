hostname: media-rss
password: ASDqwe123
account security code: 8958
city born: gorkii


get version: lsb_release -a
ubuntu 9.04

http://askubuntu.com/questions/48025/what-to-do-when-cant-update-anymore-with-apt-get

http://www.ubuntugeek.com/how-to-install-the-latest-rtorrent-and-libtorrent.html


run updatedb to make locate work

I found the solution.
Add the following line to /etc/ld.so.conf
include /usr/local/lib

Run ldconfig

rtorrent will run fine now!

--------------------
make rtorrent start on boot
configure rtorrent log file




setup apache2 to forward port 80 to tomcat 8080
-----------------------------------------------
http://blog.eventloud.com/2011/04/20/how-to-setup-apache-forwarding-to-tomcat-run-tomcat-on-port-80/
changed /var/www/index.html to redirect to /index.jsp
/etc/apache2/httpd.conf

root@media-rss:~# cat /etc/apache2/httpd.conf
ProxyPass / http://208.115.203.158:8080/media-rss/
ProxyPassReverse / http://208.115.203.158:8080/media-rss/
ProxyPassReverseCookieDomain localhost 208.115.203.158
ProxyPassReverseCookiePath /media-rss /

<Location "/">
Order allow,deny
Allow from all
</Location>
root@media-rss:~#



enable tomcat compressionThe following torrents were not found


install speed_mon
http://www.turnkeylinux.org/forum/support/20120514/how-do-i-install-modpagespeed


change timezone on the machine
====
http://www.thegeekstuff.com/2010/09/change-timezone-in-linux/












todo
====
an email when scheduled movie is out (need an option to disable emails then)
help
tvshows change icons, add shceduled state
instead by imdb id - search by name
icons

 Wrong/not existing subtitles - no eng default
 subcenter

- update schedule when tracked shows updated
 - Stop searching in the middle of the search - Stuck everything! strange red line
 - Tracked TV shows - table looks bad

movies mark all as viewed

when tracked show becomes ended - notify somehow to the user in the ui? (so he can remove it manually) - dont remove it for him
   cuz maybe he still not downloaded some episode from that show with the rss

showlistdownloader runs tooo long - why?

when doing action shown session timeout dialog but when clickin tab redirected to login wothout message

what if shceduked an old movie but no hd torrent found - maybe will be scheduled for ever cuz there is no hd movie likae that....
if found a movie wonce with 2 torrents. later when adding this movie again to some user, maybe now there are more torrent and better - we will not download them..

when searching in torrentz, how to know that the result are what we need and not something else, need to verify somehow (house vs housewifes... the right season got back
    anatomy grey seaosn 8 vs s08 vs season 8 720p....)
    also partial seasons: Greys Anatomy Season 8 Episode 1-22

search results  hover text too long
save all jobs run history not only the last one
make all tooltips be one line

handle the case of a movie not out yet: Iron Man: Rise of Technovore (Video 2013) then there are no viewers yet and no point printing the warning

what if the tracked shows schedule download job was broken and next time it runs we skipped an episode?

about your schedule in the site i think it would be better user experience if the user could choose time zone for the series release.

add kickass torrents parser and add it to torrentz parsing like piratebay - to check need to be able to download how i met your mother s02.e03

how to prevent when searching house from finding house of lies or house of cards

when both jobs run the same time - one locks shows table and the other crashes cuz cant lock and timeout

if a show has ended and some user has it tracked - mark in the ui for him red alert
schedule - if some show has an episode in more than 7 days - maybe should add it too
schedule - if 7 days ahead have only 1 episode or no episdodes, maybe  better to display more days?


still requests such as Pretty Little Liars s02e-1 720p are getting into the log



problemm:
Removing 'The Office US S03E13.HDTV.XviD' cuz a bad match for 'The Office (US) s03e13'
test

[ERROR] TVShowsTorrentEntriesDownloader - Failed retrieving "Homeland s02e06 720p": excuteAPI error: DownloadSubtitles
java.lang.RuntimeException: excuteAPI error: DownloadSubtitles
        at rss.services.SubtitlesServiceImpl.downloadEpisodeSubtitles(SubtitlesServiceImpl.java:143)
        at sun.reflect.GeneratedMethodAccessor73.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
        at java.lang.reflect.Method.invoke(Unknown Source)
        at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:317)
        at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:183)
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:150)
        at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:110)
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:172)
        at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:204)
        at $Proxy53.downloadEpisodeSubtitles(Unknown Source)
        at rss.services.downloader.TVShowsTorrentEntriesDownloader.onTorrentFound(TVShowsTorrentEntriesDownloader.java:133)
        at rss.services.downloader.TVShowsTorrentEntriesDownloader.onTorrentFound(TVShowsTorrentEntriesDownloader.java:38)
        at rss.services.downloader.TorrentEntriesDownloader$1$1.doInTransactionWithoutResult(TorrentEntriesDownloader.java:86)
        at org.springframework.transaction.support.TransactionCallbackWithoutResult.doInTransaction(TransactionCallbackWithoutResult.java:33)
        at org.springframework.transaction.support.TransactionTemplate.execute(TransactionTemplate.java:131)
        at rss.services.downloader.TorrentEntriesDownloader$1.run(TorrentEntriesDownloader.java:63)
        at rss.services.downloader.TorrentEntriesDownloader$1.run(TorrentEntriesDownloader.java:59)
        at rss.util.MultiThreadExecutor$1.run(MultiThreadExecutor.java:20)
        at java.util.concurrent.Executors$RunnableAdapter.call(Unknown Source)
        at java.util.concurrent.FutureTask$Sync.innerRun(Unknown Source)
        at java.util.concurrent.FutureTask.run(Unknown Source)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source)
        at java.lang.Thread.run(Unknown Source)
Caused by: com.googlecode.opensubtitlesjapi.OpenSubtitlesException: excuteAPI error: DownloadSubtitles
        at com.googlecode.opensubtitlesjapi.OpenSubtitlesAPI.executeAPI(OpenSubtitlesAPI.java:198)
        at com.googlecode.opensubtitlesjapi.OpenSubtitlesAPI.download(OpenSubtitlesAPI.java:149)
        at rss.services.SubtitlesServiceImpl.createSubtitles(SubtitlesServiceImpl.java:178)
        at rss.services.SubtitlesServiceImpl.downloadEpisodeSubtitles(SubtitlesServiceImpl.java:134)
        ... 24 more
Caused by: com.googlecode.opensubtitlesjapi.OpenSubtitlesException: 407 Download limit reached
        at com.googlecode.opensubtitlesjapi.OpenSubtitlesAPI.executeAPI(OpenSubtitlesAPI.java:195)
        ... 27 more
