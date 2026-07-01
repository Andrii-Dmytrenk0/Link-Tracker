package com.linktracker.repository;

import com.linktracker.entity.ClickEvent;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

/**
 * Builds {@link Specification} predicates for {@link ClickEvent} export
 * filters. Each filter is added to the query only when it is actually
 * present, so an absent (null) filter never becomes a bind parameter at
 * all -- avoiding PostgreSQL's "could not determine data type of
 * parameter" error that JPQL patterns like {@code (:param IS NULL OR ...)}
 * can trigger.
 */
public final class ClickEventSpecifications {

    private ClickEventSpecifications() {
    }

    public static Specification<ClickEvent> withFilters(Long influencerId, Instant from, Instant to,
                                                          String country, String city) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (influencerId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("influencer").get("id"), influencerId));
            }
            if (from != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("timestamp"), from));
            }
            if (to != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("timestamp"), to));
            }
            if (country != null && !country.isBlank()) {
                predicate = cb.and(predicate, cb.equal(cb.lower(root.get("country")), country.toLowerCase()));
            }
            if (city != null && !city.isBlank()) {
                predicate = cb.and(predicate, cb.equal(cb.lower(root.get("city")), city.toLowerCase()));
            }

            query.orderBy(cb.desc(root.get("timestamp")));
            return predicate;
        };
    }
}
