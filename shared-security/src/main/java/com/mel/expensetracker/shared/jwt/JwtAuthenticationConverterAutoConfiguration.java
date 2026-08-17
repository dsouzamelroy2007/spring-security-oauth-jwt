package com.mel.expensetracker.shared.jwt;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

/**
 * [FEATURE B7] Only activates for modules that actually decode JWTs (i.e. have
 * the resource-server classes on the classpath) -- a module using only the
 * RFC 9457 error handlers from this library never pulls this bean in.
 */
@AutoConfiguration
@ConditionalOnClass(JwtAuthenticationConverter.class)
public class JwtAuthenticationConverterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new RoleAndScopeAuthoritiesConverter());
        return converter;
    }
}
