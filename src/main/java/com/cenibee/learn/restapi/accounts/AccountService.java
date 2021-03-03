package com.cenibee.learn.restapi.accounts;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class AccountService implements UserDetailsService {

    final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return User.builder()
                .username(account.getEmail())
                .password(account.getPassword())
                .authorities(account.getRoles().stream().map(role ->
                        new SimpleGrantedAuthority("ROLE_" + role.name())).collect(Collectors.toSet()))
                .build();
    }
}
