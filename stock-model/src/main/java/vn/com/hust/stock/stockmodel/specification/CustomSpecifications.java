package vn.com.hust.stock.stockmodel.specification;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public abstract class CustomSpecifications<T> {
    public Specification<T> eq(String attributeName, Object value) {
        if (value == null) {
            return null;
        }

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(attributeName), value);
    }

    public   Specification<T> likeAnyPosition(String attributeName, String value) {
        if (value == null) {
            return null;
        }

        String condition = "%" + value + "%";

        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get(attributeName), condition);
    }
    public   Specification<T> formDate(String attributeName, LocalDateTime fromDate) {
        if (fromDate == null) {
            return null;
        }

        return  (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get(attributeName), fromDate);
    }
    public   Specification<T> toDate(String attributeName, LocalDateTime toDate) {
        if (toDate == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get(attributeName), toDate);
    }
}
