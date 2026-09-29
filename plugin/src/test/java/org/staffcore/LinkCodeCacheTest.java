package org.staffcore;

import org.junit.jupiter.api.Test;
import org.staffcore.link.LinkCodeCache;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class LinkCodeCacheTest {

    @Test
    void testCodeGenerationAndConsume() {
        LinkCodeCache cache = new LinkCodeCache(300, "MC-");
        UUID uuid = UUID.randomUUID();

        String code = cache.generateCode(uuid);
        assertNotNull(code);
        assertTrue(code.startsWith("MC-"));

        // First consume succeeds
        Optional<UUID> consumed = cache.consumeCode(code);
        assertTrue(consumed.isPresent());
        assertEquals(uuid, consumed.get());

        // Second consume fails (single-use)
        Optional<UUID> consumedAgain = cache.consumeCode(code);
        assertTrue(consumedAgain.isEmpty());
    }

    @Test
    void testCodeExpiry() throws InterruptedException {
        LinkCodeCache cache = new LinkCodeCache(1, "MC-"); // 1s TTL
        UUID uuid = UUID.randomUUID();

        String code = cache.generateCode(uuid);
        Thread.sleep(1200);

        Optional<UUID> consumed = cache.consumeCode(code);
        assertTrue(consumed.isEmpty(), "Expired code should not be consumed");
    }
}
