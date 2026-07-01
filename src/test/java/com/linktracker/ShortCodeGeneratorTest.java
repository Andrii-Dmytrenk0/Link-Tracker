package com.linktracker;

import com.linktracker.util.ShortCodeGenerator;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShortCodeGeneratorTest {

    @RepeatedTest(20)
    void generatesCodeOfDefaultLength() {
        String code = ShortCodeGenerator.generate();
        assertEquals(7, code.length());
        assertTrue(code.matches("^[A-Za-z0-9]+$"));
    }

    @Test
    void generatesCodeOfRequestedLength() {
        String code = ShortCodeGenerator.generate(8);
        assertEquals(8, code.length());
    }

    @Test
    void successiveCodesAreLikelyUnique() {
        String a = ShortCodeGenerator.generate();
        String b = ShortCodeGenerator.generate();
        assertTrue(!a.equals(b), "Two randomly generated codes collided, which is statistically very unlikely");
    }
}
