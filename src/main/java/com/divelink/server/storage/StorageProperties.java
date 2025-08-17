package com.divelink.server.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
  private String type = "local";
  private long urlTtlSeconds = 1800; // presigned URL TTL (기본 30분)
  private Local local = new Local();
  private S3 s3 = new S3();

  @Getter @Setter
  public static class Local{
    private String basePath;
    private String publicBaseUrl;
  }

  @Getter @Setter
  public static class S3{
    private String bucket;
    private String region;
  }
}
