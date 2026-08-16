package com.max.ai_dev_companion.config;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.max.ai_dev_companion.infrastructure.llm.LlmConfig;

import dev.langchain4j.model.chat.ChatModel;

class GroqPropertiesResolutionTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(LlmConfig.class)
            .withPropertyValues("groq.api-key=test-groq-key");

    @Test
    void chatModelBeanIsCreatedWhenGroqApiKeyIsPresent() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(ChatModel.class)).isNotNull();
        });
    }

    @Test
    void quotedGroqApiKeyIsNormalized() {
        LlmConfig config = new LlmConfig();
        ReflectionTestUtils.setField(config, "apiKey", "\"test-groq-key\"");

        config.init();

        assertThat(ReflectionTestUtils.getField(config, "apiKey")).isEqualTo("test-groq-key");
    }
}
