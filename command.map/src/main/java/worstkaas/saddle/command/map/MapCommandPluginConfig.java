/*
 * Copyright (c) ${year} Strelitzia. All rights reserved.
 *
 */

package worstkaas.saddle.command.map;

import com.crimsonwarpedcraft.cwcommons.config.Config;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Static configuration settings for the MapCommand plugin, contains the link to the webmap
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MapCommandPluginConfig implements Config {

    @NotBlank
    @JsonProperty("webmap-url")
    private String webmapUrl = "map.example.com";

}
