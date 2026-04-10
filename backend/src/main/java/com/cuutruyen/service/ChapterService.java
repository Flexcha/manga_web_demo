package com.cuutruyen.service;

import com.cuutruyen.entity.Chapter;
import com.cuutruyen.entity.Page;
import com.cuutruyen.repository.ChapterRepository;
import com.cuutruyen.repository.PageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChapterService {
    private final ChapterRepository chapterRepository;
    private final PageRepository pageRepository;
    private final FileStorageService fileStorageService;

    public Optional<Chapter> getChapter(Integer chapterId) {
        return chapterRepository.findById(chapterId);
    }

    public List<Page> getChapterPages(Integer chapterId) {
        return pageRepository.findByChapterChapterIdOrderByPageNumberAsc(chapterId);
    }

    public List<Chapter> getChaptersBySeries(Integer seriesId) {
        return chapterRepository.findBySeriesSeriesIdOrderByChapterNumberDesc(seriesId);
    }

    public Chapter createChapter(Chapter chapter) {
        return chapterRepository.save(chapter);
    }

    public void uploadPages(Integer chapterId, MultipartFile[] files) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        // Sort files by name to ensure correct page order
        Arrays.sort(files, Comparator.comparing(f -> f.getOriginalFilename() != null ? f.getOriginalFilename() : ""));

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            String subDir = "manga_" + chapter.getSeries().getSeriesId() + "/chapter_" + chapterId;
            String url = fileStorageService.saveFile(file, subDir);

            Page page = new Page();
            page.setChapter(chapter);
            page.setPageNumber((short) (i + 1));
            page.setImageUrl(url);
            pageRepository.save(page);
        }
    }
}
