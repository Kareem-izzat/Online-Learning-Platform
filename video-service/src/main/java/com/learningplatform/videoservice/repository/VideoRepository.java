package com.learningplatform.videoservice.repository;

import com.learningplatform.videoservice.entity.UploadStatus;
import com.learningplatform.videoservice.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    List<Video> findByLessonId(Long lessonId);

    List<Video> findByUploadedBy(Long uploadedBy);

    List<Video> findByUploadStatus(UploadStatus status);

    @Query("SELECT v FROM Video v WHERE v.lessonId = :lessonId AND v.uploadStatus = 'COMPLETED'")
    List<Video> findCompletedVideosByLesson(Long lessonId);

    @Query("SELECT v FROM Video v WHERE v.uploadedBy = :userId AND v.uploadStatus = 'COMPLETED'")
    List<Video> findCompletedVideosByUploader(Long userId);

    @Query("SELECT COUNT(v) FROM Video v WHERE v.lessonId = :lessonId AND v.uploadStatus = 'COMPLETED'")
    Long countCompletedVideosByLesson(Long lessonId);
}
