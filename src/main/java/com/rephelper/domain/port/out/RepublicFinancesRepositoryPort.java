package com.rephelper.domain.port.out;

import com.rephelper.domain.model.RepublicFinances;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port for the republic finances repository
 */

public interface RepublicFinancesRepositoryPort {
    /**
     * Saves republic finances
     */
    RepublicFinances save(RepublicFinances republicFinances);

    /**
     * Finds republic finances by ID
     */
    Optional<RepublicFinances> findById(Long id);

    /**
     * Finds republic finances by republic ID
     */
    Optional<RepublicFinances> findByRepublicId(UUID republicId);

    /**
     * Finds all republic finances
     */
    List<RepublicFinances> findAll();

    /**
     * Deletes republic finances
     */
    void delete(RepublicFinances republicFinances);
}