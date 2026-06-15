@echo off
chcp 65001 >nul

echo.
echo ========================================
echo Sokoban - Очистка проекта
echo ========================================
echo.

cd /d "%~dp0"

echo Удаление папки out...
if exist "out" (
    rmdir /s /q "out"
)

echo.
echo Очистка завершена!
echo.
