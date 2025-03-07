package com.healthcare.accesscontrol;

import java.util.HashMap;
import java.util.Map;

public class MockBlockchainService {
    // In-memory map to simulate blockchain transaction storage.
    private Map<String, String> transactionLog = new HashMap<>();

    /**
     * Simulates logging a transaction hash to the blockchain.
     * @param key The unique key (e.g., userId).
     * @param data The SHA-256 hash of the stored data.
     */
    public void logTransaction(String key, String data) {
        System.out.println("[MOCK] Logging transaction: key = " + key + ", hash = " + data);
        transactionLog.put(key, data);
    }

    /**
     * Simulates querying a transaction from the blockchain.
     * @param key The unique key for the transaction.
     * @return The stored hash or a dummy message if not found.
     */
    public String queryTransaction(String key) {
        System.out.println("[MOCK] Querying transaction for key: " + key);
        return transactionLog.getOrDefault(key, "[MOCK] No transaction found for key: " + key);
    }
}
