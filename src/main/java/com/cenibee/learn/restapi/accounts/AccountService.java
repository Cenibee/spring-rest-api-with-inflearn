package com.cenibee.learn.restapi.accounts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class AccountService implements UserDetailsService {

    final AccountRepository accountRepository;

    final PasswordEncoder passwordEncode;

    @Autowired
    public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncode) {
        this.accountRepository = accountRepository;
        this.passwordEncode = passwordEncode;
    }

    public Account saveAccount(Account account) {
        account.setPassword(this.passwordEncode.encode(account.getPassword()));
        return this.accountRepository.save(account);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return new AccountAdapter(account);
    }
}
