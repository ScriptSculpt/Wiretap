package com.scriptsculpt.wiretap.repository;

import com.scriptsculpt.wiretap.entity.ApiHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiHistoryRespository extends JpaRepository<ApiHistory, Long> {
}
