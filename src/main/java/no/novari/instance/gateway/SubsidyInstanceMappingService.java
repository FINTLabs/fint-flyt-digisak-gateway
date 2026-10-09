package no.novari.instance.gateway;

import kotlin.jvm.functions.Function1;
import lombok.extern.slf4j.Slf4j;
import no.novari.instance.gateway.model.digisak.SubsidyDokumentfil;
import no.novari.instance.gateway.model.digisak.SubsidyInstance;
import no.novari.flyt.gateway.instance.InstanceMapper;
import no.novari.flyt.gateway.instance.model.File;
import no.novari.flyt.gateway.instance.model.instance.InstanceObject;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@Slf4j
public class SubsidyInstanceMappingService implements InstanceMapper<SubsidyInstance> {

    @NotNull
    @Override
    public InstanceObject map(
            long sourceApplicationId,
            SubsidyInstance subsidyInstance,
            @NotNull Function1<? super File, UUID> persistFile
    ) {
        Map<String, String> valuePerKey = new HashMap<>();
        valuePerKey.putAll(fieldValueMapper(persistFile, subsidyInstance.getFields(), sourceApplicationId, subsidyInstance.getInstanceId()));
        valuePerKey.putAll(groupValueMapper(persistFile, subsidyInstance, sourceApplicationId));

        return new InstanceObject(
                valuePerKey,
                collectionValueMapper(persistFile, subsidyInstance, sourceApplicationId)
        );
    }

    private Map<String, String> fieldValueMapper(
            Function1<? super File, UUID> persistFile,
            Map<String, Object> fields,
            long sourceApplicationId,
            String instanceId
    ) {
        Map<String, String> result = new HashMap<>();
        if (fields == null) {
            return result;
        }
        fields.forEach((key, value) -> result.putAll(parseField(persistFile, key, value, sourceApplicationId, instanceId)));
        return result;
    }

    private Map<String, String> groupValueMapper(
            Function1<? super File, UUID> persistFile,
            SubsidyInstance subsidyInstance,
            long sourceApplicationId
    ) {
        Map<String, String> result = new HashMap<>();
        if (subsidyInstance.getGroups() == null) {
            return result;
        }
        subsidyInstance.getGroups().forEach((groupName, groupFields) ->
                groupFields.forEach((fieldName, value) -> result.putAll(parseField(
                        persistFile,
                        concatGroupNameWithFieldName(groupName, fieldName),
                        value,
                        sourceApplicationId,
                        subsidyInstance.getInstanceId()
                )))
        );
        return result;
    }

    private String concatGroupNameWithFieldName(String group, String field) {
        return group.concat(StringUtils.capitalize(field));
    }

    private Map<String, Collection<InstanceObject>> collectionValueMapper(
            Function1<? super File, UUID> persistFile,
            SubsidyInstance instance,
            long sourceApplicationId
    ) {
        Map<String, Collection<InstanceObject>> result = new HashMap<>();
        if (instance.getCollections() == null) {
            return result;
        }
        instance.getCollections().forEach((collectionName, collectionMaps) -> {
            List<InstanceObject> objects = collectionMaps.stream()
                    .map(collectionMap -> new InstanceObject(
                            fieldValueMapper(persistFile, collectionMap, sourceApplicationId, instance.getInstanceId()),
                            new HashMap<>()
                    ))
                    .toList();
            result.put(collectionName, objects);
        });
        return result;
    }

    private Map<String, String> parseField(
            Function1<? super File, UUID> persistFile,
            String key,
            Object value,
            long sourceApplicationId,
            String instanceId
    ) {
        if (value instanceof Map<?, ?>) {
            SubsidyDokumentfil dokumentfil = toSubsidyDokumentfil(value);
            UUID uuid = postFile(persistFile, dokumentfil, sourceApplicationId, instanceId);
            return Map.of(
                    key.concat("Data"), uuid.toString(),
                    key.concat("Format"), dokumentfil.getFormat().toString(),
                    key.concat("Filnavn"), dokumentfil.getFilnavn()
            );
        } else if (value instanceof String) {
            return Map.of(key, (String) value);
        } else {
            throw new IllegalArgumentException(
                    String.format("Field (%s) with value (%s) is not a valid type.", key, value)
            );
        }
    }

    private SubsidyDokumentfil toSubsidyDokumentfil(Object object) {
        Map<?, ?> subsidyDocumentfilMap = (Map<?, ?>) object;
        return SubsidyDokumentfil.builder()
                .filnavn((String) subsidyDocumentfilMap.get("filnavn"))
                .format(MediaType.valueOf((String) subsidyDocumentfilMap.get("format")))
                .data((String) subsidyDocumentfilMap.get("data"))
                .build();
    }

    private UUID postFile(
            Function1<? super File, UUID> persistFile,
            SubsidyDokumentfil field,
            long sourceApplicationId,
            String instanceId
    ) {
        File fileContent = new File(
                field.getFilnavn(),
                sourceApplicationId,
                instanceId,
                field.getFormat(),
                "UTF-8",
                field.getData()
        );
        return persistFile.invoke(fileContent);
    }
}
