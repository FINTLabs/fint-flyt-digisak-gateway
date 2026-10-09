package no.novari.instance.gateway;

import no.novari.instance.gateway.model.digisak.SubsidyInstance;
import no.novari.flyt.gateway.instance.InstanceProcessor;
import no.novari.flyt.gateway.instance.InstanceProcessorFactoryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InstanceProcessorConfiguration {

    @Bean
    public InstanceProcessor<SubsidyInstance> subsidyInstanceProcessor(
            InstanceProcessorFactoryService instanceProcessorFactoryService,
            SubsidyInstanceMappingService subsidyInstanceMappingService) {

        return instanceProcessorFactoryService.createInstanceProcessor(
                SubsidyInstance::getIntegrationId,
                SubsidyInstance::getInstanceId,
                subsidyInstanceMappingService
        );
    }
}
