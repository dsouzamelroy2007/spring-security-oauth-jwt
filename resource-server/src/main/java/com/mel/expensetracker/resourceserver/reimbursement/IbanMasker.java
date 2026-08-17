package com.mel.expensetracker.resourceserver.reimbursement;

/**
 * [FEATURE C5] An IBAN is a bank account identifier -- leaking it to anyone
 * but Finance is the textbook field-redaction case. Keeps the country code
 * and last 4 digits (enough to eyeball "is this the right account" without
 * exposing the number that actually moves money).
 */
public final class IbanMasker {

    private IbanMasker() {}

    public static String mask(String iban) {
        if (iban.length() <= 6) {
            return "*".repeat(iban.length());
        }
        String countryCode = iban.substring(0, 2);
        String lastFour = iban.substring(iban.length() - 4);
        String middle = "*".repeat(iban.length() - 6);
        return countryCode + middle + lastFour;
    }
}
