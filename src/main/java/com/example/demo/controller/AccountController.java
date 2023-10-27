package com.example.demo.controller;

import com.example.demo.dto.AccountDto;
import com.example.demo.dto.PayDto;
import com.example.demo.exception.InvalidPinException;
import com.example.demo.exception.NotMoneyException;
import com.example.demo.exception.TransactionException;
import com.example.demo.model.Account;
import com.example.demo.service.AccountService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping()
    public ResponseEntity<List<Account>> getAllAccounts() {
        List<Account> account = accountService.getAllAccounts();
        return new ResponseEntity<>(account, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<String> createAccount(@RequestBody AccountDto accountJson) {
        String name = accountJson.getName();
        String pin = accountJson.getPin();

        try {
            accountService.createAccount(name, pin);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Bad pin", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Create", HttpStatus.OK);

    }

    @GetMapping(value = "/{accountNumber}")
    public ResponseEntity<Account> getAccount(@PathVariable @NotBlank String accountNumber) {
        var accountByAccountNumber = accountService.getAccountNumber(accountNumber);

        return accountByAccountNumber.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() ->
                new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PatchMapping(value = "/{accountNumber}/deposit")
    public ResponseEntity<String> deposit(@PathVariable @NotBlank String accountNumber, @RequestBody PayDto paymentDto) {

        var amount = paymentDto.getAmount();
        try {
            accountService.deposit(accountNumber, amount);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>("Перевод выполнен", HttpStatus.OK);
    }

    @PatchMapping(value = "/{accountNumberFroms}/transfer/{accountNumberTos}")
    public ResponseEntity<String> transfer(@PathVariable  @NotBlank String accountNumberFroms,
                                           @PathVariable @NotBlank  String accountNumberTos,
                                           @RequestBody PayDto payDto) {

        var amount = payDto.getAmount();
        var pin = payDto.getPin();

        try {
            accountService.transfer(accountNumberFroms, accountNumberTos, amount, pin);


        } catch (TransactionException e) {
            return new ResponseEntity<>("Ошибка перевода", HttpStatus.BAD_REQUEST);
        } catch (InvalidPinException e) {
            return new ResponseEntity<>("Неверный пин", HttpStatus.BAD_REQUEST);
        } catch (NotMoneyException e) {
            return new ResponseEntity<>("Недостаточно средств", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Перевод выполнен успешно", HttpStatus.OK);

    }

    @PatchMapping(value = "/{accountNumber}/withdraw")
    public ResponseEntity<String> withdraw(@PathVariable @NotBlank String accountNumber,
                                           @RequestBody PayDto payDto) {

        var amount = payDto.getAmount();
        var pin = payDto.getPin();

        try {
            accountService.withdraw(accountNumber, amount, pin);

        } catch (TransactionException e) {
            return new ResponseEntity<>("Ошибка перевода", HttpStatus.BAD_REQUEST);
        } catch (NotMoneyException e) {
            return new ResponseEntity<>("Недостаточно средств", HttpStatus.BAD_REQUEST);
        } catch (InvalidPinException e) {
            return new ResponseEntity<>("Неверный пин", HttpStatus.BAD_REQUEST);

        }

        return new ResponseEntity<>("Перевод выполнен успешно", HttpStatus.OK);


    }


}
