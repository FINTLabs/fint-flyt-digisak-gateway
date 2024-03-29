package no.fintlabs.discovery.gateway.model.digisak;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
