package no.novari.golden;

import no.novari.discovery.gateway.DigisakIntegrationMetadataController;
import no.novari.discovery.gateway.model.digisak.SubsidyCollectionDefinition;
import no.novari.discovery.gateway.model.digisak.SubsidyDefinition;
import no.novari.discovery.gateway.model.digisak.SubsidyFieldDefinition;
import no.novari.discovery.gateway.model.digisak.SubsidyGroupDefinition;
import no.novari.flyt.gateway.metadata.IntegrationMetadataMapper;
import no.novari.flyt.gateway.metadata.IntegrationMetadataProcessor;
import no.novari.flyt.gateway.metadata.IntegrationMetadataValidator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.Authentication;

import java.util.List;

import static no.novari.golden.GoldenFiles.assertMatchesGolden;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class IntegrationMetadataGoldenTest {

    @Test
    @SuppressWarnings("unchecked")
    void subsidyDefinitionIsMappedToIntegrationMetadata() throws Exception {
        IntegrationMetadataProcessor processor = mock(IntegrationMetadataProcessor.class);
        IntegrationMetadataValidator<SubsidyDefinition> validator = mock(IntegrationMetadataValidator.class);
        Authentication authentication = mock(Authentication.class);
        SubsidyDefinition definition = SubsidyDefinition.builder()
                .integrationId("FRIP")
                .integrationDisplayName("Fredete og verneverdige bygninger")
                .version(4L)
                .fieldDefinitions(List.of(
                        new SubsidyFieldDefinition("saksnummer", "Saksnummer", null),
                        new SubsidyFieldDefinition("fil1", "Søknad", "FILE")
                ))
                .groupDefinitions(List.of(new SubsidyGroupDefinition("hoveddokument", "Hoveddokument", List.of(
                        new SubsidyFieldDefinition("tittel", "Tittel", "STRING"),
                        new SubsidyFieldDefinition("fil2", "Fil", "FILE")
                ))))
                .collectionDefinitions(List.of(new SubsidyCollectionDefinition("vedlegg", "Vedlegg", List.of(
                        new SubsidyFieldDefinition("filnavn", "Filnavn", null),
                        new SubsidyFieldDefinition("fil3", "Fil", "FILE")
                ))))
                .build();

        new DigisakIntegrationMetadataController(processor, validator).postIntegrationMetadata(definition, authentication);

        ArgumentCaptor<IntegrationMetadataMapper<SubsidyDefinition>> mapper = ArgumentCaptor.forClass(IntegrationMetadataMapper.class);
        verify(processor).processIntegrationMetadata(eq(authentication), eq(definition), mapper.capture(), eq(validator));
        assertMatchesGolden("digisak-integration-metadata", mapper.getValue().toIntegrationMetadata(3L, definition));
    }
}
