package com.hitech.lms.domain.admin;

import com.hitech.lms.domain.user.UsersRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

	private final AdminPostRepository adminPostRepository;
	private final UsersRepository usersRepository;

}
