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


sudo apt-get update
sudo apt-get install build-essential
sudo apt-get install apache2-dev

http://edwardaux.wordpress.com/2006/09/20/3/#comment-487
http://archive.apache.org/dist/jakarta/tomcat-connectors/jk/source/jk-1.2.14/ - didnt work
http://tomcat.apache.org/download-connectors.cgi


setup apache2 to forward port 80 to tomcat 8080
-----------------------------------------------
http://blog.eventloud.com/2011/04/20/how-to-setup-apache-forwarding-to-tomcat-run-tomcat-on-port-80/
commands:
a2enmod proxy
a2enmod proxy_http

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


change timezone on the machine
====
http://www.thegeekstuff.com/2010/09/change-timezone-in-linux/

creating a folder with all the jars of the webapp - lib-media-rss and mapping it in catalina.properties to lower the size of the war file copied each time



last time added -XX:+UseCodeCacheFlushing to solve this:
Java HotSpot(TM) Client VM warning: CodeCache is full. Compiler has been disabled.
Java HotSpot(TM) Client VM warning: Try increasing the code cache size using -XX:ReservedCodeCacheSize=
Code Cache  [0xb5326000, 0xb5a7e000, 0xb7326000)
 total_blobs=3911 nmethods=3719 adapters=124 free_code_cache=25254Kb largest_free_block=25854656

