# Android录音应用

一个功能完整的Android录音应用，支持录音、播放和管理录音文件。

## 功能特性

### 核心功能
- ✅ 高质量音频录制（44.1kHz, 16-bit PCM, WAV格式）
- ✅ 实时显示录音时长
- ✅ 实时音频电平显示
- ✅ 录音文件播放
- ✅ 录音列表管理
- ✅ 删除录音文件
- ✅ 运行时权限管理
- 🔄 转写功能（预留接口，待集成DeepSeek API）

### 技术特点
- 使用Kotlin语言开发
- 使用AudioRecord API进行音频录制
- 使用Kotlin协程处理异步操作
- ViewBinding简化UI操作
- Material Design设计风格

## 项目结构

```
AndroidRecordingApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/recordingapp/
│   │   │   ├── MainActivity.kt              # 主界面
│   │   │   ├── RecordingActivity.kt         # 录音界面
│   │   │   ├── RecordingListActivity.kt     # 录音列表
│   │   │   ├── audio/
│   │   │   │   ├── AudioRecordManager.kt    # 录音管理器
│   │   │   │   ├── AudioPlayer.kt           # 音频播放器
│   │   │   │   └── WavFileWriter.kt         # WAV文件写入
│   │   │   ├── model/
│   │   │   │   └── Recording.kt             # 录音数据模型
│   │   │   └── utils/
│   │   │       └── PermissionHelper.kt      # 权限管理
│   │   └── res/                             # 资源文件
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## 系统要求

- 最低Android版本：6.0 (API 23)
- 目标Android版本：13 (API 33)
- 编译工具：Android Studio Flamingo或更高版本
- Gradle版本：8.0
- Kotlin版本：1.9.0

## 构建说明

### 1. 环境准备
确保已安装：
- Android Studio
- Android SDK (API 23-33)
- JDK 8或更高版本

### 2. 导入项目
```bash
# 克隆或下载项目
cd AndroidRecordingApp

# 使用Android Studio打开项目
# File -> Open -> 选择AndroidRecordingApp目录
```

### 3. 构建APK

#### 方法一：使用Android Studio
1. 打开项目
2. 点击菜单：Build -> Build Bundle(s) / APK(s) -> Build APK(s)
3. 等待构建完成
4. APK位置：`app/build/outputs/apk/debug/app-debug.apk`

#### 方法二：使用命令行
```bash
# 在项目根目录执行
./gradlew assembleDebug

# Windows系统使用
gradlew.bat assembleDebug

# APK输出位置：app/build/outputs/apk/debug/app-debug.apk
```

### 4. 安装APK
```bash
# 连接Android设备或启动模拟器
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 使用说明

### 录音
1. 打开应用，点击"开始录音"
2. 首次使用需要授予录音权限
3. 录音界面显示实时时长和音频电平
4. 点击"停止录音"保存文件

### 播放和管理
1. 在主界面点击"查看录音列表"
2. 点击"播放"按钮播放录音
3. 点击"删除"按钮删除录音
4. 点击"转写"按钮查看转写功能说明

## 音频规格

- 采样率：44100 Hz
- 声道：单声道（Mono）
- 位深度：16-bit
- 格式：WAV (PCM)
- 存储位置：应用私有目录 `/data/data/com.example.recordingapp/files/recordings/`

## 权限说明

应用需要以下权限：
- `RECORD_AUDIO`：录制音频
- `WRITE_EXTERNAL_STORAGE`（仅Android 9及以下）：存储录音文件

## 未来扩展

### 转写功能集成
项目已预留转写功能接口，可集成DeepSeek API实现语音转文字：

```kotlin
// 在RecordingListActivity中添加
private suspend fun transcribeAudio(recording: Recording): String {
    // TODO: 调用DeepSeek API
    // 1. 读取WAV文件
    // 2. 转换为API所需格式
    // 3. 发送请求
    // 4. 返回转写结果
    return ""
}
```

### 其他可扩展功能
- 音频格式转换（MP3、AAC等）
- 云端存储同步
- 录音编辑功能
- 音频降噪处理
- 多语言转写支持

## 故障排除

### 构建失败
1. 检查网络连接（需要下载依赖）
2. 清理项目：`./gradlew clean`
3. 同步Gradle：Android Studio -> File -> Sync Project with Gradle Files

### 权限问题
- 确保在设备设置中授予了录音权限
- Android 6.0+需要运行时权限请求

### 录音无声音
- 检查设备麦克风是否正常
- 确认其他应用没有占用麦克风
- 重启应用重试

## 许可证

本项目仅供学习和参考使用。

## 联系方式

如有问题或建议，欢迎反馈。
