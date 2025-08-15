package com.divelink.server.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

  @Bean
  public FileStorage fileStorage(StorageProperties properties){
    return switch(properties.getType()){
      case "s3" -> new S3FileStorageImpl(properties);
      default -> new LocalFileStorageImpl(properties);
    };
  }
}
