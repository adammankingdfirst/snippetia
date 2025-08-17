package com.snippetia

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class SnippetiaApplicationTests {

    @Test
    fun contextLoads() {
        // Test that the Spring context loads successfully
    }
}