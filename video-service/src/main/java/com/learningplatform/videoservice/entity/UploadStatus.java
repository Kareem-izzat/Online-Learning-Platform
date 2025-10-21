package com.learningplatform.videoservice.entity;

public enum UploadStatus {
    PENDING,      // Video upload initiated but not started
    UPLOADING,    // Video is being uploaded
    PROCESSING,   // Video is being processed (transcoding, thumbnail generation, etc.)
    COMPLETED,    // Video successfully uploaded and processed
    FAILED        // Upload or processing failed
}
