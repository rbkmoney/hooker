package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.model.InvoiceCartPosition;
import lombok.RequiredArgsConstructor;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InvoicingCartDaoImpl implements InvoicingCartDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static RowMapper<InvoiceCartPosition> cartPositionRowMapper = new InvoicingCartPositionRowMapper();

    @Override
    public List<InvoiceCartPosition> getByMessageId(Long messageId) throws DaoException {
        final String sqlCarts = "SELECT * FROM hook.cart_position WHERE message_id =:message_id";
        MapSqlParameterSource paramsCarts = new MapSqlParameterSource("message_id", messageId);
        try {
            return jdbcTemplate.query(sqlCarts, paramsCarts, cartPositionRowMapper);
        } catch (NestedRuntimeException e) {
            throw new DaoException("Couldn't get carts by messageId = " + messageId, e);
        }

    }

    @Override
    public int[] saveBatch(List<InvoiceCartPosition> carts) throws DaoException {
        try {
            final String sql = "INSERT INTO hook.cart_position(message_id, product, price, quantity, cost, rate) " +
                    "VALUES (:message_id, :product, :price, :quantity, :cost, :rate) ";

            MapSqlParameterSource[] sqlParameterSources = carts
                    .stream()
                    .map(cartPosition -> new MapSqlParameterSource()
                            .addValue("message_id", cartPosition.getMessageId())
                            .addValue("product", cartPosition.getProduct())
                            .addValue("price", cartPosition.getPrice())
                            .addValue("quantity", cartPosition.getQuantity())
                            .addValue("cost", cartPosition.getCost())
                            .addValue("rate", cartPosition.getTaxMode() == null ? null : cartPosition.getTaxMode().getRate()))
                    .toArray(MapSqlParameterSource[]::new);

            return jdbcTemplate.batchUpdate(sql, sqlParameterSources);
        } catch (NestedRuntimeException e) {
            List<Long> messageIds = carts.stream().map(InvoiceCartPosition::getMessageId).collect(Collectors.toList());
            throw new DaoException("Couldn't save carts with messageIds: " + messageIds, e);
        }
    }
}
