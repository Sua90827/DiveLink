package com.divelink.server.repository.projection;

public interface GearCapacityRow {
  Long getGearId();
  Integer getCapacity();
  Integer getPrice();
  Long getUsed(); // 집계 값은 Long이 더 안전 (COUNT/SUM 결과)
}
