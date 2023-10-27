package com.example.demo.controller;

import com.example.demo.model.Transaction;
import com.example.demo.service.TransactionService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        var allTransactions = transactionService.getAllTransaction();

        return new ResponseEntity<>(allTransactions, HttpStatus.OK);
    }

    @GetMapping(value = "/{accountNumber}")
    public ResponseEntity<List<Transaction>> getAllTransactionsById(@PathVariable @NotBlank String accountNumber) {

        var allTransactionsByAccountNumber = transactionService.getTransaction(accountNumber);

        return new ResponseEntity<>(allTransactionsByAccountNumber, HttpStatus.OK);
    }
}
