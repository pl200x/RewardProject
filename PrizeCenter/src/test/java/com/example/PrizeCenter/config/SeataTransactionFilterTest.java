package com.example.PrizeCenter.config;

import org.apache.seata.core.context.RootContext;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

class SeataTransactionFilterTest {
    private final SeataTransactionFilter filter = new SeataTransactionFilter();

    @Test
    void bindsInternalAwardAndUnbindsEvenAfterException() {
        var request = new MockHttpServletRequest("POST", "/prize/send_reward");
        request.setServletPath("/prize/send_reward");
        request.addHeader("TX_XID", "127.0.0.1:18091:123");
        assertThrows(IllegalStateException.class, () -> filter.doFilter(request, new MockHttpServletResponse(),
                (req, res) -> {
                    assertEquals("127.0.0.1:18091:123", RootContext.getXID());
                    throw new IllegalStateException("downstream failed");
                }));
        assertNull(RootContext.getXID());
    }

    @Test
    void ignoresTransactionHeadersOnOtherEndpoints() throws Exception {
        var request = new MockHttpServletRequest("GET", "/prize/search");
        request.setServletPath("/prize/search");
        request.addHeader("TX_XID", "untrusted");
        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> assertNull(RootContext.getXID()));
        assertNull(RootContext.getXID());
    }
}
