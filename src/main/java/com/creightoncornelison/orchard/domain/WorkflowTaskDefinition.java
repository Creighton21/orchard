package com.creightoncornelison.orchard.domain;


public record WorkflowTaskDefinition(
        int id,
        int organizationId,
        int workflowId,
        String taskKey,
        int capabilityId
) {}