package com.creightoncornelison.orchard.application.persistence;

import com.creightoncornelison.orchard.domain.WorkflowDefinition;

import java.util.Optional;

public interface WorkflowDefinitionLookup {
    Optional<WorkflowDefinition> find(
            String organizationKey,
            String workflowName
    );
}
