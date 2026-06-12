DROP DATABASE IF EXISTS manga_db;

CREATE DATABASE manga_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE manga_db;

-- ============================================================
-- 1. USERS
-- ============================================================

CREATE TABLE Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    avatar_url VARCHAR(255),
    role ENUM(
        'admin',
        'user',
        'translator',
        'uploader'
    ) DEFAULT 'user',
    status ENUM(
        'active',
        'banned',
        'inactive'
    ) DEFAULT 'active',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
-- Gộp UserProfiles vào Users để đơn giản — profile đơn giản không cần tách bảng
-- Gộp Roles/UserRoles vào 1 ENUM — 99% app không cần multi-role phức tạp

CREATE INDEX idx_users_status ON Users (status);

-- ============================================================
-- 2. SERIES (Manga / Manhwa / Manhua / Comic)
-- ============================================================

CREATE TABLE Series (
    series_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    alternative_title VARCHAR(150),
    description TEXT,
    series_type ENUM(
        'manga',
        'manhwa',
        'manhua',
        'comic'
    ) NOT NULL,
    status ENUM(
        'ongoing',
        'completed',
        'hiatus',
        'cancelled'
    ) DEFAULT 'ongoing',
    age_rating ENUM('all', 'teen', 'mature') DEFAULT 'all',
    cover_url VARCHAR(255),
    -- Cached counters — cập nhật bằng application layer
    total_views BIGINT DEFAULT 0,
    average_rating DECIMAL(4, 2) DEFAULT 0.00,
    total_ratings INT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_series_type_status ON Series (series_type, status);

CREATE INDEX idx_series_active ON Series (is_deleted, status);

-- ============================================================
-- 3. MASTER DATA
-- ============================================================

CREATE TABLE Authors (
    author_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    bio TEXT
);

CREATE TABLE Genres (
    genre_id INT AUTO_INCREMENT PRIMARY KEY,
    genre_name VARCHAR(50) NOT NULL UNIQUE
);

-- Junction tables — quan hệ nhiều-nhiều
CREATE TABLE SeriesAuthors (
    series_id INT NOT NULL,
    author_id INT NOT NULL,
    PRIMARY KEY (series_id, author_id),
    FOREIGN KEY (series_id) REFERENCES Series (series_id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES Authors (author_id) ON DELETE CASCADE
);

CREATE TABLE SeriesGenres (
    series_id INT NOT NULL,
    genre_id INT NOT NULL,
    PRIMARY KEY (series_id, genre_id),
    FOREIGN KEY (series_id) REFERENCES Series (series_id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES Genres (genre_id) ON DELETE CASCADE
);

-- ============================================================
-- 4. CHAPTERS & PAGES
-- ============================================================

CREATE TABLE Chapters (
    chapter_id INT AUTO_INCREMENT PRIMARY KEY,
    series_id INT NOT NULL,
    chapter_number DECIMAL(6, 2) NOT NULL,
    title VARCHAR(150),
    view_count BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (series_id) REFERENCES Series (series_id) ON DELETE CASCADE,
    UNIQUE (series_id, chapter_number)
);

CREATE INDEX idx_chapters_series ON Chapters (series_id, is_deleted);

CREATE TABLE Pages (
    page_id INT AUTO_INCREMENT PRIMARY KEY,
    chapter_id INT NOT NULL,
    page_number SMALLINT NOT NULL CHECK (page_number > 0),
    image_url VARCHAR(255) NOT NULL,
    FOREIGN KEY (chapter_id) REFERENCES Chapters (chapter_id) ON DELETE CASCADE,
    UNIQUE (chapter_id, page_number)
);

-- ============================================================
-- 5. READING FEATURES
-- ============================================================

CREATE TABLE Favorites (
    user_id INT NOT NULL,
    series_id INT NOT NULL,
    added_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, series_id),
    FOREIGN KEY (user_id) REFERENCES Users (user_id) ON DELETE CASCADE,
    FOREIGN KEY (series_id) REFERENCES Series (series_id) ON DELETE CASCADE
);

CREATE INDEX idx_favorites_series ON Favorites (series_id);

-- Lưu tiến độ đọc: đang đọc đến chapter/trang nào
CREATE TABLE ReadingProgress (
    user_id INT NOT NULL,
    series_id INT NOT NULL,
    chapter_id INT NOT NULL,
    page_number SMALLINT DEFAULT 1,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, series_id),
    FOREIGN KEY (user_id) REFERENCES Users (user_id) ON DELETE CASCADE,
    FOREIGN KEY (series_id) REFERENCES Series (series_id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES Chapters (chapter_id) ON DELETE CASCADE
);

-- ============================================================
-- 6. RATINGS & COMMENTS
-- ============================================================

-- Mỗi user chỉ rate 1 lần cho 1 series
-- Application tự tính lại Series.average_rating sau INSERT/UPDATE/DELETE ở đây
CREATE TABLE Ratings (
    user_id INT NOT NULL,
    series_id INT NOT NULL,
    rating TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    rated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, series_id),
    FOREIGN KEY (user_id) REFERENCES Users (user_id) ON DELETE CASCADE,
    FOREIGN KEY (series_id) REFERENCES Series (series_id) ON DELETE CASCADE
);

CREATE INDEX idx_ratings_series ON Ratings (series_id);

CREATE TABLE Comments (
    comment_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    series_id INT NOT NULL,
    chapter_id INT NULL, -- NULL = comment cấp series
    parent_comment_id INT NULL, -- NULL = comment gốc
    content TEXT NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE, -- soft-delete cho moderation
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users (user_id) ON DELETE CASCADE,
    FOREIGN KEY (series_id) REFERENCES Series (series_id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES Chapters (chapter_id) ON DELETE SET NULL,
    -- SET NULL: xóa chapter không kéo theo mất comment
    FOREIGN KEY (parent_comment_id) REFERENCES Comments (comment_id) ON DELETE SET NULL
    -- SET NULL: xóa comment cha không mất cả cây reply
);

CREATE INDEX idx_comments_series ON Comments (series_id, is_deleted);

CREATE INDEX idx_comments_chapter ON Comments (chapter_id);

-- ============================================================
-- 8. NOTIFICATIONS
-- ============================================================

CREATE TABLE Notifications (
    notif_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type VARCHAR(30) NOT NULL, -- 'chapter_update', 'comment_reply', v.v.
    title VARCHAR(150),
    message TEXT NOT NULL,
    ref_id INT NULL, -- series_id hoặc chapter_id tùy type
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users (user_id) ON DELETE CASCADE
);

CREATE INDEX idx_notif_user_unread ON Notifications (user_id, is_read);

-- ============================================================
-- 9. VIEWS TIỆN LỢI
-- ============================================================

-- Series kèm tên tác giả + thể loại (dùng GROUP_CONCAT)
CREATE VIEW vw_series AS
SELECT
    s.series_id,
    s.title,
    s.series_type,
    s.status,
    s.age_rating,
    s.cover_url,
    s.average_rating,
    s.total_ratings,
    s.total_views,
    GROUP_CONCAT(
        DISTINCT a.name
        ORDER BY a.name SEPARATOR ', '
    ) AS authors,
    GROUP_CONCAT(
        DISTINCT g.genre_name
        ORDER BY g.genre_name SEPARATOR ', '
    ) AS genres
FROM
    Series s
    LEFT JOIN SeriesAuthors sa ON sa.series_id = s.series_id
    LEFT JOIN Authors a ON a.author_id = sa.author_id
    LEFT JOIN SeriesGenres sg ON sg.series_id = s.series_id
    LEFT JOIN Genres g ON g.genre_id = sg.genre_id
WHERE
    s.is_deleted = FALSE
GROUP BY
    s.series_id;

-- ============================================================
-- 10. DEFAULT DATA
-- ============================================================

INSERT INTO
    Genres (genre_name)
VALUES ('Action'),
    ('Adventure'),
    ('Comedy'),
    ('Drama'),
    ('Fantasy'),
    ('Horror'),
    ('Mystery'),
    ('Romance'),
    ('Sci-Fi'),
    ('Slice of Life'),
    ('Sports'),
    ('Supernatural'),
    ('Thriller'),
    ('Martial Arts');

-- ============================================================
-- 11. REPORTS
-- ============================================================

CREATE TABLE Reports (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id INT NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users (user_id) ON DELETE CASCADE
);