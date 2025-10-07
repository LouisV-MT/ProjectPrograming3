package org.example.recipeapp.domain;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum Role {
    USER(Set.of(Permission.USER_READ,
            Permission.USER_UPDATE,
            Permission.USER_CREATE,
            Permission.USER_DELETE)),
    ADMIN(Set.of(Permission.ADMIN_READ,
            Permission.ADMIN_UPDATE,
            Permission.ADMIN_CREATE,
            Permission.ADMIN_DELETE,
            Permission.USER_READ,
            Permission.USER_UPDATE,
            Permission.USER_CREATE,
            Permission.USER_DELETE));

    private final Set<Permission> permissions;

    // ✅ 手写构造器，替代 Lombok
    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    // ✅ 手写 getter
    public Set<Permission> getPermissions() {
        return permissions;
    }

    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}
