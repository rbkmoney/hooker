package com.rbkmoney.hooker.model;

import com.rbkmoney.swag_webhook_events.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;

/**
 * Created by inalarsanukaev on 15.05.17.
 */
@NoArgsConstructor
@Getter
@Setter
public class Payment {
    private String id;
    private String createdAt;
    private String status;
    private PaymentError error;
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
            this.error = new PaymentError();
            this.error.setCode(other.error.getCode());
            this.error.setMessage(other.error.getMessage());
            this.error.setSubError(other.error.getSubError());
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
        } else if (other.payer instanceof RecurrentPayer) {
            RecurrentPayer otherPayer = (RecurrentPayer) other.payer;
            this.payer = new RecurrentPayer()
                    .contactInfo(new ContactInfo()
                            .email(otherPayer.getContactInfo().getEmail())
                            .phoneNumber(otherPayer.getContactInfo().getPhoneNumber()))
                    .recurrentParentPayment(new PaymentRecurrentParent()
                    .invoiceID(otherPayer.getRecurrentParentPayment().getInvoiceID())
                    .paymentID(otherPayer.getRecurrentParentPayment().getPaymentID()));
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
                        .bin(paymentToolDetails.getBin())
                        .lastDigits(paymentToolDetails.getLastDigits())
                        .cardNumberMask(paymentToolDetails.getCardNumberMask())
                        .tokenProvider(paymentToolDetails.getTokenProvider())
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

}
