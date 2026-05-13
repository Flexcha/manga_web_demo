window.initSwipers = function () {
    const heroSwiperEl = document.querySelector('.heroSwiper');
    if (heroSwiperEl) {
        new Swiper('.heroSwiper', {
            loop: true,
            autoplay: { delay: 4000, disableOnInteraction: false },
            pagination: { el: '.swiper-pagination', clickable: true },
            grabCursor: true
        });
    }

    const updatedSwiperEl = document.querySelector('.updatedSwiper');
    if (updatedSwiperEl) {
        new Swiper('.updatedSwiper', {
            slidesPerView: 'auto',
            spaceBetween: 16,
            grabCursor: true,
            navigation: { nextEl: '.swiper-button-next' },
            slidesOffsetAfter: 20
        });
    }
};

const injectAuthComponents = () => {
    // Inject Modals
    if (!document.getElementById('loginModal')) {
        const modalHTML = `
            <!-- Login Modal -->
            <div class="modal fade auth-modal" id="loginModal" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h4 class="modal-title fw-bold">Đăng nhập tài khoản</h4>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <div class="mb-3">
                                <label class="form-label small fw-bold text-muted">Tên tài khoản</label>
                                <input type="text" id="loginUsername" class="form-control auth-input" placeholder="admin, uploader, translator...">
                            </div>
                            <div class="mb-4">
                                <label class="form-label small fw-bold text-muted">Mật khẩu</label>
                                <input type="password" id="loginPassword" class="form-control auth-input" placeholder="••••••••">
                            </div>
                            <button id="btnSubmitLogin" class="btn-auth-submit">ĐĂNG NHẬP</button>
                            <div class="mt-4 text-center">
                                <p class="text-muted small">Chưa có tài khoản? <a href="#" class="text-dark fw-bold" data-bs-toggle="modal" data-bs-target="#registerModal">Đăng ký ngay</a></p>
                                <hr>
                                <p class="text-muted border p-2 rounded bg-light" style="font-size: 0.75rem">
                                    <strong>Gợi ý:</strong> admin/admin123, uploader/upload123, translator/trans123, user/user123
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Register Modal -->
            <div class="modal fade auth-modal" id="registerModal" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h4 class="modal-title fw-bold">Đăng ký thành viên</h4>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <div class="mb-3">
                                <label class="form-label small fw-bold text-muted">Tên tài khoản</label>
                                <input type="text" id="regUsername" class="form-control auth-input">
                            </div>
                            <div class="mb-3">
                                <label class="form-label small fw-bold text-muted">Email</label>
                                <input type="email" id="regEmail" class="form-control auth-input">
                            </div>
                            <div class="mb-3">
                                <label class="form-label small fw-bold text-muted">Tên hiển thị</label>
                                <input type="text" id="regDisplayName" class="form-control auth-input">
                            </div>
                            <div class="mb-4">
                                <label class="form-label small fw-bold text-muted">Mật khẩu</label>
                                <input type="password" id="regPassword" class="form-control auth-input">
                            </div>
                            <button id="btnSubmitRegister" class="btn-auth-submit">ĐĂNG KÝ</button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Create Group Request Modal -->
            <div class="modal fade auth-modal" id="createGroupRequestModal" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h4 class="modal-title fw-bold">Yêu cầu Tạo Nhóm Dịch</h4>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <p class="text-secondary small mb-4">Để có thể đăng truyện, bạn cần phải có một nhóm dịch. Vui lòng gửi yêu cầu để Admin phê duyệt.</p>
                            <div class="mb-4">
                                <label class="form-label small fw-bold text-muted">Tên nhóm dịch muốn tạo</label>
                                <input type="text" id="reqGroupName" class="form-control auth-input" placeholder="Ví dụ: Cứu Truyện Team">
                            </div>
                            <button id="btnSubmitGroupRequest" class="btn-auth-submit">GỬI YÊU CẦU CHO ADMIN</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        document.body.insertAdjacentHTML('beforeend', modalHTML);
    }

    // User header injection removed as it's now in HTML for better design control
};

const setupAuthHandlers = () => {
    const btnSubmitLogin = document.getElementById('btnSubmitLogin');
    if (btnSubmitLogin) {
        btnSubmitLogin.addEventListener('click', async () => {
            const username = document.getElementById('loginUsername').value;
            const password = document.getElementById('loginPassword').value;


            try {
                btnSubmitLogin.disabled = true;
                const response = await ApiService.login(username, password);
                localStorage.setItem('token', response.token);
                localStorage.setItem('user', JSON.stringify(response));

                // Hide modal
                bootstrap.Modal.getInstance(document.getElementById('loginModal')).hide();
                window.location.reload();
            } catch (err) {
                alert("Lỗi: " + err.message);
            } finally {
                btnSubmitLogin.disabled = false;
            }
        });
    }

    const btnSubmitRegister = document.getElementById('btnSubmitRegister');
    if (btnSubmitRegister) {
        btnSubmitRegister.addEventListener('click', async () => {
            const username = document.getElementById('regUsername').value;
            const email = document.getElementById('regEmail').value;
            const displayName = document.getElementById('regDisplayName').value;
            const password = document.getElementById('regPassword').value;

            try {
                btnSubmitRegister.disabled = true;
                const data = { username, email, displayName, password };
                await ApiService.register(data);
                alert("Đăng ký thành công! Hãy đăng nhập.");
                
                // Switch to login modal
                bootstrap.Modal.getInstance(document.getElementById('registerModal')).hide();
                new bootstrap.Modal(document.getElementById('loginModal')).show();
            } catch (err) {
                alert("Lỗi: " + err.message);
            } finally {
                btnSubmitRegister.disabled = false;
            }
        });
    }


    const btnSubmitGroupRequest = document.getElementById('btnSubmitGroupRequest');
    if (btnSubmitGroupRequest) {
        btnSubmitGroupRequest.addEventListener('click', async () => {
            const groupName = document.getElementById('reqGroupName').value;
            if(!groupName) {
                alert("Vui lòng nhập tên nhóm dịch!");
                return;
            }
            
            try {
                btnSubmitGroupRequest.disabled = true;
                await ApiService.requestGroup(groupName);
                
                alert("Đã gửi yêu cầu tạo nhóm đến Admin! Vui lòng chờ phê duyệt.");
                bootstrap.Modal.getInstance(document.getElementById('createGroupRequestModal')).hide();
                document.getElementById('reqGroupName').value = '';
            } catch(err) {
                alert("Lỗi: " + err.message);
            } finally {
                btnSubmitGroupRequest.disabled = false;
            }
        });
    }


    const btnLogout = document.getElementById('btnLogout');
    if (btnLogout) {
        btnLogout.addEventListener('click', (e) => {
            e.preventDefault();
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = 'index.html';
        });
    }
};

const updateHeaderUI = () => {
    const userData = JSON.parse(localStorage.getItem('user'));
    const headerGuestView = document.getElementById('headerGuestView');
    const headerUserView = document.getElementById('headerUserView');
    const uploadLinks = document.querySelectorAll('#navUploadManga, #headerUploadLink, #menuUploadManga');
    const manageGroupLinks = document.querySelectorAll('#navManageGroup');
    const systemAdminLinks = document.querySelectorAll('#navSystemAdmin');

    // 1. Role-based visibility for specific links
    if (userData) {
        // Tự động thăng cấp role nếu được Admin duyệt (Lưu ở localStorage)
        const approvedUploaders = JSON.parse(localStorage.getItem('approved_uploaders') || '[]');
        if (approvedUploaders.includes(userData.username) && userData.role.toLowerCase() === 'user') {
            userData.role = 'uploader';
            localStorage.setItem('user', JSON.stringify(userData));
        }

        const role = userData.role.toLowerCase();

        // Upload permission (Admin, Uploader, Translator) can go to upload page.
        // Normal users see the button but it opens the create group modal.
        const canUpload = (role === 'admin' || role === 'uploader' || role === 'translator');
        uploadLinks.forEach(link => {
            link.classList.remove('d-none');
            // If user cannot upload, intercept the click
            if (!canUpload) {
                // Remove existing href if any to prevent navigation
                link.href = "#";
                link.addEventListener('click', (e) => {
                    e.preventDefault();
                    new bootstrap.Modal(document.getElementById('createGroupRequestModal')).show();
                });
            }
        });

        // Group Management (Admin, Uploader)
        const canManageGroup = (role === 'admin' || role === 'uploader');
        manageGroupLinks.forEach(link => canManageGroup ? link.classList.remove('d-none') : link.classList.add('d-none'));

        // System Admin (Admin only)
        const isSystemAdmin = (role === 'admin');
        systemAdminLinks.forEach(link => isSystemAdmin ? link.classList.remove('d-none') : link.classList.add('d-none'));

        // Hide support link for Admin
        const supportLinks = document.querySelectorAll('.header-links a');
        supportLinks.forEach(link => {
            if (link.textContent.trim().toUpperCase() === 'ỦNG HỘ') {
                isSystemAdmin ? link.classList.add('d-none') : link.classList.remove('d-none');
            }
        });

    } else {
        // Guest mode - hide all protected links
        [...uploadLinks, ...manageGroupLinks, ...systemAdminLinks].forEach(link => link.classList.add('d-none'));
    }

    // 2. Toggle Guest vs User header
    if (userData && headerUserView && headerGuestView) {
        headerGuestView.classList.add('d-none');
        headerUserView.classList.remove('d-none');
        headerUserView.classList.add('d-flex');

        const avatarEl = document.getElementById('headerAvatarText');
        if (avatarEl) avatarEl.textContent = userData.username.charAt(0).toUpperCase();

        // Fix Logout Buttons (both global and dropdown)
        document.querySelectorAll('#btnLogout').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                localStorage.removeItem('token');
                localStorage.removeItem('user');
                window.location.href = 'index.html';
            });
        });

        // Setup notification bell
        const bellIcon = document.querySelector('.fa-bell');
        if (bellIcon) {
            const bellBtn = bellIcon.closest('button') || bellIcon.closest('a');
            if (bellBtn) {
                bellBtn.addEventListener('click', (e) => {
                    e.preventDefault();
                    window.location.href = 'thong-bao.html';
                });
            }
        }
    } else if (headerGuestView && headerUserView) {
        headerGuestView.classList.remove('d-none');
        headerUserView.classList.add('d-none');
    }
};

document.addEventListener('DOMContentLoaded', () => {
    injectAuthComponents();
    setupAuthHandlers();
    updateHeaderUI();

    // Initial check for non-dynamic pages
    if (!document.querySelector('.heroSwiper .swiper-wrapper')) {
        window.initSwipers();
    }

    // LIVE SEARCH
    const btnSearchToggle = document.getElementById('btnSearchToggle');
    const searchOverlay = document.getElementById('searchOverlay');
    const closeSearchBtn = document.getElementById('closeSearchBtn');
    const liveSearchInput = document.getElementById('liveSearchInput');
    const searchResults = document.getElementById('searchResults');
    const searchResultList = document.getElementById('searchResultList');

    if (btnSearchToggle && searchOverlay) {
        btnSearchToggle.addEventListener('click', () => {
            searchOverlay.classList.remove('d-none');
            setTimeout(() => liveSearchInput && liveSearchInput.focus(), 100);
        });

        closeSearchBtn.addEventListener('click', () => {
            searchOverlay.classList.add('d-none');
        });

        let typingTimer;
        if (liveSearchInput) {
            liveSearchInput.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') {
                    const query = liveSearchInput.value.trim();
                    if (query) {
                        window.location.href = `search.html?q=${encodeURIComponent(query)}`;
                    }
                }
            });

            const btnFullSearch = document.getElementById('btnFullSearch');
            if (btnFullSearch) {
                btnFullSearch.addEventListener('click', () => {
                    const query = liveSearchInput.value.trim();
                    if (query) {
                        window.location.href = `search.html?q=${encodeURIComponent(query)}`;
                    }
                });
            }

            liveSearchInput.addEventListener('input', (e) => {
                const val = e.target.value.trim();
                clearTimeout(typingTimer);
                
                if (val.length >= 1) {
                    typingTimer = setTimeout(async () => {
                        try {
                            const response = await ApiService.searchMangas(val, 0, 10);
                            const matches = response.content || [];

                            if (searchResultList) {
                                if (matches.length > 0) {
                                    searchResultList.innerHTML = matches.map(m => `
                                        <a href="chi-tiet-truyen.html?id=${m.seriesId}" class="list-group-item list-group-item-action d-flex align-items-center gap-3 p-3">
                                             <img src="${getImageUrl(m.coverUrl)}" alt="Cover" style="width: 50px; height: 70px; object-fit: cover; border-radius: 4px;">
                                             <div>
                                                 <h6 class="mb-1 fw-bold text-dark">${m.title}</h6>
                                                 <small class="text-muted">${m.seriesType || 'Manga'}</small>
                                             </div>
                                         </a>
                                    `).join('');
                                } else {
                                    searchResultList.innerHTML = `<div class="p-3 text-center text-muted">Không tìm thấy kết quả nào cho "${val}"</div>`;
                                }
                                searchResults.classList.remove('d-none');
                            }
                        } catch (err) { console.error(err); }
                    }, 300);
                } else {
                    if (searchResults) searchResults.classList.add('d-none');
                }
            });
        }
    }
});
