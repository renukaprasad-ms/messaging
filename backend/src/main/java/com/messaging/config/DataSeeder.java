package com.messaging.config;

import com.messaging.platformrole.service.PlatformRoleService;
import com.messaging.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final RoleService roleService;
    private final PlatformRoleService platformRoleService;

    @Bean
    public ApplicationRunner seedRoles() {
        return args -> {
            roleService.createIfMissing("OWNER", "Company owner");
            roleService.createIfMissing("ADMIN", "Company administrator");
            roleService.createIfMissing("MANAGER", "Company manager");
            roleService.createIfMissing("MEMBER", "Regular company user");

            platformRoleService.createIfMissing("SUPERADMIN", "Platform super administrator");
            platformRoleService.createIfMissing("ADMIN", "Platform administrator");
        };
    }
}
