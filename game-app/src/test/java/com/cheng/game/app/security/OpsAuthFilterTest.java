package com.cheng.game.app.security;

import com.cheng.game.app.config.AppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpsAuthFilterTest {

    @Test
    void rejectsMissingAndWrongTokenWith403() throws Exception {
        OpsAuthFilter filter = newFilter("ops-secret");

        MockHttpServletResponse missing = dispatch(filter, null);
        assertEquals(403, missing.getStatus());
        assertTrue(missing.getContentAsString().contains("invalid ops token"));

        MockHttpServletResponse wrong = dispatch(filter, "nope");
        assertEquals(403, wrong.getStatus());
        assertTrue(wrong.getContentAsString().contains("invalid ops token"));
    }

    @Test
    void acceptsMatchingToken() throws Exception {
        OpsAuthFilter filter = newFilter("ops-secret");
        AtomicBoolean continued = new AtomicBoolean();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ops/online");
        request.addHeader(OpsAuthFilter.HEADER, "ops-secret");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> continued.set(true));
        assertTrue(continued.get());
        assertEquals(200, response.getStatus());
    }

    @Test
    void matchesUsesConstantTimeEquality() {
        assertTrue(OpsAuthFilter.matches("abc", "abc"));
        assertFalse(OpsAuthFilter.matches("abc", "abd"));
        assertFalse(OpsAuthFilter.matches("abc", null));
        assertFalse(OpsAuthFilter.matches(" ", " "));
    }

    private static OpsAuthFilter newFilter(String token) {
        AppProperties properties = new AppProperties();
        properties.setOpsToken(token);
        return new OpsAuthFilter(properties, new ObjectMapper());
    }

    private static MockHttpServletResponse dispatch(OpsAuthFilter filter, String token)
            throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ops/online");
        if (token != null) {
            request.addHeader(OpsAuthFilter.HEADER, token);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean continued = new AtomicBoolean();
        filter.doFilter(request, response, (req, res) -> continued.set(true));
        assertFalse(continued.get());
        return response;
    }
}
