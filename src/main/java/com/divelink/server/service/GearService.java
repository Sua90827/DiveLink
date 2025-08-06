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
          .gearSet(set)
          .gear(gear)
          .build();
      gearSetMappingRepository.save(mapping);
    }
  }

  public List<GearSetWithGearListResponse> getGearList(String userId, Pageable pageable) {

    List<GearSet> setList = gearSetRepository.findWithMappingsAndGearsByCreatedBy(userId, pageable);

    return setList.stream()
        .map(set -> {
          List<GearResponse> gearResponses = set.getMappings().stream()
              .map(mapping -> new GearResponse(mapping.getGear()))
              .toList();
          return new GearSetWithGearListResponse(set.getId(), gearResponses);
        })
        .toList();
  }
}