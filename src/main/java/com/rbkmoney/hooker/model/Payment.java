package com.rbkmoney.hooker.model;

import com.rbkmoney.swag_webhook_events.*;

/**
 * Created by inalarsanukaev on 15.05.17.
 */
public class Payment {
    private String id;
    private String createdAt;
    private String status;
    private PaymentStatusError error;
    private long amount;
    private String currency;
    private Payer payer;

    public Payment(Payment other) {
        this.id = other.id;
        this.createdAt = other.createdAt;
        this.status = other.status;
        if (other.error != null) {
            this.error = new PaymentStatusError(other.error);
        }
        this.amount = other.amount;
        this.currency = other.currency;
        //TODO copy constructor
        if (other.payer instanceof CustomerPayer) {
            this.payer = new CustomerPayer()
                    .customerID(((CustomerPayer) other.payer).getCustomerID());
        } else if (other.payer instanceof PaymentResourcePayer) {
            PaymentResourcePayer otherPayer = (PaymentResourcePayer) other.payer;
            PaymentResourcePayer copyPayer = new PaymentResourcePayer()
                    .paymentSession(otherPayer.getPaymentSession())
                    .paymentToolToken(otherPayer.getPaymentToolToken())
                    .clientInfo(new ClientInfo()
                            .ip(otherPayer.getClientInfo().getIp())
                            .fingerprint(otherPayer.getClientInfo().getFingerprint()))
                    .contactInfo(new ContactInfo()
                            .email(otherPayer.getContactInfo().getEmail())
                            .phoneNumber(otherPayer.getContactInfo().getPhoneNumber()));
            this.payer = copyPayer;
            if (otherPayer.getPaymentToolDetails() instanceof PaymentToolDetailsBankCard) {
                PaymentToolDetailsBankCard paymentToolDetails = (PaymentToolDetailsBankCard) otherPayer.getPaymentToolDetails();
                copyPayer.setPaymentToolDetails(new PaymentToolDetailsBankCard()
                        .cardNumberMask(paymentToolDetails.getCardNumberMask())
                        .paymentSystem(paymentToolDetails.getPaymentSystem()));
            } else if (otherPayer.getPaymentToolDetails() instanceof PaymentToolDetailsPaymentTerminal) {
                copyPayer.setPaymentToolDetails(new PaymentToolDetailsPaymentTerminal()
                        .provider(((PaymentToolDetailsPaymentTerminal) otherPayer.getPaymentToolDetails()).getProvider()));
            }
            copyPayer.getPaymentToolDetails().detailsType(otherPayer.getPaymentToolDetails().getDetailsType());
        }
        this.payer.payerType(other.payer.getPayerType());
    }

    public Payment() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PaymentStatusError getError() {
        return error;
    }

    public void setError(PaymentStatusError error) {
        this.error = error;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Payer getPayer() {
        return payer;
    }

    public void setPayer(Payer payer) {
        this.payer = payer;
    }
}
