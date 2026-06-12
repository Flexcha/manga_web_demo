package com.cuutruyen.controller;

import com.cuutruyen.entity.Menu;
import com.cuutruyen.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {
    
    private final MenuRepository menuRepository;

    @GetMapping
    public ResponseEntity<List<Menu>> getAllMenus() {
        return ResponseEntity.ok(menuRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Menu> createMenu(@RequestBody Menu menu) {
        if (menu.getIsHidden() == null) menu.setIsHidden(false);
        return ResponseEntity.ok(menuRepository.save(menu));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Menu> updateMenu(@PathVariable Integer id, @RequestBody Menu updatedMenu) {
        return menuRepository.findById(id).map(menu -> {
            if (updatedMenu.getTitle() != null) menu.setTitle(updatedMenu.getTitle());
            if (updatedMenu.getUrl() != null) menu.setUrl(updatedMenu.getUrl());
            if (updatedMenu.getRoles() != null) menu.setRoles(updatedMenu.getRoles());
            if (updatedMenu.getIsHidden() != null) menu.setIsHidden(updatedMenu.getIsHidden());
            return ResponseEntity.ok(menuRepository.save(menu));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Integer id) {
        if (menuRepository.existsById(id)) {
            menuRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
