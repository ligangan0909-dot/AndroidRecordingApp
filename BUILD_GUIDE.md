# Android录音应用 - 构建指南

本文档详细说明如何构建和生成可运行的APK文件。

## 前置要求

### 必需软件
1. **Android Studio** (推荐版本：Flamingo 2022.2.1或更高)
   - 下载地址：https://developer.android.com/studio

2. **JDK** (Java Development Kit 8或更高)
   - Android Studio通常自带JDK
   - 或从 https://adoptium.net/ 下载

3. **Android SDK**
   - API Level 23 (Android 6.0) - 最低要求
   - API Level 33 (Android 13) - 目标版本
   - Android Studio会自动下载所需SDK

## 构建步骤

### 方法一：使用Android Studio（推荐）

#### 1. 导入项目
```
1. 启动Android Studio
2. 选择 "Open an Existing Project"
3. 导航到 AndroidRecordingApp 目录
4. 点击 "OK"
```

#### 2. 同步项目
```
1. 等待Gradle自动同步
2. 如果没有自动同步，点击 File -> Sync Project with Gradle Files
3. 首次同步会下载依赖，需要几分钟时间
```

#### 3. 配置SDK
```
1. 打开 File -> Project Structure
2. 在 SDK Location 中确认Android SDK路径
3. 确保已安装 API 23 和 API 33
4. 如未安装，点击 "Edit" 下载所需SDK
```

#### 4. 构建APK
```
1. 点击菜单 Build -> Build Bundle(s) / APK(s) -> Build APK(s)
2. 等待构建完成（首次构建可能需要5-10分钟）
3. 构建成功后会显示通知
4. 点击通知中的 "locate" 查看APK位置
```

#### 5. APK位置
```
app/build/outputs/apk/debug/app-debug.apk
```

### 方法二：使用命令行

#### 1. 准备环境
```bash
# 确保JAVA_HOME已设置
echo $JAVA_HOME  # Linux/Mac
echo %JAVA_HOME%  # Windows

# 如未设置，添加到环境变量
export JAVA_HOME=/path/to/jdk  # Linux/Mac
set JAVA_HOME=C:\path\to\jdk   # Windows
```

#### 2. 赋予执行权限（仅Linux/Mac）
```bash
cd AndroidRecordingApp
chmod +x gradlew
```

#### 3. 构建Debug APK
```bash
# Linux/Mac
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug
```

#### 4. 构建Release APK（需要签名）
```bash
# Linux/Mac
./gradlew assembleRelease

# Windows
gradlew.bat assembleRelease
```

#### 5. 查看构建输出
```
Debug APK: app/build/outputs/apk/debug/app-debug.apk
Release APK: app/build/outputs/apk/release/app-release-unsigned.apk
```

## 安装APK

### 使用ADB安装

#### 1. 启用USB调试
```
在Android设备上：
1. 进入 设置 -> 关于手机
2. 连续点击 "版本号" 7次启用开发者选项
3. 返回 设置 -> 开发者选项
4. 启用 "USB调试"
```

#### 2. 连接设备
```bash
# 连接设备到电脑
# 检查设备是否连接
adb devices

# 应该显示类似：
# List of devices attached
# XXXXXXXXXX    device
```

#### 3. 安装APK
```bash
# 安装Debug版本
adb install app/build/outputs/apk/debug/app-debug.apk

# 如果已安装，使用-r参数重新安装
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 直接安装到设备

#### 方法1：通过文件管理器
```
1. 将APK文件复制到设备
2. 使用文件管理器打开APK
3. 点击安装（需要允许安装未知来源应用）
```

#### 方法2：通过Android Studio
```
1. 连接设备
2. 点击工具栏的 "Run" 按钮（绿色三角形）
3. 选择目标设备
4. 应用会自动安装并启动
```

## 常见问题

### 1. Gradle同步失败
**问题**：无法下载依赖
**解决**：
```bash
# 清理项目
./gradlew clean

# 使用代理（如果在中国）
# 在gradle.properties中添加：
systemProp.http.proxyHost=127.0.0.1
systemProp.http.proxyPort=7890
systemProp.https.proxyHost=127.0.0.1
systemProp.https.proxyPort=7890
```

### 2. SDK未找到
**问题**：SDK location not found
**解决**：
```
1. 打开 local.properties（如不存在则创建）
2. 添加：sdk.dir=/path/to/Android/Sdk
3. 路径示例：
   - Windows: C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
   - Mac: /Users/YourName/Library/Android/sdk
   - Linux: /home/YourName/Android/Sdk
```

### 3. 构建内存不足
**问题**：Out of memory error
**解决**：
在 gradle.properties 中增加内存：
```
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
```

### 4. 安装失败
**问题**：INSTALL_FAILED_UPDATE_INCOMPATIBLE
**解决**：
```bash
# 先卸载旧版本
adb uninstall com.example.recordingapp

# 再重新安装
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 5. 权限问题
**问题**：Permission denied
**解决**：
```bash
# Linux/Mac
chmod +x gradlew

# 或使用sudo
sudo ./gradlew assembleDebug
```

## 签名Release APK

### 1. 生成密钥库
```bash
keytool -genkey -v -keystore my-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-key-alias
```

### 2. 配置签名
在 app/build.gradle.kts 中添加：
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("my-release-key.jks")
            storePassword = "your-password"
            keyAlias = "my-key-alias"
            keyPassword = "your-password"
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ...
        }
    }
}
```

### 3. 构建签名APK
```bash
./gradlew assembleRelease
```

## 验证APK

### 检查APK信息
```bash
# 查看APK内容
aapt dump badging app/build/outputs/apk/debug/app-debug.apk

# 查看签名信息
jarsigner -verify -verbose -certs app-debug.apk
```

### 测试APK
```bash
# 安装并启动
adb install -r app-debug.apk
adb shell am start -n com.example.recordingapp/.MainActivity
```

## 优化建议

### 减小APK大小
1. 启用代码混淆（ProGuard）
2. 移除未使用的资源
3. 使用WebP格式图片
4. 启用资源压缩

### 提高构建速度
1. 启用Gradle缓存
2. 使用并行构建
3. 增加Gradle内存
4. 使用增量编译

## 下一步

构建成功后，您可以：
1. 在真实设备上测试应用
2. 使用Android Profiler分析性能
3. 集成DeepSeek API实现转写功能
4. 发布到Google Play商店

## 技术支持

如遇到问题：
1. 查看Android Studio的Build输出
2. 检查logcat日志
3. 参考官方文档：https://developer.android.com
