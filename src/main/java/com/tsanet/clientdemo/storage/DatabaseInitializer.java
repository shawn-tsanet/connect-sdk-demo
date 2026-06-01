package com.tsanet.clientdemo.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {
    static final String COLLABORATION_REQUEST_TABLE = """
        CREATE TABLE IF NOT EXISTS collaboration_request (
            id INTEGER PRIMARY KEY,
            status TEXT,
            summary TEXT,
            submit_company_name TEXT,
            submit_company_id INTEGER,
            receive_company_name TEXT,
            receive_company_id INTEGER,
            token TEXT NOT NULL UNIQUE,
            created_at TEXT,
            updated_at TEXT,
            fetched_at TEXT NOT NULL
        )
        """;

    static final String CASE_NOTE_TABLE = """
        CREATE TABLE IF NOT EXISTS case_note (
            id INTEGER PRIMARY KEY,
            case_id INTEGER,
            case_token TEXT NOT NULL,
            company_name TEXT,
            creator_username TEXT,
            creator_email TEXT,
            creator_name TEXT,
            summary TEXT,
            description TEXT,
            priority TEXT,
            status TEXT,
            token TEXT NOT NULL UNIQUE,
            created_at TEXT,
            updated_at TEXT,
            fetched_at TEXT NOT NULL
        )
        """;

    static final String CASE_RESPONSE_TABLE = """
        CREATE TABLE IF NOT EXISTS case_response (
            id INTEGER NOT NULL,
            case_token TEXT NOT NULL,
            type TEXT,
            case_number TEXT,
            engineer_name TEXT,
            engineer_phone TEXT,
            engineer_email TEXT,
            next_steps TEXT,
            created_at TEXT,
            fetched_at TEXT NOT NULL,
            PRIMARY KEY (id, case_token)
        )
        """;

    static final String USER_CONTEXT_TABLE = """
        CREATE TABLE IF NOT EXISTS user_context (
            id INTEGER PRIMARY KEY CHECK (id = 1),
            company_id INTEGER,
            company_name TEXT,
            user_id INTEGER,
            username TEXT,
            email TEXT,
            first_name TEXT,
            last_name TEXT,
            fetched_at TEXT NOT NULL
        )
        """;

    static final String WEBHOOK_SUBSCRIPTION_TABLE = """
        CREATE TABLE IF NOT EXISTS webhook_subscription (
            id INTEGER PRIMARY KEY,
            callback_url TEXT,
            event_types TEXT,
            active INTEGER,
            created_at TEXT,
            updated_at TEXT,
            fetched_at TEXT NOT NULL
        )
        """;

    static final String PARTNER_SELECTION_TABLE = """
        CREATE TABLE IF NOT EXISTS partner_selection (
            row_id INTEGER PRIMARY KEY AUTOINCREMENT,
            search_term TEXT NOT NULL,
            label TEXT,
            company_name TEXT,
            department_name TEXT,
            company_id INTEGER,
            department_id INTEGER,
            document_id INTEGER,
            fetched_at TEXT NOT NULL
        )
        """;

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    void initialize() {
        createSchema(jdbcTemplate);
    }

    static void createSchema(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute(COLLABORATION_REQUEST_TABLE);
        jdbcTemplate.execute(CASE_NOTE_TABLE);
        jdbcTemplate.execute(CASE_RESPONSE_TABLE);
        jdbcTemplate.execute(USER_CONTEXT_TABLE);
        jdbcTemplate.execute(WEBHOOK_SUBSCRIPTION_TABLE);
        jdbcTemplate.execute(PARTNER_SELECTION_TABLE);
    }
}
