@ECHO OFF
SET DIR=%~dp0
SET WRAPPER_JAR=%DIR%\gradle\wrapper\gradle-wrapper.jar
IF NOT EXIST "%WRAPPER_JAR%" (
  ECHO Gradle wrapper jar missing. Please run gradle wrapper.
  EXIT /B 1
)
"%JAVA_HOME%\bin\java.exe" -jar "%WRAPPER_JAR%" %*
