package com.messaging.platformrole.repository;

import com.messaging.platformrole.entity.PlatformRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformRoleRepository extends JpaRepository<PlatformRole, Long> {

    Optional<PlatformRole> findByName(String name);
}
