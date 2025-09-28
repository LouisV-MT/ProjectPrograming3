package org.example.recipeapp.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
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
                Permission.USER_DELETE))
    ;
        @Getter
        private final Set<Permission> permissions;

        public List<SimpleGrantedAuthority> getAuthorities() {
            return getPermissions()
                    .stream()
                    .map(permission ->  new SimpleGrantedAuthority("ROLE_"+this.name()))
                    .toList();
        }

}
