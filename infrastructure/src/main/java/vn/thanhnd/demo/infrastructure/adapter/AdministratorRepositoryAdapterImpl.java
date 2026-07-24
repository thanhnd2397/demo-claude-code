package vn.thanhnd.demo.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import vn.thanhnd.demo.domain.adapter.AdministratorRepositoryPort;
import vn.thanhnd.demo.domain.enums.AdministratorStatus;
import vn.thanhnd.demo.domain.exception.DomainValidationException;
import vn.thanhnd.demo.domain.model.Administrator;
import vn.thanhnd.demo.infrastructure.mapper.AdministratorMapper;
import vn.thanhnd.demo.infrastructure.persistence.entity.AdministratorEntity;
import vn.thanhnd.demo.infrastructure.persistence.entity.RoleEntity;
import vn.thanhnd.demo.infrastructure.persistence.repository.AdministratorJpaRepository;
import vn.thanhnd.demo.infrastructure.persistence.repository.RoleJpaRepository;
import vn.thanhnd.demo.util.annotation.Adapter;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Adapter
@RequiredArgsConstructor
public class AdministratorRepositoryAdapterImpl implements AdministratorRepositoryPort {

    private final AdministratorJpaRepository administratorJpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final AdministratorMapper administratorMapper;

    @Override
    public Optional<Administrator> findByUsername(String username) {
        return administratorJpaRepository.findByUsername(username).map(administratorMapper::toDomain);
    }

    @Override
    public Optional<Administrator> findById(String id) {
        return administratorJpaRepository.findById(id).map(administratorMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return administratorJpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return administratorJpaRepository.existsByEmail(email);
    }

    @Override
    public Administrator save(Administrator administrator) {
        AdministratorEntity entity = administratorMapper.toEntity(administrator);
        Set<RoleEntity> roles = roleJpaRepository.findByNameIn(administrator.roleNames());
        entity.setRoles(roles);
        AdministratorEntity saved = administratorJpaRepository.save(entity);
        return administratorMapper.toDomain(saved);
    }

    @Override
    public List<Administrator> findAll() {
        return administratorJpaRepository.findAll().stream().map(administratorMapper::toDomain).toList();
    }

    @Override
    public Administrator updateRoles(String administratorId, Set<String> roleNames) {
        AdministratorEntity entity = findEntityOrThrow(administratorId);
        Set<RoleEntity> roles = roleJpaRepository.findByNameIn(roleNames);
        if (roles.size() != roleNames.size()) {
            throw new DomainValidationException("E-01-ADMINISTRATOR-0011");
        }
        entity.setRoles(roles);
        return administratorMapper.toDomain(administratorJpaRepository.save(entity));
    }

    @Override
    public Administrator updateStatus(String administratorId, AdministratorStatus status) {
        AdministratorEntity entity = findEntityOrThrow(administratorId);
        entity.setStatus(status);
        return administratorMapper.toDomain(administratorJpaRepository.save(entity));
    }

    private AdministratorEntity findEntityOrThrow(String administratorId) {
        return administratorJpaRepository.findById(administratorId)
                .orElseThrow(() -> new DomainValidationException("E-01-ADMINISTRATOR-0010"));
    }
}
