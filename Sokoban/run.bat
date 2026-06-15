@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo.
echo ========================================
echo Sokoban - Запуск проекта
echo ========================================
echo.

cd /d "%~dp0"

echo Запуск компиляции...
call build.bat
if errorlevel 1 (
    echo Ошибка при компиляции!
    exit /b 1
)

if not exist "out\production\Sokoban\game\SokobanGame.class" (
    echo Ошибка: SokobanGame.class не найден!
    exit /b 1
)

echo.
echo Запуск игры Sokoban...
echo.

java -cp out\production\Sokoban game.Main
