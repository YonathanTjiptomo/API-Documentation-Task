package org.example;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RateLimitingFilter implements Filter {
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final long capacity = 10;
    private final long tokensPerInterval = 1;
    private final long interval = 1;
    private final TimeUnit unit = TimeUnit.SECONDS;

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String clientId = request.getRemoteAddr(); // or use a more unique identifier

        TokenBucket tokenBucket = buckets.computeIfAbsent(clientId, k -> new TokenBucket(capacity, tokensPerInterval, interval, unit));

        if (tokenBucket.tryConsume()) {
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).setStatus(429); // 429 Too Many Requests
            response.getWriter().write("Rate limit exceeded");
        }
    }

    @Override
    public void destroy() {
    }
}
