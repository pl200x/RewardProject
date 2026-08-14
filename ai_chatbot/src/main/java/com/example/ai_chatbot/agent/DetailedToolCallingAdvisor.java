package com.example.ai_chatbot.agent;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 将工具调用循环显式化并限制最大迭代次数。日志仅记录元数据，
 * 避免把用户输入、模型内容、工具参数或工具结果写入生产日志。
 */
public class DetailedToolCallingAdvisor extends ToolCallingAdvisor {

    private static final Logger log = LoggerFactory.getLogger(DetailedToolCallingAdvisor.class);

    private final int maxIterations;

    public DetailedToolCallingAdvisor(ToolCallingManager toolCallingManager, int maxIterations) {
        super(toolCallingManager,
                chatResponse -> chatResponse != null && chatResponse.hasToolCalls(),
                DEFAULT_ORDER,
                true);
        Assert.isTrue(maxIterations > 0, "maxIterations must be greater than 0");
        this.maxIterations = maxIterations;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        Assert.notNull(request, "request must not be null");
        Assert.notNull(chain, "chain must not be null");

        if (!(request.prompt().getOptions() instanceof ToolCallingChatOptions options)) {
            return chain.nextCall(request);
        }

        List<Message> instructions = request.prompt().getInstructions();

        for (int iteration = 1; iteration <= this.maxIterations; iteration++) {
            log.info("========== HelloAgent 思考轮次 {}/{} ==========", iteration, this.maxIterations);

            ChatClientRequest iterationRequest = ChatClientRequest.builder()
                    .prompt(new Prompt(instructions, options))
                    .context(request.context())
                    .build();

            ChatClientResponse clientResponse = chain.copy(this).nextCall(iterationRequest);
            ChatResponse chatResponse = clientResponse.chatResponse();
            Assert.notNull(chatResponse, "chatResponse must not be null");

            printModelResponse(chatResponse);

            if (!chatResponse.hasToolCalls()) {
                log.info("HelloAgent 在第 {} 轮完成回答", iteration);
                return clientResponse;
            }

            if (iteration == this.maxIterations) {
                String message = "已达到最大思考轮次上限（" + this.maxIterations + "），停止继续调用工具。";
                log.warn(message);
                ChatResponse limitedResponse = ChatResponse.builder()
                        .from(chatResponse)
                        .generations(List.of(new Generation(new AssistantMessage(message))))
                        .build();
                return clientResponse.mutate().chatResponse(limitedResponse).build();
            }

            ToolExecutionResult executionResult = this.toolCallingManager
                    .executeToolCalls(iterationRequest.prompt(), chatResponse);
            printToolResults(executionResult);

            if (executionResult.returnDirect()) {
                ChatResponse directResponse = ChatResponse.builder()
                        .from(chatResponse)
                        .generations(ToolExecutionResult.buildGenerations(executionResult))
                        .build();
                return clientResponse.mutate().chatResponse(directResponse).build();
            }

            instructions = executionResult.conversationHistory();
        }

        throw new IllegalStateException("Unexpected end of HelloAgent iteration loop");
    }

    private void printModelResponse(ChatResponse chatResponse) {
        for (Generation generation : chatResponse.getResults()) {
            AssistantMessage output = generation.getOutput();
            log.debug("模型响应完成: finishReason={}, hasText={}",
                    generation.getMetadata().getFinishReason(),
                    output.getText() != null && !output.getText().isBlank());

            for (AssistantMessage.ToolCall toolCall : output.getToolCalls()) {
                log.debug("调用工具: name={}, id={}, type={}",
                        toolCall.name(), toolCall.id(), toolCall.type());
            }
        }
    }

    private void printToolResults(ToolExecutionResult executionResult) {
        for (Message message : executionResult.conversationHistory()) {
            if (message instanceof ToolResponseMessage toolResponseMessage) {
                for (ToolResponseMessage.ToolResponse response : toolResponseMessage.getResponses()) {
                    log.debug("工具执行完成: name={}, id={}", response.name(), response.id());
                }
            }
        }
    }
}
