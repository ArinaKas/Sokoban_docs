@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo.
echo ========================================
echo Sokoban - Упаковка в JAR
echo ========================================
echo.

cd /d "%~dp0"

REM Проверяем, скомпилирован ли проект
if not exist "out\production\Sokoban" (
    echo Ошибка: проект не скомпилирован!
    echo Сначала запустите build.bat
    exit /b 1
)

REM Создаем папку для JAR если её нет
if not exist "dist" (
    mkdir "dist"
)

REM Удаляем старый JAR если существует
if exist "dist\Sokoban.jar" (
    echo Удаление старого JAR...
    del "dist\Sokoban.jar"
)

REM Создаем манифест
echo Создание манифеста...
if not exist "out\production\Sokoban\META-INF" (
    mkdir "out\production\Sokoban\META-INF"
)
(
    echo Manifest-Version: 1.0
    echo Main-Class: game.SokobanGame
    echo.
) > "out\production\Sokoban\META-INF\MANIFEST.MF"

REM Упаковываем в JAR
echo Упаковка в JAR...
jar cvfm "dist\Sokoban.jar" "out\production\Sokoban\META-INF\MANIFEST.MF" -C "out\production\Sokoban" .

if errorlevel 1 (
    echo.
    echo Ошибка при упаковке!
    exit /b 1
)

echo.
echo Успешно! JAR файл создан: dist\Sokoban.jar
echo Запуск: java -jar dist\Sokoban.jar
echo.
pause
