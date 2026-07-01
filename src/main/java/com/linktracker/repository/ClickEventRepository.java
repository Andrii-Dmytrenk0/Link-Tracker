package com.linktracker.repository;

import com.linktracker.entity.ClickEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long>, JpaSpecificationExecutor<ClickEvent> {

    long countByInfluencerId(Long influencerId);

    long countByInfluencerIdAndTimestampAfter(Long influencerId, Instant since);

    long countByInfluencerIdAndBotFalseAndTimestampAfter(Long influencerId, Instant since);

    Page<ClickEvent> findByInfluencerIdOrderByTimestampDesc(Long influencerId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM ClickEvent c WHERE c.timestamp >= :since")
    long countSince(@Param("since") Instant since);

    @Query("SELECT COUNT(DISTINCT c.ip) FROM ClickEvent c")
    long countDistinctIps();

    @Query("""
            SELECT c.influencer.id, c.influencer.name, c.influencer.code, COUNT(c)
            FROM ClickEvent c
            WHERE c.bot = false
            GROUP BY c.influencer.id, c.influencer.name, c.influencer.code
            ORDER BY COUNT(c) DESC
            """)
    List<Object[]> topInfluencers(Pageable pageable);

    @Query(value = """
            SELECT CAST(timestamp AS date) AS day, COUNT(*) AS total
            FROM click_events
            WHERE timestamp >= :since AND bot = false
            GROUP BY day
            ORDER BY day
            """, nativeQuery = true)
    List<Object[]> clicksByDay(@Param("since") Instant since);

    @Query(value = """
            SELECT EXTRACT(HOUR FROM timestamp) AS hr, COUNT(*) AS total
            FROM click_events
            WHERE timestamp >= :since AND bot = false
            GROUP BY hr
            ORDER BY hr
            """, nativeQuery = true)
    List<Object[]> clicksByHour(@Param("since") Instant since);

    @Query(value = """
            SELECT COALESCE(country, 'Unknown') AS country, COUNT(*) AS total
            FROM click_events
            WHERE bot = false
            GROUP BY country
            ORDER BY total DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> topCountries(@Param("limit") int limit);

    @Query(value = """
            SELECT COALESCE(city, 'Unknown') AS city, COUNT(*) AS total
            FROM click_events
            WHERE bot = false
            GROUP BY city
            ORDER BY total DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> topCities(@Param("limit") int limit);

    @Query(value = """
            SELECT COALESCE(browser, 'Unknown') AS browser, COUNT(*) AS total
            FROM click_events
            WHERE bot = false
            GROUP BY browser
            ORDER BY total DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> topBrowsers(@Param("limit") int limit);

    @Query(value = """
            SELECT COALESCE(os, 'Unknown') AS os, COUNT(*) AS total
            FROM click_events
            WHERE bot = false
            GROUP BY os
            ORDER BY total DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> topOperatingSystems(@Param("limit") int limit);

    @Query(value = """
            SELECT COALESCE(device_type, 'UNKNOWN') AS device_type, COUNT(*) AS total
            FROM click_events
            WHERE bot = false
            GROUP BY device_type
            ORDER BY total DESC
            """, nativeQuery = true)
    List<Object[]> topDeviceTypes();

    @Query(value = """
            SELECT country, latitude, longitude, COUNT(*) AS total
            FROM click_events
            WHERE bot = false AND latitude IS NOT NULL AND longitude IS NOT NULL
            GROUP BY country, latitude, longitude
            """, nativeQuery = true)
    List<Object[]> worldMapPoints();

    /**
     * Counts how many clicks were made from the same influencer + IP combination
     * within the given time window, used for simple repeat-click / abuse detection.
     */
    @Query("""
            SELECT COUNT(c) FROM ClickEvent c
            WHERE c.influencer.id = :influencerId AND c.ip = :ip AND c.timestamp >= :since
            """)
    long countRecentClicksFromIp(@Param("influencerId") Long influencerId,
                                  @Param("ip") String ip,
                                  @Param("since") Instant since);

    // NOTE: filtered export queries are built dynamically via
    // ClickEventSpecifications + JpaSpecificationExecutor (see below) instead
    // of a JPQL query with "(:param IS NULL OR ...)" branches. PostgreSQL's
    // extended query protocol cannot determine a parameter's data type when
    // its only textual occurrence is inside an "IS NULL" check with no other
    // type-establishing context, which throws
    // "could not determine data type of parameter $N" at prepare time,
    // regardless of the value actually bound. Building predicates
    // dynamically in Java sidesteps the issue entirely: a filter that is
    // null simply never becomes a bind parameter in the first place.
}
