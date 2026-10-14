package com.hitech.lms.domain.user;

public interface SmsService {
	void send(String phone, String message);
}
