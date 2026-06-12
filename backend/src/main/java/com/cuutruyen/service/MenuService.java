package com.cuutruyen.service;

import com.cuutruyen.entity.Menu;
import com.cuutruyen.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;

    public List<Menu> getAllMenus() {
        return menuRepository.findAll();
    }

    public Menu createMenu(Menu menu) {
        if (menu.getIsHidden() == null) {
            menu.setIsHidden(false);
        }
        return menuRepository.save(menu);
    }

    public Optional<Menu> updateMenu(Integer id, Menu updatedMenu) {
        return menuRepository.findById(id).map(menu -> {
            if (updatedMenu.getTitle() != null) menu.setTitle(updatedMenu.getTitle());
            if (updatedMenu.getUrl() != null) menu.setUrl(updatedMenu.getUrl());
            if (updatedMenu.getRoles() != null) menu.setRoles(updatedMenu.getRoles());
            if (updatedMenu.getIsHidden() != null) menu.setIsHidden(updatedMenu.getIsHidden());
            return menuRepository.save(menu);
        });
    }

    public boolean deleteMenu(Integer id) {
        if (menuRepository.existsById(id)) {
            menuRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
