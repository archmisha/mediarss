@echo off

Setlocal EnableDelayedExpansion

::Change the directory to be the running file directory (workspace)
cd /D %~dp0

IF NOT DEFINED BASE_PATH (
    pushd ..
    set BASE_PATH=!CD!
    popd
)

echo using BASE_PATH as %BASE_PATH%

echo copy configuration to .idea folder
	if Not exist %BASE_PATH%\.idea (
		mkdir %BASE_PATH%\.idea
	)	
	if Not exist %BASE_PATH%\.idea\runConfigurations (
		mkdir %BASE_PATH%\.idea\runConfigurations
	)	
	xcopy runConfigurations %BASE_PATH%\.idea\runConfigurations /Y

echo Done :)
pause