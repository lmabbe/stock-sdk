package io.github.stock.sdk;

import io.github.stock.sdk.config.ProxyConfig;
import io.github.stock.sdk.config.SdkConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProxyConfigTest {

    @Test
    public void testProxyConfig() {
        ProxyConfig proxy = ProxyConfig.builder()
                .enabled(true)
                .type(ProxyConfig.Type.HTTP)
                .host("127.0.0.1")
                .port(7890)
                .username("user")
                .password("pwd")
                .build();

        Assertions.assertTrue(proxy.isEnabled());
        Assertions.assertEquals("127.0.0.1", proxy.getHost());
        Assertions.assertEquals(7890, proxy.getPort());
        Assertions.assertEquals("user", proxy.getUsername());

        SdkConfig config = SdkConfig.builder()
                .proxyConfig(proxy)
                .build();

        Assertions.assertNotNull(config.getProxyConfig());
        Assertions.assertTrue(config.getProxyConfig().isEnabled());
    }
}
