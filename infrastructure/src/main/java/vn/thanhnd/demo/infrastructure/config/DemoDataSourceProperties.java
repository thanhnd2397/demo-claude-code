package vn.thanhnd.demo.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Binds {@code app.datasource.primary} and {@code app.datasource.replicas} properties.
 */
@Data
@ConfigurationProperties(prefix = "app.datasource")
public class DemoDataSourceProperties {

    private Node primary = new Node();
    private List<Node> replicas = new ArrayList<>();

    @Data
    public static class Node {
        private String key;
        private String url;
        private String username;
        private String password;
        private String driverClassName = "com.mysql.cj.jdbc.Driver";
    }
}
