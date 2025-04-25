package com.github.vvojtas.dailogi_server.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "dailogi.img")
public class ImgPathProperties {
    private String path;
} 