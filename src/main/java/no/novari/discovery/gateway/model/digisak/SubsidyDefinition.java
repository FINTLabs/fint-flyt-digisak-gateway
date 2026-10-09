package no.novari.discovery.gateway.model.digisak;

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
public class SubsidyDefinition {

    @NotBlank
    private String integrationId;

    @NotBlank
    private String integrationDisplayName;

    @NotNull
    private Long version;

    @NotEmpty
    private List<@NotNull SubsidyFieldDefinition> fieldDefinitions;

    private List<@NotNull SubsidyGroupDefinition> groupDefinitions;

    private List<@NotNull SubsidyCollectionDefinition> collectionDefinitions;
}
