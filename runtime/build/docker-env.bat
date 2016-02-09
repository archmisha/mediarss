REM SET DOCKER_TLS_VERIFY=1
REM SET DOCKER_HOST=tcp://192.168.99.100:2376
REM SET DOCKER_CERT_PATH=C:\Users\mishad\.docker\machine\machines\default
REM SET DOCKER_MACHINE_NAME=default
REM Run this command to configure your shell: 
FOR /f "tokens=*" %%i IN ('"C:\Program Files\Docker Toolbox\docker-machine.exe" env -shell cmd default') DO %%i
