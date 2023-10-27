package com.example.demo.service;

import com.example.demo.exception.InvalidPinException;
import com.example.demo.exception.NotMoneyException;
import com.example.demo.exception.TransactionException;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.model.Account;
import com.example.demo.model.Operation;
import com.example.demo.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    @Transactional
    public Optional<Account> createAccount(String name, String pin) {
        if ((name == null) || (pin.length() > 4)) {
            throw new InvalidPinException("Invalid value");
        }
        Account account = new Account(name, pin);
        accountRepository.save(account);
        return Optional.of(account);
    }

    @Override
    public Optional<Account> getAccountNumber(String accountNumber) {
        return accountRepository.findById(accountNumber);
    }

    @Override
    @Transactional
    public void deposit(String accountNumber, BigDecimal amount) {
        var byId = accountRepository.findById(accountNumber);

        if (byId.isEmpty()) {
            throw new TransactionException("Аккаунт с данным номером не найден");
        }
        var account = byId.get();
        var transaction = new Transaction(accountNumber, accountNumber, LocalTime.now(), amount, Operation.DEPOSIT);
        account.setAmount(account.getAmount().add(amount));

        accountRepository.save(account);
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void transfer(String accountNumberFrom, String accountNumberTo,BigDecimal amount, String pin) {
        var fromAccount = accountRepository.findById(accountNumberFrom);
        var toAccount = accountRepository.findById(accountNumberTo);

        if ((fromAccount.isEmpty()) || (toAccount.isEmpty())) {
            throw new TransactionException("Ошибка перевода");
        }
        if (!(fromAccount.get().getPin().equals(pin))) {
            throw new InvalidPinException("Неверный пин код");
        }
        if (fromAccount.get().getAmount().compareTo(amount) < 0) {
            throw new NotMoneyException("No monet");
        }

        fromAccount.get().setAmount(fromAccount.get().getAmount().subtract(amount));
        toAccount.get().setAmount(toAccount.get().getAmount().add(amount));

        var transaction = new Transaction(accountNumberFrom, accountNumberTo, LocalTime.now(), amount, Operation.TRANSFER);

        accountRepository.save(fromAccount.get());
        accountRepository.save(toAccount.get());
        transactionRepository.save(transaction);

    }

    @Override
    @Transactional
    public void withdraw(String accountNumber, BigDecimal amount, String pin) {
        var byId = accountRepository.findById(accountNumber);

        if (byId.isEmpty()) {
            throw new TransactionException("Аккаунт с данным номером не найден");
        }
        if (byId.get().getAmount().compareTo(amount) < 0) {
            throw new NotMoneyException("Not money");
        }
        if (!(byId.get().getPin().equals(pin))) {
            throw new InvalidPinException("Неверный пин код");
        }

        var account = byId.get();
        var transaction = new Transaction(accountNumber, accountNumber, LocalTime.now(), amount, Operation.WITHDRAW);

        account.setAmount(account.getAmount().subtract(amount));

        accountRepository.save(account);
        transactionRepository.save(transaction);
    }
}