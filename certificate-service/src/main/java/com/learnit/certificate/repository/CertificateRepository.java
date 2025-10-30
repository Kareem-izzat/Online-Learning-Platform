package com.learnit.certificate.repository;

import com.learnit.certificate.entity.Certificate;
import com.learnit.certificate.entity.CertificateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    Optional<Certificate> findByVerificationCode(String verificationCode);

    Optional<Certificate> findByCertificateNumber(String certificateNumber);

    List<Certificate> findByStudentIdOrderByIssueDateDesc(Long studentId);

    List<Certificate> findByCourseIdOrderByIssueDateDesc(Long courseId);

    List<Certificate> findByStudentIdAndCourseId(Long studentId, Long courseId);

    List<Certificate> findByStatusOrderByIssueDateDesc(CertificateStatus status);

    @Query("SELECT c FROM Certificate c WHERE c.studentId = :studentId AND c.status = :status ORDER BY c.issueDate DESC")
    List<Certificate> findByStudentIdAndStatus(@Param("studentId") Long studentId, @Param("status") CertificateStatus status);

    @Query("SELECT COUNT(c) FROM Certificate c WHERE c.studentId = :studentId")
    Long countByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(c) FROM Certificate c WHERE c.courseId = :courseId")
    Long countByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(c) FROM Certificate c WHERE c.studentId = :studentId AND c.status = :status")
    Long countByStudentIdAndStatus(@Param("studentId") Long studentId, @Param("status") CertificateStatus status);

    @Query("SELECT AVG(c.finalGrade) FROM Certificate c WHERE c.studentId = :studentId")
    Double getAverageGradeByStudent(@Param("studentId") Long studentId);

    @Query("SELECT AVG(c.finalGrade) FROM Certificate c WHERE c.courseId = :courseId")
    Double getAverageGradeByCourse(@Param("courseId") Long courseId);

    @Query("SELECT SUM(c.downloadCount) FROM Certificate c WHERE c.courseId = :courseId")
    Long getTotalDownloadsByCourse(@Param("courseId") Long courseId);

    @Query("SELECT c FROM Certificate c WHERE c.issueDate BETWEEN :startDate AND :endDate ORDER BY c.issueDate DESC")
    List<Certificate> findCertificatesByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(c) FROM Certificate c WHERE c.issueDate BETWEEN :startDate AND :endDate")
    Long countCertificatesByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    boolean existsByStudentIdAndCourseIdAndStatus(Long studentId, Long courseId, CertificateStatus status);
}
