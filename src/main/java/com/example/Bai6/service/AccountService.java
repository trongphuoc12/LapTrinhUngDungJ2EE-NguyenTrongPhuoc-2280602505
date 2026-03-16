package com.example.Bai6.service;

import com.example.Bai6.model.Account;
import com.example.Bai6.repository.AccountRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class AccountService implements UserDetailsService {

	@Autowired
	private AccountRepository accountRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Account account = accountRepository.findByLoginName(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
		Collection<GrantedAuthority> authorities = account.getRoles()
				.stream()
				.map(role -> new SimpleGrantedAuthority(role.getName()))
				.collect(Collectors.toList());

		return User.builder()
				.username(account.getLoginName())
				.password(account.getPassword())
				.authorities(authorities)
				.build();
	}
}