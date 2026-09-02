package com.creightoncornelison.orchard.domain;

import java.util.List;

public record WorkflowDefinition(
        int organizationId,
        int workflowId,
        String workflowName,
        List<WorkflowTaskDefinition> taskDefinitions
) {
    public WorkflowDefinition {
        taskDefinitions = List.copyOf(taskDefinitions);
    }
}
