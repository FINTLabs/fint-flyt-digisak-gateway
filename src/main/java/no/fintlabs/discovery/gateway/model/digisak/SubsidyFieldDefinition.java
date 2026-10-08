package no.fintlabs.discovery.gateway.model.digisak;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubsidyFieldDefinition {

    @NotBlank
    private String id;

    @NotBlank
    private String displayName;

    private String type;
}
