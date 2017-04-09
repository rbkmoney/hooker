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


@Component
public class InvoiceDaoImpl extends NamedParameterJdbcDaoSupport implements InvoiceDao {
    Logger log = LoggerFactory.getLogger(this.getClass());

    public InvoiceDaoImpl(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public InvoiceInfo get(String invoiceId) throws Exception {
        InvoiceInfo result = null;
        final String sql = "SELECT * FROM hook.invoice WHERE invoice_id =:invoice_id";
        MapSqlParameterSource params = new MapSqlParameterSource("invoice_id", invoiceId);
        try {
            result = getNamedParameterJdbcTemplate().queryForObject(sql, params, new RowMapper<InvoiceInfo>() {
                @Override
                public InvoiceInfo mapRow(ResultSet rs, int i) throws SQLException {
                    InvoiceInfo invoiceInfo = new InvoiceInfo();
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
            log.error("PaymentPayerDaoImpl.getById error", e);
            throw new DaoException(e);
        }
        return result;
    }

    @Override
    public boolean add(InvoiceInfo invoiceInfo) throws Exception {
        String invoiceId = invoiceInfo.getInvoiceId();
        if (get(invoiceId) != null) {
            log.warn("Payment info with invoiceId = {} already exists", invoiceId);
            return false;
        }
        final String sql = "INSERT INTO hook.invoice(invoice_id, party_id, shop_id, amount, currency, created_at, content_type, content_data) " +
                "VALUES (:invoice_id, :party_id, :shop_id, :amount, :currency, :created_at, :content_type, :content_data)";
        MapSqlParameterSource params = new MapSqlParameterSource()
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
            log.error("PaymentPayerDaoImpl.add error", e);
            throw new DaoException(e);
        }
        log.info("Party info with invoiceId = {} added to table", invoiceId);
        return true;
    }

    @Override
    public boolean delete(final String id) {
        log.info("Start deleting payment info with invoiceId = {}", id);
        final String sql = "DELETE FROM hook.invoice where invoice_id=:invoice_id";
        try {
            int updateCount = getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("invoice_id", id));
            if (updateCount != 1) {
                return false;
            }
        } catch (NestedRuntimeException e) {
            log.error("PaymentPayerDaoImpl.delete error", e);
            throw new DaoException(e);
        }
        log.info("Payment info with invoiceId = {} deleted from table", id);
        return true;
    }
}
