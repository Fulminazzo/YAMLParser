package it.fulminazzo.yamlparser.configuration;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class SimpleConfigurationTest {

    @Test
    void testEmptySimpleConfiguration() {
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration();
        SimpleConfiguration expected = new SimpleConfiguration("", new LinkedHashMap<>());
        assertEquals(simpleConfiguration, expected);
    }
}