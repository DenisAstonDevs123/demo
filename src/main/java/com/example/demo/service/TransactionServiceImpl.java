package com.example.demo.service;

import com.example.demo.repository.TransactionRepository;
import com.example.demo.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;

    @Override
    public List<Transaction> getAllTransaction() {
        return transactionRepository.findAll();
    }

    @Override
    public List<Transaction> getTransaction(String accountNumber) {
        return transactionRepository.findTransactionsByAccountNumberFrom(accountNumber);
    }
}