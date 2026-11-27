package no.novari.instance.gateway;

import lombok.extern.slf4j.Slf4j;
import no.novari.instance.gateway.model.Status;
import no.novari.instance.gateway.model.digisak.SubsidyInstance;
import no.novari.flyt.gateway.instance.HistoryClient;
import no.novari.flyt.gateway.instance.InstanceProcessor;
import no.novari.flyt.webresourceserver.security.client.sourceapplication.SourceApplicationAuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static no.novari.flyt.webresourceserver.UrlPaths.EXTERNAL_API;

@RestController
@RequestMapping(EXTERNAL_API + "/digisak/instances")
@Slf4j
public class InstanceController {

    private final InstanceProcessor<SubsidyInstance> subsidyInstanceProcessor;
    private final HistoryClient historyClient;
    private final SourceApplicationAuthorizationService sourceApplicationAuthorizationService;

    public InstanceController(
            InstanceProcessor<SubsidyInstance> subsidyInstanceInstanceProcessor,
            HistoryClient historyClient,
            SourceApplicationAuthorizationService sourceApplicationAuthorizationService
    ) {
        this.subsidyInstanceProcessor = subsidyInstanceInstanceProcessor;
        this.historyClient = historyClient;
        this.sourceApplicationAuthorizationService = sourceApplicationAuthorizationService;
    }

    @PostMapping("instance")
    public ResponseEntity<Void> postIncomingSubsidy(
            @RequestBody SubsidyInstance incomingInstance,
            Authentication authentication
    ) {
        return subsidyInstanceProcessor.processInstance(authentication, incomingInstance);
    }

    @GetMapping("status/{instanceId}")
    public ResponseEntity<?> getInstanceStatus(
            Authentication authentication,
            @PathVariable String instanceId) {

        long sourceApplicationId = sourceApplicationAuthorizationService.getSourceApplicationId(authentication);
        log.debug("Trying to get the latest status for instance {} (sourceApplication {})", instanceId, sourceApplicationId);

        String caseId = historyClient.getArchiveInstanceId(sourceApplicationId, instanceId);
        if (caseId != null) {
            return ResponseEntity.ok(Status.builder()
                    .instanceId(instanceId)
                    .destinationId(caseId)
                    .status("Instans godtatt av destinasjon").build());
        }
        return ResponseEntity.badRequest().body(Status.builder()
                .instanceId(instanceId)
                .status("Ukjent status").build());
    }

}
