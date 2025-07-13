package com.watchtower.sources;

import com.watchtower.fakes.AzureMonitorFake;
import com.watchtower.model.LogEntry;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * Azure Monitor adapter implementing our common interface
 */
@RequiredArgsConstructor
public class AzureLogSource implements CloudLogSource {
    private AzureMonitorFake client;
    
    @Override
    public void initialize(Map<String, String> config) {
        System.out.println(">>> Initializing Azure Log Source...");
        
        // Azure uses subscription ID and tenant ID
        Map<String, String> azureCredentials = Map.of(
            "subscriptionId", config.getOrDefault("AZURE_SUBSCRIPTION_ID", ""),
            "tenantId", config.getOrDefault("AZURE_TENANT_ID", ""),
            "clientId", config.getOrDefault("AZURE_CLIENT_ID", ""),
            "clientSecret", config.getOrDefault("AZURE_CLIENT_SECRET", "")
        );
        
        this.client = new AzureMonitorFake(azureCredentials);
    }
    
    @Override
    public List<LogEntry> fetchLogs(String logicalResource, String filter, int limit) {
        // Translate to Azure-specific format
        String workspace = translateToAzureWorkspace(logicalResource);
        String query = buildKustoQuery(filter, limit);
        
        return client.queryLogs(workspace, query);
    }
    
    @Override
    public String getCloudProvider() {
        return "AZURE";
    }
    
    @Override
    public SourceCapabilities getCapabilities() {
        return SourceCapabilities.builder()
            .provider("AZURE")
            .description("Azure Monitor log analytics")
            .supportedOperations(List.of("fetchLogs"))  // Basic operations only
            .supportedResources(List.of("payment-service"))  // Limited resources
            .supportedFilters(List.of("Error", "Warning"))  // Different format
            .supportedTimeRanges(List.of("24h"))  // Limited time ranges
            .metadata(Map.of(
                "region", "eastus",
                "queryLanguage", "KQL"
            ))
            .build();
    }
    
    private String translateToAzureWorkspace(String logicalResource) {
        return switch (logicalResource) {
            case "payment-service" -> "PaymentWorkspace";
            case "order-service" -> "OrderWorkspace";
            default -> "DefaultWorkspace";
        };
    }
    
    private String buildKustoQuery(String filter, int limit) {
        // Azure uses KQL (Kusto Query Language)
        String severity = switch (filter) {
            case "ERROR" -> "Error";
            case "WARN" -> "Warning";
            default -> "Information";
        };
        return String.format("AppTraces | where SeverityLevel == '%s' | take %d", severity, limit);
    }
}