package com.cuutruyen.controller;

import com.cuutruyen.entity.Comment;
import com.cuutruyen.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/chapter/{id}")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Integer id) {
        return ResponseEntity.ok(commentService.getCommentsByChapter(id));
    }

    @PostMapping
    public ResponseEntity<Comment> addComment(@RequestBody Map<String, Object> payload) {
        Integer userId = (Integer) payload.get("userId");
        Integer seriesId = (Integer) payload.get("seriesId");
        Integer chapterId = (Integer) payload.get("chapterId");
        String content = (String) payload.get("content");
        
        return ResponseEntity.ok(commentService.addComment(userId, seriesId, chapterId, content));
    }
}
