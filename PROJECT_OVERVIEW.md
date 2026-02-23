# Android录音应用 - 项目概览

## 项目信息

- **项目名称**: AndroidRecordingApp
- **包名**: com.example.recordingapp
- **语言**: Kotlin
- **最低SDK**: API 23 (Android 6.0)
- **目标SDK**: API 33 (Android 13)
- **构建工具**: Gradle 8.0
- **开发工具**: Android Studio

## 核心功能

### 1. 录音功能 ✅
- 使用AudioRecord API进行高质量录音
- 采样率：44100 Hz
- 格式：16-bit PCM，单声道
- 输出：WAV文件格式
- 实时显示录音时长（HH:MM:SS）
- 实时显示音频电平（0-100）

### 2. 播放功能 ✅
- 使用MediaPlayer播放录音
- 支持播放/暂停控制
- 显示播放进度

### 3. 文件管理 ✅
- 列表显示所有录音文件
- 显示文件名和创建时间
- 支持删除录音文件
- 文件存储在应用私有目录

### 4. 权限管理 ✅
- 运行时请求录音权限
- 友好的权限说明
- 权限拒绝处理

### 5. 转写功能 🔄
- 预留API接口
- 待集成DeepSeek API
- 占位UI已实现

## 技术架构

### 架构模式
```
MVP (Model-View-Presenter) 简化版
- Model: Recording数据模型
- View: Activity + ViewBinding
- Logic: Manager类（AudioRecordManager, AudioPlayer）
```

### 核心组件

#### 1. UI层
- **MainActivity**: 应用入口，提供导航
- **RecordingActivity**: 录音界面，显示时长和电平
- **RecordingListActivity**: 录音列表，管理文件

#### 2. 业务逻辑层
- **AudioRecordManager**: 录音管理
  - 初始化AudioRecord
  - 录制音频数据
  - 计算音频电平
  - 协程处理异步操作
  
- **AudioPlayer**: 播放管理
  - MediaPlayer封装
  - 播放状态控制
  - 进度回调

- **WavFileWriter**: WAV文件写入
  - 写入WAV文件头
  - 写入PCM数据
  - 更新文件大小信息

#### 3. 数据层
- **Recording**: 录音数据模型
  - 文件引用
  - 元数据（名称、时长、时间戳）

#### 4. 工具层
- **PermissionHelper**: 权限管理工具
  - 检查权限
  - 请求权限

### 依赖库

```kotlin
// 核心库
androidx.core:core-ktx:1.10.1
androidx.appcompat:appcompat:1.6.1

// UI库
com.google.android.material:material:1.9.0
androidx.constraintlayout:constraintlayout:2.1.4

// 协程
org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1

// 生命周期
androidx.lifecycle:lifecycle-runtime-ktx:2.6.1
```

## 文件结构

```
AndroidRecordingApp/
├── app/
│   ├── build.gradle.kts              # 应用级构建配置
│   ├── proguard-rules.pro            # 混淆规则
│   └── src/main/
│       ├── AndroidManifest.xml       # 应用清单
│       ├── java/com/example/recordingapp/
│       │   ├── MainActivity.kt                    # 主界面
│       │   ├── RecordingActivity.kt              # 录音界面
│       │   ├── RecordingListActivity.kt          # 列表界面
│       │   ├── audio/
│       │   │   ├── AudioRecordManager.kt         # 录音管理器
│       │   │   ├── AudioPlayer.kt                # 播放器
│       │   │   └── WavFileWriter.kt              # WAV写入器
│       │   ├── model/
│       │   │   └── Recording.kt                  # 数据模型
│       │   └── utils/
│       │       └── PermissionHelper.kt           # 权限工具
│       └── res/
│           ├── layout/                           # 布局文件
│           │   ├── activity_main.xml
│           │   ├── activity_recording.xml
│           │   ├── activity_recording_list.xml
│           │   └── item_recording.xml
│           ├── values/                           # 资源值
│           │   ├── strings.xml
│           │   ├── colors.xml
│           │   └── themes.xml
│           ├── drawable/                         # 图片资源
│           └── mipmap-*/                         # 应用图标
├── build.gradle.kts                  # 项目级构建配置
├── settings.gradle.kts               # 项目设置
├── gradle.properties                 # Gradle属性
├── gradlew                          # Gradle包装器(Unix)
├── gradlew.bat                      # Gradle包装器(Windows)
├── gradle/wrapper/                  # Gradle包装器文件
├── README.md                        # 项目说明
├── BUILD_GUIDE.md                   # 构建指南
├── PROJECT_OVERVIEW.md              # 项目概览（本文件）
├── quick-build.sh                   # 快速构建脚本(Unix)
└── quick-build.bat                  # 快速构建脚本(Windows)
```

## 数据流

### 录音流程
```
用户点击"开始录音"
    ↓
检查权限
    ↓
创建输出文件
    ↓
初始化AudioRecord
    ↓
启动录音（协程）
    ↓
循环读取音频数据
    ↓
写入WAV文件 + 计算电平
    ↓
更新UI（时长、电平）
    ↓
用户点击"停止录音"
    ↓
停止AudioRecord
    ↓
关闭文件并更新文件头
    ↓
保存完成
```

### 播放流程
```
用户点击"播放"
    ↓
初始化MediaPlayer
    ↓
设置数据源（文件路径）
    ↓
准备播放
    ↓
开始播放
    ↓
监听播放完成
    ↓
释放资源
```

## 关键技术点

### 1. AudioRecord使用
```kotlin
// 计算缓冲区大小
val bufferSize = AudioRecord.getMinBufferSize(
    SAMPLE_RATE,
    CHANNEL_CONFIG,
    AUDIO_FORMAT
)

// 创建AudioRecord实例
audioRecord = AudioRecord(
    MediaRecorder.AudioSource.MIC,
    SAMPLE_RATE,
    CHANNEL_CONFIG,
    AUDIO_FORMAT,
    bufferSize
)

// 开始录音
audioRecord.startRecording()

// 读取数据
val readSize = audioRecord.read(buffer, 0, bufferSize)
```

### 2. WAV文件格式
```
WAV文件结构：
- RIFF头（12字节）
- fmt块（24字节）
- data块（8字节 + 音频数据）

关键参数：
- 采样率：44100 Hz
- 声道数：1（单声道）
- 位深度：16 bit
- 字节率：采样率 × 声道数 × 位深度/8
```

### 3. 音频电平计算
```kotlin
// 计算PCM样本的平均振幅
val average = sum / (size / 2)

// 转换为分贝
val db = 20 * log10(average / 32768.0)

// 归一化到0-100
val normalized = ((db + 60) / 60 * 100).coerceIn(0.0, 100.0)
```

### 4. 协程使用
```kotlin
// 在IO线程录音
recordingJob = CoroutineScope(Dispatchers.IO).launch {
    while (isRecording) {
        // 读取音频数据
        val readSize = audioRecord?.read(buffer, 0, bufferSize)
        
        // 切换到主线程更新UI
        withContext(Dispatchers.Main) {
            onAudioLevelChanged?.invoke(level)
        }
    }
}
```

### 5. ViewBinding
```kotlin
// 在Activity中使用
private lateinit var binding: ActivityMainBinding

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    
    // 直接访问视图
    binding.btnStartRecording.setOnClickListener { }
}
```

## 性能优化

### 已实现
1. 使用协程处理异步操作，避免阻塞主线程
2. 使用ViewBinding减少findViewById开销
3. RecyclerView复用视图
4. 及时释放AudioRecord和MediaPlayer资源

### 可优化
1. 使用ViewModel管理UI状态
2. 使用Room数据库存储录音元数据
3. 实现音频数据缓存
4. 添加音频压缩（MP3/AAC）
5. 使用WorkManager处理后台任务

## 安全性

### 已实现
1. 运行时权限请求
2. 文件存储在应用私有目录
3. 权限拒绝处理

### 建议增强
1. 添加文件加密
2. 实现安全的网络传输
3. 添加用户认证
4. 实现数据备份

## 扩展方向

### 短期（1-2周）
1. 集成DeepSeek API实现转写
2. 添加音频波形显示
3. 实现录音暂停/继续
4. 添加录音质量设置

### 中期（1-2月）
1. 支持多种音频格式（MP3、AAC）
2. 实现音频编辑功能
3. 添加云端同步
4. 实现分享功能

### 长期（3-6月）
1. 实时语音识别
2. 多语言支持
3. AI降噪处理
4. 语音标注功能

## 测试建议

### 单元测试
- WavFileWriter文件写入测试
- 音频电平计算测试
- 权限检查逻辑测试

### 集成测试
- 录音流程测试
- 播放流程测试
- 文件管理测试

### UI测试
- 权限请求流程
- 录音界面交互
- 列表操作测试

### 性能测试
- 长时间录音测试
- 内存泄漏检测
- 电池消耗测试

## 已知限制

1. 不支持后台录音（需要Foreground Service）
2. 不支持音频格式转换
3. 不支持音频编辑
4. 转写功能未实现
5. 无云端存储

## 许可证

本项目仅供学习和参考使用。

## 更新日志

### v1.0.0 (2024)
- ✅ 初始版本
- ✅ 基础录音功能
- ✅ 播放功能
- ✅ 文件管理
- ✅ 权限管理
- 🔄 转写功能（预留）
