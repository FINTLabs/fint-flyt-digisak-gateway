package no.novari.golden;

import no.novari.flyt.gateway.instance.HistoryClient;
import no.novari.flyt.gateway.instance.InstanceProcessor;
import no.novari.flyt.webresourceserver.security.client.sourceapplication.SourceApplicationAuthorizationService;
import no.novari.instance.gateway.InstanceController;
import no.novari.instance.gateway.model.digisak.SubsidyInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.LinkedHashMap;
import java.util.Map;

import static no.novari.golden.GoldenFiles.assertMatchesGolden;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InstanceControllerGoldenTest {

    private static final long SOURCE_APPLICATION_ID = 3L;

    @SuppressWarnings("unchecked")
    private final InstanceProcessor<SubsidyInstance> instanceProcessor = mock(InstanceProcessor.class);
    private final HistoryClient historyClient = mock(HistoryClient.class);
    private final SourceApplicationAuthorizationService sourceApplicationAuthorizationService =
            mock(SourceApplicationAuthorizationService.class);
    private final Authentication authentication = mock(Authentication.class);

    private InstanceController controller;

    @BeforeEach
    void setUp() {
        when(sourceApplicationAuthorizationService.getSourceApplicationId(authentication)).thenReturn(SOURCE_APPLICATION_ID);
        controller = new InstanceController(instanceProcessor, historyClient, sourceApplicationAuthorizationService);
    }

    @Test
    void statusForArchivedInstance() throws Exception {
        when(historyClient.getArchiveInstanceId(SOURCE_APPLICATION_ID, "12345")).thenReturn("2026/42");

        assertMatchesGolden("digisak-status-archived", toMap(controller.getInstanceStatus(authentication, "12345")));
    }

    @Test
    void statusForUnknownInstance() throws Exception {
        assertMatchesGolden("digisak-status-unknown", toMap(controller.getInstanceStatus(authentication, "99999")));
    }

    @Test
    void postInstanceIsPassedToTheProcessor() throws Exception {
        SubsidyInstance instance = SubsidyInstance.builder().integrationId("FRIP").instanceId("12345").build();
        when(instanceProcessor.processInstance(authentication, instance)).thenReturn(ResponseEntity.accepted().build());

        ResponseEntity<Void> response = controller.postIncomingSubsidy(instance, authentication);

        verify(instanceProcessor).processInstance(authentication, instance);
        assertMatchesGolden("digisak-post-instance", toMap(response));
    }

    private static Map<String, Object> toMap(ResponseEntity<?> response) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("status", response.getStatusCode().value());
        map.put("body", response.getBody());
        return map;
    }
}
