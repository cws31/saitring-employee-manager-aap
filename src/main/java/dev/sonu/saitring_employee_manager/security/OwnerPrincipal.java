package dev.sonu.saitring_employee_manager.security;

import dev.sonu.saitring_employee_manager.owner.entity.Owner;
import dev.sonu.saitring_employee_manager.owner.entity.OwnerStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class OwnerPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String phoneNumber;
    private final String organizationName;
    private final String password;
    private final OwnerStatus status;
    private final Collection<? extends GrantedAuthority> authorities;

    public OwnerPrincipal(Long id, String email, String phoneNumber, String organizationName,
                          String password, OwnerStatus status,
                          Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.organizationName = organizationName;
        this.password = password;
        this.status = status;
        this.authorities = authorities;
    }

    public static OwnerPrincipal create(Owner owner) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(owner.getRole().name()));
        return new OwnerPrincipal(
                owner.getId(),
                owner.getEmail(),
                owner.getPhoneNumber(),
                owner.getOrganizationName(),
                owner.getPasswordHash(),
                owner.getStatus(),
                authorities
        );
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getOrganizationName() {
        return organizationName;
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
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != OwnerStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == OwnerStatus.ACTIVE;
    }
}
