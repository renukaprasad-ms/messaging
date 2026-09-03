package com.messaging.role.service;

import com.messaging.role.entity.Role;
import com.messaging.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role createIfMissing(String name, String description) {
        return roleRepository.findByName(name)
                .orElseGet(() -> create(name, description));
    }

    private Role create(String name, String description) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        role.setActive(true);
        return roleRepository.save(role);
    }
}
