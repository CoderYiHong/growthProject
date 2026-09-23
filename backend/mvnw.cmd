@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup script for Windows
@REM ----------------------------------------------------------------------------

@if "%DEBUG%"=="" @echo off
@setlocal

set MAVEN_PROJECTBASEDIR=%CD%
if not "%MAVEN_PROJECTBASEDIR%"=="" goto endDetectBaseDir

set "MAVEN_PROJECTBASEDIR=%~dp0"
:endDetectBaseDir

set "WRAPPER_DIR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper"
set "WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar"
set "WRAPPER_PROPERTIES=%WRAPPER_DIR%\maven-wrapper.properties"

if not exist "%WRAPPER_JAR%" (
    echo Downloading Maven Wrapper...
    powershell -Command "& { Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.1/maven-wrapper-3.3.1.jar' -OutFile '%WRAPPER_JAR%' }"
    if not exist "%WRAPPER_JAR%" (
        echo ERROR: Failed to download Maven Wrapper jar
        exit /b 1
    )
)

set "MAVEN_OPTS=-Xmx512m"
set "MAVEN_CMD_LINE_ARGS=%*"

"%JAVA_HOME%\bin\java.exe" ^
  -classpath "%WRAPPER_JAR%" ^
  -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" ^
  -Dwrapper.properties="%WRAPPER_PROPERTIES%" ^
  org.apache.maven.wrapper.MavenWrapperMain ^
  %MAVEN_CMD_LINE_ARGS%

@endlocal
