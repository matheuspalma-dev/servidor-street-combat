package com.street.combat.module.usuario.security.tirar.xss;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

@Component
@RequiredArgsConstructor
public class TirarXssAspect extends ValueDeserializer<String> {
    private final TirarXssService tirarXssService;

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        String texto = p.getValueAsString();
        if (texto == null){
            return null;
        }
        return tirarXssService.tirarXss(texto);
    }
}
