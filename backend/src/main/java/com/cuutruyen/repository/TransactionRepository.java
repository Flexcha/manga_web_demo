package com.cuutruyen.repository;

import com.cuutruyen.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findByWallet_WalletIdOrderByCreatedAtDesc(Integer walletId);
    List<Transaction> findAllByOrderByCreatedAtDesc();
    List<Transaction> findByTypeOrderByCreatedAtDesc(Transaction.TransactionType type);
}
