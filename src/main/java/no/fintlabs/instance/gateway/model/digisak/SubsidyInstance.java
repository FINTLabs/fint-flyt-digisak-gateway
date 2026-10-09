package no.fintlabs.instance.gateway.model.digisak;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

@Getter
@Jacksonized
@EqualsAndHashCode
@Builder
public class SubsidyInstance {

    @NotBlank
    private String integrationId;

    @NotBlank
    private String instanceId;

    @NotNull
    private Map<String, Object> fields;

    private Map<String, Map<String, Object>> groups;

    private Map<String, List<Map<String, Object>>> collections;
}
