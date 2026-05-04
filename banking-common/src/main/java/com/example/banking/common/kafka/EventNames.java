package com.example.banking.common.kafka;

public final class EventNames {

    public static final String CUSTOMER_REGISTERED = "customer-registered";
    public static final String PROFILE_KYC_SUBMITTED = "profile-kyc-submitted";
    public static final String ACCOUNT_OPENED = "account-opened";
    public static final String ACCOUNT_FROZEN = "account-frozen";
    public static final String TRANSACTION_POSTED = "transaction-posted";
    public static final String PAYMENT_INITIATED = "payment-initiated";
    public static final String PAYMENT_APPROVED = "payment-approved";
    public static final String PAYMENT_EXECUTED = "payment-executed";
    public static final String PAYMENT_REJECTED = "payment-rejected";
    public static final String NOTIFICATION_CREATED = "notification-created";

    private EventNames() {
    }
}
