package com.tsanet.api.storage;

import com.tsanet.api.connectapi.dto.CollaborationRequestFormDto;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class CollaborationRequestFormRepository {
    private static final RowMapper<CollaborationRequestFormDto> ROW_MAPPER =
        CollaborationRequestFormRepository::mapRow;

    private final JdbcTemplate jdbcTemplate;

    public CollaborationRequestFormRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(CollaborationRequestFormDto form) {
        String fetchedAt = Instant.now().toString();
        jdbcTemplate.update(
            """
            INSERT INTO collaboration_request_form (
                receiver_company_id, document_id, custom_field_count, fetched_at
            ) VALUES (?, ?, ?, ?)
            ON CONFLICT(receiver_company_id) DO UPDATE SET
                document_id = excluded.document_id,
                custom_field_count = excluded.custom_field_count,
                fetched_at = excluded.fetched_at
            """,
            form.receiverCompanyId(),
            form.documentId(),
            form.customFieldCount(),
            fetchedAt
        );
    }

    public List<CollaborationRequestFormDto> findAll() {
        return jdbcTemplate.query(
            """
            SELECT receiver_company_id, document_id, custom_field_count
            FROM collaboration_request_form
            ORDER BY receiver_company_id
            """,
            ROW_MAPPER
        );
    }

    public List<CollaborationRequestFormDto> findByReceiverCompanyId(long receiverCompanyId) {
        return jdbcTemplate.query(
            """
            SELECT receiver_company_id, document_id, custom_field_count
            FROM collaboration_request_form
            WHERE receiver_company_id = ?
            ORDER BY receiver_company_id
            """,
            ROW_MAPPER,
            receiverCompanyId
        );
    }

    private static CollaborationRequestFormDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new CollaborationRequestFormDto(
            rs.getLong("receiver_company_id"),
            rs.getLong("document_id"),
            rs.getInt("custom_field_count")
        );
    }
}
