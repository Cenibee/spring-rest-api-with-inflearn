package com.cenibee.learn.restapi.accounts;

import com.cenibee.learn.restapi.common.BaseTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AccountServiceTest extends BaseTest {

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Test
    void findByUsername() {
        // Given:
        String password = "1234";
        String username = "ksj3452@email.com";
        Account account = Account.builder()
                .email(username)
                .password(password)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();
        accountRepository.save(account);

        // When:
        UserDetailsService userDetailsService = (UserDetailsService) accountService;
        UserDetails userDetails = userDetailsService.loadUserByUsername("ksj3452@email.com");

        Assertions.assertThat(userDetails.getPassword()).isEqualTo(password);
    }

}