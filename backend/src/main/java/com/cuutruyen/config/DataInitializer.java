package com.cuutruyen.config;

import com.cuutruyen.entity.User;
import com.cuutruyen.entity.Wallet;
import com.cuutruyen.entity.Transaction;
import com.cuutruyen.entity.TranslationGroup;
import com.cuutruyen.entity.Series;
import com.cuutruyen.repository.UserRepository;
import com.cuutruyen.repository.WalletRepository;
import com.cuutruyen.repository.TransactionRepository;
import com.cuutruyen.repository.TranslationGroupRepository;
import com.cuutruyen.repository.SeriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TranslationGroupRepository groupRepository;
    private final SeriesRepository seriesRepository;
    private final PasswordEncoder passwordEncoder;

    private static final long INITIAL_BALANCE = 100000L;

    @Override
    public void run(String... args) {
        log.info("Checking and initializing sample accounts...");
        
        createUserIfNotExist("admin", "admin@cuutruyen.com", "admin123", User.Role.admin);
        createUserIfNotExist("translator", "trans@cuutruyen.com", "trans123", User.Role.translator);
        createUserIfNotExist("uploader", "upload@cuutruyen.com", "upload123", User.Role.uploader);
        createUserIfNotExist("user", "user@cuutruyen.com", "user123", User.Role.user);

        // Ensure every user has a wallet with initial balance
        ensureAllUsersHaveWallet();

        // Create demo translation groups
        createDemoGroups();

        // Assign existing mangas to groups if they don't have one
        assignMangasToDemoGroups();
        
        log.info("Initialization complete.");
    }

    private void createUserIfNotExist(String username, String email, String password, User.Role role) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setRole(role);
            user.setDisplayName(username.toUpperCase());
            userRepository.save(user);
            log.info("Created user: {}", username);
        }
    }

    private void ensureAllUsersHaveWallet() {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            var optWallet = walletRepository.findByUser_UserId(user.getUserId());
            if (optWallet.isEmpty()) {
                Wallet wallet = new Wallet();
                wallet.setUser(user);
                wallet.setBalance(INITIAL_BALANCE);
                walletRepository.save(wallet);

                // Create initial deposit transaction
                Transaction tx = new Transaction();
                tx.setWallet(wallet);
                tx.setAmount(INITIAL_BALANCE);
                tx.setType(Transaction.TransactionType.deposit);
                tx.setNote("Quà tặng chào mừng thành viên mới");
                transactionRepository.save(tx);

                log.info("Created wallet with {} coins for user: {}", INITIAL_BALANCE, user.getUsername());
            } else {
                // Ensure existing wallet has at least INITIAL_BALANCE if it was 0 (old accounts)
                Wallet existing = optWallet.get();
                if (existing.getBalance() == 0L) {
                    existing.setBalance(INITIAL_BALANCE);
                    walletRepository.save(existing);

                    Transaction tx = new Transaction();
                    tx.setWallet(existing);
                    tx.setAmount(INITIAL_BALANCE);
                    tx.setType(Transaction.TransactionType.deposit);
                    tx.setNote("Quà tặng chào mừng - cập nhật hệ thống");
                    transactionRepository.save(tx);
                    log.info("Topped up wallet for user: {}", user.getUsername());
                }
            }
        }
    }

    private void createDemoGroups() {
        User uploader = userRepository.findByUsername("uploader").orElse(null);
        User translator = userRepository.findByUsername("translator").orElse(null);
        User admin = userRepository.findByUsername("admin").orElse(null);

        if (uploader != null) {
            createGroupIfNotExist("Cứu Truyện Team", uploader, TranslationGroup.Status.ACTIVE);
        }
        if (translator != null) {
            createGroupIfNotExist("Manga World VN", translator, TranslationGroup.Status.ACTIVE);
        }
        if (admin != null) {
            createGroupIfNotExist("Admin Scans", admin, TranslationGroup.Status.ACTIVE);
        }
    }

    private void createGroupIfNotExist(String name, User leader, TranslationGroup.Status status) {
        if (!groupRepository.existsByName(name)) {
            TranslationGroup group = new TranslationGroup();
            group.setName(name);
            group.setLeader(leader);
            group.setStatus(status);
            group.setBalance(0L);
            groupRepository.save(group);
            log.info("Created demo group: {}", name);
        }
    }

    private void assignMangasToDemoGroups() {
        List<Series> mangas = seriesRepository.findAll();
        List<TranslationGroup> groups = groupRepository.findAll();
        if (groups.isEmpty()) return;

        TranslationGroup defaultGroup = groups.get(0); // Cứu Truyện Team

        for (Series s : mangas) {
            boolean updated = false;
            if (s.getTranslationGroup() == null) {
                s.setTranslationGroup(defaultGroup);
                updated = true;
            }
            if (s.getUploadedBy() == null) {
                s.setUploadedBy(defaultGroup.getLeader());
                updated = true;
            }
            if (s.getApprovalStatus() == null) {
                s.setApprovalStatus(Series.ApprovalStatus.approved);
                updated = true;
            }
            if (s.getCoverUrl() != null && !s.getCoverUrl().startsWith("/") && !s.getCoverUrl().startsWith("http")) {
                s.setCoverUrl("/uploads/covers/" + s.getCoverUrl());
                updated = true;
            }
            if (updated) {
                seriesRepository.save(s);
            }
        }
        if (!mangas.isEmpty()) {
            log.info("Assigned {} existing mangas to demo groups.", mangas.size());
        }
    }
}
