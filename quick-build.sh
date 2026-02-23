#!/bin/bash

# Android录音应用 - 快速构建脚本
# 用于Linux/Mac系统

echo "=========================================="
echo "Android录音应用 - 快速构建"
echo "=========================================="
echo ""

# 检查Java环境
if [ -z "$JAVA_HOME" ]; then
    echo "警告: JAVA_HOME未设置"
    echo "尝试查找Java..."
    if command -v java &> /dev/null; then
        echo "✓ 找到Java命令"
    else
        echo "✗ 未找到Java，请先安装JDK"
        exit 1
    fi
else
    echo "✓ JAVA_HOME: $JAVA_HOME"
fi

echo ""

# 赋予gradlew执行权限
echo "设置gradlew执行权限..."
chmod +x gradlew
echo "✓ 完成"

echo ""

# 清理项目
echo "清理项目..."
./gradlew clean
echo "✓ 完成"

echo ""

# 构建Debug APK
echo "开始构建Debug APK..."
echo "（首次构建可能需要几分钟下载依赖）"
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "✓ 构建成功！"
    echo "=========================================="
    echo ""
    echo "APK位置："
    echo "app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    
    # 检查是否有连接的设备
    if command -v adb &> /dev/null; then
        DEVICES=$(adb devices | grep -w "device" | wc -l)
        if [ $DEVICES -gt 0 ]; then
            echo "检测到已连接的Android设备"
            read -p "是否立即安装APK？(y/n) " -n 1 -r
            echo ""
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                echo "正在安装..."
                adb install -r app/build/outputs/apk/debug/app-debug.apk
                if [ $? -eq 0 ]; then
                    echo "✓ 安装成功！"
                    echo ""
                    read -p "是否启动应用？(y/n) " -n 1 -r
                    echo ""
                    if [[ $REPLY =~ ^[Yy]$ ]]; then
                        adb shell am start -n com.example.recordingapp/.MainActivity
                        echo "✓ 应用已启动"
                    fi
                fi
            fi
        fi
    fi
else
    echo ""
    echo "=========================================="
    echo "✗ 构建失败"
    echo "=========================================="
    echo ""
    echo "请检查错误信息并参考BUILD_GUIDE.md"
    exit 1
fi
