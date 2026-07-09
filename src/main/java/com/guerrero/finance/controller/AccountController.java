package com.guerrero.finance.controller;

import com.guerrero.finance.model.Account;
import com.guerrero.finance.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/finance/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<Account>> getAccounts(Principal principal) {
        return ResponseEntity.ok(accountService.getAccounts(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(Principal principal, @RequestBody Account account) {
        return ResponseEntity.ok(accountService.createAccount(principal.getName(), account));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(
            Principal principal,
            @PathVariable Long id,
            @RequestBody Account accountDetails) {
        return ResponseEntity.ok(accountService.updateAccount(principal.getName(), id, accountDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(Principal principal, @PathVariable Long id) {
        accountService.deleteAccount(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
