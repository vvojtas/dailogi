package com.github.vvojtas.dailogi_server.character.api;

public sealed interface ValidationError {
    record DuplicateName(String name) implements ValidationError {}
    record CharacterLimitExceeded(int limit) implements ValidationError {}
    record InvalidLLMReference(Long llmId) implements ValidationError {}
    record CharacterNotFound(Long id) implements ValidationError {}
    record CharacterInUse(Long id) implements ValidationError {}
    record UnauthorizedAccess(Long characterId, Long userId) implements ValidationError {}
} 