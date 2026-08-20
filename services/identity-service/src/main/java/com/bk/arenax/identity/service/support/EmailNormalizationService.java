package com.bk.arenax.identity.service.support;

import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class EmailNormalizationService {

    public String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}