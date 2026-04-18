package org.openmrs.module.ugandaemrsync.dto;

import org.openmrs.Order;

import java.util.Set;

/**
 * Result object for encounter order completion operations.
 * Contains information about which orders were completed, which are still pending,
 * and a summary of the results.
 */
public class EncounterCompletionResult {
    public final Set<Order> resultedOrders;
    public final Set<Order> allActiveOrders;
    public final boolean hasOrdersWithoutResults;
    public final String resultSummary;

    public EncounterCompletionResult(Set<Order> resultedOrders, Set<Order> allActiveOrders,
                                    boolean hasOrdersWithoutResults, String resultSummary) {
        this.resultedOrders = resultedOrders;
        this.allActiveOrders = allActiveOrders;
        this.hasOrdersWithoutResults = hasOrdersWithoutResults;
        this.resultSummary = resultSummary;
    }
}
