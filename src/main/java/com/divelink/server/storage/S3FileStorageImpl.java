package com.divelink.server.storage;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

public class S3FileStorageImpl implements FileStorage {
  private final String bucket;
  private final Region region;
  private final long urlTtlSeconds;
  private final S3Client s3;
  private final S3Presigner presigner;

  public S3FileStorageImpl(StorageProperties props) {
    this.bucket = props.getS3().getBucket();
    this.region = Region.of(props.getS3().getRegion());
    this.urlTtlSeconds = props.getUrlTtlSeconds();

    var creds = DefaultCredentialsProvider.create(); // 환경변수/IAM Role 사용
    this.s3 = S3Client.builder().region(region).credentialsProvider(creds).build();
    this.presigner = S3Presigner.builder().region(region).credentialsProvider(creds).build();
  }

  @Override
  public UploadResult upload(InputStream in, long size, String contentType, String keyHint) {
    String ext = (contentType != null && contentType.contains("jpeg")) ? "jpg"
        : (contentType != null && contentType.contains("png")) ? "png"
            : "bin";
    String key = keyHint + "/" + UUID.randomUUID() + "." + ext;

    PutObjectRequest put = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .contentType(contentType)
        .build();

    s3.putObject(put, RequestBody.fromInputStream(in, size));

    // 업로드 직후 바로 볼 수 있도록 presigned URL 한 번 만들어서 리턴
    String url = getUrl(key);
    return new UploadResult(key, url);
  }

  @Override
  public void delete(String key) {
    s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
  }

  @Override
  public String getUrl(String key) {
    GetObjectRequest get = GetObjectRequest.builder()
        .bucket(bucket).key(key).build();
    GetObjectPresignRequest req = GetObjectPresignRequest.builder()
        .getObjectRequest(get)
        .signatureDuration(Duration.ofSeconds(urlTtlSeconds))
        .build();
    URL url = presigner.presignGetObject(req).url();
    return url.toString();
  }
}
