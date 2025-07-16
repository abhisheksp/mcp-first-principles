package com.watchtower.llm;

import com.watchtower.functions.FunctionCall;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response from LLM that may include function calls
 */
@Data
@AllArgsConstructor
public class LLMResponse {
    private String content;
    private FunctionCall functionCall;
    private boolean needsMoreData;
    
    public static LLMResponse functionCall(String reasoning, FunctionCall call) {
        return new LLMResponse(reasoning, call, true);
    }
    
    public static LLMResponse finalAnswer(String answer) {
        return new LLMResponse(answer, null, false);
    }
}