@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup script, version 3.3.2 (Windows)
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
set MAVEN_WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar
set MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties

if not exist "%MAVEN_WRAPPER_JAR%" (
  for /f "tokens=2 delims==" %%a in ('findstr /i "wrapperUrl" "%MAVEN_WRAPPER_PROPERTIES%"') do (
    set WRAPPER_URL=%%a
  )
  if defined WRAPPER_URL (
    curl -fsSL -o "%MAVEN_WRAPPER_JAR%" "%WRAPPER_URL%" 2>nul
    if errorlevel 1 (
      powershell -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%MAVEN_WRAPPER_JAR%'"
    )
  )
)

if defined JAVA_HOME (
  set JAVA_EXECUTABLE=%JAVA_HOME%\bin\java.exe
) else (
  set JAVA_EXECUTABLE=java
)

"%JAVA_EXECUTABLE%" -classpath "%MAVEN_WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
endlocal
