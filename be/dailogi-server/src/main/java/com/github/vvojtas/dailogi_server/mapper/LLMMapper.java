package com.github.vvojtas.dailogi_server.mapper;

import com.github.vvojtas.dailogi_server.db.entity.LLM;
import com.github.vvojtas.dailogi_server.model.llm.response.LLMDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LLMMapper {
    
    public LLMDTO toDTO(LLM llm) {
        return new LLMDTO(
            llm.getId(),
            llm.getName(),
            llm.getOpenrouterIdentifier()
        );
    }

    public List<LLMDTO> toDTOs(List<LLM> llms) {
        return llms.stream()
            .map(this::toDTO)
            .toList();
    }
} 