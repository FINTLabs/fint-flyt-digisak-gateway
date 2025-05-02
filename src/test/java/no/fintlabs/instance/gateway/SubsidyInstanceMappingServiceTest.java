package no.fintlabs.instance.gateway;

import no.fintlabs.gateway.instance.model.File;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.instance.gateway.model.digisak.SubsidyInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SubsidyInstanceMappingServiceTest {

    @Mock
    Function<File, Mono<UUID>> persistFile;

    private SubsidyInstanceMappingService service;
    private SubsidyInstance subsidyInstance;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new SubsidyInstanceMappingService();

        subsidyInstance = SubsidyInstance.builder()
                .integrationId("FRIP")
                .instanceId("12345")
                .fields(Map.of(
                        "kulturminneId", "99",
                        "saksnummer", "55",
                        "fil1", Map.of(
                                "format", "text/plain",
                                "filnavn", "fil.txt",
                                "data", "BASE64_STRING"
                        )))
                .groups(
                        Map.of("hoveddokument",
                                Map.of(
                                        "mediatype", "text/plain",
                                        "filnavn", "hoveddokument.txt",
                                        "fil2", Map.of(
                                                "format", "text/plain",
                                                "filnavn", "fil.txt",
                                                "data", "BASE64_STRING"
                                        )
                                )
                        ))
                .collections(
                        Map.of("vedlegg", List.of(
                                Map.of(
                                        "mediatype", "text/plain",
                                        "filnavn", "vedlegg1.txt",
                                        "fil3", Map.of(
                                                "format", "text/plain",
                                                "filnavn", "fil.txt",
                                                "data", "BASE64_STRING"
                                        )
                                )
                        )))
                .build();

    }

    @Test
    void shouldReturnValidInstanceObject() {

        when(persistFile.apply(any(File.class))).thenReturn(Mono.just(UUID.randomUUID()));

        InstanceObject result = service.map(0L, subsidyInstance, persistFile).block();

        assertEquals("99", result.getValuePerKey().get("kulturminneId"));
        assertEquals("55", result.getValuePerKey().get("saksnummer"));
        assertEquals("hoveddokument.txt", result.getValuePerKey().get("hoveddokumentFilnavn"));
        assertEquals("vedlegg1.txt", result.getObjectCollectionPerKey().get("vedlegg").stream().findFirst().get().getValuePerKey().get("filnavn"));
    }

    @Test
    void shouldConvertFileContentToUuidOnField() {
        String hoveddokumentUuid = UUID.randomUUID().toString();

        when(persistFile.apply(any(File.class))).thenReturn(Mono.just(UUID.fromString(hoveddokumentUuid)));

        InstanceObject instanceObject = service.map(0L, subsidyInstance, persistFile).block();

        assertEquals(hoveddokumentUuid, instanceObject.getValuePerKey().get("fil1Data"));
        assertEquals("fil.txt", instanceObject.getValuePerKey().get("fil1Filnavn"));
        assertEquals("text/plain", instanceObject.getValuePerKey().get("fil1Format"));
    }

    @Test
    void shouldConvertFileContentToUuidOnGroups() {
        String hoveddokumentUuid = UUID.randomUUID().toString();

        when(persistFile.apply(any(File.class))).thenReturn(Mono.just(UUID.fromString(hoveddokumentUuid)));

        InstanceObject instanceObject = service.map(0L, subsidyInstance, persistFile).block();

        assertEquals(hoveddokumentUuid, instanceObject.getValuePerKey().get("hoveddokumentFil2Data"));
        assertEquals("fil.txt", instanceObject.getValuePerKey().get("hoveddokumentFil2Filnavn"));
        assertEquals("text/plain", instanceObject.getValuePerKey().get("hoveddokumentFil2Format"));
    }

    @Test
    void shouldConvertFileContentToUuidOnCollections() {
        String vedleggUuid = UUID.randomUUID().toString();

        when(persistFile.apply(any(File.class))).thenReturn(Mono.just(UUID.fromString(vedleggUuid)));

        InstanceObject instanceObject = service.map(0L, subsidyInstance, persistFile).block();

        Map<String, String> vedlegg = instanceObject.getObjectCollectionPerKey().get("vedlegg").stream()
                .findFirst().get().getValuePerKey();

        assertEquals(vedleggUuid, vedlegg.get("fil3Data"));
        assertEquals("fil.txt", vedlegg.get("fil3Filnavn"));
        assertEquals("text/plain", vedlegg.get("fil3Format"));
    }

}