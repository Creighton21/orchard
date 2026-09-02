package com.creightoncornelison.orchard.application.persistence;

import com.creightoncornelison.orchard.domain.TaskExecution;
import com.creightoncornelison.orchard.domain.WorkflowRun;

import java.util.Optional;
import java.util.UUID;

public interface RunStore {
    void saveAcceptedRun(
            WorkflowRun run,
            TaskExecution[] executions
    );

    Optional<WorkflowRun> find(
            String organizationKey,
            UUID runId
    );
}
