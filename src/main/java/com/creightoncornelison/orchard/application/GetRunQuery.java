package com.creightoncornelison.orchard.application;

import java.util.UUID;

public record GetRunQuery(
        String organizationKey,
        UUID runId
) { }
