package vn.thanhnd.demo.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import vn.thanhnd.demo.domain.adapter.RoleRepositoryPort;
import vn.thanhnd.demo.domain.model.Role;
import vn.thanhnd.demo.infrastructure.mapper.RoleMapper;
import vn.thanhnd.demo.infrastructure.persistence.repository.RoleJpaRepository;
import vn.thanhnd.demo.util.annotation.Adapter;

import java.util.Set;
import java.util.stream.Collectors;

@Adapter
@RequiredArgsConstructor
public class RoleRepositoryAdapterImpl implements RoleRepositoryPort {

    private final RoleJpaRepository roleJpaRepository;
    private final RoleMapper roleMapper;

    @Override
    public Set<Role> findByNames(Set<String> names) {
        return roleJpaRepository.findByNameIn(names).stream()
                .map(roleMapper::toDomain)
                .collect(Collectors.toSet());
    }
}
