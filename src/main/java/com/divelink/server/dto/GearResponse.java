package com.divelink.server.dto;

import com.divelink.server.domain.Gear;
import lombok.Getter;

@Getter
public class GearResponse {
  private Long id;
  private String name;
  private Integer price;
  private Integer quantity;

  public GearResponse(Gear gear) {
    this.id = gear.getId();
    this.name = gear.getName();
    this.price = gear.getPrice();
    this.quantity = gear.getQuantity();
  }
}
