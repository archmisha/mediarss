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

need tooltips in show search result

can improve the show name matching algo speed to reduce from 1 second?
when doing action shown session timeout dialog but when clickin tab redirected to login wothout message


what if shceduked an old movie but no hd torrent found - maybe will be scheduled for ever cuz there is no hd movie likae that....
if found a movie wonce with 2 torrents. later when adding this movie again to some user, maybe now there are more torrent and better - we will not download them..

double espidoes - so8e01e02 greys
full seasons download not good
search should not go to to internet
when searching in torrentz, how to know that the result are what we need and not something else, need to verify somehow (house vs housewifes... the right season got back
    anatomy grey seaosn 8 vs s08 vs season 8 720p....)
    same verification needed in pirate bay
    also partial seasons: Greys Anatomy Season 8 Episode 1-22

search results  hover text too long

make all tooltips be one line