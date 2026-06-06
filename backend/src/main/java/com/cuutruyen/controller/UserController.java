package com.cuutruyen.controller;

import com.cuutruyen.entity.User;
import com.cuutruyen.entity.Wallet;
import com.cuutruyen.repository.UserRepository;
import com.cuutruyen.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateRole(@PathVariable Integer id, @RequestBody Map<String, String> request) {
        try {
            User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + id));
            
            String roleStr = request.get("role");
            User.Role newRole = User.Role.valueOf(roleStr.toLowerCase());
            user.setRole(newRole);
            userRepository.save(user);
            
            return ResponseEntity.ok("Cập nhật quyền thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Lấy thông tin user hiện tại
    @GetMapping("/me")
    public ResponseEntity<?> getMe(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body("Unauthorized");
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");
        return ResponseEntity.ok(user);
    }

    // Lấy ví của user đang đăng nhập
    @GetMapping("/me/wallet")
    public ResponseEntity<?> getMyWallet(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body("Unauthorized");
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");

        Wallet wallet = walletRepository.findByUser_UserId(user.getUserId()).orElse(null);
        if (wallet == null) return ResponseEntity.ok(Map.of("balance", 0));
        return ResponseEntity.ok(wallet);
    }

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @PutMapping("/me/email")
    public ResponseEntity<?> updateEmail(@RequestBody Map<String, String> request, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body("Unauthorized");
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");

        String newEmail = request.get("email");
        if (newEmail == null || newEmail.isEmpty()) {
            return ResponseEntity.badRequest().body("Email không hợp lệ");
        }

        user.setEmail(newEmail);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Cập nhật email thành công", "email", newEmail));
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> request, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body("Unauthorized");
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");

        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (currentPassword == null || newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body("Mật khẩu mới phải có ít nhất 6 ký tự");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            return ResponseEntity.badRequest().body("Mật khẩu hiện tại không chính xác");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Cập nhật mật khẩu thành công"));
    }
}
