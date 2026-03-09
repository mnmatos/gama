@echo off
REM Build GAMA Package with JavaFX included

echo =====================================
echo   GAMA Package Builder with JavaFX
echo =====================================
echo.

where jpackage >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] jpackage nao encontrado no PATH.
    exit /b 1
)

where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Maven nao encontrado no PATH.
    exit /b 1
)

echo [1/4] Compilando o projeto com Maven...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo [ERROR] Erro ao compilar o projeto.
    exit /b 1
)
echo [OK] Projeto compilado!
echo.

echo [2/4] Criando pacote base com jpackage...
if not exist "dist" mkdir dist

REM Create package WITHOUT --add-modules to avoid jlink error
jpackage --input target --name GAMA --main-jar digital-library-api-1.2.2-jar-with-dependencies.jar --main-class com.digitallib.MainFX --type app-image --dest dist --java-options "--enable-preview" --icon book_ico.ico --win-console

if %errorlevel% neq 0 (
    echo [ERROR] Erro ao criar o pacote.
    exit /b 1
)
echo [OK] Pacote base criado!
echo.

echo [3/4] Baixando e adicionando JavaFX...
echo [INFO] Para adicionar JavaFX automaticamente, use o script PowerShell:
echo        .\build-package.ps1
echo.
echo [INFO] Ou baixe manualmente de:
echo        https://gluonhq.com/products/javafx/
echo        E copie os arquivos da pasta lib para: dist\GAMA\runtime\lib
echo.

echo [4/4] Resumo
echo [OK] Pacote criado em dist\GAMA
echo.
echo IMPORTANTE: Para funcionar, voce precisa adicionar JavaFX ao pacote.
echo Use o script PowerShell para fazer isso automaticamente:
echo   .\build-package.ps1
echo.
pause

