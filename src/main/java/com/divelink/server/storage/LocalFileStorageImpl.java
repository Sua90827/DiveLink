package com.divelink.server.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalFileStorageImpl implements FileStorage {
  private final Path baseDir;
  private final String publicBaseUrl;

  public LocalFileStorageImpl(StorageProperties properties) {
    this.baseDir = Paths.get(properties.getLocal().getBasePath()).toAbsolutePath();
    this.publicBaseUrl = properties.getLocal().getPublicBaseUrl();
    try {
      Files.createDirectories(baseDir);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public UploadResult upload(InputStream inputStream, long size, String contentType, String keyHint) {
    String ext = guessExt(contentType, keyHint);
    String fileName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
    Path target = baseDir.resolve(keyHint).resolve(fileName);
    try {
      Files.createDirectories(target.getParent());

      long copied = Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
      if (copied == 0) {

        log.warn("[LocalFileStorage] WARNING: 0 bytes copied -> {}", target);
      } else {
        log.info("[LocalFileStorage] copied={} -> {}", copied, target);
      }
      String key = keyHint + "/" + fileName;
      String url = publicBaseUrl + "/" + key;
      return new UploadResult(key, url);
    } catch (IOException e) {
      throw new RuntimeException("Local store failed", e);
    }
  }

  @Override
  public void delete(String key) {
    try { Files.deleteIfExists(baseDir.resolve(key)); } catch (IOException ignored) {}
  }

  @Override
  public String getUrl(String key) {
    return publicBaseUrl + "/" + key;
  }

  private String guessExt(String contentType, String keyHint) {
    if (contentType != null) {
      if (contentType.equals("image/png")) return "png";
      if (contentType.equals("image/jpeg") || contentType.equals("image/jpg")) return "jpg";
      if (contentType.equals("image/webp")) return "webp";
    }
    int dot = keyHint.lastIndexOf('.');
    return (dot >= 0) ? keyHint.substring(dot + 1) : "";
  }
}