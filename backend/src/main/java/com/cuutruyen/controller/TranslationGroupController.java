package com.cuutruyen.controller;

import com.cuutruyen.entity.TranslationGroup;
import com.cuutruyen.entity.User;
import com.cuutruyen.repository.TranslationGroupRepository;
import com.cuutruyen.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class TranslationGroupController {

    @Autowired
    private TranslationGroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    // Lấy tất cả nhóm dịch
    @GetMapping
    public ResponseEntity<List<TranslationGroup>> getAllGroups() {
        return ResponseEntity.ok(groupRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TranslationGroup> getGroupById(@PathVariable Integer id) {
        return ResponseEntity.ok(groupRepository.findById(id).orElse(null));
    }

    // Lấy nhóm dịch của user đang đăng nhập
    @GetMapping("/my-group")
    public ResponseEntity<?> getMyGroup(Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(401).body("Unauthorized");
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");

        List<TranslationGroup> groups = groupRepository.findByLeader_UserId(user.getUserId());
        TranslationGroup myGroup = groups.stream()
            .filter(g -> g.getStatus() == TranslationGroup.Status.ACTIVE)
            .findFirst()
            .orElse(groups.isEmpty() ? null : groups.get(0));

        // Nếu không phải leader, kiểm tra xem có thuộc nhóm nào không thông qua groupId
        if (myGroup == null && user.getGroupId() != null) {
            myGroup = groupRepository.findById(user.getGroupId()).orElse(null);
        }

        if (myGroup == null) return ResponseEntity.ok(Map.of("found", false));
        return ResponseEntity.ok(myGroup);
    }

    // Lấy danh sách nhóm đang chờ duyệt
    @GetMapping("/pending")
    public ResponseEntity<List<TranslationGroup>> getPendingGroups() {
        return ResponseEntity.ok(groupRepository.findByStatus(TranslationGroup.Status.PENDING));
    }

    // Yêu cầu tạo nhóm mới (User)
    @PostMapping("/request")
    public ResponseEntity<?> requestGroup(@RequestBody Map<String, String> request, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");

        String name = request.get("name");
        if (name == null || name.isEmpty()) return ResponseEntity.badRequest().body("Tên nhóm không được để trống");

        TranslationGroup group = new TranslationGroup();
        group.setName(name);
        group.setLeader(user);
        group.setStatus(TranslationGroup.Status.PENDING);
        groupRepository.save(group);

        return ResponseEntity.ok(group);
    }

    // Duyệt nhóm (Admin)
    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptGroup(@PathVariable Integer id) {
        TranslationGroup group = groupRepository.findById(id).orElse(null);
        if (group == null) return ResponseEntity.badRequest().body("Group not found");

        group.setStatus(TranslationGroup.Status.ACTIVE);
        groupRepository.save(group);

        // Cập nhật quyền uploader cho leader
        User leader = group.getLeader();
        leader.setRole(User.Role.uploader);
        userRepository.save(leader);

        return ResponseEntity.ok(group);
    }

    // Từ chối nhóm (Admin)
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectGroup(@PathVariable Integer id) {
        TranslationGroup group = groupRepository.findById(id).orElse(null);
        if (group == null) return ResponseEntity.badRequest().body("Group not found");

        group.setStatus(TranslationGroup.Status.REJECTED);
        groupRepository.save(group);

        return ResponseEntity.ok(group);
    }

    // Xóa nhóm
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable Integer id) {
        groupRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
