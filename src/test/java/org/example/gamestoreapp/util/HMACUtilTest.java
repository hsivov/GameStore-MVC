package org.example.gamestoreapp.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HMACUtilTest {

    @Test
    void testGenerateHMAC() {
        String data = "HelloWorld";
        String key = "SecretKey";

        // Expected output for validation (computed from a trusted source)
        String expectedHMAC = "dcY9c1XM1dqho0VYeI78edGJe833XVWS8YNoLz2KrMM=";

        String actualHMAC = HMACUtil.generateHMAC(data, key);

        assertEquals(expectedHMAC, actualHMAC, "HMAC output should match the expected value.");
    }

    @Test
    void testGenerateHMACWithEmptyData() {
        String data = "";
        String key = "SecretKey";

        String expectedHMAC = "vLvN4Xdkm+pRqkWRk3JMZFLIYAwASg2apgT81shZFI4="; // Precomputed expected value
        String actualHMAC = HMACUtil.generateHMAC(data, key);

        assertEquals(expectedHMAC, actualHMAC, "HMAC for empty data should be correct.");
    }
}