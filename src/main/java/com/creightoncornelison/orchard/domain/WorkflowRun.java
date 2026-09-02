package com.creightoncornelison.orchard.domain;

import java.util.UUID;

public record WorkflowRun(
        UUID id,
        int workflowId,
        RunStatus status,
        String message
) {}