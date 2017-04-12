package com.rbkmoney.hooker.dao;

import com.rbkmoney.damsel.base.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


@Component
public class InvoiceDaoImpl extends NamedParameterJdbcDaoSupport implements InvoiceDao {
    Logger log = LoggerFactory.getLogger(this.getClass());

    public InvoiceDaoImpl(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public InvoiceInfo get(String invoiceId) throws DaoException {
        InvoiceInfo result = null;
        final String sql = "SELECT * FROM hook.invoice WHERE invoice_id =:invoice_id";
        MapSqlParameterSource params = new MapSqlParameterSource("invoice_id", invoiceId);
        try {
            result = getNamedParameterJdbcTemplate().queryForObject(sql, params, new RowMapper<InvoiceInfo>() {
                @Override
                public InvoiceInfo mapRow(ResultSet rs, int i) throws SQLException {
                    InvoiceInfo invoiceInfo = new InvoiceInfo();
                    invoiceInfo.setEventId(rs.getLong("event_id"));
                    invoiceInfo.setInvoiceId(rs.getString("invoice_id"));
                    invoiceInfo.setPartyId(rs.getString("party_id"));
                    invoiceInfo.setShopId(rs.getInt("shop_id"));
                    invoiceInfo.setAmount(rs.getLong("amount"));
                    invoiceInfo.setCurrency(rs.getString("currency"));
                    invoiceInfo.setCreatedAt(rs.getString("created_at"));
                    Content metadata = new Content();
                    metadata.setType(rs.getString("content_type"));
                    metadata.setData(rs.getBytes("content_data"));
                    invoiceInfo.setMetadata(metadata);
                    return invoiceInfo;
                }
            });
        } catch (EmptyResultDataAccessException e) {
            log.warn("Invoice with id "+invoiceId+" not exist!");
        } catch (NestedRuntimeException e) {
            log.warn("InvoiceDaoImpl.getById error", e);
            throw new DaoException(e);
        }
        return result;
    }

    @Override
    public boolean add(InvoiceInfo invoiceInfo) throws DaoException {
        String invoiceId = invoiceInfo.getInvoiceId();
        final String sql = "INSERT INTO hook.invoice(event_id, invoice_id, party_id, shop_id, amount, currency, created_at, content_type, content_data) " +
                "VALUES (:event_id, :invoice_id, :party_id, :shop_id, :amount, :currency, :created_at, :content_type, :content_data) " +
                "ON CONFLICT(invoice_id) DO NOTHING";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("event_id", invoiceInfo.getEventId())
                .addValue("invoice_id", invoiceId)
                .addValue("party_id", invoiceInfo.getPartyId())
                .addValue("shop_id", invoiceInfo.getShopId())
                .addValue("amount", invoiceInfo.getAmount())
                .addValue("currency", invoiceInfo.getCurrency())
                .addValue("created_at", invoiceInfo.getCreatedAt())
                .addValue("content_type", invoiceInfo.getMetadata().getType())
                .addValue("content_data", invoiceInfo.getMetadata().getData());
        try {
            int updateCount = getNamedParameterJdbcTemplate().update(sql, params);
            if (updateCount != 1) {
                return false;
            }
        } catch (NestedRuntimeException e) {
            log.warn("InvoiceDaoImpl.add error", e);
            throw new DaoException(e);
        }
        log.info("Party info with invoiceId = {} added to table", invoiceId);
        return true;
    }

    @Override
    public Long getMaxEventId() {
        final String sql = "SELECT max(event_id) FROM hook.invoice";
        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql, new HashMap<>(), Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public boolean delete(String id) throws DaoException {
        log.info("Start deleting payment info with invoiceId = {}", id);
        final String sql = "DELETE FROM hook.invoice where invoice_id=:invoice_id";
        try {
            int updateCount = getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("invoice_id", id));
            if (updateCount != 1) {
                return false;
            }
        } catch (DataAccessException e) {
            log.warn("InvoiceDaoImpl.delete error", e);
            throw new DaoException(e);
        }
        log.info("Payment info with invoiceId = {} deleted from table", id);
        return true;
    }
}
