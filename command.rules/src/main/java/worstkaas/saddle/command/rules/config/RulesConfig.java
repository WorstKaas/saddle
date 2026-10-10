/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package worstkaas.saddle.command.rules.config;

import com.crimsonwarpedcraft.cwcommons.config.Config;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Configuration for the rules plugin
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RulesConfig implements Config {

    @JsonProperty("title")
    private String title;

    /// Text of the accept button
    @JsonProperty("accept-button")
    private String acceptButton;

    /// Text of the close button
    @JsonProperty("close-button")
    private String closeButton;

    /// List of rule paragraphs to display in the GUI
    @JsonProperty("body")
    private List<RuleParagraph> body;

    /// Global welcome message shown when a player accepts the rules for the first time
    @JsonProperty("welcome-message")
    private String welcomeMessage;

    public RulesConfig copy() {
        return this.toBuilder()
                .body(body == null ? null : body.stream()
                        .map(RuleParagraph::copy)
                        .toList())
                .build();
    }

    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleParagraph {

        /// Text body of the paragraph
        @JsonProperty("paragraph")
        private String paragraph;

        /// Colour of the paragraph
        @JsonProperty("color")
        private String color;

        public RuleParagraph copy() {
            return this.toBuilder().build();
        }
    }
}
