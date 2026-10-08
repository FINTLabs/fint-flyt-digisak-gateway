package no.fintlabs.instance.gateway.model.digisak;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.novari.flyt.gateway.instance.validation.constraints.ValidBase64;
import org.springframework.http.MediaType;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
public class SubsidyDokumentfil {

    @NotBlank
    private String filnavn;

    @NotNull
    private MediaType format;

    @NotNull
    @ValidBase64
    private String data;
}