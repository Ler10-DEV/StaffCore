package org.staffcore;

import org.junit.jupiter.api.Test;
import org.staffcore.commandlog.MaskFilter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MaskFilterTest {

    @Test
    void testSensitiveCommandMasking() {
        MaskFilter filter = new MaskFilter(List.of("(?i)^/(login|register|changepassword|auth|pin|email|pass|setpassword|changepass)\\b.*"));

        assertEquals("/login *******", filter.maskCommand("/login secret123"));
        assertEquals("/register *******", filter.maskCommand("/register secret123 secret123"));
        assertEquals("/changepassword *******", filter.maskCommand("/changepassword oldPass newPass"));
        assertEquals("/help", filter.maskCommand("/help"));
        assertEquals("/spawn", filter.maskCommand("/spawn"));
    }
}
