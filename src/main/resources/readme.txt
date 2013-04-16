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

 after adding a movie and its already available make it be sorted to top?

 Wrong/not existing subtitles - no eng default
 subcenter

- update schedule when tracked shows updated
 - Stop searching in the middle of the search - Stuck everything! strange red line
 - Tracked TV shows - table looks bad

movies mark all as viewed

when tracked show becomes ended - notify somehow to the user in the ui? (so he can remove it manually) - dont remove it for him
   cuz maybe he still not downloaded some episode from that show with the rss

showlistdownloader runs tooo long - why?

can improve the show name matching algo speed to reduce from 1 second?
when doing action shown session timeout dialog but when clickin tab redirected to login wothout message


what if shceduked an old movie but no hd torrent found - maybe will be scheduled for ever cuz there is no hd movie likae that....
if found a movie wonce with 2 torrents. later when adding this movie again to some user, maybe now there are more torrent and better - we will not download them..

search should not go to to internet
when searching in torrentz, how to know that the result are what we need and not something else, need to verify somehow (house vs housewifes... the right season got back
    anatomy grey seaosn 8 vs s08 vs season 8 720p....)
    same verification needed in pirate bay
    also partial seasons: Greys Anatomy Season 8 Episode 1-22

search results  hover text too long
save all jobs run history not only the last one
make all tooltips be one line

handle the case of a movie not out yet: Iron Man: Rise of Technovore (Video 2013) then there are no viewers yet and no point printing the warning


in search hold on each show when it was being downladed scheduled last and if ended dont download if already was downloaded
the only problem is the shows that not ended and not tracked and searching then will redownload schedule again
   if the last episode scheduled for after the search date - its ok no nneed to redownload schedule we are up to date enough
   if the last episdoe schedule was before the search date - problem not up to date - must go to internet
when show becomes ended - download its full shchedule just in case so wont be any paar. and mark the dnload date
what if the tracked shows schedule download job was broken and next time it runs we skipped an episode?

about your schedule in the site i think it would be better user experience if the user could choose time zone for the series release.

add kickass torrents parser and add it to torrentz parsing like piratebay - to check need to be able to download how i met your mother s02.e03

how to prevent when searching house from finding house of lies or house of cards
what if user was offline for a long time, it needs to download for him the whole delta not only the last episode

need job to download full seasons

