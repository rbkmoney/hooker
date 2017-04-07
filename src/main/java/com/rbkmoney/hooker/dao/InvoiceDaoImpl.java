package com.rbkmoney.hooker.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;


@Component
public class InvoiceDaoImpl extends NamedParameterJdbcDaoSupport implements InvoiceDao {
    Logger log = LoggerFactory.getLogger(this.getClass());

    public InvoiceDaoImpl(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public String getParty(String invoiceId) throws Exception {
        final String sql = "SELECT * FROM hook.invoice WHERE invoice_id =:invoice_id";
        MapSqlParameterSource params = new MapSqlParameterSource("invoice_id", invoiceId);
        String partyId = null;
        try {
            partyId = (String) getNamedParameterJdbcTemplate().queryForObject(sql, params, String.class);
        } catch (EmptyResultDataAccessException e) {
            //do nothing
        } catch (NestedRuntimeException e) {
            log.error("PaymentPayerDaoImpl.getById error", e);
            throw new DaoException(e);
        }
        return partyId;
    }

    @Override
    public boolean add(String partyId, String invoiceId) throws Exception {
        if (getParty(invoiceId) != null) {
            log.warn("Payment info with invoiceId = {} already exists", invoiceId);
            return false;
        }
        final String sql = "INSERT INTO hook.invoice(invoice_id, party_id) " +
                "VALUES (:invoice_id, :party_id)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("invoice_id", invoiceId)
                .addValue("party_id", partyId);
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
        final String sql = "DELETE FROM hook.payment_payer where invoice_id=:invoice_id";
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
