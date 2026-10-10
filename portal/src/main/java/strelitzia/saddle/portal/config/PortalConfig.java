/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal.config;

import com.crimsonwarpedcraft.cwcommons.config.Config;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/// Configuration model for portal plugin settings
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PortalConfig implements Config {

    /// Default particle effect type for new portals
    @JsonProperty("default-effect")
    private String defaultEffect;

    /// Default BlueMap icon for new portals
    @JsonProperty("default-icon")
    private String defaultIcon;

    /// Creates a copy of this configuration
    /// @return a new PortalConfig instance with the same values
    public PortalConfig copy() {
        return this.toBuilder().build();
    }
}
