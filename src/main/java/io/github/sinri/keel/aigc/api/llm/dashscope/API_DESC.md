# DashScope API

本文介绍如何通过 DashScope API 调用千问模型，包括输入输出参数说明及调用示例。

## Endpoint

### 华北2（北京）地域

HTTP 请求地址：

- 纯文本模型（如`qwen-plus`）
  - `POST https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation`
- 多模态模型（如`qwen3.6-plus`或`qwen3-vl-plus`）
  - `POST https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation`

### 新加坡地域

HTTP 请求地址：

- 纯文本模型（如`qwen-plus`）
  - `POST https://dashscope-intl.aliyuncs.com/api/v1/services/aigc/text-generation/generation`
- 多模态模型（如`qwen3.6-plus`或`qwen3-vl-plus`）
  - `POST https://dashscope-intl.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation`

### 美国（弗吉尼亚）地域

HTTP 请求地址：

- 纯文本模型
  - `POST https://dashscope-us.aliyuncs.com/api/v1/services/aigc/text-generation/generation`
- 千问VL模型
  - `POST https://dashscope-us.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation`

SDK 调用配置的 `base_url`：

## CURL Sample Code

### 文本输入

```shell
curl --location "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation" \\
    --header "Authorization: Bearer $DASHSCOPE_API_KEY" \\
    --header "Content-Type: application/json" \\
    --data '{
        "model": "qwen-plus",
        "input": {
            "messages": [
                {
                    "role": "system",
                    "content": "You are a helpful assistant."
                },
                {
                    "role": "user",
                    "content": "你是谁？"
                }
            ]
        },
        "parameters": {
            "result_format": "message"
        }
    }'
```

### 流式输出

> 相关文档：[流式输出](https://help.aliyun.com/zh/model-studio/stream)。

### 文本生成模型

```shell
curl --location "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation" \\
    --header "Authorization: Bearer $DASHSCOPE_API_KEY" \\
    --header "Content-Type: application/json" \\
    --header "X-DashScope-SSE: enable" \\
    --data '{
        "model": "qwen-plus",
        "input": {
            "messages": [
                {
                    "role": "system",
                    "content": "You are a helpful assistant."
                },
                {
                    "role": "user",
                    "content": "你是谁？"
                }
            ]
        },
        "parameters": {
            "result_format": "message",
            "incremental_output": true
        }
    }'
```

### 多模态模型

```shell
# ======= 重要提示 =======
# 新加坡和北京地域的API Key不同。获取API Key：https://help.aliyun.com/zh/model-studio/get-api-key
# 以下为北京地域url，若使用新加坡地域的模型，需将url替换为：https://dashscope-intl.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation
# === 执行时请删除该注释 ===
curl -X POST https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation \\
    -H "Authorization: Bearer $DASHSCOPE_API_KEY" \\
    -H 'Content-Type: application/json' \\
    -H 'X-DashScope-SSE: enable' \\
    -d '{
        "model": "qwen3-vl-plus",
        "input": {
            "messages": [
                {
                    "role": "user",
                    "content": [
                        {
                            "image": "https://help-static-aliyun-doc.aliyuncs.com/file-manage-files/zh-CN/20241022/emyrja/dog_and_girl.jpeg"
                        },
                        {
                            "text": "图中描绘的是什么景象？"
                        }
                    ]
                }
            ]
        },
        "parameters": {
            "incremental_output": true
        }
    }'
```

### 图像输入

> 关于大模型分析图像的更多用法，请参见[图像与视频理解](https://help.aliyun.com/zh/model-studio/vision)。

```shell
curl --location 'https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation' \\
    --header "Authorization: Bearer $DASHSCOPE_API_KEY" \\
    --header 'Content-Type: application/json' \\
    --data '{
        "model": "qwen-vl-plus",
        "input": {
            "messages": [
                {
                    "role": "user",
                    "content": [
                        {
                            "image": "https://dashscope.oss-cn-beijing.aliyuncs.com/images/dog_and_girl.jpeg"
                        },
                        {
                            "image": "https://dashscope.oss-cn-beijing.aliyuncs.com/images/tiger.png"
                        },
                        {
                            "image": "https://dashscope.oss-cn-beijing.aliyuncs.com/images/rabbit.png"
                        },
                        {
                            "text": "这些是什么?"
                        }
                    ]
                }
            ]
        }
    }'
```

### 视频输入

> 以下为传入视频帧的示例代码，关于更多用法（如传入视频文件），请参见[视觉理解](https://help.aliyun.com/zh/model-studio/vision#80dbf6ca8fh6s)。

```shell
curl -X POST https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation \\
    -H "Authorization: Bearer $DASHSCOPE_API_KEY" \\
    -H 'Content-Type: application/json' \\
    -d '{
        "model": "qwen-vl-max-latest",
        "input": {
            "messages": [
                {
                    "role": "user",
                    "content": [
                        {
                            "video": [
                                "https://img.alicdn.com/imgextra/i3/O1CN01K3SgGo1eqmlUgeE9b_!!6000000003923-0-tps-3840-2160.jpg",
                                "https://img.alicdn.com/imgextra/i4/O1CN01BjZvwg1Y23CF5qIRB_!!6000000003000-0-tps-3840-2160.jpg",
                                "https://img.alicdn.com/imgextra/i4/O1CN01Ib0clU27vTgBdbVLQ_!!6000000007859-0-tps-3840-2160.jpg",
                                "https://img.alicdn.com/imgextra/i1/O1CN01aygPLW1s3EXCdSN4X_!!6000000005710-0-tps-3840-2160.jpg"
                            ]
                        },
                        {
                            "text": "描述这个视频的具体过程"
                        }
                    ]
                }
            ]
        }
    }'
```

### 音频输入

#### 音频理解

> 关于大模型分析音频的更多用法，请参见[音频理解-Qwen-Audio](https://help.aliyun.com/zh/model-studio/audio-language-model)。

```shell
curl --location 'https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation' \\
    --header "Authorization: Bearer $DASHSCOPE_API_KEY" \\
    --header 'Content-Type: application/json' \\
    --data '{
        "model": "qwen-audio-turbo",
        "input": {
            "messages": [
                {
                    "role": "system",
                    "content": [
                        {
                            "text": "You are a helpful assistant."
                        }
                    ]
                },
                {
                    "role": "user",
                    "content": [
                        {
                            "audio": "https://dashscope.oss-cn-beijing.aliyuncs.com/audios/welcome.mp3"
                        },
                        {
                            "text": "这段音频在说什么?"
                        }
                    ]
                }
            ]
        }
    }'
```

### 联网搜索

```shell
curl -X POST https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation \\
    -H "Authorization: Bearer $DASHSCOPE_API_KEY" \\
    -H "Content-Type: application/json" \\
    -d '{
        "model": "qwen-plus",
        "input": {
            "messages": [
                {
                    "role": "system",
                    "content": "You are a helpful assistant."
                },
                {
                    "role": "user",
                    "content": "明天杭州天气如何？"
                }
            ]
        },
        "parameters": {
            "enable_search": true,
            "result_format": "message"
        }
    }'
```

### 工具调用
> 完整的Function Calling 流程代码请参见[Function Calling](https://help.aliyun.com/zh/model-studio/qwen-function-calling#0f0fcbd808d8o)。

```shell
curl --location "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation" \\
    --header "Authorization: Bearer $DASHSCOPE_API_KEY" \\
    --header "Content-Type: application/json" \\
    --data '{
        "model": "qwen-plus",
        "input": {
            "messages": [
                {
                    "role": "user",
                    "content": "杭州天气怎么样"
                }
            ]
        },
        "parameters": {
            "result_format": "message",
            "tools": [
                {
                    "type": "function",
                    "function": {
                        "name": "get_current_time",
                        "description": "当你想知道现在的时间时非常有用。",
                        "parameters": {}
                    }
                },
                {
                    "type": "function",
                    "function": {
                        "name": "get_current_weather",
                        "description": "当你想查询指定城市的天气时非常有用。",
                        "parameters": {
                            "type": "object",
                            "properties": {
                                "location": {
                                    "type": "string",
                                    "description": "城市或县区，比如北京市、杭州市、余杭区等。"
                                }
                            }
                        },
                        "required": ["location"]
                    }
                }
            ]
        }
    }'
```

### 文档理解

> 请将 {FILE\\_ID}替换为您实际对话场景所使用的 file-id

```shell
curl --location "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation" \\
    --header "Authorization: Bearer $DASHSCOPE_API_KEY" \\
    --header "Content-Type: application/json" \\
    --data '{
        "model": "qwen-long",
        "input": {
            "messages": [
                {
                    "role": "system",
                    "content": "You are a helpful assistant."
                },
                {
                    "role": "system",
                    "content": "fileid://{FILE_ID}"
                },
                {
                    "role": "user",
                    "content": "这篇文章讲了什么？"
                }
            ]
        },
        "parameters": {
            "result_format": "message"
        }
    }'
```

## 请求体

### `model` (`string`)

**（必选）** 模型名称。 支持的模型：Qwen 大语言模型（商业版、开源版）、Qwen-VL、Qwen-Coder、千问Audio、数学模型。 **具体模型名称和计费，请参见**[文本生成-千问](https://help.aliyun.com/zh/model-studio/models#9f8890ce29g5u)。

### `messages` (`array`)

**（必选）** 传递给大模型的上下文，按对话顺序排列。

> 通过HTTP调用时，请将**messages** 放入 **input** 对象中。 
> 

#### 消息类型 

每种消息类型均以`object`形式构造。

##### System Message （可选）

系统消息，用于设定大模型的角色、语气、任务目标或约束条件等。一般放在`messages`数组的第一位。
 
> QwQ模型不建议设置 System Message，QVQ 模型设置 System Message不会生效。 

- `object`
  - **content** `*string*`**（必选）** 消息内容。 
  - **role** `*string*` **（必选）** 系统消息的角色，固定为`system`。 

##### User Message 

**（必选）** 

用户消息，用于向模型传递问题、指令或上下文等。 

- `object`
  - `content` `*string 或 array*`**（必选）** 消息内容。若输入只有文本，则为 string 类型；若输入包含图像等多模态数据，或启用显式缓存，则为 array 类型。
    - `text` `*string*`**（必选）** 输入的文本。 
    - `image` `*string*`（可选） 指定用于图片理解的图像文件，图像支持以下三种方式传入： 
      - 公网 URL：公网可访问的图像链接 - 图片的 Base64 编码，格式为 `data:image/<format>;base64,<data>` 
      - 本地文件：本地文件的绝对路径 适用模型：[Qwen-VL](https://help.aliyun.com/zh/model-studio/vision#f18fc2bb52wxo)、[QVQ](https://help.aliyun.com/zh/model-studio/visual-reasoning#f18fc2bb52wxo) 示例值：`{"image":"https://xxxx.jpeg"}` 
    - `video` `*array 或 string*`（可选） 使用[Qwen-VL 模型](https://help.aliyun.com/zh/model-studio/vision)或[QVQ模型](https://help.aliyun.com/zh/model-studio/visual-reasoning)传入的视频。
      - 若传入图像列表，则为`*array*`类型； 
      - 若传入视频文件，则为`*string*`类型。
      - 传入本地文件请参见[本地文件（Qwen-VL）](https://help.aliyun.com/zh/model-studio/vision#f18fc2bb52wxo)或[本地文件（QVQ）](https://help.aliyun.com/zh/model-studio/visual-reasoning#f18fc2bb52wxo)。 
      - 示例值： 
        - 图像列表：`{"video":["https://xx1.jpg",...,"https://xxn.jpg"]}` 
        - 视频文件：`{"video":"https://xxx.mp4"}` 
    - `fps` `*float*` （可选） 每秒抽帧数。取值范围为 `[0.1, 10]`，默认值为2.0。 
      - **功能说明** fps有两个功能： 
        - 输入视频文件时，控制抽帧频率，每 `1/fps` 秒抽取一帧。 
          - 适用于[Qwen-VL 模型](https://help.aliyun.com/zh/model-studio/vision)与[QVQ模型](https://help.aliyun.com/zh/model-studio/visual-reasoning)。 
        - 告知模型相邻帧之间的时间间隔，帮助其更好地理解视频的时间动态。同时适用于输入视频文件与图像列表时。该功能同时支持视频文件和图像列表输入，适用于事件时间定位或分段内容摘要等场景。 
          - 支持Qwen3.6、Qwen3.5、`Qwen3-VL`、`Qwen2.5-VL`与QVQ模型。 
      - 较大的`fps`适合高速运动的场景（如体育赛事、动作电影等），较小的`fps`适合长视频或内容偏静态的场景。 
      - **示例值** 
        - 图像列表传入：`{"video":["https://xx1.jpg",...,"https://xxn.jpg"]，"fps":2}` 
        - 视频文件传入：`{"video": "https://xx1.mp4"，"fps":2}` 
    - `max_frames` `*integer*` （可选） 
      - 视频抽取帧数的上限。当按`fps`计算的帧数超过 `max_frames`时，系统将自动调整为：在`max_frames`内均匀抽帧，确保总帧数不超过限制。 
      - **取值范围** 
        - qwen3.6系列、qwen3.5系列、`qwen3-vl-plus`系列、`qwen3-vl-flash`系列、`qwen3-vl-235b-a22b-thinking`、`qwen3-vl-235b-a22b-instruct`：最大值和默认值均为 2000。 
        - `qwen-vl-max`、`qwen-vl-max-latest`、`qwen-vl-max-0813`、`qwen-vl-plus`、`qwen-vl-plus-latest`、`qwen-vl-plus-0815``、qwen-vl-plus-0710`：最大值和默认值均为 512。 
      - **示例值** 
        - `{"type": "video_url","video_url": {"url":"https://xxxx.mp4"},"max_frame": 2000}` 
        - 使用 OpenAI 兼容API调用时，不支持自定义`max_frames`参数，API 将自动使用各模型对应的默认值。 
    - `min_pixels` `*integer*` （可选） 
      - 设定输入图像或视频帧的最小像素阈值。当输入图像或视频帧的像素小于`min_pixels`时，会将其进行放大，直到总像素高于`min_pixels`。 
      - **取值范围** 
        - **输入图像：** 
          - `Qwen3.6、Qwen3.5`、`Qwen3-VL`：默认值和最小值均为：`65536` - `qwen-vl-max`、`qwen-vl-max-latest`、`qwen-vl-max-0813`、`qwen-vl-plus`、`qwen-vl-plus-latest`、`qwen-vl-plus-0815``、qwen-vl-plus-0710`：默认值和最小值均为`4096` 
          - 其他`qwen-vl-plus`模型、其他`qwen-vl-max`模型、`Qwen2.5-VL`开源系列及`QVQ`系列模型：默认值和最小值均为`3136` 
        - **输入视频文件或图像列表：** 
          - `Qwen3.6、Qwen3.5`、Qwen3-VL（包括商业版和开源版）、`qwen-vl-max`、`qwen-vl-max-latest`、`qwen-vl-max-0813`、`qwen-vl-plus`、`qwen-vl-plus-latest`、`qwen-vl-plus-0815``、qwen-vl-plus-0710`：默认值为`65536`，最小值为`4096` 
          - 其他`qwen-vl-plus`模型、其他`qwen-vl-max`模型、`Qwen2.5-VL`开源系列及`QVQ`系列模型：默认值为`50176`，最小值为`3136` 
      - **示例值** 
        - 输入图像：`{"type": "image_url","image_url": {"url":"https://xxxx.jpg"},"min_pixels": 65536}` 
        - 输入视频文件时：`{"type": "video_url","video_url": {"url":"https://xxxx.mp4"},"min_pixels": 65536}` 
        - 输入图像列表时：`{"type": "video","video": ["https://xx1.jpg",...,"https://xxn.jpg"],"min_pixels": 65536}` 
    - `max_pixels` `*integer*` （可选） 
      - 用于设定输入图像或视频帧的最大像素阈值。当输入图像或视频的像素在`[min_pixels, max_pixels]`区间内时，模型会按原图进行识别。当输入图像像素大于`max_pixels`时，会将图像进行缩小，直到总像素低于`max_pixels`。 
      - **取值范围** 
        - **输入图像：** `max_pixels` 的取值与是否开启`[vl_high_resolution_images](https://help.aliyun.com/zh/model-studio/qwen-api-reference/#0edad44583knr)`参数有关。 
          - 当`vl_high_resolution_images`为`False`时： 
            - `Qwen3.6、Qwen3.5`、`Qwen3-VL`：默认值为`2621440`，最大值为：`16777216` 
            - `qwen-vl-max`、`qwen-vl-max-latest`、`qwen-vl-max-0813`、`qwen-vl-plus`、`qwen-vl-plus-latest`、`qwen-vl-plus-0815``、qwen-vl-plus-0710`：默认值为`1310720`，最大值为：`16777216` 
            - 其他`qwen-vl-plus`模型、其他`qwen-vl-max`模型、`Qwen2.5-VL`开源系列及`QVQ`系列模型：默认值为`1003520` ，最大值为`12845056` 
          - 当`vl_high_resolution_images`为`True`时： 
            - `Qwen3.6、Qwen3.5`、Qwen3-VL、`qwen-vl-max`、`qwen-vl-max-latest`、`qwen-vl-max-0813`、`qwen-vl-plus`、`qwen-vl-plus-latest`、`qwen-vl-plus-0815``、qwen-vl-plus-0710`：`max_pixels`无效，输入图像的最大像素固定为`16777216` 
            - 其他`qwen-vl-plus`模型、其他`qwen-vl-max`模型、`Qwen2.5-VL`开源系列及`QVQ`系列模型：`max_pixels`无效，输入图像的最大像素固定为`12845056` 
        - **输入视频文件或图像列表：** 
          - `qwen3.6系列、qwen3.5系列、qwen3-vl-plus`系列、`qwen3-vl-flash`系列、`qwen3-vl-235b-a22b-thinking`、`qwen3-vl-235b-a22b-instruct`：默认值为`655360`，最大值为`2048000` 
          - 其他`Qwen3-VL`开源模型、`qwen-vl-max`、`qwen-vl-max-latest`、`qwen-vl-max-0813`、`qwen-vl-plus`、`qwen-vl-plus-latest`、`qwen-vl-plus-0815``、qwen-vl-plus-0710`：默认值`655360`，最大值为`786432` 
          - 其他`qwen-vl-plus`模型、其他`qwen-vl-max`模型、`Qwen2.5-VL`开源系列及`QVQ`系列模型：默认值为`501760`，最大值为`602112` 
      - **示例值** 
        - 输入图像：`{"type": "image_url","image_url": {"url":"https://xxxx.jpg"},"max_pixels": 8388608}` 
        - 输入视频文件时：`{"type": "video_url","video_url": {"url":"https://xxxx.mp4"},"max_pixels": 655360}` 
        - 输入图像列表时：`{"type": "video","video": ["https://xx1.jpg",...,"https://xxn.jpg"],"max_pixels": 655360}` 
    - `total_pixels` `*integer*` （可选） 
      - 用于限制从视频中抽取的所有帧的总像素（单帧图像像素 × 总帧数）。如果视频总像素超过此限制，系统将对视频帧进行缩放，但仍会确保单帧图像的像素值在`[min_pixels, max_pixels]`范围内。适用于 Qwen-VL、QVQ 模型。 对于抽帧数量较多的长视频，可适当降低此值以减少Token消耗和处理时间，但这可能会导致图像细节丢失。 
      - **取值范围** 
        - qwen3.6系列、qwen3.5系列、`qwen3-vl-plus`系列、`qwen3-vl-flash`系列、`qwen3-vl-235b-a22b-thinking`、`qwen3-vl-235b-a22b-instruct`：默认值和最小值均为134217728，该值对应 `131072` 个图像 Token（每 32×32 像素对应 1 个图像 Token）。 
        - 其他`Qwen3-VL`开源模型、`qwen-vl-max`、`qwen-vl-max-latest`、`qwen-vl-max-0813`、`qwen-vl-plus`、`qwen-vl-plus-latest`、`qwen-vl-plus-0815``、qwen-vl-plus-0710`：默认值和最小值均为`67108864`，该值对应 `65536` 个图像 Token（每 32×32 像素对应 1 个图像 Token）。 
        - 其他`qwen-vl-plus`模型、其他`qwen-vl-max`模型、`Qwen2.5-VL`开源系列及`QVQ`系列模型：默认值和最小值均为`51380224`，该值对应 `65536` 个图像 Token（每 28×28 像素对应 1 个图像 Token）。 
      - **示例值** 
        - 输入视频文件时：`{"type": "video_url","video_url": {"url":"https://xxxx.mp4"},"total_pixels": 134217728}` 
        - 输入图像列表时：`{"type": "video","video": ["https://xx1.jpg",...,"https://xxn.jpg"],"total_pixels": 134217728}` 
    - **audio** `*string*` (模型为音频理解时，是必选参数)
      - 模型为音频理解时，是必选参数，如模型为qwen-audio-turbo等。 
      - 使用音频理解功能时，传入的音频文件。 
      - 示例值：`{"audio":"https://xxx.mp3"}` 
    - `cache_control` `*object*` **（可选）** 
      - 仅支持[显式缓存](https://help.aliyun.com/zh/model-studio/context-cache#825f201c5fy6o)的模型支持，用于开启显式缓存。 
      - **属性** 
        - **type** `*string*`**（必选）** 固定为`ephemeral`。 
  - `role` `*string*` **（必选）** 用户消息的角色，固定为`user`。 

##### Assistant Message 

（可选） 

模型对用户消息的回复。 

- `object`
  - `content` `string` （可选） 消息内容。仅当助手消息中指定`tool_calls`参数时非必选。 
  - `role` `string` **（必选）** 固定为`assistant`。 
  - `partial` `boolean` （可选） 是否开启前缀续写。相关文档与支持的模型：[前缀续写](https://help.aliyun.com/zh/model-studio/partial-mode)。 
  - `tool_calls` `array` （可选） 发起 Function Calling 后，返回的工具与入参信息，包含一个或多个对象。由上一轮模型响应的`tool_calls`字段获得。 
    - `id` `string` 工具响应的ID。 
    - `type` `string` 工具类型，当前只支持设为`function`。 
    - `function` `object` 工具与入参信息。 
      - `name` `string` 工具名称。 
      - `arguments` `string` 入参信息，为JSON格式字符串。 
    - `index` `integer` 当前工具信息在`tool_calls`数组中的索引。 
    
##### Tool Message 

（可选） 

工具的输出信息。 

- `object`
  - `content` `string` **（必选）** 工具函数的输出内容，必须为字符串格式。 
  - `role` `string` **（必选）** 固定为`tool`。 
  - `tool_call_id` `*string*` **（可选）** 发起 Function Calling 后返回的 id，可以通过`response.output.choices[0].message.tool_calls[$index]["id"]`获取，用于标记 Tool Message 对应的工具。

### `parameters` (`object`)

可包含下面的属性。

#### `temperature` (`float`)

（可选） 

采样温度，控制模型生成文本的多样性。 temperature越高，生成的文本更多样，反之，生成的文本更确定。 取值范围： `[0, 2)`

默认值:

- Qwen3.6（非思考模式）、Qwen3.5（非思考模式）、Qwen3（非思考模式）、Qwen3-Instruct系列、Qwen3-Coder系列、qwen-max系列、qwen-plus系列（非思考模式）、qwen-flash系列（非思考模式）、qwen-turbo系列（非思考模式）、qwen开源系列、qwen-coder系列、qwen-doc-turbo、qwen-vl-max-2025-08-13、Qwen3-VL（非思考）：0.7； 
- QVQ系列 、qwen-vl-plus-2025-07-10、qwen-vl-plus-2025-08-15 : 0.5； 
- qwen-audio-turbo系列：0.00001； 
- qwen-vl系列、qvq-72b-preview：0.01； 
- qwen-math系列：0； 
- Qwen3.6（思考模式）、Qwen3.5（思考模式）、Qwen3（思考模式）、Qwen3-Thinking、Qwen3-Omni-Captioner、QwQ 系列：0.6； 
- qwen3-max-preview（思考模式）、qwen-long系列： 1.0； 
- qwen-plus-character：0.92 
- Qwen3-VL（思考模式）：0.8

> 不建议修改QVQ模型的默认 temperature 值。

####  `top_p` (`float`)

（可选） 

核采样的概率阈值，控制模型生成文本的多样性。 top_p越高，生成的文本更多样。反之，生成的文本更确定。 

取值范围： `（0,1.0]`。 

默认值: 
- Qwen3.6（非思考模式）、Qwen3.5（非思考模式）、Qwen3（非思考模式）、Qwen3-Instruct系列、Qwen3-Coder系列、qwen-max系列、qwen-plus系列（非思考模式）、qwen-flash系列（非思考模式）、qwen-turbo系列（非思考模式）、qwen开源系列、qwen-coder系列、qwen-long、qwen-doc-turbo、qwq-32b-preview、qwen-audio-turbo系列、qwen-vl-max-2025-08-13、Qwen3-VL（非思考模式）：0.8； 
- qwen-vl-max-2024-11-19：0.01； 
- qwen-vl-plus系列、qwen-vl-max、qwen-vl-max-latest、qwen-vl-max-2025-04-08、qwen-vl-max-2025-04-02、qwen-vl-max-2025-01-25、qwen-vl-max-2024-12-30、qvq-72b-preview、qwen2.5-vl-3b-instruct、qwen2.5-vl-7b-instruct、qwen2.5-vl-32b-instruct、qwen2.5-vl-72b-instruct：0.001； 
- QVQ系列、qwen-vl-plus-2025-07-10、qwen-vl-plus-2025-08-15 ：0.5； 
- qwen3-max-preview（思考模式）、qwen-math系列、：1.0； 
- Qwen3.6（思考模式）、Qwen3.5（思考模式）、Qwen3（思考模式）、Qwen3-VL（思考模式）、Qwen3-Thinking、QwQ 系列、Qwen3-Omni-Captioner、qwen-plus-character：0.95

> 不建议修改QVQ模型的默认 top\\_p 值。

#### `top_k` (`integer`)

（可选） 
生成过程中采样候选集的大小。
例如，取值为50时，仅将单次生成中得分最高的50个Token组成随机采样的候选集。取值越大，生成的随机性越高；取值越小，生成的确定性越高。
取值为None或当top_k大于100时，表示不启用top_k策略，此时仅有top_p策略生效。 
取值需要大于或等于0。 

默认值:

- QVQ系列、qwen-vl-plus-2025-07-10、qwen-vl-plus-2025-08-15：10； 
- QwQ 系列：40； 
- qwen-math 系列、其余qwen-vl-plus系列、qwen-vl-max-2025-08-13之前的模型、qwen-audio-turbo系列、qvq-72b-preview：1； 
- 其余模型均为20；

> 不建议修改QVQ模型的默认 top\\_k 值。

#### `enable_thinking` (`boolean`)

（可选） 

使用混合思考模型时，是否开启思考模式，适用于Qwen3.6、Qwen3.5、 Qwen3 、Qwen3-VL模型。相关文档：[深度思考](https://help.aliyun.com/zh/model-studio/deep-thinking) 

可选值： 
- 
- `true`：开启
  - 开启后，思考内容将通过`reasoning_content`字段返回。 
- `false`：不开启 

不同模型的默认值：[支持的模型](https://help.aliyun.com/zh/model-studio/deep-thinking#78286fdc35hlw)

#### `preserve_thinking` (`boolean`)

（可选）

默认值为 `false` 

是否将对话历史中 assistant 消息的 reasoning_content 拼接至模型输入。适用于需要模型参考历史思考过程的场景。 
目前仅支持 qwen3.6-plus、qwen3.6-plus-2026-04-02。 

- 若历史消息中不包含 reasoning_content，开启此参数不会报错，正常兼容。 
- 开启后，历史对话中的 reasoning_content 会计入输入 Token 数量并计费。

#### `thinking_budget` (`integer`)

（可选） 

思考过程的最大长度。
适用于Qwen3.6、Qwen3.5、Qwen3-VL、Qwen3 的商业版与开源版模型。

相关文档：[限制思考长度](https://help.aliyun.com/zh/model-studio/deep-thinking#e7c0002fe4meu)。 
默认值为模型最大思维链长度，请参见：[模型列表](https://help.aliyun.com/zh/model-studio/models)

#### `enable_code_interpreter` (`boolean`)

（可选）

默认值为 `false` 

是否开启代码解释器功能。

仅支持qwen3.5，以及思考模式下的 qwen3-max与 qwen3-max-2026-01-23、qwen3-max-preview。
相关文档：[代码解释器](https://help.aliyun.com/zh/model-studio/qwen-code-interpreter) 

可选值：

- `true`：开启 
- `false`：不开启


#### `repetition_penalty` (`float`)

（可选） 

模型生成时连续序列中的重复度。
提高repetition_penalty时可以降低模型生成的重复度，1.0表示不做惩罚。
没有严格的取值范围，只要大于0即可。 

默认值:

- qwen-max、qwen-max-latest、qwen-max-2024-09-19、qwen-math系列、qwen-vl-max系列、qvq-72b-preview、qwen-vl-plus-2025-01-02、qwen-vl-plus-2025-05-07、qwen-vl-plus-2025-07-10、qwen-vl-plus-2025-08-15、qwen-vl-plus-latest、qwen2.5-vl-3b-instruct、qwen2.5-vl-7b-instruct、qwen2.5-vl-32b-instruct、qwen2.5-vl-72b-instruct、qwen-audio-turbo系列、QVQ系列、QwQ系列、qwq-32b-preview、Qwen3-VL： 1.0； 
- qwen-coder系列、qwen2.5-1.5b-instruct、qwen2.5-0.5b-instruct：1.1； 
- qwen-vl-plus、qwen-vl-plus-2025-01-25：1.2； 
- 其余模型为1.05。

> 使用qwen-vl-plus_2025-01-25模型进行文字提取时，建议设置repetition_penalty为1.0。
> 不建议修改QVQ模型的默认 repetition_penalty 值。

#### `presence_penalty` (`float`)

（可选） 

控制模型生成文本时的内容重复度。
正值降低重复度，负值增加重复度。
在创意写作或头脑风暴等需要多样性、趣味性或创造力的场景中，建议调高该值；在技术文档或正式文本等强调一致性与术语准确性的场景中，建议调低该值。

取值范围：`[-2.0, 2.0]`。

默认值:

- Qwen3.6（非思考模式）、Qwen3.5-Omni、Qwen3.5（非思考模式）、qwen3-max-preview（思考模式）、Qwen3（非思考模式）、Qwen3-Instruct系列、qwen3-0.6b/1.7b/4b（思考模式）、QVQ系列、qwen-max、qwen-max-latest、qwen-max-latest、qwen-max-2024-09-19、qwen2.5-vl系列、qwen-vl-max系列、qwen-vl-plus、qqwen-vl-plus-2025-01-02、Qwen3-VL（非思考）：1.5； 
- qwen-vl-plus-latest、qwen-vl-plus-2025-08-15、qwen-vl-plus-2025-07-10：1.2 qwen-vl-plus-2025-01-25：1.0； 
- qwen3-8b/14b/32b/30b-a3b/235b-a22b（思考模式）、qwen-plus/qwen-plus-latest/2025-04-28（思考模式）、qwen-turbo/qwen-turbo/2025-04-28（思考模式）：0.5； 
- 其余均为0.0。 

**原理介绍** 

如果参数值是正数，模型将对目前文本中已存在的Token施加一个惩罚值（惩罚值与文本出现的次数无关），减少这些Token重复出现的几率，从而减少内容重复度，增加用词多样性。 

**示例** 

提示词：把这句话翻译成中文“This movie is good. The plot is good, the acting is good, the music is good, and overall, the whole movie is just good. It is really good, in fact. The plot is so good, and the acting is so good, and the music is so good.” 

- 参数值为2.0：这部电影很好。剧情很棒，演技棒，音乐也非常好听，总的来说，整部电影都好得不得了。实际上它真的很优秀。剧情非常精彩，演技出色，音乐也是那么的动听。 
- 参数值为0.0：这部电影很好。剧情好，演技好，音乐也好，总的来说，整部电影都很好。事实上，它真的很棒。剧情非常好，演技也非常出色，音乐也同样优秀。 
- 参数值为-2.0：这部电影很好。情节很好，演技很好，音乐也很好，总的来说，整部电影都很好。实际上，它真的很棒。情节非常好，演技也非常好，音乐也非常好。

> 使用qwen-vl-plus-2025-01-25模型进行文字提取时，建议设置presence_penalty为1.5。
> 不建议修改QVQ模型的默认presence_penalty值。

#### `vl_high_resolution_images` (`boolean`)

（可选）

默认值为`false` 

是否将输入图像的像素上限提升至 16384 Token 对应的像素值。

相关文档：[处理高分辨率图像](https://help.aliyun.com/zh/model-studio/vision#e7e2db755f9h7)。 

取值与影响：

- `true`，使用固定分辨率策略，忽略 `max_pixels` 设置，超过此分辨率时会将图像总像素缩小至此上限内。
  - 不同模型像素上限不同
    - `Qwen3.6`系列、`Qwen3.5`系列、`Qwen3-VL系列`、`qwen-vl-max`、`qwen-vl-max-latest`、`qwen-vl-max-0813`、`qwen-vl-plus`、`qwen-vl-plus-latest`、`qwen-vl-plus-0815``、qwen-vl-plus-0710`模型：`16777216`（每`Token`对应`32*32`像素，即`16384*32*32`） 
    - `QVQ系列`、其他`Qwen2.5-VL系列`模型：`12845056`（每`Token`对应`28*28`像素，即 `16384*28*28`） 
- `false`，像素上限由 `max_pixels` 决定，输入图像的像素超过`max_pixels`会将图像缩小至`max_pixels`内。各模型的默认像素上限即`max_pixels`的默认值。

#### `vl_enable_image_hw_output` (`boolean`)

（可选）

默认值为 `false` 

是否返回图像缩放后的尺寸。模型会对输入的图像进行缩放处理，配置为 True 时会返回图像缩放后的高度和宽度，开启流式输出时，该信息在最后一个数据块（chunk）中返回。
支持[Qwen-VL模型](https://help.aliyun.com/zh/model-studio/vision)。

#### `max_tokens` (`integer`)

（可选） 

用于限制模型输出的最大 Token 数。若生成内容超过此值，生成将提前停止，且返回的`finish_reason`为`length`。 
默认值与最大值均为模型的最大输出长度，请参见[文本生成-千问](https://help.aliyun.com/zh/model-studio/models#9f8890ce29g5u)。 
适用于需控制输出长度的场景，如生成摘要、关键词，或用于降低成本、缩短响应时间。 
触发 `max_tokens` 时，响应的 finish_reason 字段为 `length`。

> `max_tokens`不限制思考模型思维链的长度。

#### `seed` (`integer`)

（可选） 

随机数种子。用于确保在相同输入和参数下生成结果可复现。若调用时传入相同的 `seed` 且其他参数不变，模型将尽可能返回相同结果。 

取值范围：`[0,231−1]`。

默认值：

- qwen-vl-plus-2025-01-02、qwen-vl-max、qwen-vl-max-latest、qwen-vl-max-2025-04-08、qwen-vl-max-2025-04-02、qwen-vl-max-2024-12-30、qvq-72b-preview、qvq-max系列：3407； 
- qwen-vl-max-2025-01-25、qwen-vl-max-2024-11-19、qwen-vl-max-2024-02-01、qwen-vl-plus、qwen-vl-plus-latest、qwen-vl-plus-2025-05-07、qwen-vl-plus-2025-01-25：无默认值； 
- 其余模型均为1234。

#### `stream` (`boolean`)

（可选） 

默认值为`false` 是否流式输出回复。

参数值： 

- false：模型生成完所有内容后一次性返回结果。 
- true：边生成边输出，即每生成一部分内容就立即输出一个片段（chunk）。通过HTTP实现流式输出请在Header中指定`X-DashScope-SSE`为`enable`。

> Qwen3商业版（思考模式）、Qwen3开源版、QwQ、QVQ只支持流式输出。

####  `incremental_output` (`boolean`)

（可选）
默认为`false`；（Qwen3-Max、Qwen3-VL、[Qwen3 开源版](https://help.aliyun.com/zh/model-studio/models#9d516d17965af)、[QwQ](https://help.aliyun.com/zh/model-studio/deep-thinking) 、[QVQ](https://help.aliyun.com/zh/model-studio/visual-reasoning)模型默认值为 `true`） 

在流式输出模式下是否开启增量输出。推荐您优先设置为`true`。 

参数值： 
- 
- false：每次输出为当前已经生成的整个序列，最后一次输出为生成的完整结果。
    ```
    I I like I like apple I like apple.
    ```
- true（推荐）：增量输出，即后续输出内容不包含已输出的内容。您需要实时地逐个读取这些片段以获得完整的结果。
    ```
    I like apple .
    ```

> QwQ 模型与思考模式下的 Qwen3 模型只支持设置为 `true`。由于 Qwen3 商业版模型默认值为`false`，您需要在思考模式下手动设置为 `true`。
> Qwen3 开源版模型不支持设置为 `false`。

#### `response_format` (`object`)

（可选） 

默认值为`{"type": "text"}` 返回内容的格式。

可选值： 

- `{"type": "text"}`：输出文字回复； 
- `{"type": "json_object"}`：输出标准格式的JSON字符串；需在提示词中明确指示模型输出JSON，如：“请按照json格式输出”，否则会报错。

> 相关文档：[结构化输出](https://help.aliyun.com/zh/model-studio/qwen-structured-output)。
> 支持的模型参见[支持的模型](https://help.aliyun.com/zh/model-studio/qwen-structured-output#7a8e438e89xeq)。


#### `result_format` (`string`)

（可选） 

默认为`text`（Qwen3-Max、Qwen3-VL、[QwQ](https://help.aliyun.com/zh/model-studio/deep-thinking) 模型、Qwen3 开源模型（除了qwen3-next-80b-a3b-instruct）与 Qwen-Long 模型默认值为 `message`） 。返回数据的格式。推荐您优先设置为`message`，可以更方便地进行[多轮对话](https://help.aliyun.com/zh/model-studio/multi-round-conversation)。

平台后续将统一调整默认值为`message`。

> 模型为千问VL/QVQ/Audio时，设置`text`不生效。
> Qwen3-Max、Qwen3-VL、思考模式下的 Qwen3 模型只能设置为`message`，由于 Qwen3 商业版模型默认值为`text`，您需要将其设置为`message`。

#### `logprobs` (`boolean`)

（可选）

默认值为 `false` 是否返回输出 Token 的对数概率

可选值： 

- `true` 返回 
- `false` 不返回 

支持以下模型： 

- qwen-plus系列的快照模型（不包含稳定版模型） 
- qwen-turbo 系列的快照模型（不包含稳定版模型） 
- qwen3-vl-plus系列（包含稳定版模型） 
- qwen3-vl-flash系列（包含稳定版模型） 
- Qwen3 开源模型

#### `top_logprobs` (`integer`)

（可选）

默认值为0 

指定在每一步生成时，返回模型最大概率的候选 Token 个数。 
取值范围：`[0,5]`。
仅当 `logprobs` 为 `true` 时生效。

#### `n` (`integer`)

（可选）

默认值为1 。

生成响应的个数，取值范围是1、2、3、4。对于需要生成多个响应的场景（如创意写作、广告文案等），可以设置较大的 n 值。

> 当前仅支持 [Qwen3（非思考模式）](https://help.aliyun.com/zh/model-studio/deep-thinking#be9890136awsc)、qwen-plus-character 模型，且在传入 tools 参数时固定为1。
> 设置较大的 n 值不会增加输入 Token 消耗，会增加输出 Token 的消耗。

#### `stop` (`string 或 array`)

（可选） 

用于指定停止词。

当模型生成的文本中出现`stop` 指定的字符串或`token_id`时，生成将立即终止。 可传入敏感词以控制模型的输出。

> stop为数组时，不可将`token_id`和字符串同时作为元素输入，比如不可以指定为`["你好",104307]`。

#### `tools` (`array`)

（可选） 

包含一个或多个工具对象的数组，供模型在 Function Calling 中调用。相关文档：[Function Calling](https://help.aliyun.com/zh/model-studio/qwen-function-calling) 

使用 `tools` 时，必须将`result_format`设为`message`。 

发起 Function Calling，或提交工具执行结果时，都必须设置`tools`参数。 

属性：

- `type` `*string*` **（必选）** 工具类型
  - 当前仅支持`function`。 
- `function` `*object*` **（必选）** 
  - `name` `*string*` **（必选）** 工具函数的名称，必须是字母、数字，可以包含下划线和短划线，最大长度为64。 
  - `description` `*string*` **（必选）** 工具函数的描述，供模型选择何时以及如何调用工具函数。 
  - `parameters` `*object*` （可选）默认值为 `{}` 工具的参数描述，需要是一个合法的JSON Schema。
    - JSON Schema的描述可以见[链接](https://json-schema.org/understanding-json-schema)。若`parameters`参数为空，表示该工具没有入参（如时间查询工具）。
  
> 为提高工具调用的准确性，建议传入 `parameters`。
> 暂时不支持qwen-vl与qwen-audio系列模型。

#### `tool_choice` (`string 或 object`)

（可选）

默认值为 `auto`。

工具选择策略。若需对某类问题强制指定工具调用方式（例如始终使用某工具或禁用所有工具），可设置此参数。 

- `auto` 大模型自主选择工具策略； 
- `none` 若在特定请求中希望临时禁用工具调用，可设定`tool_choice`参数为`none`； 
- `{"type": "function", "function": {"name": "the_function_to_call"}}` 
  - 若希望强制调用某个工具，可设定`tool_choice`参数为`{"type": "function", "function": {"name": "the_function_to_call"}}`，其中`the_function_to_call`是指定的工具函数名称。

> 思考模式的模型不支持强制调用某个工具。

#### `parallel_tool_calls` (`boolean`)

（可选）

默认值为 `false`。

是否开启并行工具调用。 

可选值： 

- `true`：开启 
- `false`：不开启。 

并行工具调用详情请参见：[并行工具调用](https://help.aliyun.com/zh/model-studio/qwen-function-calling#cb6b5c484bt4x)。

#### `enable_search` (`boolean`)

（可选） 

默认值为`false`。 

模型在生成文本时是否使用互联网搜索结果进行参考。

取值如下： 

- true：启用互联网搜索，模型会将搜索结果作为文本生成过程中的参考信息，但模型会基于其内部逻辑判断是否使用互联网搜索结果。 若开启后未联网搜索，可优化提示词，或设置`search_options`中的`forced_search`参数开启强制搜索。 
- false：关闭互联网搜索。 计费信息请参见[计费说明](https://help.aliyun.com/zh/model-studio/web-search#92ce83df3a599)。

> 启用互联网搜索功能可能会增加 Token 的消耗。

#### `search_options` (`object`)

（可选） 

联网搜索的策略。仅当`enable_search`为`true`时生效。

详情参见[联网搜索](https://help.aliyun.com/zh/model-studio/web-search#cbddf5b28bug8)。

属性：

- `enable_source` `*boolean*`（可选）默认值为`false` 
  - 在返回结果中是否展示搜索到的信息。
  - 参数值： 
    - true：展示； 
    - false：不展示。 
- `enable_citation` `*boolean*`（可选）默认值为`false` 
  - 是否开启\\[1\\]或\\[ref\\_1\\]样式的角标标注功能。在`enable_source`为`true`时生效。
  - 参数值： 
    - true：开启； 
    - false：不开启。 
- `citation_format` `*string*`（可选）默认值为`"[<number>]"` 
  - 角标样式。在`enable_citation`为`true`时生效。
  - 参数值： 
    - `[<number>]`：角标形式为`[1]`； 
    - `[ref_<number>]`：角标形式为`[ref_1]`。 
- `forced_search` `*boolean*`（可选）默认值为`false` 
  - 是否强制开启搜索。
  - 参数值： 
    - true：强制开启； 
    - false：不强制开启。 
- `search_strategy` `*string*`（可选）默认值为`turbo` 
  - 搜索互联网信息的策略。 
  - 可选值： 
    - `turbo` （默认）: 兼顾响应速度与搜索效果，适用于大多数场景。 
    - `max`: 采用更全面的搜索策略，可调用多源搜索引擎，以获取更详尽的搜索结果，但响应时间可能更长。 
    - `agent`：可多次调用联网搜索工具与大模型，实现多轮信息检索与内容整合。 
      - 该策略仅适用于qwen3.5-plus、qwen3.5-plus-2026-02-15、qwen3.5-flash、qwen3.5-flash-2026-02-23、qwen3-max与 qwen3-max-2026-01-23 的思考模式（仅支持流式）、qwen3-max-2026-01-23的非思考模式、qwen3-max-2025-09-23。 
      - 启用该策略时，仅支持**返回搜索来源**（`enable_source: true`），其他联网搜索功能不可用。 
    - `agent_max`：在`agent`策略基础上支持网页抓取，参见：[网页抓取](https://help.aliyun.com/zh/model-studio/web-extractor)。
      - 该策略仅适用于qwen3.5-plus、qwen3.5-plus-2026-02-15、qwen3.5-flash、qwen3.5-flash-2026-02-23，以及 qwen3-max与 qwen3-max-2026-01-23 的思考模式。 
      - 启用该策略时，仅支持**返回搜索来源**（`enable_source: true`），其他联网搜索功能不可用。 
- `enable_search_extension` `*boolean*`（可选）默认值为`false` 
  - 是否开启特定领域增强。
  - 参数值： 
    - `true` 开启。 
    - `false`（默认值） 不开启。 
- `prepend_search_result` `*boolean*`（可选）默认值为`false` 
  - 在流式输出且`enable_source`为`true`时，可通过`prepend_search_result`配置**第一个返回的数据包**是否只包含搜索来源信息。
  - 可选值： 
    - `true` 只包含搜索来源信息。 
    - `false`（默认值） 包含搜索来源信息与大模型回复信息。

#### `X-DashScope-DataInspection` (`string`)

（可选） 

在千问 API 的内容安全能力基础上，是否进一步识别输入输出内容的违规信息。

取值如下： 
- `'{"input":"cip","output":"cip"}'`：进一步识别； 
- 不设置该参数：不进一步识别。 

- 通过 HTTP 调用时请放入请求头：`-H "X-DashScope-DataInspection: {\"input\": \"cip\", \"output\": \"cip\"}"`； 

> 不适用于Qwen-Audio 系列模型。

## chat响应对象（流式与非流式输出格式一致）

### 示例响应

```json
{ "status_code": 200, "request_id": "902fee3b-f7f0-9a8c-96a1-6b4ea25af114", "code": "", "message": "", "output": { "text": null, "finish_reason": null, "choices": [ { "finish_reason": "stop", "message": { "role": "assistant", "content": "我是阿里云开发的一款超大规模语言模型，我叫千问。" } } ] }, "usage": { "input_tokens": 22, "output_tokens": 17, "total_tokens": 39 } }
```
### `status_code` (`string`)
本次请求的状态码。200 表示请求成功，否则表示请求失败。

### `request_id` (`string`)
本次调用的唯一标识符。

### `code` (`string`)
错误码，调用成功时为空值。
> 只有Python SDK返回该参数。

### `output` (`object`)
调用结果信息。 

属性: 

- `text` `*string*` 模型生成的回复。当设置输入参数**result_format**为**text**时将回复内容返回到该字段。 
- `finish_reason` `*string*` 当设置输入参数**result_format**为**text**时该参数不为空。 
  - 有四种情况： 
    - 正在生成时为null； 
    - 因模型输出自然结束，或触发输入参数中的stop条件而结束时为stop； 
    - 因生成长度过长而结束为length； 
    - 因发生工具调用为tool_calls。 
- `choices` `*array*` 模型的输出信息。当result_format为message时返回choices参数。 
  - `finish_reason` `*string*` 
    - 有四种情况： 
      - 正在生成时为null； 
      - 因模型输出自然结束，或触发输入参数中的stop条件而结束时为stop； 
      - 因生成长度过长而结束为length； 
      - 因发生工具调用为tool_calls。 
  - `message` `*object*` 模型输出的消息对象。 
    - `role` `*string*` 输出消息的角色，固定为assistant。 
    - `content` `*string或array*` 输出消息的内容。当使用qwen-vl或qwen-audio系列模型时为`array`，其余情况为`string`。 
      - 如果发起Function Calling，则该值为空。 
      - `text` `*string*` 当使用qwen-vl或qwen-audio系列模型时，输出消息的内容。 
      - `image_hw` `*array*` 当Qwen-VL系列模型启用 vl_enable_image_hw_output 参数时，有两种情况： 
        - 图像输入：返回图像的高度和高度（数值单位：像素） 
        - 视频输入：返回空数组 
    - `reasoning_content` `*string*` 模型的深度思考内容。 
    - `tool_calls` `*array*` 若模型需要调用工具，则会生成tool_calls参数。 
      - `function` `object` 调用工具的名称，以及输入参数。 
        - `name` `*string*` 调用工具的名称 
        - `arguments` `*string*` 需要输入到工具中的参数，为JSON字符串。
          - 由于大模型响应有一定随机性，输出的JSON字符串并不总满足于您的函数，建议您在将参数输入函数前进行参数的有效性校验。 
      - `index` `*integer*` 当前**tool_calls**对象在tool_calls数组中的索引。 
      - `id` `*string*` 本次工具响应的ID。 
      - `type` `*string*` 工具类型，固定为`function`。 
  - `logprobs` `*object*` 当前 choices 对象的概率信息。 
    - `content` `*array*` 带有对数概率信息的 Token 数组。 
      - `token` `*string*` 当前 Token。 
      - `bytes` `*array*` 当前 Token 的 UTF‑8 原始字节列表，用于精确还原输出内容，在处理表情符号、中文字符时有帮助。 
      - `logprob` `*float*` 当前 Token 的对数概率。返回值为 null 表示概率值极低。 
      - `top_logprobs` `*array*` 当前 Token 位置最可能的若干个 Token 及其对数概率，元素个数与入参的`top_logprobs`保持一致。 
        - `token` `*string*` 当前 Token。 
        - `bytes` `*array*` 当前 Token 的 UTF‑8 原始字节列表，用于精确还原输出内容，在处理表情符号、中文字符时有帮助。 
        - `logprob` `*float*` 当前 Token 的对数概率。返回值为 null 表示概率值极低。 
  - `search_info` `*object*` 联网搜索到的信息，在设置`search_options`参数后会返回该参数。 
    - `search_results` `*array*` 联网搜索到的结果。 
      - `site_name` `*string*` 搜索结果来源的网站名称。 
      - `icon` `*string*` 来源网站的图标URL，如果没有图标则为空字符串。 
      - `index` `*integer*` 搜索结果的序号，表示该搜索结果在`search_results`中的索引。 
      - `title` `*string*` 搜索结果的标题。 
      - `url` `*string*` 搜索结果的链接地址。 
    - `extra_tool_info` `*array*` 开启`enable_search_extension`参数后返回的领域增强信息。 
      - `result` `*string*` 领域增强工具输出信息。 
      - `tool` `*string*` 领域增强使用的工具。

#### `usage` (`map`)

本次chat请求使用的Token信息。 

属性:

- `input_tokens` `*integer*` 用户输入内容转换成Token后的长度。 
- `output_tokens` `*integer*` 模型输出内容转换成Token后的长度。 
- `input_tokens_details` `*integer*` 
  - 使用[Qwen-VL 模型](https://help.aliyun.com/zh/model-studio/vision)或[QVQ模型](https://help.aliyun.com/zh/model-studio/visual-reasoning)时，输入内容转换成Token后的长度详情。 
  - 属性 
    - `text_tokens` `*integer*` 使用[Qwen-VL 模型](https://help.aliyun.com/zh/model-studio/vision)或[QVQ模型](https://help.aliyun.com/zh/model-studio/visual-reasoning)时，为输入的文本转换为Token后的长度。 
    - `image_tokens` `*integer*` 输入的图像转换为Token后的长度。 
    - `video_tokens` `*integer*` 输入的视频文件或图像列表转换为Token后的长度。 
    - `total_tokens` `*integer*` 当输入为纯文本时返回该字段，为**input_tokens**与**output_tokens**之和**。** **image_tokens** `*integer*` 输入内容包含`image`时返回该字段。为用户输入图片内容转换成Token后的长度。 
    - `video_tokens` `*integer*` 输入内容包含`video`时返回该字段。为用户输入视频内容转换成Token后的长度。 
    - `audio_tokens` `*integer*` 输入内容包含`audio`时返回该字段。为用户输入音频内容转换成Token后的长度。 
    - `output_tokens_details` `*integer*` 输出内容转换成 Token后的长度详情。 
      - `text_tokens` `*integer*` 输出的文本转换为Token后的长度。 
      - `reasoning_tokens` `*integer*` 思考过程转换为Token后的长度。 
    - `prompt_tokens_details` `*object*` 输入 Token 的细粒度分类。 
      - `cached_tokens` `*integer*` 命中 Cache 的 Token 数。Context Cache 详情请参见[上下文缓存](https://help.aliyun.com/zh/model-studio/context-cache)。 
      - `cache_creation` `*object*` [显式缓存](https://help.aliyun.com/zh/model-studio/context-cache#825f201c5fy6o)创建信息。 
        - `ephemeral_5m_input_tokens` `*integer*` 用于创建5分钟有效期显式缓存的 Token 长度。 
      - `cache_creation_input_tokens` `*integer*` 用于创建显式缓存的 Token 长度。 
      - `cache_type` `*string*` 使用[显式缓存](https://help.aliyun.com/zh/model-studio/context-cache#825f201c5fy6o)时，参数值为`ephemeral`，否则该参数不存在。

## 错误码

如果模型调用失败并返回报错信息，请参见[错误信息](https://help.aliyun.com/zh/model-studio/error-code)进行解决。
