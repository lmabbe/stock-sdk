package io.github.stock.sdk.util;

import io.github.stock.sdk.config.SdkConfig;
import io.github.stock.sdk.exception.StockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 基于 Java 11 HttpClient 的轻量网络请求工具
 */
public class HttpClientHelper {

    private static final Logger log = LoggerFactory.getLogger(HttpClientHelper.class);
    public static final Charset GBK = Charset.forName("GBK");

    private final HttpClient httpClient;
    private final SdkConfig config;

    public HttpClientHelper(SdkConfig config) {
        this.config = config != null ? config : SdkConfig.defaultConfig();
        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(this.config.getTimeout())
                .followRedirects(HttpClient.Redirect.NORMAL);

        io.github.stock.sdk.config.ProxyConfig pc = this.config.getProxyConfig();
        if (pc != null && pc.isEnabled()) {
            clientBuilder.proxy(java.net.ProxySelector.of(new java.net.InetSocketAddress(pc.getHost(), pc.getPort())));
            if (pc.getUsername() != null && !pc.getUsername().isEmpty()) {
                clientBuilder.authenticator(new java.net.Authenticator() {
                    @Override
                    protected java.net.PasswordAuthentication getPasswordAuthentication() {
                        return new java.net.PasswordAuthentication(pc.getUsername(),
                                (pc.getPassword() != null ? pc.getPassword() : "").toCharArray());
                    }
                });
            }
            log.info("HttpClient configured with proxy: {}:{} ({})", pc.getHost(), pc.getPort(), pc.getType());
        }

        this.httpClient = clientBuilder.build();
    }

    /**
     * 发起 GET 请求并按指定编码返回字符串响应
     */
    public String get(String url, Map<String, String> headers, Charset charset) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(config.getTimeout())
                .header("User-Agent", config.getUserAgent())
                .GET();

        if (headers != null) {
            headers.forEach(builder::header);
        }

        HttpRequest request = builder.build();
        int attempts = 0;
        int maxRetries = Math.max(0, config.getMaxRetries());
        Exception lastException = null;

        while (attempts <= maxRetries) {
            attempts++;
            try {
                HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    Charset cs = charset != null ? charset : StandardCharsets.UTF_8;
                    return new String(response.body(), cs);
                } else {
                    throw new StockException("HTTP_ERROR", "Request to " + url + " failed with status: " + response.statusCode());
                }
            } catch (IOException | InterruptedException e) {
                lastException = e;
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    throw new StockException("INTERRUPTED", "HTTP request interrupted", e);
                }
                log.warn("GET request to {} attempt {} failed: {}", url, attempts, e.getMessage());
                if (attempts <= maxRetries) {
                    try {
                        Thread.sleep(200L * attempts);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new StockException("INTERRUPTED", "Retry sleep interrupted", ie);
                    }
                }
            }
        }

        throw new StockException("NETWORK_TIMEOUT", "Failed to GET " + url + " after " + (maxRetries + 1) + " attempts", lastException);
    }

    /**
     * 默认以 UTF-8 发起 GET 请求
     */
    public String get(String url, Map<String, String> headers) {
        return get(url, headers, StandardCharsets.UTF_8);
    }

    /**
     * 异步 GET 请求
     */
    public CompletableFuture<String> getAsync(String url, Map<String, String> headers, Charset charset) {
        return CompletableFuture.supplyAsync(() -> get(url, headers, charset));
    }
}
