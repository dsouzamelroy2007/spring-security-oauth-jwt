package com.mel.expensetracker.authserver.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Carries org context alongside the standard {@link UserDetails} contract so
 * {@link com.mel.expensetracker.authserver.config.token.OrgRoleClaimsTokenCustomizer}
 * can read it straight off the authenticated principal. [FEATURE B6]
 *
 * <p>Spring Authorization Server persists the authenticated Authentication
 * (principal included) as JSON in {@code oauth2_authorization}, so this class
 * needs to be Jackson-constructible, not just a plain immutable value type --
 * {@code ignoreUnknown = true} covers UserDetails' default interface methods
 * (isAccountNonExpired etc.) and the derived {@link #getRoleNames()}, none of
 * which have a matching constructor parameter. The authorities field is typed
 * concretely as {@code SimpleGrantedAuthority} (not the {@code GrantedAuthority}
 * interface) so deserialization doesn't need polymorphic type metadata.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class AppUserPrincipal implements UserDetails {

    private final UUID userId;
    private final UUID orgId;
    private final String orgSlug;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final Set<SimpleGrantedAuthority> authorities;

    @JsonCreator
    private AppUserPrincipal(
            @JsonProperty("userId") UUID userId,
            @JsonProperty("orgId") UUID orgId,
            @JsonProperty("orgSlug") String orgSlug,
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("enabled") boolean enabled,
            @JsonProperty("authorities") Set<SimpleGrantedAuthority> authorities) {
        this.userId = userId;
        this.orgId = orgId;
        this.orgSlug = orgSlug;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    public static AppUserPrincipal from(User user) {
        // A plain LinkedHashSet, not Collectors.toUnmodifiableSet(): that
        // returns a JDK-internal ImmutableCollections$SetN, which Jackson's
        // default typing tags with its own @class metadata on serialization,
        // and no allowlist permits deserializing a JDK-internal class back.
        Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>(
                user.getAuthorities().stream().map(SimpleGrantedAuthority::new).toList());
        return new AppUserPrincipal(
                user.getId(),
                user.getOrganisation().getId(),
                user.getOrganisation().getSlug(),
                user.getUsername(),
                user.getPasswordHash(),
                user.isEnabled(),
                authorities);
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getOrgId() {
        return orgId;
    }

    public String getOrgSlug() {
        return orgSlug;
    }

    public Set<String> getRoleNames() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
