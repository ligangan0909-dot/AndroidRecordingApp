package com.example.recordingapp.data.model

/**
 * Exception wrapper for TranscriptionError.
 * 
 * This exception class wraps TranscriptionError instances to make them compatible
 * with Result.failure() which requires a Throwable. The wrapped error can be
 * accessed via the error property, and the exception message is set to the
 * user-friendly message from TranscriptionError.toUserMessage().
 * 
 * @param error The TranscriptionError to wrap
 */
class TranscriptionException(val error: TranscriptionError) : Exception(error.toUserMessage())
