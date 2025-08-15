package com.divelink.server.storage;

import java.io.InputStream;

public interface FileStorage {
  record UploadResult(String key, String url) {}

  UploadResult upload(InputStream in, long size, String contentType, String keyHint);
  void delete(String key);
  String getUrl(String key);
}