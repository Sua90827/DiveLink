package com.divelink.server.service;

import com.divelink.server.domain.Gear;
import com.divelink.server.domain.GearSet;
import com.divelink.server.domain.GearSetMapping;
import com.divelink.server.dto.GearListRequest;
import com.divelink.server.dto.GearRequest;
import com.divelink.server.dto.GearResponse;
import com.divelink.server.dto.GearSetWithGearListResponse;
import com.divelink.server.repository.GearRepository;
import com.divelink.server.repository.GearSetMappingRepository;
import com.divelink.server.repository.GearSetRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GearService {

  private final GearRepository gearRepository;
  private final GearSetRepository gearSetRepository;
  private final GearSetMappingRepository gearSetMappingRepository;

  @Transactional
  public void register(List<GearRequest> requests, String userId){
    //1. GearSet 저장
    GearSet set = GearSet.builder()
        .createdAt(LocalDateTime.now())
        .createdBy(userId)
        .build();
    gearSetRepository.save(set);

    //2. Gear 저장
    for(GearRequest request : requests){
      Gear gear = Gear.builder()
          .name(request.getName())
          .price(request.getPrice())
          .quantity(request.getQuantity())
          .createdAt(LocalDateTime.now())
          .createdBy(userId)
          .build();
      gearRepository.save(gear);

      //3. GearSetMapping 저장
      GearSetMapping mapping = GearSetMapping.builder()
          .gearSetId(set.getId())
          .gearId(gear.getId())
          .build();
      gearSetMappingRepository.save(mapping);
    }
  }

  public List<GearSetWithGearListResponse> getGearList(String userId, Pageable pageable) {
    //GearSet 목록 조회
    List<GearSet> setList = gearSetRepository.findAllByCreatedByOrderByCreatedAtDesc(userId, pageable);
    //GearSet Id 목록 추출
    List<Long> setIds = setList.stream()
        .map(GearSet::getId)
        .toList();
    //매핑정보 가져오기
    List<GearSetMapping> setMappingList = gearSetMappingRepository.findAllByGearSetIdIn(setIds);
    //Gear Id 목록 추출
    List<Long> gearIds = setMappingList.stream()
        .map(GearSetMapping::getGearId)
        .toList();
    //Gear 한번에 조회
    List<Gear> gears = gearRepository.findAllById(gearIds);
    Map<Long, Gear> gearMap = gears.stream()
        .collect(Collectors.toMap(Gear::getId, g->g));

    //setId -> List<GearResponse> 매핑
    Map<Long, List<GearResponse>> setToGears = new HashMap<>();
    for(GearSetMapping mapping : setMappingList){
      Long setId = mapping.getGearSetId();
      Gear gear = gearMap.get(mapping.getGearId());
      GearResponse gearResponse = new GearResponse(gear);
      setToGears.computeIfAbsent(setId, k -> new ArrayList<>()).add(gearResponse);
    }

    // 6. 최종 결과 변환
    List<GearSetWithGearListResponse> result = new ArrayList<>();
    for (GearSet set : setList){
      Long setId = set.getId();
      List<GearResponse> gearResponses = setToGears.getOrDefault(setId, List.of());
      result.add(new GearSetWithGearListResponse(setId, gearResponses));
    }
    return result;
  }
}
