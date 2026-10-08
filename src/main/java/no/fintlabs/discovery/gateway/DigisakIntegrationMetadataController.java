package no.fintlabs.discovery.gateway;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.discovery.gateway.model.digisak.SubsidyDefinition;
import no.fintlabs.discovery.gateway.model.digisak.SubsidyFieldDefinition;
import no.novari.flyt.gateway.metadata.IntegrationMetadataProcessor;
import no.novari.flyt.gateway.metadata.IntegrationMetadataValidator;
import no.novari.flyt.gateway.metadata.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.novari.flyt.webresourceserver.UrlPaths.EXTERNAL_API;

@Slf4j
@RestController
@RequestMapping(EXTERNAL_API + "/digisak/metadata")
public class DigisakIntegrationMetadataController {

    private final IntegrationMetadataProcessor integrationMetadataProcessor;
    private final IntegrationMetadataValidator<SubsidyDefinition> digisakSubsidyDefinitionValidator;

    public DigisakIntegrationMetadataController(
            IntegrationMetadataProcessor integrationMetadataProcessor,
            IntegrationMetadataValidator<SubsidyDefinition> digisakSubsidyDefinitionValidator
    ) {
        this.integrationMetadataProcessor = integrationMetadataProcessor;
        this.digisakSubsidyDefinitionValidator = digisakSubsidyDefinitionValidator;
    }

    @PostMapping()
    public ResponseEntity<Void> postIntegrationMetadata(
            @RequestBody SubsidyDefinition subsidyDefinition,
            Authentication authentication
    ) {
        return integrationMetadataProcessor.processIntegrationMetadata(
                authentication,
                subsidyDefinition,
                DigisakIntegrationMetadataController::toIntegrationMetadata,
                digisakSubsidyDefinitionValidator
        );
    }

    private static IntegrationMetadata toIntegrationMetadata(long sourceApplicationId, SubsidyDefinition subsidyDefinition) {
        return new IntegrationMetadata(
                sourceApplicationId,
                subsidyDefinition.getIntegrationId(),
                null,
                subsidyDefinition.getIntegrationDisplayName(),
                subsidyDefinition.getVersion(),
                new InstanceMetadataContent(
                        getInstanceValueMetadata(subsidyDefinition),
                        getInstanceObjectCollectionMetadata(subsidyDefinition),
                        getInstanceMetadataCategories(subsidyDefinition)
                )
        );
    }

    private static List<InstanceValueMetadata> getInstanceValueMetadata(SubsidyDefinition subsidyDefinition) {
        return subsidyDefinition.getFieldDefinitions().stream()
                .map(subsidyField -> new InstanceValueMetadata(
                        subsidyField.getDisplayName(),
                        getType(subsidyField),
                        subsidyField.getId()))
                .collect(Collectors.toList());
    }

    private static List<InstanceMetadataCategory> getInstanceMetadataCategories(SubsidyDefinition subsidyDefinition) {
        return subsidyDefinition.getGroupDefinitions().stream()
                .map(subsidyGroupDefinition -> new InstanceMetadataCategory(
                        subsidyGroupDefinition.getDisplayName(),
                        new InstanceMetadataContent(
                                subsidyGroupDefinition.getFieldDefinitions().stream()
                                        .flatMap(subsidyField ->
                                                toInstanceValueMetadata(subsidyGroupDefinition.getId().concat(StringUtils.capitalize(subsidyField.getId())), subsidyField))
                                        .collect(Collectors.toList()),
                                List.of(),
                                List.of()
                        )))
                .collect(Collectors.toList());
    }

    private static List<InstanceObjectCollectionMetadata> getInstanceObjectCollectionMetadata(SubsidyDefinition subsidyDefinition) {
        return subsidyDefinition.getCollectionDefinitions().stream()
                .map(subsidyCollectionDefinition -> new InstanceObjectCollectionMetadata(
                        subsidyCollectionDefinition.getDisplayName(),
                        new InstanceMetadataContent(
                                subsidyCollectionDefinition.getFieldDefinitions().stream()
                                        .flatMap(subsidyField -> toInstanceValueMetadata(subsidyField.getId(), subsidyField))
                                        .collect(Collectors.toList()),
                                List.of(),
                                List.of()
                        ),
                        subsidyCollectionDefinition.getId()))
                .collect(Collectors.toList());
    }

    private static Stream<InstanceValueMetadata> toInstanceValueMetadata(String keyPrefix, SubsidyFieldDefinition subsidyField) {
        if (InstanceValueMetadata.Type.FILE.equals(getType(subsidyField))) {
            return Stream.of(
                    new InstanceValueMetadata("Fil", InstanceValueMetadata.Type.FILE, keyPrefix.concat("Data")),
                    new InstanceValueMetadata("Format", InstanceValueMetadata.Type.STRING, keyPrefix.concat("Format")),
                    new InstanceValueMetadata("Filnavn", InstanceValueMetadata.Type.STRING, keyPrefix.concat("Filnavn"))
            );
        } else {
            return Stream.of(
                    new InstanceValueMetadata(subsidyField.getDisplayName(), getType(subsidyField), keyPrefix));
        }
    }

    private static InstanceValueMetadata.Type getType(SubsidyFieldDefinition subsidyField) {
        return "FILE".equals(subsidyField.getType()) ? InstanceValueMetadata.Type.FILE : InstanceValueMetadata.Type.STRING;
    }

}
