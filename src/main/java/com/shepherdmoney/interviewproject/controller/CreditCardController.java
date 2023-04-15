package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.Transaction;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class CreditCardController {

    // TODO: wire in CreditCard repository here (~1 line)
    @Autowired
    CreditCardRepository creditCardRepo;

    @Autowired
    private UserRepository userRepo;

    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        // TODO: Create a credit card entity, and then associate that credit card with user with given userId
        //       Return 200 OK with the credit card id if the user exists and credit card is successfully associated with the user
        //       Return other appropriate response code for other exception cases
        //       Do not worry about validating the card number, assume card number could be any arbitrary format and length
        Optional<User> user = userRepo.findById(payload.getUserId());
        if (user.isPresent()) {
            CreditCard creditCard = new CreditCard();
            creditCard.setNumber(payload.getCardNumber());
            creditCard.setIssuanceBank(payload.getCardIssuanceBank());
            creditCard.setOwner(user.get());
            creditCardRepo.save(creditCard);
            return ResponseEntity.ok(creditCard.getId());
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
        // TODO: return a list of all credit card associated with the given userId, using CreditCardView class
        //       if the user has no credit card, return empty list, never return null
        Optional<User> user = userRepo.findById(userId);
        if (user.isPresent()) {
            List<CreditCardView> creditCardViews = user.get().getCreditCards().stream()
                    .map(CreditCardView::fromCreditCard)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(creditCardViews);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        // TODO: Given a credit card number, efficiently find whether there is a user associated with the credit card
        //       If so, return the user id in a 200 OK response. If no such user exists, return 400 Bad Request
        Optional<CreditCard> creditCard = creditCardRepo.findByNumber(creditCardNumber);
        if (creditCard.isPresent()) {
            return ResponseEntity.ok(creditCard.get().getOwner().getId());
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/credit-card/update-balance")
    public ResponseEntity<String> updateCreditCardBalance(@RequestBody List<UpdateBalancePayload> payloads) {
        for (UpdateBalancePayload payload : payloads) {
            String creditCardNumber = payload.getCreditCardNumber();
            Optional<CreditCard> optionalCreditCard = creditCardRepo.findByNumber(creditCardNumber);

            if (!optionalCreditCard.isPresent()) {
                return ResponseEntity.badRequest().body("The given card number is not associated with a card.");
            }

            CreditCard creditCard = optionalCreditCard.get();
            List<BalanceHistory> balanceHistory = creditCard.getBalanceHistory();
            LocalDate transactionDate = payload.getTransactionTime().atZone(ZoneId.systemDefault()).toLocalDate();

            // Find the index of the existing BalanceHistory entry with the same date as the transaction
            int index = -1;
            for (int i = 0; i < balanceHistory.size(); i++) {
                if (balanceHistory.get(i).getDate().equals(transactionDate)) {
                    index = i;
                    break;
                }
            }

            if (index != -1) {
                BalanceHistory entry = balanceHistory.get(index);
                entry.setBalance(payload.getCurrentBalance());

                // Update the balances for all future dates
                for (int i = index - 1; i >= 0; i--) {
                    BalanceHistory futureEntry = balanceHistory.get(i);
                    futureEntry.setBalance(payload.getCurrentBalance());
                }
            } else {
                // Insert a new BalanceHistory entry for the transaction date
                BalanceHistory newEntry = new BalanceHistory();
                newEntry.setDate(Instant.from(transactionDate));
                newEntry.setBalance(payload.getCurrentBalance());

                balanceHistory.add(newEntry);
                Collections.sort(balanceHistory, (a, b) -> b.getDate().compareTo(a.getDate()));
            }

            creditCardRepo.save(creditCard);
        }

        return ResponseEntity.ok().body("Update is done and successful.");
    }



}
