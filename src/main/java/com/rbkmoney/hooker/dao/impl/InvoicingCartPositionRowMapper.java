package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.model.InvoiceCartPosition;
import com.rbkmoney.hooker.model.TaxMode;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InvoicingCartPositionRowMapper implements RowMapper<InvoiceCartPosition> {
    @Override
    public InvoiceCartPosition mapRow(ResultSet rs, int i) throws SQLException {
        InvoiceCartPosition invoiceCartPosition = new InvoiceCartPosition();
        invoiceCartPosition.setMessageId(rs.getLong("message_id"));
        invoiceCartPosition.setProduct(rs.getString("product"));
        invoiceCartPosition.setPrice(rs.getLong("price"));
        invoiceCartPosition.setQuantity(rs.getInt("quantity"));
        invoiceCartPosition.setCost(rs.getLong("cost"));
        String rate = rs.getString("rate");
        if (rate != null) {
            invoiceCartPosition.setTaxMode(new TaxMode(rate));
        }
        return invoiceCartPosition;
    }
}
