\
@echo off
setlocal enabledelayedexpansion
REM Minimal Gradle "wrapper" bootstrapper (no gradle-wrapper.jar).
REM Downloads the Gradle distribution defined in gradle\wrapper\gradle-wrapper.properties and runs it.

set APP_HOME=%~dp0
set PROPS=%APP_HOME%gradle\wrapper\gradle-wrapper.properties

if not exist "%PROPS%" (
  echo Missing %PROPS%
  exit /b 1
)

for /f "tokens=2 delims==" %%a in ('findstr /b "distributionUrl=" "%PROPS%"') do set DIST_URL=%%a
set DIST_URL=%DIST_URL:\:=%

for %%f in (%DIST_URL%) do set DIST_NAME=%%~nxf
set DIST_DIR=%APP_HOME%.gradle-wrapper\dist
set ZIP_PATH=%DIST_DIR%\%DIST_NAME%
set UNPACK_DIR=%DIST_DIR%\unpacked

if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"
if not exist "%UNPACK_DIR%" mkdir "%UNPACK_DIR%"

if not exist "%ZIP_PATH%" (
  echo Downloading Gradle distribution: %DIST_URL%
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%ZIP_PATH%'" || exit /b 1
)

REM get top directory name
for /f "delims=" %%t in ('powershell -NoProfile -Command ^
  "$z = [IO.Compression.ZipFile]::OpenRead('%ZIP_PATH%'); $e = $z.Entries[0].FullName.Split('/')[0]; $z.Dispose(); $e"') do set TOP_DIR=%%t

if not exist "%UNPACK_DIR%\%TOP_DIR%" (
  echo Unpacking %DIST_NAME%
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "Expand-Archive -Force '%ZIP_PATH%' '%UNPACK_DIR%'" || exit /b 1
)

set GRADLE_BIN=%UNPACK_DIR%\%TOP_DIR%\bin\gradle.bat
if not exist "%GRADLE_BIN%" (
  echo Gradle binary not found at %GRADLE_BIN%
  exit /b 1
)

call "%GRADLE_BIN%" %*
