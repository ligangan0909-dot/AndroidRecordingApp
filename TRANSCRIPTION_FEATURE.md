# Transcription Feature Documentation

## Overview

This document describes the simplified transcription feature implementation for the Android Recording App. The feature integrates with DeepSeek API to provide audio-to-text transcription with summary generation.

## Components Created

### 1. Domain Layer

#### ITranscriptionService
- **Location**: `com.example.recordingapp.domain.ITranscriptionService`
- **Purpose**: Interface defining transcription service operations
- **Methods**:
  - `transcribe(recordingId, audioFilePath)`: Start transcription, returns Flow<TranscriptionState>
  - `cancelTranscription(recordingId)`: Cancel in-progress transcription
  - `getCachedResult(recordingId)`: Retrieve cached transcription result

#### TranscriptionService
- **Location**: `com.example.recordingapp.domain.TranscriptionService`
- **Purpose**: Implementation of transcription service
- **Features**:
  - Audio file validation (format, size, permissions)
  - State emission (Idle, Uploading, Processing, Success, Error, Cancelled)
  - Integration with DeepSeekApiClient and TranscriptionRepository
  - Simplified version: Only transcription and summary (no speaker diarization, no task extraction)

### 2. Presentation Layer

#### TranscriptionViewModel
- **Location**: `com.example.recordingapp.ui.transcription.TranscriptionViewModel`
- **Purpose**: Manages transcription state and user actions
- **Features**:
  - StateFlow for transcription state
  - Methods: startTranscription(), cancelTranscription(), retryTranscription(), loadCachedResult()
  - Coroutine-based async operations with IO dispatcher

#### TranscriptionActivity
- **Location**: `com.example.recordingapp.ui.transcription.TranscriptionActivity`
- **Purpose**: UI for displaying transcription progress and results
- **Features**:
  - Progress indicator with upload/processing status
  - Cancel button during transcription
  - Result display with summary and full text
  - Error handling with retry button
  - Cache indicator for previously transcribed recordings

#### SettingsActivity
- **Location**: `com.example.recordingapp.ui.settings.SettingsActivity`
- **Purpose**: API key configuration interface
- **Features**:
  - API key input field with validation
  - Save and clear buttons
  - Masked API key display for security
  - Status indicator

### 3. Layout Files

#### activity_transcription.xml
- Progress layout with progress bar and cancel button
- Result layout with summary and full text sections
- Error layout with error message and retry button
- Responsive design with ConstraintLayout

#### activity_settings.xml
- API key input with TextInputLayout
- Save and clear buttons
- Status indicator and help text
- Material Design components

### 4. Integration

#### RecordingListActivity Update
- Modified `showTranscriptionPlaceholder()` to launch TranscriptionActivity
- Passes recording ID and file path via Intent extras

#### AndroidManifest.xml
- Added TranscriptionActivity declaration
- Added SettingsActivity declaration

#### strings.xml
- Added all necessary string resources for transcription and settings screens
- Chinese language support

## Usage Flow

1. **User selects transcription**: User taps "转写" button on a recording in RecordingListActivity
2. **Launch TranscriptionActivity**: Activity receives recording ID and file path via Intent
3. **Start transcription**: ViewModel calls TranscriptionService.transcribe()
4. **Service validates file**: Checks file exists, readable, size < 25MB, WAV format
5. **Upload and process**: Emits Uploading → Processing states
6. **API call**: DeepSeekApiClient uploads file with summary enabled
7. **Save result**: TranscriptionRepository saves result to database
8. **Display result**: Activity shows summary and full text
9. **Cache support**: Subsequent views load from cache

## Configuration

### API Key Setup

Users must configure their DeepSeek API key before using transcription:

1. Open SettingsActivity (needs to be added to app menu)
2. Enter API key (32-128 characters, alphanumeric + underscore/hyphen)
3. Tap "保存" to save
4. API key is encrypted using EncryptedSharedPreferences

### Simplified Feature Set

This implementation is simplified and includes:
- ✅ Audio transcription
- ✅ Summary generation
- ❌ Speaker diarization (disabled)
- ❌ Task extraction (disabled)

To enable full features, modify `TranscriptionService.transcribe()`:
```kotlin
val result = apiClient.transcribeAudio(
    audioFile = file,
    enableDiarization = true,  // Change to true
    enableTaskExtraction = true,  // Change to true
    enableSummary = true
)
```

## Dependencies

All required dependencies are already added in the project:
- Retrofit for API calls
- OkHttp for network layer
- Room for database
- EncryptedSharedPreferences for secure storage
- Kotlin Coroutines for async operations

## TODO: Dependency Injection

The current implementation has placeholder comments for dependency injection. To complete the integration:

1. **Add Hilt/Koin setup** (if not already present)
2. **Create ViewModelFactory** for TranscriptionViewModel
3. **Inject dependencies** in Activities:
   - TranscriptionActivity needs TranscriptionViewModel
   - SettingsActivity needs IApiKeyManager

Example with Hilt:
```kotlin
@HiltViewModel
class TranscriptionViewModel @Inject constructor(
    private val transcriptionService: ITranscriptionService
) : ViewModel() { ... }

@AndroidEntryPoint
class TranscriptionActivity : AppCompatActivity() {
    private val viewModel: TranscriptionViewModel by viewModels()
    ...
}
```

## Testing

The implementation is ready for testing. Key test scenarios:

1. **File validation**: Test with invalid files, oversized files, missing files
2. **State transitions**: Verify all states are emitted correctly
3. **Cancellation**: Test cancel during upload and processing
4. **Error handling**: Test network errors, API errors, timeout
5. **Cache**: Test loading cached results
6. **API key**: Test validation, save, clear operations

## Security

- API keys are encrypted using EncryptedSharedPreferences with AES256_GCM
- API keys are masked in UI display
- HTTPS-only connections enforced
- Sensitive data sanitized in logs

## Performance

- Coroutines with IO dispatcher for non-blocking operations
- File streaming for large audio uploads
- Database caching to avoid redundant API calls
- Efficient state management with StateFlow

## Future Enhancements

1. Add settings menu item to access SettingsActivity
2. Enable speaker diarization and task extraction
3. Add progress percentage for upload
4. Add notification for background transcription
5. Add export functionality for transcription results
6. Add re-transcription option in result view
