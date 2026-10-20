package dev.sonu.saitring_employee_manager.security;

import dev.sonu.saitring_employee_manager.owner.entity.Owner;
import dev.sonu.saitring_employee_manager.owner.repository.OwnerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final OwnerRepository ownerRepository;

    public CustomUserDetailsService(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        Owner owner = ownerRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("Owner not found with identifier: " + identifier));
        return OwnerPrincipal.create(owner);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Owner not found with id: " + id));
        return OwnerPrincipal.create(owner);
    }
}
