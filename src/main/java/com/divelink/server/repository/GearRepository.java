package com.divelink.server.repository;

import com.divelink.server.domain.Gear;
import com.divelink.server.repository.projection.GearCapacityRow;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GearRepository  extends JpaRepository<Gear, Long> {

  @Query(value = """
SELECT
  g.id AS gearId,
  g.quantity AS capacity,
  g.price AS price,
  COALESCE(COUNT(pa.id), 0) AS used
FROM gear g
LEFT JOIN practice_application pa
  ON pa.practice_notice_id = :noticeId
 AND pa.status IN ('PAYMENT_PENDING','CONFIRM_REQUESTED','PAYMENT_CONFIRMED')
 AND FIND_IN_SET(
       CAST(g.id AS CHAR),
       REPLACE(REPLACE(REPLACE(IFNULL(pa.gear_ids, ''), '[',''),']',''), '"','')
     ) > 0
WHERE g.id IN (:gearIds)
GROUP BY g.id, g.quantity, g.price
""", nativeQuery = true)
  List<GearCapacityRow> checkCapacityForNotice(@Param("noticeId") Long noticeId,
      @Param("gearIds") List<Long> gearIds);
}