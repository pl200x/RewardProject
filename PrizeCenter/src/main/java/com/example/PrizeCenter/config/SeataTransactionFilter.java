package com.example.PrizeCenter.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.seata.core.context.RootContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Feign propagates XID to this internal endpoint; always release pooled servlet threads. */
@Component
@ConditionalOnProperty(name = "seata.enabled", havingValue = "true", matchIfMissing = true)
public class SeataTransactionFilter extends OncePerRequestFilter {
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod())
                || !"/prize/send_reward".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String xid = request.getHeader(RootContext.KEY_XID);
        String previous = RootContext.getXID();
        try {
            if (xid != null && !xid.isBlank()) RootContext.bind(xid);
            chain.doFilter(request, response);
        } finally {
            RootContext.unbind();
            if (previous != null) RootContext.bind(previous);
        }
    }
}
