package com.cuutruyen.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Menu")
@Data
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "roles")
    private String roles;

    @Column(name = "is_hidden")
    private Boolean isHidden = false;
}
