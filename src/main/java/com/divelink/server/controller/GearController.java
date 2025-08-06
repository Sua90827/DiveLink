package com.divelink.server.controller;

import static org.springframework.data.domain.Sort.Direction.DESC;

import com.divelink.server.context.UserContext;
import com.divelink.server.dto.GearListRequest;
import com.divelink.server.dto.GearRequest;
import com.divelink.server.dto.GearSetWithGearListResponse;
import com.divelink.server.service.GearService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gear")
@RequiredArgsConstructor
public class GearController {

  private final GearService gearService;

  private boolean isAdmin() {
    return "ADMIN".equals(UserContext.getUserRole());
  }

  //새로운 set 장비 등록
  @PostMapping
  public ResponseEntity<String> registerGear(@RequestBody GearListRequest request) {
      if (!isAdmin()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }
      gearService.register(request.getGearList(), UserContext.getUserId());
      return ResponseEntity.ok("등록 성공");
  }

  //장비 리스트 조회
  @GetMapping
  public ResponseEntity<List<GearSetWithGearListResponse>> getGear(@PageableDefault(size = 5, sort = "createdAt", direction = DESC) Pageable pageable){
    String userId = UserContext.getUserId();
    List<GearSetWithGearListResponse> list = gearService.getGearList(userId, pageable);
    return ResponseEntity.ok(list);
  }
}
