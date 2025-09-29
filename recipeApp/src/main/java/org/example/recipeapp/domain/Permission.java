package org.example.recipeapp.domain;

public enum Permission {
    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_CREATE("admin:create"),
    ADMIN_DELETE("admin:delete"),
    USER_READ("user:read"),
    USER_UPDATE("user:update"),
    USER_CREATE("user:create"),
    USER_DELETE("user:delete");

    private final String permission;

    // ✅ 手写构造器
    Permission(String permission) {
        this.permission = permission;
    }

    // ✅ 手写 getter
    public String getPermission() {
        return permission;
    }
}
