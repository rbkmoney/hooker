package com.rbkmoney.hooker.model;

import com.rbkmoney.swag_webhook_events.*;

import java.util.Arrays;

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
    private String paymentToolToken;
    private String paymentSession;
    private PaymentContactInfo contactInfo;
    private String ip;
    private String fingerprint;
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
        this.paymentToolToken = other.paymentToolToken;
        this.paymentSession = other.paymentSession;
        this.contactInfo = new PaymentContactInfo(other.contactInfo);
        this.ip = other.ip;
        this.fingerprint = other.fingerprint;
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
            PaymentToolDetails otherPayerPaymentToolDetails = otherPayer.getPaymentToolDetails();
            if (otherPayerPaymentToolDetails instanceof PaymentToolDetailsBankCard) {
                PaymentToolDetailsBankCard paymentToolDetails = (PaymentToolDetailsBankCard) otherPayerPaymentToolDetails;
                copyPayer.setPaymentToolDetails(new PaymentToolDetailsBankCard()
                        .cardNumberMask(paymentToolDetails.getCardNumberMask())
                        .paymentSystem(paymentToolDetails.getPaymentSystem()));
            } else if (otherPayerPaymentToolDetails instanceof PaymentToolDetailsPaymentTerminal) {
                copyPayer.setPaymentToolDetails(new PaymentToolDetailsPaymentTerminal()
                        .provider(((PaymentToolDetailsPaymentTerminal) otherPayerPaymentToolDetails).getProvider()));
            } else if (otherPayerPaymentToolDetails instanceof PaymentToolDetailsDigitalWalletWrapper) {
                PaymentToolDetailsDigitalWalletWrapper otherPaymentToolDetailsWrapper = (PaymentToolDetailsDigitalWalletWrapper) otherPayerPaymentToolDetails;
                PaymentToolDetailsDigitalWalletWrapper paymentToolDetails = new PaymentToolDetailsDigitalWalletWrapper();
                DigitalWalletDetails.DigitalWalletDetailsTypeEnum digitalWalletDetailsType = otherPaymentToolDetailsWrapper.getDigitalWalletDetails().getDigitalWalletDetailsType();
                switch (digitalWalletDetailsType) {
                    case DIGITALWALLETDETAILSQIWI:
                        DigitalWalletDetailsQIWI otehrDigitalWalletDetails = (DigitalWalletDetailsQIWI)otherPaymentToolDetailsWrapper.getDigitalWalletDetails();
                        paymentToolDetails.setDigitalWalletDetails(new DigitalWalletDetailsQIWI()
                                .phoneNumberMask(otehrDigitalWalletDetails.getPhoneNumberMask()));
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown digitalWalletDetailsType "+ digitalWalletDetailsType +"; must be one of these: "+ Arrays.toString(DigitalWalletDetails.DigitalWalletDetailsTypeEnum.values()));
                }
                paymentToolDetails.getDigitalWalletDetails().setDigitalWalletDetailsType(digitalWalletDetailsType);
                copyPayer.setPaymentToolDetails(paymentToolDetails);
            }
            copyPayer.getPaymentToolDetails().detailsType(otherPayerPaymentToolDetails.getDetailsType());
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

    public String getPaymentToolToken() {
        return paymentToolToken;
    }

    public void setPaymentToolToken(String paymentToolToken) {
        this.paymentToolToken = paymentToolToken;
    }

    public String getPaymentSession() {
        return paymentSession;
    }

    public void setPaymentSession(String paymentSession) {
        this.paymentSession = paymentSession;
    }

    public PaymentContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(PaymentContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public Payer getPayer() {
        return payer;
    }

    public void setPayer(Payer payer) {
        this.payer = payer;
    }
}
