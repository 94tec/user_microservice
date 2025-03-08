package com.warmUP.user_Auth.repository;

import com.warmUP.user_Auth.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUsername(String username);

    List<AuditLog> findByAction(String action);

    List<AuditLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<AuditLog> findByUsernameAndTimestampBetween(String username, LocalDateTime startDate, LocalDateTime endDate);

    List<AuditLog> findByActionAndTimestampBetween(String action, LocalDateTime startDate, LocalDateTime endDate);

    List<AuditLog> findByUsernameAndAction(String username, String action);

    List<AuditLog> findByUsernameAndActionAndTimestampBetween(String username, String action, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT a FROM AuditLog a WHERE a.username = :username AND a.action LIKE %:keyword%")
    List<AuditLog> findByUsernameAndActionKeyword(@Param("username") String username, @Param("keyword") String keyword);

    @Query("SELECT a FROM AuditLog a WHERE a.action LIKE %:keyword% AND a.timestamp BETWEEN :startDate AND :endDate")
    List<AuditLog> findByActionKeywordAndTimestampBetween(@Param("keyword") String keyword, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM AuditLog a WHERE a.username = :username AND a.action LIKE %:keyword% AND a.timestamp BETWEEN :startDate AND :endDate")
    List<AuditLog> findByUsernameAndActionKeywordAndTimestampBetween(@Param("username") String username, @Param("keyword") String keyword, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}