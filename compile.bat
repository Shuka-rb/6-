@echo off
REM Compile script for Lab 6 - Multi-threaded Warehouse Management System

set SRC_DIR=src\main\java
set OUT_DIR=target\classes

REM Create output directory
if not exist %OUT_DIR% mkdir %OUT_DIR%

REM Find javac
set JAVAC=javac

REM Compile all Java files
echo Compiling Java sources...
%JAVAC% -d %OUT_DIR% -sourcepath %SRC_DIR% %SRC_DIR%\org\example\model\Product.java
%JAVAC% -d %OUT_DIR% -sourcepath %SRC_DIR% %SRC_DIR%\org\example\dto\Request.java
%JAVAC% -d %OUT_DIR% -sourcepath %SRC_DIR% %SRC_DIR%\org\example\dto\Response.java
%JAVAC% -d %OUT_DIR% -sourcepath %SRC_DIR% %SRC_DIR%\org\example\repository\ProductRepository.java
%JAVAC% -d %OUT_DIR% -sourcepath %SRC_DIR% %SRC_DIR%\org\example\handler\ClientHandler.java
%JAVAC% -d %OUT_DIR% -sourcepath %SRC_DIR% %SRC_DIR%\org\example\client\Client.java
%JAVAC% -d %OUT_DIR% -sourcepath %SRC_DIR% %SRC_DIR%\org\example\server\Server.java
%JAVAC% -d %OUT_DIR% -sourcepath %SRC_DIR% %SRC_DIR%\org\example\Main.java

if %ERRORLEVEL% EQU 0 (
    echo Compilation successful!
    echo.
    echo To run the application:
    echo   java -cp target\classes org.example.Main
) else (
    echo Compilation failed!
)

pause
