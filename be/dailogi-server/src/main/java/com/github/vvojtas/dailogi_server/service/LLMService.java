package com.github.vvojtas.dailogi_server.service;

import com.github.vvojtas.dailogi_server.db.repository.LLMRepository;
import com.github.vvojtas.dailogi_server.mapper.LLMMapper;
import com.github.vvojtas.dailogi_server.model.llm.response.LLMDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LLMService {
    private final LLMRepository llmRepository;
    private final LLMMapper llmMapper;

    @Transactional(readOnly = true)
    public List<LLMDTO> getLLMs() {
        log.debug("Fetching all LLMs");
        List<LLMDTO> llms = llmMapper.toDTOs(llmRepository.findAll());
        log.info("Found {} LLMs", llms.size());
        return llms;
    }
} 