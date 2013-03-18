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



enable tomcat compression


install speed_mon
http://www.turnkeylinux.org/forum/support/20120514/how-do-i-install-modpagespeed

















todo
====
an email when scheduled movie is out
help
tvshows change icons, add shceduled state

