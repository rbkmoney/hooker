package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class PartyMetadataRowMapper implements RowMapper<PartyMetadata> {
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public PartyMetadata mapRow(ResultSet resultSet, int i) throws SQLException {
        String metadata = resultSet.getString("metadata");
        if (metadata == null) {
            return null;
        }
        return objectMapper.readValue(metadata, PartyMetadata.class);
    }
}
