package com.example.demo.Service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.demo.Model.User;
import com.example.demo.Repository.UserRepository;

@Service
public class OtpService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    public OtpService(UserRepository userRepository, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    public void generateAndSendOtp(User user) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);
        sendOtpEmail(user.getEmail(), otp);
    }

    public boolean validateOtp(User user, String inputOtp) {
        if (user.getOtp() == null || user.getOtpExpiry() == null) return false;
        if (LocalDateTime.now().isAfter(user.getOtpExpiry())) return false;
        return user.getOtp().equals(inputOtp);
    }

    public void clearOtp(User user) {
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }

    private void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Kode OTP Outfix");
        message.setText("Kode OTP kamu adalah: " + otp + "\n\nKode ini berlaku selama 5 menit. Jangan bagikan kode ini kepada siapapun.");
        mailSender.send(message);
    }
}