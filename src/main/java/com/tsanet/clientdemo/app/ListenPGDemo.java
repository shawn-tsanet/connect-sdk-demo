package com.tsanet.clientdemo.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.OptionalLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

public final class ListenPGDemo {

    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/test";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    private static final String CHANNEL = "tsa_events";

    private static final int POLL_TIMEOUT_MS = 10_000;

    private static final String SELECT_UNSYNCED =
        "SELECT id, operation_type FROM entities_outbox WHERE synced = FALSE ORDER BY id";
    private static final String MARK_SYNCED =
        "UPDATE entities_outbox SET synced = TRUE WHERE id = ?";

    private static final Pattern ENTITY_ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");

    private ListenPGDemo() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println("ListenPGDemo starting");
        System.out.println("  url=" + JDBC_URL);
        System.out.println("  channel=" + CHANNEL);

        try (
            Connection listenConnection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            Connection workConnection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)
        ) {
            processUnsyncedOutbox(workConnection);

            PGConnection pgConnection = listenConnection.unwrap(PGConnection.class);

            try (Statement statement = listenConnection.createStatement()) {
                statement.execute("LISTEN " + CHANNEL);
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    unlisten(listenConnection, CHANNEL);
                } catch (SQLException ignored) {
                }
            }));

            System.out.println("Waiting for NOTIFY on channel '" + CHANNEL + "' (Ctrl+C to stop)...");
            listenLoop(pgConnection, workConnection);
        }
    }

    private static void processUnsyncedOutbox(Connection connection) throws SQLException {
        try (
            PreparedStatement statement = connection.prepareStatement(SELECT_UNSYNCED);
            ResultSet resultSet = statement.executeQuery()
        ) {
            int count = 0;
            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String operationType = resultSet.getString("operation_type");
                System.out.printf("Outbox backlog: id=%d operation=%s%n", id, operationType);
                markOutboxSynced(connection, id);
                count++;
            }

            if (count == 0) {
                System.out.println("No unsynced outbox events.");
            } else {
                System.out.printf("Processed %d unsynced outbox event(s).%n", count);
            }
        }
    }

    private static void listenLoop(PGConnection pgConnection, Connection workConnection)
        throws SQLException, InterruptedException {
        while (!Thread.currentThread().isInterrupted()) {
            PGNotification[] notifications = pgConnection.getNotifications(POLL_TIMEOUT_MS);
            if (notifications == null) {
                continue;
            }

            for (PGNotification notification : notifications) {
                onNotification(workConnection, notification);
            }
        }
    }

    private static void onNotification(Connection workConnection, PGNotification notification) {
        String payload = notification.getParameter();
        System.out.printf(
            "NOTIFY received: channel=%s pid=%d payload=%s%n",
            notification.getName(),
            notification.getPID(),
            payload
        );

        extractEntityId(payload).ifPresentOrElse(
            entityId -> {
                try {
                    markOutboxSynced(workConnection, entityId);
                    System.out.printf("Marked outbox synced for id=%d%n", entityId);
                } catch (SQLException ex) {
                    System.err.printf("Failed to mark outbox synced for id=%d: %s%n", entityId, ex.getMessage());
                }
            },
            () -> System.err.println("Could not extract entity id from notification payload; outbox not updated.")
        );
    }

    private static void markOutboxSynced(Connection connection, long entityId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(MARK_SYNCED)) {
            statement.setLong(1, entityId);
            int updated = statement.executeUpdate();
            if (updated == 0) {
                System.out.printf("No outbox row updated for id=%d%n", entityId);
            }
        }
    }

    private static OptionalLong extractEntityId(String payload) {
        if (payload == null || payload.isBlank()) {
            return OptionalLong.empty();
        }

        Matcher matcher = ENTITY_ID_PATTERN.matcher(payload);
        if (!matcher.find()) {
            return OptionalLong.empty();
        }

        return OptionalLong.of(Long.parseLong(matcher.group(1)));
    }

    private static void unlisten(Connection connection, String channel) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("UNLISTEN " + channel);
        }
    }
}

// Connect v1, Connect v2 -> activeMQ. 
// More events
