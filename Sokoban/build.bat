@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo.
echo ========================================
echo Sokoban - Компиляция проекта
echo ========================================
echo.

cd /d "%~dp0"

if not exist "src" (
    echo Ошибка: папка src не найдена!
    exit /b 1
)

echo Очистка папки out...
if exist "out" (
    rmdir /s /q "out"
    if errorlevel 1 (
        echo Ошибка при удалении папки out
        exit /b 1
    )
)

echo Создание папки out\production\Sokoban...
mkdir "out\production\Sokoban"
if errorlevel 1 (
    echo Ошибка при создании папки
    exit /b 1
)

echo Копирование ресурсов...
xcopy "src\resources" "out\production\Sokoban\resources" /E /I /Y >nul
if errorlevel 1 (
    echo Предупреждение: ошибка при копировании ресурсов
)

echo.
echo Компиляция Java файлов...
javac -encoding UTF-8 -d out\production\Sokoban src\game\*.java src\ui\*.java src\entities\*.java src\level\*.java src\score\*.java src\graphics\*.java
if errorlevel 1 (
    echo.
    echo Ошибка компиляции!
    exit /b 1
)

echo.
echo Компиляция успешна!
echo.
