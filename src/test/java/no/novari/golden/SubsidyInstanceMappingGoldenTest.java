package no.novari.golden;

import kotlin.jvm.functions.Function1;
import no.novari.flyt.gateway.instance.model.File;
import no.novari.flyt.gateway.instance.model.instance.InstanceObject;
import no.novari.instance.gateway.SubsidyInstanceMappingService;
import no.novari.instance.gateway.model.digisak.SubsidyInstance;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static no.novari.golden.GoldenFiles.assertMatchesGolden;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SubsidyInstanceMappingGoldenTest {

    private static final long SOURCE_APPLICATION_ID = 3L;

    private final SubsidyInstanceMappingService service = new SubsidyInstanceMappingService();
    private final List<Map<String, Object>> uploadedFiles = new ArrayList<>();

    @Test
    void instanceWithFieldsGroupsAndCollections() throws Exception {
        SubsidyInstance instance = SubsidyInstance.builder()
                .integrationId("FRIP")
                .instanceId("12345")
                .fields(Map.of(
                        "kulturminneId", "99",
                        "saksnummer", "55",
                        "fil1", file("text/plain", "felt.txt", "ZmVsdA==")
                ))
                .groups(Map.of(
                        "hoveddokument", Map.of(
                                "mediatype", "application/pdf",
                                "filnavn", "hoveddokument.pdf",
                                "fil2", file("application/pdf", "hoveddokument.pdf", "aG92ZWQ=")
                        ),
                        "soker", Map.of("navn", "Ola Nordmann")
                ))
                .collections(Map.of(
                        "vedlegg", List.of(
                                Map.of("filnavn", "vedlegg1.txt", "fil3", file("text/plain", "vedlegg1.txt", "djE=")),
                                Map.of("filnavn", "vedlegg2.txt", "fil3", file("text/plain", "vedlegg2.txt", "djI="))
                        ),
                        "tomSamling", List.of()
                ))
                .build();

        assertMatchesGolden("digisak-mapping-full", map(instance));
    }

    @Test
    void instanceWithOnlyFields() throws Exception {
        SubsidyInstance instance = SubsidyInstance.builder()
                .integrationId("FRIP")
                .instanceId("12346")
                .fields(Map.of("saksnummer", "56"))
                .build();

        assertMatchesGolden("digisak-mapping-only-fields", map(instance));
    }

    @Test
    void fieldWithInvalidTypeIsRejected() throws Exception {
        SubsidyInstance instance = SubsidyInstance.builder()
                .integrationId("FRIP")
                .instanceId("12347")
                .fields(Map.of("antall", 7))
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> map(instance));

        assertMatchesGolden("digisak-mapping-invalid-field", Map.of(
                "exception", exception.getClass().getName(),
                "message", exception.getMessage(),
                "uploadedFiles", uploadedFiles
        ));
    }

    private Map<String, Object> map(SubsidyInstance instance) {
        Function1<? super File, UUID> persistFile = file -> {
            uploadedFiles.add(toMap(file));
            return UUID.nameUUIDFromBytes((file.getName() + "|" + file.getBase64Contents()).getBytes(StandardCharsets.UTF_8));
        };

        InstanceObject instanceObject = service.map(SOURCE_APPLICATION_ID, instance, persistFile);

        uploadedFiles.sort(Comparator.comparing(file -> (String) file.get("name")));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("instanceObject", instanceObject);
        result.put("uploadedFiles", uploadedFiles);
        return result;
    }

    private static Map<String, String> file(String format, String filnavn, String data) {
        return Map.of("format", format, "filnavn", filnavn, "data", data);
    }

    private static Map<String, Object> toMap(File file) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", file.getName());
        map.put("sourceApplicationId", file.getSourceApplicationId());
        map.put("sourceApplicationInstanceId", file.getSourceApplicationInstanceId());
        map.put("type", file.getType().toString());
        map.put("encoding", file.getEncoding());
        map.put("base64Contents", file.getBase64Contents());
        return map;
    }
}
