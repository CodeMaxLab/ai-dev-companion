package com.max.ai_dev_companion.application.port;

public interface EmbeddingGateway {

    float[] embed(String text);
}
