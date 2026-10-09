package no.fintlabs.discovery.gateway;

import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.discovery.gateway.model.digisak.SubsidyDefinition;
import no.novari.flyt.gateway.metadata.IntegrationMetadataValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class DigisakSubsidyDefinitionValidator implements IntegrationMetadataValidator<SubsidyDefinition> {

    private final Validator validator;

    public DigisakSubsidyDefinitionValidator(ValidatorFactory validatorFactory) {
        this.validator = validatorFactory.getValidator();
    }

    @Override
    public List<String> validate(SubsidyDefinition subsidyDefinition) {
        List<String> errors = validator.validate(subsidyDefinition)
                .stream()
                .map(constraintViolation ->
                        constraintViolation.getPropertyPath() + " " + constraintViolation.getMessage())
                .sorted()
                .collect(Collectors.toList());

        return errors.isEmpty() ? null : errors;
    }
 }
