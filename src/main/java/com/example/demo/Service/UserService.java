package com.example.demo.Service;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Model.User;
import com.example.demo.Repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    public UserService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            OtpService otpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan."));
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email tidak ditemukan."));
    }

    @Transactional
    public void markTutorialSeen(String username) {
        User user = getByUsername(username);
        user.setTutorialSeen(true);
        userRepository.save(user);
    }

    // Register: simpan user (belum verified), kirim OTP
    public User register(String name, String email, String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username sudah digunakan.");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email sudah digunakan.");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setTutorialSeen(false);
        user.setVerified(false);

        userRepository.save(user);
        otpService.generateAndSendOtp(user);
        return user;
    }

    // Verifikasi OTP saat register
    public void verifyRegisterOtp(String username, String inputOtp) {
        User user = getByUsername(username);
        if (!otpService.validateOtp(user, inputOtp)) {
            throw new RuntimeException("OTP tidak valid atau sudah kadaluarsa.");
        }
        user.setVerified(true);
        otpService.clearOtp(user);
    }

    // Kirim OTP untuk login 2FA
    public void sendLoginOtp(String username) {
        User user = getByUsername(username);
        otpService.generateAndSendOtp(user);
    }

    // Verifikasi OTP saat login
    public void verifyLoginOtp(String username, String inputOtp) {
        User user = getByUsername(username);
        if (!otpService.validateOtp(user, inputOtp)) {
            throw new RuntimeException("OTP tidak valid atau sudah kadaluarsa.");
        }
        otpService.clearOtp(user);
    }

    // Kirim OTP ke email untuk reset password
    public void sendForgotPasswordOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email tidak ditemukan."));
        otpService.generateAndSendOtp(user);
    }

    // Reset password setelah OTP diverifikasi
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email tidak ditemukan."));
        if (!otpService.validateOtp(user, otp)) {
            throw new RuntimeException("OTP tidak valid atau sudah kadaluarsa.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        otpService.clearOtp(user);
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User tidak ditemukan."));

        if (!user.isVerified()) {
            throw new UsernameNotFoundException("Akun belum diverifikasi. Cek email kamu.");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
    }
}