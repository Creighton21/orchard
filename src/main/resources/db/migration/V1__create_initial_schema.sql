CREATE TABLE organization (
    id SERIAL PRIMARY KEY,
    organization_key VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE workflow (
    id SERIAL PRIMARY KEY,
    organization_id INT NOT NULL REFERENCES organization(id),
    workflow_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_workflow_id_organization
        UNIQUE (id, organization_id),

    CONSTRAINT uq_workflow_name_per_organization
        UNIQUE (organization_id, workflow_name)
);

CREATE TABLE capability (
    id SERIAL PRIMARY KEY,
    organization_id INT NOT NULL REFERENCES organization(id),
    capability_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_capability_id_organization
        UNIQUE (id, organization_id),

    CONSTRAINT uq_capability_name_per_organization
        UNIQUE (organization_id, capability_name)
);

CREATE TABLE workflow_task_definition (
    id SERIAL PRIMARY KEY,
    organization_id INT NOT NULL REFERENCES organization(id),
    workflow_id INT NOT NULL,
    task_key VARCHAR(255) NOT NULL,
    capability_id INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_task_definition_id_workflow
        UNIQUE (id, workflow_id),

    CONSTRAINT uq_task_key_per_workflow
        UNIQUE (workflow_id, task_key),

    CONSTRAINT fk_task_definition_workflow_organization
        FOREIGN KEY (workflow_id, organization_id)
            REFERENCES workflow (id, organization_id),

    CONSTRAINT fk_task_definition_capability_organization
        FOREIGN KEY (capability_id, organization_id)
            REFERENCES capability (id, organization_id)
);

CREATE TABLE workflow_run (
    id UUID PRIMARY KEY,
    workflow_id INT NOT NULL REFERENCES workflow(id),
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    input_message JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (id, workflow_id),

    CONSTRAINT chk_workflow_run_status
      CHECK (status IN ('PENDING', 'SUCCEEDED'))
);

CREATE TABLE task_execution (
    id UUID PRIMARY KEY,
    workflow_id INT NOT NULL REFERENCES workflow(id),
    workflow_run_id UUID NOT NULL,
    workflow_task_definition_id INT NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_task_execution_run_workflow
        FOREIGN KEY (workflow_run_id, workflow_id)
            REFERENCES workflow_run(id, workflow_id),

    CONSTRAINT fk_task_execution_definition_workflow
        FOREIGN KEY (workflow_task_definition_id, workflow_id)
            REFERENCES workflow_task_definition(id, workflow_id),

    CONSTRAINT chk_task_execution_status
        CHECK (status IN ('PENDING', 'SUCCEEDED'))
);



INSERT INTO organization (
    organization_key,
    display_name
)
VALUES (
    'default',
    'Default Organization'
);

INSERT INTO workflow (
    organization_id,
    workflow_name
)
SELECT
    id,
    'echo-workflow'
FROM organization
WHERE organization_key = 'default';

INSERT INTO capability (
    organization_id,
    capability_name
)
SELECT
    id,
    'echo'
FROM organization
WHERE organization_key = 'default';

INSERT INTO workflow_task_definition (
    organization_id,
    workflow_id,
    task_key,
    capability_id
)
SELECT
    organization.id,
    workflow.id,
    'echo-message',
    capability.id
FROM organization
         JOIN workflow
              ON workflow.organization_id = organization.id
         JOIN capability
              ON capability.organization_id = organization.id
WHERE organization.organization_key = 'default'
  AND workflow.workflow_name = 'echo-workflow'
  AND capability.capability_name = 'echo';