package com.creightoncornelison.orchard.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
class SchemaIntegrityTest {

    private static final String FOREIGN_KEY_VIOLATION = "23503";

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:18.6-alpine")
                    .withDatabaseName("orchard_test")
                    .withUsername("orchard")
                    .withPassword("orchard");

    private Connection connection;

    @BeforeAll
    static void migrateDatabase() {
        Flyway.configure()
                .dataSource(
                        POSTGRES.getJdbcUrl(),
                        POSTGRES.getUsername(),
                        POSTGRES.getPassword()
                )
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    @BeforeEach
    void openTransaction() throws SQLException {
        connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword()
        );

        connection.setAutoCommit(false);
    }

    @AfterEach
    void rollBackTransaction() throws SQLException {
        if (connection != null) {
            connection.rollback();
            connection.close();
        }
    }

    @Test
    void rejectsCapabilityFromAnotherOrganization() throws SQLException {
        int organizationA = insertOrganization("organization-a");
        int organizationB = insertOrganization("organization-b");

        int workflowA = insertWorkflow(
                organizationA,
                "workflow-a"
        );

        int capabilityB = insertCapability(
                organizationB,
                "capability-b"
        );

        PSQLException exception = assertThrows(
                PSQLException.class,
                () -> insertTaskDefinition(
                        organizationA,
                        workflowA,
                        "invalid-cross-organization-task",
                        capabilityB
                )
        );

        assertEquals(
                FOREIGN_KEY_VIOLATION,
                exception.getSQLState()
        );

        assert exception.getServerErrorMessage() != null;
        assertEquals(
                "fk_task_definition_capability_organization",
                exception.getServerErrorMessage().getConstraint()
        );
    }

    @Test
    void rejectsTaskDefinitionFromAnotherWorkflow() throws SQLException {
        int organization = insertOrganization("shared-organization");

        int capability = insertCapability(
                organization,
                "echo"
        );

        int workflowA = insertWorkflow(
                organization,
                "workflow-a"
        );

        int workflowB = insertWorkflow(
                organization,
                "workflow-b"
        );

        int definitionB = insertTaskDefinition(
                organization,
                workflowB,
                "echo-message",
                capability
        );

        UUID runA = insertWorkflowRun(workflowA);

        PSQLException exception = assertThrows(
                PSQLException.class,
                () -> insertTaskExecution(
                        workflowA,
                        runA,
                        definitionB
                )
        );

        assertEquals(
                FOREIGN_KEY_VIOLATION,
                exception.getSQLState()
        );

        assert exception.getServerErrorMessage() != null;
        assertEquals(
                "fk_task_execution_definition_workflow",
                exception.getServerErrorMessage().getConstraint()
        );
    }

    @Test
    void acceptsConsistentOrganizationAndWorkflowRelationships()
            throws SQLException {

        int organization = insertOrganization("valid-organization");

        int workflow = insertWorkflow(
                organization,
                "valid-workflow"
        );

        int capability = insertCapability(
                organization,
                "valid-capability"
        );

        int definition = insertTaskDefinition(
                organization,
                workflow,
                "valid-task",
                capability
        );

        UUID run = insertWorkflowRun(workflow);

        assertDoesNotThrow(
                () -> insertTaskExecution(
                        workflow,
                        run,
                        definition
                )
        );
    }

    private int insertOrganization(String organizationKey)
            throws SQLException {

        String sql = """
                INSERT INTO organization (
                    organization_key,
                    display_name
                )
                VALUES (?, ?)
                RETURNING id
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, organizationKey);
            statement.setString(2, organizationKey);

            return executeAndReturnId(statement);
        }
    }

    private int insertWorkflow(
            int organizationId,
            String workflowName
    ) throws SQLException {

        String sql = """
                INSERT INTO workflow (
                    organization_id,
                    workflow_name
                )
                VALUES (?, ?)
                RETURNING id
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, organizationId);
            statement.setString(2, workflowName);

            return executeAndReturnId(statement);
        }
    }

    private int insertCapability(
            int organizationId,
            String capabilityName
    ) throws SQLException {

        String sql = """
                INSERT INTO capability (
                    organization_id,
                    capability_name
                )
                VALUES (?, ?)
                RETURNING id
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, organizationId);
            statement.setString(2, capabilityName);

            return executeAndReturnId(statement);
        }
    }

    private int insertTaskDefinition(
            int organizationId,
            int workflowId,
            String taskKey,
            int capabilityId
    ) throws SQLException {

        String sql = """
                INSERT INTO workflow_task_definition (
                    organization_id,
                    workflow_id,
                    task_key,
                    capability_id
                )
                VALUES (?, ?, ?, ?)
                RETURNING id
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, organizationId);
            statement.setInt(2, workflowId);
            statement.setString(3, taskKey);
            statement.setInt(4, capabilityId);

            return executeAndReturnId(statement);
        }
    }

    private UUID insertWorkflowRun(int workflowId)
            throws SQLException {

        UUID runId = UUID.randomUUID();

        String sql = """
            INSERT INTO workflow_run (
                id,
                workflow_id,
                input_message
            )
            VALUES (?, ?, CAST(? AS JSONB))
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setObject(1, runId);
            statement.setInt(2, workflowId);
            statement.setString(
                    3,
                    """
                    {"message":"integration-test"}
                    """
            );

            statement.executeUpdate();
            return runId;
        }
    }

    private void insertTaskExecution(
            int workflowId,
            UUID workflowRunId,
            int workflowTaskDefinitionId
    ) throws SQLException {

        UUID taskExecutionId = UUID.randomUUID();

        String sql = """
            INSERT INTO task_execution (
                id,
                workflow_id,
                workflow_run_id,
                workflow_task_definition_id
            )
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setObject(1, taskExecutionId);
            statement.setInt(2, workflowId);
            statement.setObject(3, workflowRunId);
            statement.setInt(
                    4,
                    workflowTaskDefinitionId
            );

            statement.executeUpdate();
        }
    }

    private int executeAndReturnId(
            PreparedStatement statement
    ) throws SQLException {

        try (ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                throw new SQLException(
                        "Insert did not return a generated ID"
                );
            }

            return resultSet.getInt("id");
        }
    }
}