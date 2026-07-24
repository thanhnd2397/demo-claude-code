# Infrastructure Layer — Detailed Reference

## Adapter Full Example: AdministratorAdapterImpl

```java
@Adapter
@RequiredArgsConstructor
public class AdministratorAdapterImpl implements AdministratorAdapter {
    private final AdministratorRepository administratorRepository;
    private final AdministratorMapper administratorMapper;

    @Override
    public Administrator findById(String id) {
        AdministratorEntity entity = administratorRepository.findActiveById(id)
            .orElseThrow(() -> new EntityNotFoundException("W-03-ADMINISTRATOR-0001"));
        return administratorMapper.toFullDomain(entity);
    }

    @Override
    public Administrator getDetailById(String id) {
        AdministratorEntity entity = administratorRepository.findDetailById(id)
            .orElseThrow(() -> new EntityNotFoundException("W-03-ADMINISTRATOR-0001"));
        return administratorMapper.toFullDomain(entity);
    }

    @Override
    public void updateAdministrator(Administrator administrator) {
        AdministratorEntity entity = administratorRepository.findActiveById(administrator.id())
            .orElseThrow(() -> new EntityNotFoundException("W-03-ADMINISTRATOR-0001"));
        administratorMapper.toUpdateEntity(administrator, entity);
        administratorRepository.updateWithOptimisticLock(entity);
        String cacheKey = ApplicationConstants.CACHE_ADMINISTRATOR_DETAIL + administrator.id();
        cacheAdapter.delete(cacheKey);
    }
}
```

---

## Entity Full Example: AdministratorEntity

```java
@Entity
@Table(name = "administrators")
public class AdministratorEntity extends BaseEntity {

    @Column(name = "admin_name")
    private String adminName;

    @Column(name = "email", unique = true)
    private String email;

    @Convert(converter = GenderEnumConverter.class)
    @Column(name = "gender")
    private GenderEnum gender;

    @Convert(converter = DeleteFlagConverter.class)
    @Column(name = "delete_flag")
    private DeleteFlag deleteFlag;

    @OneToMany(mappedBy = "administratorEntity", fetch = FetchType.LAZY)
    private Set<RoleAdministratorEntity> roleAdministratorEntities;
}
```

---

## MapStruct Mapper Full Example: AdministratorMapper

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class AdministratorMapper {

    @Autowired protected RoleMapper roleMapper;
    @Autowired protected PermissionMapper permissionMapper;

    @Mapping(target = "roles", source = "roleAdministratorEntities")
    @Mapping(target = "permissions", source = "administratorPermissionEntities")
    public abstract Administrator toFullDomain(AdministratorEntity entity);

    @Named("toBasicDomain")
    @Mapping(target = "roles", expression = "java(java.util.Collections.emptyList())")
    @Mapping(target = "permissions", expression = "java(java.util.Collections.emptyList())")
    public abstract Administrator toBasicDomain(AdministratorEntity entity);

    @Mapping(target = "roleAdministratorEntities", ignore = true)
    @Mapping(target = "administratorPermissionEntities", ignore = true)
    @Mapping(target = "id", ignore = true)
    public abstract AdministratorEntity toNewEntity(Administrator domain);

    @Mapping(target = "roleAdministratorEntities", ignore = true)
    @Mapping(target = "administratorPermissionEntities", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "id", ignore = true)
    public abstract void toUpdateEntity(Administrator domain, @MappingTarget AdministratorEntity entity);

    // Lazy-load safety: check Hibernate.isInitialized before accessing collections
    protected List<Role> mapRoleEntities(Set<RoleAdministratorEntity> entities) {
        if (CollectionUtils.isEmpty(entities) || !Hibernate.isInitialized(entities)) {
            return Collections.emptyList();
        }
        return entities.stream()
            .map(RoleAdministratorEntity::getRoleEntity)
            .map(roleMapper::toFullDomain)
            .toList();
    }

    @AfterMapping
    protected void populateJunctionTables(Administrator domain,
            @MappingTarget AdministratorEntity entity) {
        if (StringUtils.isNotEmpty(entity.getId())) return; // new entities only
        // populate roleAdministratorEntities etc.
    }
}
```

---

## Custom Repository Tuple Pattern

Full example from `MemberOrganizationRepositoryCustomImpl`:

```java
// Interface
public interface MemberOrganizationRepositoryCustom {
    Page<OrganizationMemberDto> findActiveMembersByOrganization(
        String organizationId, boolean onlyApprovers, Pageable pageable);
}

// Implementation
@Repository
public class MemberOrganizationRepositoryCustomImpl
        implements MemberOrganizationRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<OrganizationMemberDto> findActiveMembersByOrganization(
            String organizationId, boolean onlyApprovers, Pageable pageable) {

        String jpql = """
            SELECT mo, im.name
            FROM MemberOrganizationEntity mo
            LEFT JOIN ItemMasterEntity im ON im.code = mo.positionCode
            WHERE mo.organizationId = :orgId
            """;

        TypedQuery<Tuple> query = entityManager
            .createQuery(jpql, Tuple.class)
            .setParameter("orgId", organizationId);

        List<OrganizationMemberDto> results = query.getResultStream()
            .map(this::toOrganizationMemberDto)
            .toList();

        return new PageImpl<>(results, pageable, results.size());
    }

    private OrganizationMemberDto toOrganizationMemberDto(Tuple row) {
        return OrganizationMemberDto.builder()
            .memberOrganizationEntity(row.get(0, MemberOrganizationEntity.class))
            .positionName(row.get(1, String.class))
            .build();
    }
}
```

Custom DTO record location: `...infrastructure.persistence.repository.dto`

---

## Validator Pattern (Custom Bean Validation)

Validators live in `...infrastructure.validator`. Each constraint is a pair:

- **`{Name}.java`** — annotation with `@Constraint(validatedBy = {Name}Impl.class)`
- **`{Name}Impl.java`** — implements `ConstraintValidator<{Name}, T>`; no Spring injection (pure Java)

### Class-level constraint example: `@PasswordMatch`

Cross-field validation reads both fields via `BeanWrapperImpl` and reports the violation on the target field node.

```java
// Annotation
@Documented
@Constraint(validatedBy = PasswordMatchImpl.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatch {
    String message() default "{E-04-ADMINISTRATOR-0004}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String passwordField() default "newPassword";
    String confirmPasswordField() default "confirmPassword";
}

// Implementation
public class PasswordMatchImpl implements ConstraintValidator<PasswordMatch, Object> {
    private String passwordFieldName;
    private String confirmPasswordFieldName;

    @Override
    public void initialize(PasswordMatch annotation) {
        this.passwordFieldName = annotation.passwordField();
        this.confirmPasswordFieldName = annotation.confirmPasswordField();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (object == null) return true;
        BeanWrapper bw = new BeanWrapperImpl(object);
        String pw = (String) bw.getPropertyValue(passwordFieldName);
        String confirm = (String) bw.getPropertyValue(confirmPasswordFieldName);
        // Let @NotBlank handle null/empty; avoids duplicate messages
        if (StringUtils.isEmpty(pw) || StringUtils.isEmpty(confirm)) return true;
        boolean match = Objects.equals(pw, confirm);
        if (!match) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(confirmPasswordFieldName).addConstraintViolation();
        }
        return match;
    }
}
```

### Field-level constraint example: `@ValidAttachments`

Field validators receive the value directly. Always call `disableDefaultConstraintViolation()` then build with the specific error code.

```java
// Annotation
@Documented
@Constraint(validatedBy = ValidAttachmentsImpl.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAttachments {
    String message() default "";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Implementation — validate returns false on first violation
public class ValidAttachmentsImpl
        implements ConstraintValidator<ValidAttachments, List<MultipartFile>> {

    private static final long MAX_FILE_SIZE_BYTES = 10_485_760L;
    private static final int MAX_FILE_COUNT = 3;
    private static final Set<String> ALLOWED_EXTENSIONS =
        Set.of("doc", "docx", "xls", "xlsx", "png", "jpg", "jpeg");

    @Override
    public boolean isValid(List<MultipartFile> files, ConstraintValidatorContext context) {
        if (files == null || files.isEmpty()) return true;
        List<MultipartFile> nonEmpty = files.stream().filter(f -> !f.isEmpty()).toList();
        if (nonEmpty.isEmpty()) return true;

        context.disableDefaultConstraintViolation();

        if (nonEmpty.size() > MAX_FILE_COUNT) {
            context.buildConstraintViolationWithTemplate("{E-04-TICKET-CREATE-0010}")
                .addConstraintViolation();
            return false;
        }
        for (MultipartFile file : nonEmpty) {
            // extension, size, forbidden chars checks — each returns false with its own code
        }
        return true;
    }
}
```

**Usage on a request DTO:**

```java
@ValidAttachments
private List<MultipartFile> attachments;

@PasswordMatch
public class UpdatePasswordRequest { ... }
```

---

## Email Infrastructure

### EmailTemplateConfiguration (`config/`)

Provides a standalone Thymeleaf `emailTemplateEngine` bean used only for rendering outbound email HTML — unrelated to any web view layer. Resolves templates from `classpath:/templates/` with `.html` suffix.

```java
@Configuration
public class EmailTemplateConfiguration {
    @Bean(name = "emailTemplateEngine")
    protected TemplateEngine emailTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setTemplateMode("HTML");
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
```

### MailSenderAdapterImpl (`adapter/`)

Sends email via `JavaMailSender` (SMTP). Uses `spring.mail.from` as the default sender when `MailMessage.from` is null. Wraps `MailException` → `MailSendException("E-03-MAIL-0001")` and `MessagingException` → `MailSendException("E-03-MAIL-0002")`.

```java
@Adapter
@RequiredArgsConstructor
@Log4j2
public class MailSenderAdapterImpl implements MailSenderAdapter {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.from:}")
    private String defaultFrom;

    @Override
    public void send(MailMessage message) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(message.to());
            helper.setSubject(message.subject());
            String from = StringUtils.isNotBlank(message.from()) ? message.from() : defaultFrom;
            if (StringUtils.isNotBlank(from)) helper.setFrom(from);
            if (StringUtils.isNotBlank(message.htmlBody())) {
                helper.setText(message.textBody(), message.htmlBody());
            } else {
                helper.setText(message.textBody(), false);
            }
            javaMailSender.send(mimeMessage);
        } catch (MailException e) {
            throw new MailSendException("E-03-MAIL-0001", e);
        } catch (MessagingException e) {
            throw new MailSendException("E-03-MAIL-0002", e);
        }
    }
}
```

---

## Redis (CacheAdapter)

`RedisAdapterImpl` implements `CacheAdapter`. Supports standalone, sentinel, and cluster modes.
Config: `vn.thanhnd.demo.infrastructure.cache.config.RedisConfiguration`

Operations: `get(key, Class<T>)`, `set(key, data, expiredAt)`, `delete(key)`, `deleteByPattern(pattern)`, `exists(key)`.
Errors wrapped in `RedisException`.

---

## MinIO (ObjectStorageAdapter)

`MinioStorageAdapterImpl` implements `ObjectStorageAdapter`. Conditional on `app.storage.minio.enabled=true`.

| Property | Default | Description |
| -------- | ------- | ----------- |
| `app.storage.minio.enabled` | `false` | activates MinIO beans |
| `app.storage.minio.endpoint` | `http://localhost:9000` | MinIO server |
| `app.storage.minio.accessKey` | `""` | access key |
| `app.storage.minio.secretKey` | `""` | secret key |
| `app.storage.minio.defaultBucket` | `monolithic-default` | fallback when bucket is null |

Operations: `put`, `get`, `delete`, `exists`. Null/empty bucket → `defaultBucket`. `..` in objectKey → `StorageException("E-03-STORAGE-0001")`.
