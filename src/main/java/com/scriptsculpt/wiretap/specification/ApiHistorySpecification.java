package com.scriptsculpt.wiretap.specification;

import com.scriptsculpt.wiretap.entity.ApiHistory;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ApiHistorySpecification {

    public static Specification<ApiHistory> hasStatus(Integer status) {
        return (root, query, criteriaBuilder) -> {
            if(status == null) return null;
            return  criteriaBuilder.equal(root.get("statusCode"), status);
        };
    }

    public static Specification<ApiHistory> hasMethod(String method) {
        return  (root, query, criteriaBuilder) -> {
            if(method == null || method.isBlank()) return null;
            return criteriaBuilder.equal(root.get("method"), method);
        };
    }

    public static Specification<ApiHistory> greaterThanEqualTimestamp(Long minThreshold) {
        return (root, query, criteriaBuilder) -> {
            if(minThreshold == null) return null;
            return criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), minThreshold);
        };
    }

    public static Specification<ApiHistory> lessThanEqualTimestamp(Long maxThreshold) {
        return (root, query, criteriaBuilder) -> {
            if(maxThreshold == null) return null;
            return  criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), maxThreshold);
        };
    }

    public static Specification<ApiHistory> containsUrl(String url) {
        return (root, query, criteriaBuilder) -> {
            if(url == null || url.isBlank()) return null;
            return criteriaBuilder.like(root.get("url"), "%"+url+"%");
        };
    }

    public static Specification<ApiHistory> globalSearch(String search) {
        return (root, query, criteriaBuilder) ->{
            if(search == null || search.isBlank()) return null;
            String pattern = "%" + search + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("url"), pattern),
                    criteriaBuilder.like(root.get("requestBody"), pattern),
                    criteriaBuilder.like(root.get("responseBody"), pattern)
            );
        };
    }

}
