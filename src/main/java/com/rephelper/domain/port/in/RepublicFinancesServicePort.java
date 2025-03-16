package com.rephelper.domain.port.in;

import com.rephelper.domain.model.RepublicFinances;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Port for the republic finances service
 */
public interface RepublicFinancesServicePort {
    /**
     * Gets or creates finances for a republic
     */
    RepublicFinances getOrCreateRepublicFinances(UUID republicId);

    /**
     * Gets finances for a republic
     */
    RepublicFinances getRepublicFinances(UUID republicId);

    /**
     * Updates the balance of a republic's finances
     */
    RepublicFinances updateBalance(UUID republicId, BigDecimal amount);

    /**
     * Checks if a republic has enough balance for a specified amount
     */
    boolean hasEnoughBalance(UUID republicId, BigDecimal amount);
}