package no.fintlabs.discovery.gateway.model.digisak;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubsidyGroupDefinition {

    @NotBlank
    private String id;

    @NotBlank
    private String displayName;

    @NotEmpty
    private List<@NotNull SubsidyFieldDefinition> fieldDefinitions;
}
