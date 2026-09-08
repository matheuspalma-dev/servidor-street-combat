package com.street.combat.module.usuario.security.tirar.xss;

import lombok.RequiredArgsConstructor;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TirarXssService {

    private final PolicyFactory policyFactory;

    public String tirarXss(String entrada) {
        return policyFactory.sanitize(entrada);
    }
}
