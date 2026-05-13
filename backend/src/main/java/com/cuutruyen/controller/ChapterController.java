package com.cuutruyen.controller;

import com.cuutruyen.entity.*;
import com.cuutruyen.entity.Page;
import com.cuutruyen.repository.*;
import com.cuutruyen.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chapter")
@RequiredArgsConstructor
public class ChapterController {
    private final ChapterService chapterService;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PurchasedChapterRepository purchasedChapterRepository;
    private final TransactionRepository transactionRepository;
    private final TranslationGroupRepository translationGroupRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Chapter> getChapter(@PathVariable Integer id) {
        return chapterService.getChapter(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/pages")
    public ResponseEntity<List<Page>> getChapterPages(@PathVariable Integer id) {
        return ResponseEntity.ok(chapterService.getChapterPages(id));
    }

    @GetMapping("/series/{seriesId}")
    public ResponseEntity<List<Chapter>> getChaptersBySeries(@PathVariable Integer seriesId) {
        return ResponseEntity.ok(chapterService.getChaptersBySeries(seriesId));
    }

    @PostMapping
    public ResponseEntity<Chapter> createChapter(@RequestBody Chapter chapter) {
        return ResponseEntity.ok(chapterService.createChapter(chapter));
    }

    @PostMapping("/{id}/upload")
    public ResponseEntity<Void> uploadPages(
            @PathVariable Integer id,
            @RequestParam("files") MultipartFile[] files) {
        chapterService.uploadPages(id, files);
        return ResponseEntity.ok().build();
    }

    // Kiểm tra user đã mở khoá chương chưa
    @GetMapping("/{id}/check-unlock")
    public ResponseEntity<?> checkUnlock(@PathVariable Integer id, Authentication auth) {
        if (auth == null) return ResponseEntity.ok(Map.of("unlocked", false));
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.ok(Map.of("unlocked", false));

        boolean unlocked = purchasedChapterRepository.existsByUserIdAndChapterId(user.getUserId(), id);
        return ResponseEntity.ok(Map.of("unlocked", unlocked));
    }

    // Mở khoá chương (trả tiền)
    @PostMapping("/{id}/unlock")
    public ResponseEntity<?> unlockChapter(@PathVariable Integer id, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body("Chưa đăng nhập");

        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");

        Chapter chapter = chapterService.getChapter(id).orElse(null);
        if (chapter == null) return ResponseEntity.badRequest().body("Chapter not found");

        if (!chapter.isPaid() || chapter.getPrice() == 0) {
            return ResponseEntity.ok(Map.of("message", "Chương miễn phí", "unlocked", true));
        }

        // Kiểm tra đã mua chưa
        if (purchasedChapterRepository.existsByUserIdAndChapterId(user.getUserId(), id)) {
            return ResponseEntity.ok(Map.of("message", "Đã mở khoá rồi", "unlocked", true));
        }

        // Kiểm tra số dư
        Wallet userWallet = walletRepository.findByUser_UserId(user.getUserId()).orElse(null);
        if (userWallet == null || userWallet.getBalance() < chapter.getPrice()) {
            return ResponseEntity.badRequest().body("Số dư không đủ. Cần " + chapter.getPrice() + " Xu");
        }

        // Trừ tiền user
        userWallet.setBalance(userWallet.getBalance() - chapter.getPrice());
        walletRepository.save(userWallet);

        // Tạo giao dịch cho user
        Transaction userTx = new Transaction();
        userTx.setWallet(userWallet);
        userTx.setAmount((long) chapter.getPrice());
        userTx.setType(Transaction.TransactionType.purchase);
        userTx.setRefId(chapter.getChapterId());
        userTx.setNote("Mở khoá: " + chapter.getSeries().getTitle() + " - Chương " + chapter.getChapterNumber());
        transactionRepository.save(userTx);

        // Cộng tiền vào quỹ chung nhóm dịch (nhóm ACTIVE đầu tiên)
        List<TranslationGroup> groups = translationGroupRepository.findAll();
        for (TranslationGroup group : groups) {
            if (group.getStatus() == TranslationGroup.Status.ACTIVE && group.getLeader() != null) {
                group.setBalance(group.getBalance() + chapter.getPrice());
                translationGroupRepository.save(group);
                break;
            }
        }

        // Lưu bản ghi đã mua
        PurchasedChapter pc = new PurchasedChapter();
        pc.setUserId(user.getUserId());
        pc.setChapterId(chapter.getChapterId());
        pc.setPricePaid((long) chapter.getPrice());
        purchasedChapterRepository.save(pc);

        return ResponseEntity.ok(Map.of(
            "message", "Mở khoá thành công!",
            "unlocked", true,
            "newBalance", userWallet.getBalance()
        ));
    }
}

