package com.messaging.platformrole.repository;

import com.messaging.platformrole.entity.PlatformRole;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformRoleRepository extends JpaRepository<PlatformRole, Long> {

  Optional<PlatformRole> findByName(String name);
}
