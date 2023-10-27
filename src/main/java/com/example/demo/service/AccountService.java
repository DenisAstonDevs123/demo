package com.example.demo.service;


import com.example.demo.model.Account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountService {
    List<Account> getAllAccounts();
    Optional <Account> createAccount (String name, String pin);
    Optional<Account> getAccountNumber (String accountNumber);
    void deposit(String accountNumber, BigDecimal amount);
    void transfer(String accountNumberFroms, String accountNumberTos,BigDecimal amount, String pin);
    void withdraw(String accountNumberFroms, BigDecimal amount, String pin);
}