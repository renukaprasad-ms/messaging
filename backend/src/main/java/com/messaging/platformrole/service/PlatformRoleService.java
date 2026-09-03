package com.messaging.platformrole.service;

import com.messaging.platformrole.entity.PlatformRole;
import com.messaging.platformrole.repository.PlatformRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlatformRoleService {

    private final PlatformRoleRepository platformRoleRepository;

    public PlatformRole createIfMissing(String name, String description) {
        return platformRoleRepository.findByName(name)
                .orElseGet(() -> create(name, description));
    }

    private PlatformRole create(String name, String description) {
        PlatformRole role = new PlatformRole();
        role.setName(name);
        role.setDescription(description);
        role.setActive(true);
        return platformRoleRepository.save(role);
    }
}
