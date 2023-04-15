package com.shepherdmoney.interviewproject.model;

import java.time.LocalDate;

public class Transaction {
    private String creditCardNumber;
    private LocalDate date;
    private double amount;

    public Transaction() {
    }

    public Transaction(String creditCardNumber, LocalDate date, double amount) {
        this.creditCardNumber = creditCardNumber;
        this.date = date;
        this.amount = amount;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
