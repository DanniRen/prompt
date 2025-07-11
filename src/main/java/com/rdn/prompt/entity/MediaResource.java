package com.rdn.prompt.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaResource {
    private String resourceId;      // 资源ID
    private String url;             // 资源URL
    private Integer type;           // 1-图片，2-音频，3-视频
    private String description;     // 资源描述
}
