package com.cuutruyen.repository;

import com.cuutruyen.entity.PurchasedChapter;
import com.cuutruyen.entity.PurchasedChapterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchasedChapterRepository extends JpaRepository<PurchasedChapter, PurchasedChapterId> {
    boolean existsByUserIdAndChapterId(Integer userId, Integer chapterId);
    List<PurchasedChapter> findByUserId(Integer userId);
    List<PurchasedChapter> findByChapterId(Integer chapterId);
}
