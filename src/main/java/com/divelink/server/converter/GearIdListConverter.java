package com.divelink.server.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;

@Converter
public class GearIdListConverter implements AttributeConverter<List<Long>, String> {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(List<Long> attribute) {
    try {
      return objectMapper.writeValueAsString(attribute);
    } catch (Exception e) {
      throw new IllegalArgumentException("gearIds 변환 실패", e);
    }
  }

  @Override
  public List<Long> convertToEntityAttribute(String dbData) {
    try {
      return objectMapper.readValue(dbData, new TypeReference<List<Long>>() {});
    } catch (Exception e) {
      throw new IllegalArgumentException("gearIds 읽기 실패", e);
    }
  }
}
