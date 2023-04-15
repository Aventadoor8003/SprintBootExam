package com.shepherdmoney.interviewproject.vo.request;

import lombok.Data;
import lombok.Getter;

@Data

public class AddCreditCardToUserPayload {

    private int userId;

    @Getter
    private String cardIssuanceBank;

    private String cardNumber;
}
