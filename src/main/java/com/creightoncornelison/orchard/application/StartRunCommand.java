package com.creightoncornelison.orchard.application;

public record StartRunCommand(
        String organizationKey,
        String workflowName,
        String message
) { }
