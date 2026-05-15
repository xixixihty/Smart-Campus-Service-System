package com.hxq.smart_campus.model;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.metadata.*;
import org.springframework.ai.chat.model.*;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.chat.observation.ChatModelObservationDocumentation;
import org.springframework.ai.chat.observation.DefaultChatModelObservationConvention;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.*;
import org.springframework.util.LinkedMultiValueMap;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AlibabaOpenAiChatModel implements ChatModel {

    private static final Logger logger = LoggerFactory.getLogger(AlibabaOpenAiChatModel.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final ChatModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultChatModelObservationConvention();

    private static final ToolCallingManager DEFAULT_TOOL_CALLING_MANAGER = ToolCallingManager.builder().build();

    private final OpenAiChatOptions defaultOptions;
    private final RetryTemplate retryTemplate;
    private final OpenAiApi openAiApi;
    private final ObservationRegistry observationRegistry;
    private final ToolCallingManager toolCallingManager;
    private final String apiKey;
    private ChatModelObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;
    private List<ToolDefinition> registeredTools = new ArrayList<>();

    @Deprecated
    public AlibabaOpenAiChatModel(OpenAiApi openAiApi) {
        this(openAiApi, OpenAiChatOptions.builder().model("qwen-max").temperature(0.7).build());
    }

    @Deprecated
    public AlibabaOpenAiChatModel(OpenAiApi openAiApi, OpenAiChatOptions options) {
        this(openAiApi, options, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public AlibabaOpenAiChatModel(OpenAiApi openAiApi, OpenAiChatOptions options, RetryTemplate retryTemplate) {
        this(openAiApi, options, DEFAULT_TOOL_CALLING_MANAGER, retryTemplate, ObservationRegistry.NOOP);
    }

    public AlibabaOpenAiChatModel(OpenAiApi openAiApi, OpenAiChatOptions defaultOptions, ToolCallingManager toolCallingManager, RetryTemplate retryTemplate, ObservationRegistry observationRegistry) {
        this(openAiApi, defaultOptions, toolCallingManager, retryTemplate, observationRegistry, null);
    }

    public AlibabaOpenAiChatModel(OpenAiApi openAiApi, OpenAiChatOptions defaultOptions, ToolCallingManager toolCallingManager, RetryTemplate retryTemplate, ObservationRegistry observationRegistry, String apiKey) {
        Assert.notNull(openAiApi, "openAiApi cannot be null");
        Assert.notNull(defaultOptions, "defaultOptions cannot be null");
        Assert.notNull(toolCallingManager, "toolCallingManager cannot be null");
        Assert.notNull(retryTemplate, "retryTemplate cannot be null");
        Assert.notNull(observationRegistry, "observationRegistry cannot be null");
        this.openAiApi = openAiApi;
        this.defaultOptions = defaultOptions;
        this.toolCallingManager = toolCallingManager;
        this.retryTemplate = retryTemplate;
        this.observationRegistry = observationRegistry;
        this.apiKey = apiKey;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        Prompt requestPrompt = buildRequestPrompt(prompt);
        return this.internalCall(requestPrompt, null);
    }

    public ChatResponse internalCall(Prompt prompt, ChatResponse previousChatResponse) {
        OpenAiApi.ChatCompletionRequest request = createRequest(prompt, false);

        ChatModelObservationContext observationContext = ChatModelObservationContext.builder()
                .prompt(prompt)
                .provider("openai")
                .build();

        ChatResponse response = ChatModelObservationDocumentation.CHAT_MODEL_OPERATION
                .observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext, this.observationRegistry)
                .observe(() -> {
                    try {
                        if (logger.isDebugEnabled()) {
                            logger.debug("API Request: {}", request);
                            logger.debug("API Headers: {}", getAdditionalHttpHeaders(prompt));
                        }
                        
                        ResponseEntity<OpenAiApi.ChatCompletion> completionEntity = this.retryTemplate.execute(ctx -> {
                            try {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Executing API call to: {}", openAiApi);
                                    logger.debug("API Request Body: {}", request);
                                }
                                return this.openAiApi.chatCompletionEntity(request, getAdditionalHttpHeaders(prompt));
                            } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
                                logger.error("API call failed with status: {}, body: {}, URL: {}", 
                                    e.getStatusCode(), e.getResponseBodyAsString(), openAiApi.toString(), e);
                                throw e;
                            } catch (Exception e) {
                                logger.error("API call failed for request: {}, URL: {}", request, openAiApi.toString(), e);
                                throw e;
                            }
                        });

                        var chatCompletion = completionEntity.getBody();
                        if (chatCompletion == null) {
                            logger.warn("No chat completion returned for prompt: {}", prompt);
                            return new ChatResponse(List.of());
                        }

                        List<OpenAiApi.ChatCompletion.Choice> choices = chatCompletion.choices();
                        if (choices == null || choices.isEmpty()) {
                            logger.warn("No choices returned for prompt: {}", prompt);
                            return new ChatResponse(List.of());
                        }

                        List<Generation> generations = choices.stream().map(choice -> {
                            Map<String, Object> metadata = Map.of(
                                    "id", chatCompletion.id() != null ? chatCompletion.id() : "",
                                    "role", choice.message().role() != null ? choice.message().role().name() : "",
                                    "index", choice.index(),
                                    "finishReason", choice.finishReason() != null ? choice.finishReason().name() : "",
                                    "refusal", StringUtils.hasText(choice.message().refusal()) ? choice.message().refusal() : ""
                            );
                            return buildGeneration(choice, metadata, request);
                        }).toList();

                        OpenAiApi.Usage usage = completionEntity.getBody().usage();
                        Usage currentChatResponseUsage = usage != null ? getDefaultUsage(usage) : new EmptyUsage();
                        Usage accumulatedUsage = previousChatResponse != null && previousChatResponse.getMetadata() != null
                                ? new DefaultUsage(
                                        currentChatResponseUsage.getPromptTokens() + previousChatResponse.getMetadata().getUsage().getPromptTokens(),
                                        currentChatResponseUsage.getCompletionTokens() + previousChatResponse.getMetadata().getUsage().getCompletionTokens(),
                                        currentChatResponseUsage.getTotalTokens() + previousChatResponse.getMetadata().getUsage().getTotalTokens(),
                                        null
                                  )
                                : currentChatResponseUsage;

                        ChatResponse chatResponse = new ChatResponse(generations, fromChatCompletion(chatCompletion, accumulatedUsage));

                        observationContext.setResponse(chatResponse);
                        return chatResponse;
                    } catch (Exception e) {
                        logger.error("Error processing chat completion: {}", e.getMessage(), e);
                        throw new RuntimeException("Failed to process chat completion: " + e.getMessage(), e);
                    }
                });

        if (response != null && response.hasToolCalls()) {
            try {
                int toolCount = response.getResult().getOutput().getToolCalls() != null ?
                        response.getResult().getOutput().getToolCalls().size() : 0;
                logger.info("检测到 {} 个工具调用，开始执行", toolCount);
                var toolExecutionResult = this.toolCallingManager.executeToolCalls(prompt, response);
                if (toolExecutionResult.returnDirect()) {
                    logger.info("工具调用直接返回结果");
                    return ChatResponse.builder()
                            .from(response)
                            .generations(ToolExecutionResult.buildGenerations(toolExecutionResult))
                            .build();
                } else {
                    logger.info("工具调用执行完成，递归调用模型获取文本响应");
                    return this.internalCall(new Prompt(toolExecutionResult.conversationHistory(), prompt.getOptions()), response);
                }
            } catch (Exception e) {
                logger.error("Error executing tool calls: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to execute tool calls: " + e.getMessage(), e);
            }
        }

        String responseContent = response != null && response.getResult() != null ?
                response.getResult().getOutput().getText() : "null";
        logger.info("AI模型最终响应: hasToolCalls={}, contentLength={}",
                response != null ? response.hasToolCalls() : false,
                responseContent != null ? responseContent.length() : 0);
        return response;
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        Prompt requestPrompt = buildRequestPrompt(prompt);
        return internalStream(requestPrompt, null);
    }

    public Flux<ChatResponse> internalStream(Prompt prompt, ChatResponse previousChatResponse) {
        return Flux.deferContextual(contextView -> {
            OpenAiApi.ChatCompletionRequest request = createRequest(prompt, true);

            if (logger.isDebugEnabled()) {
                logger.debug("Stream API Request: {}", request);
                logger.debug("Stream API Headers: {}", getAdditionalHttpHeaders(prompt));
            }

            Flux<OpenAiApi.ChatCompletionChunk> completionChunks = this.openAiApi.chatCompletionStream(request, getAdditionalHttpHeaders(prompt))
                .retryWhen(Retry.backoff(2, java.time.Duration.ofMillis(500))
                        .maxBackoff(java.time.Duration.ofSeconds(5))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException
                                && (((org.springframework.web.reactive.function.client.WebClientResponseException) throwable)
                                        .getStatusCode().is5xxServerError())))
                .doOnError(e -> {
                    if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                        org.springframework.web.reactive.function.client.WebClientResponseException webClientException = 
                            (org.springframework.web.reactive.function.client.WebClientResponseException) e;
                        logger.error("Stream API call failed with status: {}, body: {}, URL: {}", 
                            webClientException.getStatusCode(), webClientException.getResponseBodyAsString(), 
                            openAiApi.toString(), e);
                    } else {
                        logger.error("Stream API call failed for request: {}, URL: {}", request, openAiApi.toString(), e);
                    }
                });

            ConcurrentHashMap<String, String> roleMap = new ConcurrentHashMap<>();

            final ChatModelObservationContext observationContext = ChatModelObservationContext.builder()
                    .prompt(prompt)
                    .provider("openai")
                    .build();

            Observation observation = ChatModelObservationDocumentation.CHAT_MODEL_OPERATION.observation(
                    this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext, this.observationRegistry);

            observation.parentObservation(contextView.getOrDefault(ObservationThreadLocalAccessor.KEY, null)).start();

            Flux<ChatResponse> chatResponse = completionChunks
                    .map(this::chunkToChatCompletion)
                    .map(chatCompletion -> {
                        try {
                            String id = chatCompletion.id();

                            List<Generation> generations = chatCompletion.choices().stream().map(choice -> {
                                if (choice.message().role() != null) {
                                    roleMap.putIfAbsent(id, choice.message().role().name());
                                }
                                Map<String, Object> metadata = Map.of(
                                        "id", chatCompletion.id(),
                                        "role", roleMap.getOrDefault(id, ""),
                                        "index", choice.index(),
                                        "finishReason", choice.finishReason() != null ? choice.finishReason().name() : "",
                                        "refusal", StringUtils.hasText(choice.message().refusal()) ? choice.message().refusal() : ""
                                );
                                return buildGeneration(choice, metadata, request);
                            }).toList();

                            OpenAiApi.Usage usage = chatCompletion.usage();
                            Usage currentChatResponseUsage = usage != null ? getDefaultUsage(usage) : new EmptyUsage();
                            Usage accumulatedUsage = previousChatResponse != null && previousChatResponse.getMetadata() != null
                                    ? new DefaultUsage(
                                            currentChatResponseUsage.getPromptTokens() + previousChatResponse.getMetadata().getUsage().getPromptTokens(),
                                            currentChatResponseUsage.getCompletionTokens() + previousChatResponse.getMetadata().getUsage().getCompletionTokens(),
                                            currentChatResponseUsage.getTotalTokens() + previousChatResponse.getMetadata().getUsage().getTotalTokens(),
                                            null
                                      )
                                    : currentChatResponseUsage;

                            return new ChatResponse(generations, fromChatCompletion(chatCompletion, accumulatedUsage));
                        } catch (Exception e) {
                            logger.error("Error processing chat completion chunk", e);
                            return new ChatResponse(List.of());
                        }
                    });

            Flux<ChatResponse> flux = chatResponse
                    .flatMap(response -> {
                        if (response.hasToolCalls()) {
                            try {
                                int toolCount = response.getResult().getOutput().getToolCalls() != null ?
                                        response.getResult().getOutput().getToolCalls().size() : 0;
                                logger.info("流式检测到 {} 个工具调用，开始执行", toolCount);
                                var toolExecutionResult = this.toolCallingManager.executeToolCalls(prompt, response);
                                if (toolExecutionResult.returnDirect()) {
                                    logger.info("流式工具调用直接返回结果");
                                    return Flux.just(ChatResponse.builder().from(response)
                                            .generations(ToolExecutionResult.buildGenerations(toolExecutionResult))
                                            .build());
                                } else {
                                    logger.info("流式工具调用执行完成，递归调用模型");
                                    return this.internalStream(new Prompt(toolExecutionResult.conversationHistory(), prompt.getOptions()), response);
                                }
                            } catch (Exception e) {
                                logger.error("Error executing tool calls: {}", e.getMessage(), e);
                                AssistantMessage errorMessage = new AssistantMessage("{\"error\": \"工具执行失败：" + e.getMessage() + "\"}");
                                ChatGenerationMetadata metadata = ChatGenerationMetadata.builder().finishReason("error").build();
                                Generation generation = new Generation(errorMessage, metadata);
                                List<Generation> generations = List.of(generation);
                                ChatResponse errorResponse = new ChatResponse(generations);
                                return Flux.just(errorResponse);
                            }
                        } else {
                            return Flux.just(response);
                        }
                    })
                    .doOnError(observation::error)
                    .doFinally(s -> observation.stop())
                    .contextWrite(ctx -> ctx.put(ObservationThreadLocalAccessor.KEY, observation));

            return new MessageAggregator().aggregate(flux, observationContext::setResponse);
        });
    }

    private MultiValueMap<String, String> getAdditionalHttpHeaders(Prompt prompt) {
        Map<String, String> headers = new HashMap<>(this.defaultOptions.getHttpHeaders());
        if (prompt.getOptions() != null && prompt.getOptions() instanceof OpenAiChatOptions) {
            OpenAiChatOptions chatOptions = (OpenAiChatOptions) prompt.getOptions();
            headers.putAll(chatOptions.getHttpHeaders());
        }

        if (!headers.containsKey("Authorization") && StringUtils.hasText(this.apiKey)) {
            headers.put("Authorization", "Bearer " + this.apiKey);
        }

        headers.putIfAbsent("Content-Type", "application/json");
        headers.putIfAbsent("Accept", "text/event-stream");

        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            multiValueMap.add(entry.getKey(), entry.getValue());
        }
        
        // 日志记录请求头部
        if (logger.isDebugEnabled()) {
            logger.debug("Request headers: {}", multiValueMap);
        }
        
        return multiValueMap;
    }

    private Generation buildGeneration(OpenAiApi.ChatCompletion.Choice choice, Map<String, Object> metadata, OpenAiApi.ChatCompletionRequest request) {        List<AssistantMessage.ToolCall> toolCalls = choice.message().toolCalls() == null ? new ArrayList<>()
                : choice.message().toolCalls().stream()
                .map(toolCall -> new AssistantMessage.ToolCall(toolCall.id(), "function", toolCall.function().name(), toolCall.function().arguments()))
                .collect(Collectors.toList());

        String finishReason = (choice.finishReason() != null ? choice.finishReason().name() : "");
        var generationMetadataBuilder = ChatGenerationMetadata.builder().finishReason(finishReason);

        String textContent = choice.message().content();
        
        // 检查是否包含游戏系统提示词，判断是否为游戏模块的响应
        boolean isGameModule = false;
        boolean isServiceModule = false;
        if (request.messages() != null) {
            for (var message : request.messages()) {
                if (message.role() != null && message.role().equals("SYSTEM") && message.content() != null) {
                    if (message.content().contains("高情商哄哄模拟器")) {
                        isGameModule = true;
                        break;
                    } else if (message.content().contains("课程预约") || message.content().contains("IT培训课程")) {
                        isServiceModule = true;
                    }
                }
            }
        }
        
        // 如果是游戏模块的响应，确保返回JSON格式
        if (isGameModule) {
            textContent = ensureGameJsonFormat(textContent);
        }
        // 如果是客服模块的响应，确保返回JSON格式
        else if (isServiceModule) {
            textContent = ensureServiceJsonFormat(textContent);
        }
        
        var assistantMessage = AssistantMessage.builder()
                .content(textContent)
                .toolCalls(toolCalls)
                .build();
        return new Generation(assistantMessage, generationMetadataBuilder.build());
    }
    
    /**
     * 确保游戏模块的响应是有效的JSON格式
     */
    private String ensureGameJsonFormat(String content) {
        if (content == null || content.trim().isEmpty()) {
            return createDefaultGameJson("无响应内容");
        }

        try {
            // 尝试直接解析为JSON
            JsonNode jsonNode = objectMapper.readTree(content);
            // 检查是否包含所有必要字段
            if (jsonNode.has("girlfriend_mood") && jsonNode.has("girlfriend_dialogue") &&
                jsonNode.has("hint_for_player") && jsonNode.has("game_status")) {
                return objectMapper.writeValueAsString(jsonNode);
            }
        } catch (Exception e) {
            // 不是直接有效的JSON，尝试提取JSON片段
            String extractedJson = extractJsonFromText(content);
            if (extractedJson != null) {
                try {
                    JsonNode jsonNode = objectMapper.readTree(extractedJson);
                    if (jsonNode.has("girlfriend_mood") && jsonNode.has("girlfriend_dialogue") &&
                        jsonNode.has("hint_for_player") && jsonNode.has("game_status")) {
                        return objectMapper.writeValueAsString(jsonNode);
                    }
                } catch (Exception ex) {
                    logger.debug("提取的JSON片段无法解析: {}", ex.getMessage());
                }
            }
        }
        // 无法解析为有效的JSON格式，创建默认JSON
        return createDefaultGameJson(content);
    }

    /**
     * 从文本中提取JSON对象
     */
    private String extractJsonFromText(String text) {
        if (text == null) return null;

        // 尝试找到JSON对象的开始和结束位置
        int firstBrace = text.indexOf('{');
        if (firstBrace == -1) return null;

        int lastBrace = text.lastIndexOf('}');
        if (lastBrace <= firstBrace) return null;

        String potentialJson = text.substring(firstBrace, lastBrace + 1);
        return potentialJson;
    }

    /**
     * 创建默认的游戏JSON格式
     */
    private String createDefaultGameJson(String content) {
        try {
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("girlfriend_mood", "她静静地看着你，等待你继续说下去。");
            jsonNode.put("girlfriend_dialogue", content != null ? content : "谢谢你的关心。");
            jsonNode.put("hint_for_player", "[提示] 继续表达你的心意吧！");
            jsonNode.put("game_status", "continue");
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            // 如果创建JSON失败，返回一个安全的默认JSON
            return "{\"girlfriend_mood\":\"她静静地看着你，等待你继续说下去。\",\"girlfriend_dialogue\":\"谢谢你的关心。\",\"hint_for_player\":\"[提示] 继续表达你的心意吧！\",\"game_status\":\"continue\"}";
        }
    }

    /**
     * 确保客服模块的响应是有效的JSON格式
     */
    private String ensureServiceJsonFormat(String content) {
        if (content == null || content.trim().isEmpty()) {
            return createDefaultServiceJson("无响应内容");
        }

        try {
            // 尝试直接解析为JSON
            JsonNode jsonNode = objectMapper.readTree(content);
            // 检查是否包含必要字段
            if (jsonNode.has("reply_to_user")) {
                return objectMapper.writeValueAsString(jsonNode);
            }
        } catch (Exception e) {
            // 不是直接有效的JSON，尝试提取JSON片段
            String extractedJson = extractJsonFromText(content);
            if (extractedJson != null) {
                try {
                    JsonNode jsonNode = objectMapper.readTree(extractedJson);
                    if (jsonNode.has("reply_to_user")) {
                        return objectMapper.writeValueAsString(jsonNode);
                    }
                } catch (Exception ex) {
                    logger.debug("提取的JSON片段无法解析: {}", ex.getMessage());
                }
            }
        }
        // 无法解析为有效的JSON格式，创建默认JSON
        return createDefaultServiceJson(content);
    }

    /**
     * 创建默认的客服JSON格式
     */
    private String createDefaultServiceJson(String content) {
        try {
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("reply_to_user", content != null ? content : "您好，我是小哈哈，有什么可以帮助您的？");
            jsonNode.set("tool_calls", objectMapper.createArrayNode());
            jsonNode.put("need_more_info", true);
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            return "{\"reply_to_user\":\"您好，我是小哈哈，有什么可以帮助您的？\",\"tool_calls\":[],\"need_more_info\":true}";
        }
    }

    private ChatResponseMetadata fromChatCompletion(OpenAiApi.ChatCompletion result, Usage usage) {
        Assert.notNull(result, "OpenAI ChatCompletionResult must not be null");
        var builder = ChatResponseMetadata.builder()
                .id(result.id() != null ? result.id() : "")
                .usage(usage)
                .model(result.model() != null ? result.model() : "")
                .keyValue("created", result.created() != null ? result.created() : 0L)
                .keyValue("system-fingerprint", result.systemFingerprint() != null ? result.systemFingerprint() : "");
        return builder.build();
    }

    private OpenAiApi.ChatCompletion chunkToChatCompletion(OpenAiApi.ChatCompletionChunk chunk) {
        List<OpenAiApi.ChatCompletion.Choice> choices = chunk.choices().stream()
                .map(chunkChoice -> new OpenAiApi.ChatCompletion.Choice(chunkChoice.finishReason(), chunkChoice.index(), chunkChoice.delta(), chunkChoice.logprobs()))
                .toList();

        return new OpenAiApi.ChatCompletion(chunk.id(), choices, chunk.created(), chunk.model(), chunk.serviceTier(), chunk.systemFingerprint(), "chat.completion", chunk.usage());
    }

    private DefaultUsage getDefaultUsage(OpenAiApi.Usage usage) {
        return new DefaultUsage(usage.promptTokens(), usage.completionTokens(), usage.totalTokens(), usage);
    }

    Prompt buildRequestPrompt(Prompt prompt) {
        OpenAiChatOptions runtimeOptions = null;
        if (prompt.getOptions() != null) {
            runtimeOptions = ModelOptionsUtils.copyToTarget(prompt.getOptions(), ChatOptions.class, OpenAiChatOptions.class);
        }

        OpenAiChatOptions requestOptions = ModelOptionsUtils.merge(runtimeOptions, this.defaultOptions, OpenAiChatOptions.class);

        if (runtimeOptions != null) {
            requestOptions.setHttpHeaders(mergeHttpHeaders(runtimeOptions.getHttpHeaders(), this.defaultOptions.getHttpHeaders()));
        } else {
            requestOptions.setHttpHeaders(this.defaultOptions.getHttpHeaders());
        }

        // Preserve tool callbacks from the original prompt (lost during copyToTarget/merge)
        if (prompt.getOptions() instanceof ToolCallingChatOptions) {
            ToolCallingChatOptions toolOptions = (ToolCallingChatOptions) prompt.getOptions();
            List<ToolCallback> toolCallbacks = toolOptions.getToolCallbacks();
            if (toolCallbacks != null && !toolCallbacks.isEmpty()) {
                requestOptions.setToolCallbacks(new ArrayList<>(toolCallbacks));
            }
        }

        return new Prompt(prompt.getInstructions(), requestOptions);
    }

    private Map<String, String> mergeHttpHeaders(Map<String, String> runtimeHttpHeaders, Map<String, String> defaultHttpHeaders) {
        var mergedHttpHeaders = new HashMap<>(defaultHttpHeaders);
        mergedHttpHeaders.putAll(runtimeHttpHeaders);
        return mergedHttpHeaders;
    }

    OpenAiApi.ChatCompletionRequest createRequest(Prompt prompt, boolean stream) {
        List<OpenAiApi.ChatCompletionMessage> chatCompletionMessages = convertMessages(prompt.getInstructions());
        OpenAiApi.ChatCompletionRequest request = new OpenAiApi.ChatCompletionRequest(chatCompletionMessages, stream);
        request = mergeRequestOptions(request, prompt);
        request = addToolDefinitions(request, prompt);
        return request;
    }

    private List<OpenAiApi.ChatCompletionMessage> convertMessages(List<org.springframework.ai.chat.messages.Message> messages) {
        return messages.stream().flatMap(this::convertMessage).collect(Collectors.toList());
    }

    private Stream<OpenAiApi.ChatCompletionMessage> convertMessage(org.springframework.ai.chat.messages.Message message) {
        switch (message.getMessageType()) {
            case USER:
            case SYSTEM:
                return convertUserOrSystemMessage(message);
            case ASSISTANT:
                return convertAssistantMessage((AssistantMessage) message);
            case TOOL:
                return convertToolMessage((ToolResponseMessage) message);
            default:
                throw new IllegalArgumentException("Unsupported message type: " + message.getMessageType());
        }
    }

    private Stream<OpenAiApi.ChatCompletionMessage> convertUserOrSystemMessage(org.springframework.ai.chat.messages.Message message) {
        Object content;
        if (message.getMessageType() == org.springframework.ai.chat.messages.MessageType.USER) {
            content = processMessageContent(message.getText());
        } else {
            content = message.getText();
        }
        return Stream.of(new OpenAiApi.ChatCompletionMessage(content, OpenAiApi.ChatCompletionMessage.Role.valueOf(message.getMessageType().name())));
    }

    private String processMessageContent(String content) {
        if (content == null) {
            return null;
        }

        if (content.contains("<user_action>") && content.contains("</user_action>")) {
            return extractXmlContent(content, "user_action");
        }

        if (content.contains("<user_query>") && content.contains("</user_query>")) {
            return extractXmlContent(content, "user_query");
        }

        return content;
    }

    private String extractXmlContent(String content, String tagName) {
        try {
            int startIndex = content.indexOf("<" + tagName + ">") + ("<" + tagName + ">").length();
            int endIndex = content.indexOf("</" + tagName + ">");
            if (startIndex > 0 && endIndex > startIndex) {
                return content.substring(startIndex, endIndex).trim();
            }
        } catch (Exception e) {
            logger.warn("Failed to extract XML content from: {}", content, e);
        }
        return content;
    }

    private Stream<OpenAiApi.ChatCompletionMessage> convertAssistantMessage(AssistantMessage message) {
        List<OpenAiApi.ChatCompletionMessage.ToolCall> toolCalls = null;
        if (message.getToolCalls() != null && !message.getToolCalls().isEmpty()) {
            toolCalls = message.getToolCalls().stream()
                    .map(tc -> {
                        var function = new OpenAiApi.ChatCompletionMessage.ChatCompletionFunction(tc.name(), tc.arguments());
                        return new OpenAiApi.ChatCompletionMessage.ToolCall(tc.id(), tc.type(), function);
                    })
                    .collect(Collectors.toList());
        }
        // 使用全参数构造器，确保tool_calls信息被保留（ChatCompletionMessage是record类型，不可变）
        return Stream.of(new OpenAiApi.ChatCompletionMessage(
                message.getText(),
                OpenAiApi.ChatCompletionMessage.Role.ASSISTANT,
                null,   // name
                null,   // toolCallId
                toolCalls,
                null,   // refusal
                null,   // audioOutput
                null,   // annotations
                null    // reasoningContent
        ));
    }

    private Stream<OpenAiApi.ChatCompletionMessage> convertToolMessage(ToolResponseMessage message) {
        return message.getResponses().stream().map(tr -> {
            Assert.isTrue(tr.id() != null, "ToolResponseMessage must have an id");
            return new OpenAiApi.ChatCompletionMessage(
                    tr.responseData(),
                    OpenAiApi.ChatCompletionMessage.Role.TOOL,
                    tr.name(),    // name: tool function name
                    tr.id(),      // toolCallId: REQUIRED for OpenAI-compatible API
                    null,         // toolCalls
                    null,         // refusal
                    null,         // audioOutput
                    null,         // annotations
                    null          // reasoningContent
            );
        });
    }

    private OpenAiApi.ChatCompletionRequest mergeRequestOptions(OpenAiApi.ChatCompletionRequest request, Prompt prompt) {
        OpenAiChatOptions requestOptions = prompt.getOptions() instanceof OpenAiChatOptions 
                ? (OpenAiChatOptions) prompt.getOptions() 
                : this.defaultOptions;
        return ModelOptionsUtils.merge(requestOptions, request, OpenAiApi.ChatCompletionRequest.class);
    }

    private OpenAiApi.ChatCompletionRequest addToolDefinitions(OpenAiApi.ChatCompletionRequest request, Prompt prompt) {
        OpenAiChatOptions requestOptions = prompt.getOptions() instanceof OpenAiChatOptions 
                ? (OpenAiChatOptions) prompt.getOptions() 
                : this.defaultOptions;

        List<OpenAiApi.FunctionTool> promptTools = requestOptions.getTools();
        if (!CollectionUtils.isEmpty(promptTools)) {
            return ModelOptionsUtils.merge(OpenAiChatOptions.builder().tools(promptTools).build(), request, OpenAiApi.ChatCompletionRequest.class);
        }

        List<ToolDefinition> toolDefinitions = this.toolCallingManager.resolveToolDefinitions(requestOptions);

        if (!CollectionUtils.isEmpty(toolDefinitions)) {
            return ModelOptionsUtils.merge(OpenAiChatOptions.builder().tools(this.getFunctionTools(toolDefinitions)).build(), request, OpenAiApi.ChatCompletionRequest.class);
        }
        return request;
    }

    private List<OpenAiApi.FunctionTool> getFunctionTools(List<ToolDefinition> toolDefinitions) {
        return toolDefinitions.stream().map(toolDefinition -> {
            var function = new OpenAiApi.FunctionTool.Function(toolDefinition.description(), toolDefinition.name(), toolDefinition.inputSchema());
            return new OpenAiApi.FunctionTool(function);
        }).toList();
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return OpenAiChatOptions.fromOptions(this.defaultOptions);
    }

    @Override
    public String toString() {
        return "AlibabaOpenAiChatModel [defaultOptions=" + this.defaultOptions + "]";
    }

    public void setObservationConvention(ChatModelObservationConvention observationConvention) {
        Assert.notNull(observationConvention, "observationConvention cannot be null");
        this.observationConvention = observationConvention;
    }

    public static AlibabaOpenAiChatModel.Builder builder() {
        return new AlibabaOpenAiChatModel.Builder();
    }

    public static final class Builder {
        private OpenAiApi openAiApi;
        private OpenAiChatOptions defaultOptions = OpenAiChatOptions.builder().model("qwen-max").temperature(0.7).build();
        private ToolCallingManager toolCallingManager;
        private RetryTemplate retryTemplate = RetryUtils.DEFAULT_RETRY_TEMPLATE;
        private ObservationRegistry observationRegistry = ObservationRegistry.NOOP;
        private String apiKey;

        private Builder() {
        }

        public AlibabaOpenAiChatModel.Builder openAiApi(OpenAiApi openAiApi) {
            this.openAiApi = openAiApi;
            return this;
        }

        public AlibabaOpenAiChatModel.Builder defaultOptions(OpenAiChatOptions defaultOptions) {
            this.defaultOptions = defaultOptions;
            return this;
        }

        public AlibabaOpenAiChatModel.Builder toolCallingManager(ToolCallingManager toolCallingManager) {
            this.toolCallingManager = toolCallingManager;
            return this;
        }

        public AlibabaOpenAiChatModel.Builder retryTemplate(RetryTemplate retryTemplate) {
            this.retryTemplate = retryTemplate;
            return this;
        }

        public AlibabaOpenAiChatModel.Builder observationRegistry(ObservationRegistry observationRegistry) {
            this.observationRegistry = observationRegistry;
            return this;
        }

        public AlibabaOpenAiChatModel.Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public AlibabaOpenAiChatModel build() {
            if (toolCallingManager != null) {
                return new AlibabaOpenAiChatModel(openAiApi, defaultOptions, toolCallingManager, retryTemplate, observationRegistry, apiKey);
            }
            return new AlibabaOpenAiChatModel(openAiApi, defaultOptions, DEFAULT_TOOL_CALLING_MANAGER, retryTemplate, observationRegistry, apiKey);
        }
    }
}