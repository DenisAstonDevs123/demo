package com.example.demo.service;

import com.example.demo.model.Transaction;

import java.util.List;

public interface TransactionService {
    List<Transaction> getAllTransaction();
    List<Transaction> getTransaction(String accountNumber);

}