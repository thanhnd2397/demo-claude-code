package vn.thanhnd.demo.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.thanhnd.demo.infrastructure.persistence.entity.AdministratorEntity;

import java.util.Optional;

public interface AdministratorJpaRepository extends JpaRepository<AdministratorEntity, String> {

    Optional<AdministratorEntity> findByUsername(String username);

    Optional<AdministratorEntity> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
