# 简化版智能Prompt补全功能设计

## 1. 功能概述

用户输入基础的prompt想法，系统调用大模型对其进行优化和完善，生成更完整、结构化的prompt。

## 2. 核心设计思路

### 2.1 工作流程
```
用户输入基础想法 → 系统构建meta prompt → 调用大模型 → 返回优化后的完整prompt
```

### 2.2 核心特点
- **简单直接**: 用户只需输入简单想法
- **AI驱动**: 完全依赖大模型的智能优化
- **快速响应**: 单次API调用，响应速度快
- **易于实现**: 代码结构简单，易于维护

## 3. 系统架构设计

### 3.1 整体架构
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端界面       │    │   Prompt补全     │    │   LLM服务        │
│  (输入框)        │◄──►│   Controller     │◄──►│  (ZhipuAI等)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │   Prompt优化     │
                       │   Service       │
                       └─────────────────┘
```

### 3.2 核心组件

#### 3.2.1 PromptCompletionController
```java
@RestController
@RequestMapping("/api/prompt/completion")
public class PromptCompletionController {
    
    @Autowired
    private PromptCompletionService completionService;
    
    @PostMapping("/optimize")
    public ApiBaseResponse optimizePrompt(
            @RequestBody PromptOptimizeRequest request,
            HttpServletRequest httpRequest) {
        
        String userId = httpRequest.getAttribute("userId").toString();
        return completionService.optimizePrompt(userId, request);
    }
}
```

#### 3.2.2 PromptCompletionService
```java
@Service
public class PromptCompletionService {
    
    @Autowired
    private LLMService llmService;
    
    @Autowired
    private UserService userService;
    
    public ApiBaseResponse optimizePrompt(String userId, PromptOptimizeRequest request) {
        // 1. 获取用户信息（可选，用于个性化）
        User user = userService.getUserById(userId);
        
        // 2. 构建meta prompt
        String metaPrompt = buildMetaPrompt(request, user);
        
        // 3. 调用LLM优化
        String optimizedPrompt = llmService.generateCompletion(metaPrompt);
        
        // 4. 解析和格式化结果
        PromptOptimizeResult result = parseOptimizedPrompt(optimizedPrompt);
        
        return ApiSuccessResponse.success(result);
    }
}
```

## 4. 数据结构设计

### 4.1 请求参数
```java
public class PromptOptimizeRequest {
    private String basicPrompt;        // 用户输入的基础prompt
    private String businessScene;       // 业务场景（可选）
    private String targetModel;         // 目标模型（可选）
    private String language;            // 语言偏好（可选）
    private Integer maxTokens;          // 最大token数（可选）
    private String style;               // 风格要求（可选）
    
    // getters and setters
}
```

### 4.2 返回结果
```java
public class PromptOptimizeResult {
    private String originalPrompt;      // 原始prompt
    private String optimizedPrompt;     // 优化后的prompt
    private String promptTitle;         // prompt标题
    private String description;         // 描述说明
    private List<String> tags;          // 建议标签
    private Integer estimatedTokens;    // 预估token数
    private List<String> improvementSuggestions; // 改进建议
    
    // getters and setters
}
```

## 5. Meta Prompt模板设计

### 5.1 基础优化模板
```
你是一个专业的prompt工程师。请帮我优化以下基础prompt想法，使其成为一个完整、高效、结构化的prompt。

**用户的基础想法：**
{basicPrompt}

**业务场景：** {businessScene}
**目标模型：** {targetModel}
**语言偏好：** {language}
**风格要求：** {style}

请按照以下格式返回优化结果：

## 优化后的Prompt
[完整的、结构化的prompt内容]

## Prompt标题
[简洁明了的标题]

## 描述说明
[说明这个prompt的用途和适用场景]

## 建议标签
[3-5个相关标签，用逗号分隔]

## 改进建议
[对使用这个prompt的建议和注意事项]

## 预估Token数
[预估的token数量]
```

### 5.2 针对不同场景的专门模板

#### 5.2.1 代码生成场景
```
你是一个专业的编程助手。请基于用户的基础想法，生成一个用于代码生成的优化prompt。

**用户需求：** {basicPrompt}
**编程语言：** {language}
**代码类型：** {businessScene}

请确保生成的prompt包含：
1. 明确的代码需求描述
2. 输入输出规范
3. 代码风格要求
4. 边界条件说明
5. 示例（如果适用）
```

#### 5.2.2 文案写作场景
```
你是一个专业的文案策划师。请基于用户的基础想法，生成一个用于文案写作的优化prompt。

**用户需求：** {basicPrompt}
**文案类型：** {businessScene}
**目标受众：** {targetAudience}
**写作风格：** {style}

请确保生成的prompt包含：
1. 明确的写作目标
2. 目标受众分析
3. 写作风格要求
4. 内容结构要求
5. 关键信息点
```

#### 5.2.3 数据分析场景
```
你是一个数据分析师。请基于用户的基础想法，生成一个用于数据分析的优化prompt。

**用户需求：** {basicPrompt}
**分析目标：** {businessScene}
**数据类型：** {dataType}

请确保生成的prompt包含：
1. 明确的分析目标
2. 数据输入格式说明
3. 分析方法和步骤
4. 输出结果要求
5. 注意事项和假设
```

## 6. 实现代码示例

### 6.1 Service实现
```java
@Service
@Slf4j
public class PromptCompletionServiceImpl implements PromptCompletionService {
    
    @Autowired
    private LLMService llmService;
    
    @Autowired
    private UserService userService;
    
    @Value("${llm.prompt.model:zhipuai}")
    private String defaultModel;
    
    @Override
    public ApiBaseResponse optimizePrompt(String userId, PromptOptimizeRequest request) {
        try {
            // 获取用户信息（用于个性化）
            User user = userService.getUserById(userId);
            
            // 构建meta prompt
            String metaPrompt = buildMetaPrompt(request, user);
            
            // 调用LLM生成优化结果
            String llmResponse = llmService.generateCompletion(
                metaPrompt, 
                request.getTargetModel() != null ? request.getTargetModel() : defaultModel
            );
            
            // 解析结果
            PromptOptimizeResult result = parseOptimizedPrompt(llmResponse, request.getBasicPrompt());
            
            return ApiSuccessResponse.success(result);
            
        } catch (Exception e) {
            log.error("Prompt优化失败: userId={}, request={}", userId, request, e);
            return ApiErrorResponse.error(ErrorCode.PROMPT_OPTIMIZE_FAILED);
        }
    }
    
    private String buildMetaPrompt(PromptOptimizeRequest request, User user) {
        // 根据业务场景选择不同的模板
        String template = getTemplateByScene(request.getBusinessScene());
        
        // 替换模板变量
        return template.replace("{basicPrompt}", request.getBasicPrompt())
                       .replace("{businessScene}", request.getBusinessScene() != null ? request.getBusinessScene() : "通用")
                       .replace("{targetModel}", request.getTargetModel() != null ? request.getTargetModel() : "通用大模型")
                       .replace("{language}", request.getLanguage() != null ? request.getLanguage() : "中文")
                       .replace("{style}", request.getStyle() != null ? request.getStyle() : "专业、清晰");
    }
    
    private String getTemplateByScene(String businessScene) {
        if (businessScene == null) {
            return getBasicTemplate();
        }
        
        switch (businessScene.toLowerCase()) {
            case "coding":
            case "programming":
            case "代码生成":
                return getCodingTemplate();
            case "writing":
            case "文案":
            case "写作":
                return getWritingTemplate();
            case "analysis":
            case "数据分析":
                return getAnalysisTemplate();
            default:
                return getBasicTemplate();
        }
    }
    
    private PromptOptimizeResult parseOptimizedPrompt(String llmResponse, String originalPrompt) {
        PromptOptimizeResult result = new PromptOptimizeResult();
        result.setOriginalPrompt(originalPrompt);
        
        // 解析LLM返回的结构化结果
        String[] sections = llmResponse.split("## ");
        
        for (String section : sections) {
            if (section.startsWith("优化后的Prompt")) {
                result.setOptimizedPrompt(section.substring("优化后的Prompt".length()).trim());
            } else if (section.startsWith("Prompt标题")) {
                result.setPromptTitle(section.substring("Prompt标题".length()).trim());
            } else if (section.startsWith("描述说明")) {
                result.setDescription(section.substring("描述说明".length()).trim());
            } else if (section.startsWith("建议标签")) {
                String tagsStr = section.substring("建议标签".length()).trim();
                result.setTags(Arrays.asList(tagsStr.split(",")));
            } else if (section.startsWith("改进建议")) {
                // 可以进一步解析改进建议
                result.setImprovementSuggestions(Arrays.asList(section.substring("改进建议".length()).trim().split("\n")));
            } else if (section.startsWith("预估Token数")) {
                try {
                    result.setEstimatedTokens(Integer.parseInt(section.substring("预估Token数".length()).trim()));
                } catch (NumberFormatException e) {
                    result.setEstimatedTokens(0);
                }
            }
        }
        
        return result;
    }
}
```

### 6.2 Controller实现
```java
@RestController
@RequestMapping("/api/prompt/completion")
@Api(tags = "智能Prompt补全")
@Slf4j
public class PromptCompletionController {
    
    @Autowired
    private PromptCompletionService completionService;
    
    @ApiOperation(value = "优化Prompt", notes = "基于用户输入的基础想法，生成优化的完整prompt")
    @PostMapping("/optimize")
    public ApiBaseResponse optimizePrompt(
            @ApiParam(value = "优化请求参数", required = true) @RequestBody PromptOptimizeRequest request,
            HttpServletRequest httpRequest) {
        
        String userId = httpRequest.getAttribute("userId").toString();
        log.info("开始优化Prompt: userId={}, basicPrompt={}", userId, request.getBasicPrompt());
        
        return completionService.optimizePrompt(userId, request);
    }
}
```

## 7. 使用示例

### 7.1 API调用示例
```bash
POST /api/prompt/completion/optimize
Content-Type: application/json

{
    "basicPrompt": "写一个Python函数来计算斐波那契数列",
    "businessScene": "coding",
    "targetModel": "zhipuai",
    "language": "中文",
    "style": "专业、清晰"
}
```

### 7.2 返回结果示例
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "originalPrompt": "写一个Python函数来计算斐波那契数列",
        "optimizedPrompt": "请编写一个Python函数来计算斐波那契数列。\n\n要求：\n1. 函数名为fibonacci\n2. 接收一个整数参数n，表示要计算的项数\n3. 返回包含前n个斐波那契数的列表\n4. 处理边界情况（n=0, n=1, n为负数）\n5. 提供清晰的函数文档字符串\n6. 包含使用示例\n\n请确保代码高效、可读，并添加适当的注释。",
        "promptTitle": "Python斐波那契数列计算函数",
        "description": "生成一个计算斐波那契数列的Python函数，包含完整的错误处理和文档说明",
        "tags": ["Python", "算法", "函数", "斐波那契"],
        "estimatedTokens": 150,
        "improvementSuggestions": [
            "可以考虑添加时间复杂度分析",
            "建议提供多种实现方式（递归、迭代）",
            "可以添加性能测试代码"
        ]
    }
}
```

## 8. 配置文件

### 8.1 application.properties配置
```properties
# Prompt补全配置
prompt.completion.model=zhipuai
prompt.completion.max-tokens=1000
prompt.completion.temperature=0.7

# 不同场景的模板文件路径
prompt.template.basic=classpath:templates/basic-prompt.txt
prompt.template.coding=classpath:templates/coding-prompt.txt
prompt.template.writing=classpath:templates/writing-prompt.txt
prompt.template.analysis=classpath:templates/analysis-prompt.txt
```

## 9. 实现计划

### 9.1 第一阶段：基础功能（1周）
- [ ] 定义数据结构
- [ ] 实现基础的meta prompt模板
- [ ] 集成LLM服务
- [ ] 实现基础的Controller和Service
- [ ] 编写单元测试

### 9.2 第二阶段：功能完善（1周）
- [ ] 添加不同场景的专门模板
- [ ] 完善结果解析逻辑
- [ ] 添加错误处理
- [ ] 优化用户体验
- [ ] 集成测试

### 9.3 第三阶段：优化增强（可选，0.5周）
- [ ] 添加用户历史记录
- [ ] 实现个性化优化
- [ ] 添加性能监控
- [ ] 完善文档

## 10. 优势特点

1. **简单易用**: 用户只需输入基础想法
2. **快速实现**: 开发周期短，代码简单
3. **效果可控**: 通过模板控制输出质量
4. **易于维护**: 代码结构清晰，易于理解和修改
5. **扩展性好**: 可以轻松添加新的场景模板

这个简化方案专注于核心功能，通过meta prompt的方式实现智能补全，既满足了基本需求，又避免了过度复杂化。