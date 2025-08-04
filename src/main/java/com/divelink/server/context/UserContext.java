package com.divelink.server.context;

public class UserContext {
  private static final ThreadLocal<String> userIdHolder = new ThreadLocal<>();
  private static final ThreadLocal<String> roleHolder = new ThreadLocal<>();

  public static void setUserId(String userId) {
    userIdHolder.set(userId);
  }

  public static String getUserId() {
    return userIdHolder.get();
  }

  public static void setUserRole(String role) {
    roleHolder.set(role);
  }

  public static String getUserRole() {
    return roleHolder.get();
  }

  public static void clear() {
    userIdHolder.remove();
    roleHolder.remove();
  }
}
