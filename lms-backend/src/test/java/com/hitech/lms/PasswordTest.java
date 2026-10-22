package com.hitech.lms;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
	
    @Test
    void makePassword() {
    	
    	// 1. 암호화 객체 생성
    	BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();  
    	
    	// 2. "1234" 암호화
        String encoded = encoder.encode("1234");                   
        
    	// 3. 콘솔에 출력
        System.out.println("암호화 결과: " + encoded);               
    }

}
