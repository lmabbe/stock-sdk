package io.github.stock.sdk.config;

/**
 * HTTP / SOCKS 代理配置
 */
public class ProxyConfig {

    public enum Type {
        HTTP,
        SOCKS
    }

    private boolean enabled = false;
    private Type type = Type.HTTP;
    private String host;
    private int port = 8080;
    private String username;
    private String password;

    public ProxyConfig() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ProxyConfig disabled() {
        return new ProxyConfig();
    }

    public boolean isEnabled() {
        return enabled && host != null && !host.trim().isEmpty() && port > 0;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type != null ? type : Type.HTTP;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static class Builder {
        private final ProxyConfig config = new ProxyConfig();

        public Builder enabled(boolean enabled) {
            config.setEnabled(enabled);
            return this;
        }

        public Builder type(Type type) {
            config.setType(type);
            return this;
        }

        public Builder host(String host) {
            config.setHost(host);
            return this;
        }

        public Builder port(int port) {
            config.setPort(port);
            return this;
        }

        public Builder username(String username) {
            config.setUsername(username);
            return this;
        }

        public Builder password(String password) {
            config.setPassword(password);
            return this;
        }

        public ProxyConfig build() {
            return config;
        }
    }
}
