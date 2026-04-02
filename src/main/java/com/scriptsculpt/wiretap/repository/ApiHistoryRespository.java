package com.scriptsculpt.wiretap.repository;

import com.scriptsculpt.wiretap.entity.ApiHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApiHistoryRespository extends JpaRepository<ApiHistory, Long> {
    List<ApiHistory> findByStatusCodeGreaterThanEqual(int statusCode);

    List<ApiHistory> findByTimestampGreaterThan(long timestamp);

    Page<ApiHistory> findByStatusCode(int statusCode, Pageable pageable);
}
