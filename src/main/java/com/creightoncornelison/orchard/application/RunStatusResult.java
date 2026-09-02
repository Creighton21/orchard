package com.creightoncornelison.orchard.application;

import com.creightoncornelison.orchard.domain.RunStatus;

import java.util.UUID;

public record RunStatusResult(
        UUID runId,
        RunStatus status
) { }