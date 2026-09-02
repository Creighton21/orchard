package com.creightoncornelison.orchard.domain;

import java.util.UUID;

public record TaskExecution(
        UUID id,
        int workflowId,
        UUID workflowRunId,
        int workflowTaskDefinitionId,
        RunStatus status
) {}
