package vn.thanhnd.demo.infrastructure.adapter;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import vn.thanhnd.demo.domain.adapter.PasswordHasher;
import vn.thanhnd.demo.util.annotation.Adapter;

@Adapter
public class BCryptPasswordHasherImpl implements PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return encoder.matches(rawPassword, passwordHash);
    }
}
