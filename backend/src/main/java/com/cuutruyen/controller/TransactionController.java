package com.cuutruyen.controller;

import com.cuutruyen.entity.Transaction;
import com.cuutruyen.entity.Wallet;
import com.cuutruyen.entity.User;
import com.cuutruyen.repository.TransactionRepository;
import com.cuutruyen.repository.WalletRepository;
import com.cuutruyen.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    // Lấy tất cả giao dịch toàn sàn (Admin)
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionRepository.findAllByOrderByCreatedAtDesc());
    }

    // Lấy danh sách yêu cầu rút tiền pending
    @GetMapping("/pending-withdrawals")
    public ResponseEntity<List<Transaction>> getPendingWithdrawals() {
        return ResponseEntity.ok(transactionRepository.findByTypeOrderByCreatedAtDesc(
            Transaction.TransactionType.withdrawal_pending));
    }

    // User/Uploader gửi yêu cầu rút tiền
    @PostMapping("/withdraw")
    public ResponseEntity<?> requestWithdrawal(@RequestBody Map<String, Object> request, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body("Unauthorized");

        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");

        Wallet wallet = walletRepository.findByUser_UserId(user.getUserId()).orElse(null);
        if (wallet == null) return ResponseEntity.badRequest().body("Wallet not found");

        long amount = Long.parseLong(request.get("amount").toString());
        String note = request.get("note") != null ? request.get("note").toString() : "Yêu cầu rút tiền";

        if (amount <= 0 || amount > wallet.getBalance()) {
            return ResponseEntity.badRequest().body("Số tiền không hợp lệ hoặc vượt quá số dư");
        }

        // Trừ tiền tạm thời và tạo giao dịch pending
        wallet.setBalance(wallet.getBalance() - amount);
        walletRepository.save(wallet);

        Transaction tx = new Transaction();
        tx.setWallet(wallet);
        tx.setAmount(amount);
        tx.setType(Transaction.TransactionType.withdrawal_pending);
        tx.setNote(note);
        transactionRepository.save(tx);

        return ResponseEntity.ok(tx);
    }

    // Admin duyệt rút tiền
    @PutMapping("/{txId}/approve")
    public ResponseEntity<?> approveWithdrawal(@PathVariable Integer txId) {
        Transaction tx = transactionRepository.findById(txId).orElse(null);
        if (tx == null) return ResponseEntity.badRequest().body("Transaction not found");
        if (tx.getType() != Transaction.TransactionType.withdrawal_pending) {
            return ResponseEntity.badRequest().body("Not a pending withdrawal");
        }

        tx.setType(Transaction.TransactionType.withdrawal_approved);
        tx.setNote(tx.getNote() + " [Đã duyệt]");
        transactionRepository.save(tx);

        return ResponseEntity.ok(tx);
    }

    // Admin từ chối rút tiền → hoàn lại tiền
    @PutMapping("/{txId}/reject")
    public ResponseEntity<?> rejectWithdrawal(@PathVariable Integer txId) {
        Transaction tx = transactionRepository.findById(txId).orElse(null);
        if (tx == null) return ResponseEntity.badRequest().body("Transaction not found");
        if (tx.getType() != Transaction.TransactionType.withdrawal_pending) {
            return ResponseEntity.badRequest().body("Not a pending withdrawal");
        }

        // Hoàn lại tiền vào ví
        Wallet wallet = tx.getWallet();
        wallet.setBalance(wallet.getBalance() + tx.getAmount());
        walletRepository.save(wallet);

        tx.setType(Transaction.TransactionType.withdrawal_rejected);
        tx.setNote(tx.getNote() + " [Đã từ chối - hoàn tiền]");
        transactionRepository.save(tx);

        return ResponseEntity.ok(tx);
    }
}
