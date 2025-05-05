package com.github.vvojtas.dailogi_server.llm.application;

import com.github.vvojtas.dailogi_server.db.repository.LLMRepository;
import com.github.vvojtas.dailogi_server.llm.api.GetLLMsQuery;
import com.github.vvojtas.dailogi_server.model.llm.mapper.LLMMapper;
import com.github.vvojtas.dailogi_server.model.llm.response.LLMDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LLMQueryService {
    private final LLMRepository llmRepository;
    private final LLMMapper llmMapper;

    public List<LLMDTO> getLLMs(GetLLMsQuery query) {
        log.debug("Fetching all LLMs");
        List<LLMDTO> llms = llmMapper.toDTOs(llmRepository.findAll());
        log.info("Found {} LLMs", llms.size());
        return llms;
    }
} 