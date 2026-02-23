@echo off
REM Android录音应用 - 快速构建脚本
REM 用于Windows系统

echo ==========================================
echo Android录音应用 - 快速构建
echo ==========================================
echo.

REM 检查Java环境
if "%JAVA_HOME%"=="" (
    echo 警告: JAVA_HOME未设置
    echo 尝试查找Java...
    where java >nul 2>nul
    if %ERRORLEVEL% EQU 0 (
        echo [OK] 找到Java命令
    ) else (
        echo [错误] 未找到Java，请先安装JDK
        pause
        exit /b 1
    )
) else (
    echo [OK] JAVA_HOME: %JAVA_HOME%
)

echo.

REM 清理项目
echo 清理项目...
call gradlew.bat clean
echo [OK] 完成

echo.

REM 构建Debug APK
echo 开始构建Debug APK...
echo （首次构建可能需要几分钟下载依赖）
call gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ==========================================
    echo [OK] 构建成功！
    echo ==========================================
    echo.
    echo APK位置：
    echo app\build\outputs\apk\debug\app-debug.apk
    echo.
    
    REM 检查是否有连接的设备
    where adb >nul 2>nul
    if %ERRORLEVEL% EQU 0 (
        adb devices | find "device" >nul
        if %ERRORLEVEL% EQU 0 (
            echo 检测到已连接的Android设备
            set /p INSTALL="是否立即安装APK？(y/n) "
            if /i "%INSTALL%"=="y" (
                echo 正在安装...
                adb install -r app\build\outputs\apk\debug\app-debug.apk
                if %ERRORLEVEL% EQU 0 (
                    echo [OK] 安装成功！
                    echo.
                    set /p LAUNCH="是否启动应用？(y/n) "
                    if /i "%LAUNCH%"=="y" (
                        adb shell am start -n com.example.recordingapp/.MainActivity
                        echo [OK] 应用已启动
                    )
                )
            )
        )
    )
) else (
    echo.
    echo ==========================================
    echo [错误] 构建失败
    echo ==========================================
    echo.
    echo 请检查错误信息并参考BUILD_GUIDE.md
    pause
    exit /b 1
)

echo.
pause
