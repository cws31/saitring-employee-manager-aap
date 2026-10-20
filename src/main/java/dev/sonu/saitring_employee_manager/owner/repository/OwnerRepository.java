package dev.sonu.saitring_employee_manager.owner.repository;

import dev.sonu.saitring_employee_manager.owner.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    Optional<Owner> findByEmail(String email);

    Optional<Owner> findByPhoneNumber(String phoneNumber);

    @Query("SELECT o FROM Owner o WHERE o.email = :identifier OR o.phoneNumber = :identifier")
    Optional<Owner> findByIdentifier(@Param("identifier") String identifier);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);
}
