package vn.thanhnd.demo.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.thanhnd.demo.infrastructure.persistence.entity.RoleEntity;

import java.util.Set;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, String> {

    Set<RoleEntity> findByNameIn(Set<String> names);
}
