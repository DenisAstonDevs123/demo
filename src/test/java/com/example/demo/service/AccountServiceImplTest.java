package com.example.demo.service;

import com.example.demo.exception.InvalidPinException;
import com.example.demo.exception.NotMoneyException;
import com.example.demo.exception.TransactionException;
import com.example.demo.model.Account;
import com.example.demo.model.Operation;
import com.example.demo.model.Transaction;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    public void createAccount_WithValidValues_returnsCreatedAccount() {
        String name = "Denis";
        String pin = "1234";

        Account account = new Account(name, pin);
        Mockito.doReturn(account).when(accountRepository).save(any(Account.class));

        Optional<Account> createdAccount = accountService.createAccount(name, pin);

        assertTrue(createdAccount.isPresent());
        assertEquals(createdAccount.get().getName(), name);
        assertEquals(createdAccount.get().getPin(), pin);

    }

    @Test
    public void createAccount_WithInvalidValues_throwsInvalidPinException() {
        assertThrows(InvalidPinException.class, () -> accountService.createAccount(null, "123"));
    }

    @Test
    void getAllAccounts_ReturnsAllAccountsFromRepository() {
        var account = new Account("Denis", "1234");
        var account1 = new Account("Karina", "4356");

        var list = Arrays.asList(account, account1);

        when(accountRepository.findAll()).thenReturn(list);

        var result = accountService.getAllAccounts();

        assertEquals(list, result);
    }

    @Test
    void getAccountByAccountNumber_WithValidAccountNumber_ReturnsAccount() {
        var accountNumber = "6ce5e8d1-61af-4b92-9fa8-b2a2466c9bc4";
        var account = new Account(accountNumber, "Petr", "1234", BigDecimal.ZERO);

        when(accountRepository.findById(accountNumber)).thenReturn(Optional.of(account));

        var result = accountService.getAccountNumber("6ce5e8d1-61af-4b92-9fa8-b2a2466c9bc4");

        assertTrue(result.isPresent());
        assertEquals(result.get(), account);
    }

    @Test
    public void deposit_WithValidAccountNumber_DepositsAmountAndSavesTransaction() {
        var accountNumber = "6ce5e8d1-61af-4b92-9fa8-b2a2466c9bc4";
        var amountToDeposit = new BigDecimal("100.00");
        var existingAccount = new Account(accountNumber, "Denis Tsybanov", "1245", new BigDecimal("500.00"));
        var transaction = new Transaction(accountNumber, accountNumber, LocalTime.now(), amountToDeposit, Operation.DEPOSIT);

        when(accountRepository.findById(accountNumber)).thenReturn(Optional.of(existingAccount));
        when(transactionRepository.save(any())).thenReturn(transaction);
        // act
        accountService.deposit(accountNumber, amountToDeposit);

        // assert
        verify(accountRepository).save(existingAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        assertEquals(new BigDecimal("600.00"), existingAccount.getAmount());
    }

    @Test
    void deposit_ThrowsNotFoundException_WhenAccountNotFound() {
        var accountNumber = "6ce5e8d1-61af-4b92-9fa8-b2a2466c9bc4";

        when(accountRepository.findById(accountNumber)).thenReturn(Optional.empty());

        assertThrows(TransactionException.class, () -> accountService.deposit(accountNumber, BigDecimal.TEN));

        verify(accountRepository, times(1)).findById(accountNumber);
    }

    @Test
    void deposit_IncreasesAccountBalance() {
        var accountNumber = "6ce5e8d1-61af-4b92-9fa8-b2a2466c9bc4";
        var existingAccount = new Account(accountNumber, "John Brown", "1245", BigDecimal.TEN);


        when(accountRepository.findById(accountNumber)).thenReturn(Optional.of(existingAccount));

        BigDecimal amount = new BigDecimal(100);
        accountService.deposit(accountNumber, amount);

        BigDecimal expectedBalance = BigDecimal.valueOf(110);
        assertEquals(expectedBalance, existingAccount.getAmount());

        verify(accountRepository, times(1)).save(existingAccount);
    }

    @Test
    void deposit_CreatesTransaction() {
        var accountNumber = "6ce5e8d1-61af-4b92-9fa8-b2a2466c9bc4";
        var existingAccount = new Account(accountNumber, "John Doe", "1245", BigDecimal.TEN);

        when(accountRepository.findById(accountNumber)).thenReturn(Optional.of(existingAccount));

        var amount = new BigDecimal(100);
        accountService.deposit(accountNumber, amount);

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void transferSuccess() {
        var fromAccountNumber = "6ce5e8d1-61af-4b92-9fa8-b2a2466c9bc4";
        var fromAccount = new Account(fromAccountNumber, "John Doe", "1245", BigDecimal.TEN);
        var toAccountNumber = "6ce5e8d1-61af-4b92-9fa8-b2a2466c9bc5";
        var toAccount = new Account(toAccountNumber, "Jane Doe", "1246", BigDecimal.ZERO);

        when(accountRepository.findById(fromAccountNumber)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountNumber)).thenReturn(Optional.of(toAccount));

        accountService.transfer(fromAccountNumber, toAccountNumber, BigDecimal.TEN, "1245");

        assertEquals(BigDecimal.ZERO, fromAccount.getAmount());
        assertEquals(BigDecimal.TEN, toAccount.getAmount());
    }

    @Test
    void transfer_CreatesTransaction() {
        var fromAccountNumber = "6ce5e8d1-61af-4b92-9fa8-b2a2466c9bc4";
        var fromAccount = new Account(fromAccountNumber, "John Doe", "1245", BigDecimal.TEN);
        var toAccountNumber = "6ce5e8d1-61af-4b92-9fa8-b2a2466c9bc5";
        var toAccount = new Account(toAccountNumber, "Jane Doe", "1246", BigDecimal.ZERO);

        when(accountRepository.findById(fromAccountNumber)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountNumber)).thenReturn(Optional.of(toAccount));

        accountService.transfer(fromAccountNumber, toAccountNumber, BigDecimal.TEN, "1245");

        verify(transactionRepository, times(1)).save(any(Transaction.class));

    }

    @Test()
    public void transferNotEnoughFunds() {
        var fromAccountNumber = "6ce5e8d1-61af-4b92-9fa8-b2a2466c9bc4";
        var fromAccount = new Account(fromAccountNumber, "John Doe", "1245", BigDecimal.TEN);
        var toAccountNumber = "6ce5e8d1-61af-4b92-9fa8-b2a2466c9bc5";
        var toAccount = new Account(toAccountNumber, "Jane Doe", "1246", BigDecimal.ZERO);


        when(accountRepository.findById(fromAccountNumber)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountNumber)).thenReturn(Optional.of(toAccount));

        assertThrows(NotMoneyException.class, () -> accountService.transfer(fromAccountNumber, toAccountNumber, BigDecimal.valueOf(20), "1245"));
    }

    @Test()
    public void transferInvalidPin() {
        var fromAccountNumber = "6ce5e8d1-61af-4b92-9fa8-b2a2466c9bc4";
        var fromAccount = new Account(fromAccountNumber, "John Doe", "1245", BigDecimal.TEN);
        var toAccountNumber = "6ce5e8d1-61af-4b92-9fa8-b2a2466c9bc5";
        var toAccount = new Account(toAccountNumber, "Jane Doe", "1246", BigDecimal.ZERO);

        when(accountRepository.findById(fromAccountNumber)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountNumber)).thenReturn(Optional.of(toAccount));

        assertThrows(NotMoneyException.class, () -> accountService.transfer(fromAccountNumber, toAccountNumber, BigDecimal.valueOf(20), "1245"));
    }


    @Test
    public void transferAccountNotFound() {
        var fromAccountNumber = "6ce5e8d1-61af-4b92-9fa8-b2a2466c9bc4";
        var fromAccount = new Account(fromAccountNumber, "John Doe", "1245", BigDecimal.TEN);
        String toAccountNumber = "6ce5e8d1-61af-4b92-9fa8-b2a2466c9bc5";

        when(accountRepository.findById(fromAccountNumber)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountNumber)).thenReturn(Optional.empty());

        assertThrows(TransactionException.class, () ->
                accountService.transfer(fromAccountNumber, toAccountNumber, BigDecimal.TEN, "1245"));
    }

    @Test
    public void testWithdrawSuccess() {
        var fromAccountNumber = "6ce5e8d1-61af-4b92-9fa8-b2a2466c9bc4";
        var fromAccount = new Account(fromAccountNumber, "John Doe", "1245", BigDecimal.TEN);

        when(accountRepository.findById(fromAccountNumber)).thenReturn(Optional.of(fromAccount));

        accountService.withdraw(fromAccountNumber, BigDecimal.TEN, fromAccount.getPin());

        assertEquals(BigDecimal.ZERO, fromAccount.getAmount());
    }

    @Test
    public void testWithdrawNotFoundException() {
        var accountNumber = "12345";
        var amount = new BigDecimal(100);
        var pin = "1234";

        assertThrows(TransactionException.class, () ->
                accountService.withdraw(accountNumber, amount, pin)
        );
    }

    @Test
    public void testWithdrawNotEnoughFundsException() {
        var fromAccountNumber = "6ce5e8d1-61af-4b92-9fa8-b2a2466c9bc4";
        var fromAccount = new Account(fromAccountNumber, "John Doe", "1245", BigDecimal.TEN);

        when(accountRepository.findById(fromAccountNumber)).thenReturn(Optional.of(fromAccount));

        assertThrows(NotMoneyException.class, () ->
                accountService.withdraw(fromAccountNumber, BigDecimal.valueOf(25), fromAccount.getPin())
        );
    }

    @Test
    public void testWithdrawInvalidPinException() {
        var fromAccountNumber = "6ce5e8d1-61af-4b92-9fa8-b2a2466c9bc4";
        var fromAccount = new Account(fromAccountNumber, "John Doe", "1245", BigDecimal.TEN);

        when(accountRepository.findById(fromAccountNumber)).thenReturn(Optional.of(fromAccount));

        assertThrows(InvalidPinException.class, () ->
                accountService.withdraw(fromAccountNumber, BigDecimal.TEN, "123")
        );
    }
}