// Dashboard JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('Dashboard loaded');
    
    // Initialize dashboard
    initializeDashboard();
    
    // Set up action card click handlers
    setupActionCards();
    
    // Initialize notification system
    initializeNotifications();
    
    // Update time display
    updateTimeDisplay();
    setInterval(updateTimeDisplay, 1000); // Update every second
});

function initializeDashboard() {
    // Add event listeners
    addEventListeners();
    
    // Animate stats cards
    animateStatsCards();
    
    // Load recent activity
    loadRecentActivity();
    
    // Initialize tooltips
    initializeTooltips();
}

function addEventListeners() {
    // Action card clicks
    document.querySelectorAll('.action-card').forEach(card => {
        card.addEventListener('click', function() {
            const action = this.dataset.action;
            handleActionClick(action);
        });
    });
    
    // Notification bell click
    const notificationBell = document.querySelector('.notifications');
    if (notificationBell) {
        notificationBell.addEventListener('click', toggleNotifications);
    }
    
    // Profile dropdown
    const userProfile = document.querySelector('.user-info');
    if (userProfile) {
        userProfile.addEventListener('click', toggleProfileMenu);
    }
}

function setupActionCards() {
    // Set up action card click handlers
    document.querySelectorAll('.action-card').forEach(card => {
        card.addEventListener('click', function() {
            const action = this.dataset.action;
            handleActionClick(action);
        });
        
        // Add hover effects
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });
}

function handleActionClick(action) {
    console.log('Action clicked:', action);
    
    switch(action) {
        case 'new-booking':
            window.location.href = '/customer/booking/new';
            break;
        case 'my-bookings':
            window.location.href = '/customer/bookings';
            break;
        case 'view-slots':
            window.location.href = '/customer/slots';
            break;
        case 'get-help':
            window.location.href = '/customer/support/help';
            break;
        case 'feedback':
            window.location.href = '/customer/feedback';
            break;
        case 'profile':
            window.location.href = '/customer/profile';
            break;
        case 'payments':
            window.location.href = '/customer/payments';
            break;
        default:
            console.log('Unknown action:', action);
    }
}

function initializeTooltips() {
    // Initialize tooltips for elements with title attributes
    document.querySelectorAll('[title]').forEach(element => {
        element.addEventListener('mouseenter', function() {
            const tooltip = document.createElement('div');
            tooltip.className = 'tooltip';
            tooltip.textContent = this.getAttribute('title');
            document.body.appendChild(tooltip);
            
            const rect = this.getBoundingClientRect();
            tooltip.style.left = rect.left + (rect.width / 2) - (tooltip.offsetWidth / 2) + 'px';
            tooltip.style.top = rect.top - tooltip.offsetHeight - 10 + 'px';
        });
        
        element.addEventListener('mouseleave', function() {
            const tooltip = document.querySelector('.tooltip');
            if (tooltip) {
                tooltip.remove();
            }
        });
    });
}

function animateStatsCards() {
    const cards = document.querySelectorAll('.stat-card');
    cards.forEach((card, index) => {
        setTimeout(() => {
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';
            card.style.transition = 'all 0.6s ease';
            
            setTimeout(() => {
                card.style.opacity = '1';
                card.style.transform = 'translateY(0)';
            }, 100);
        }, index * 150);
    });
}

function handleActionClick(action) {
    switch(action) {
        case 'new-booking':
            window.location.href = '/customer/booking/new';
            break;
        case 'my-bookings':
            window.location.href = '/customer/bookings';
            break;
        case 'view-slots':
            window.location.href = '/customer/slots';
            break;
        case 'get-help':
            window.location.href = '/customer/support/help';
            break;
        case 'feedback':
            window.location.href = '/customer/feedback';
            break;
        case 'profile':
            window.location.href = '/customer/profile';
            break;
        default:
            console.log('Unknown action:', action);
    }
}

function toggleNotifications() {
    // Create notification dropdown if it doesn't exist
    let dropdown = document.querySelector('.notification-dropdown');
    
    if (!dropdown) {
        dropdown = createNotificationDropdown();
        document.body.appendChild(dropdown);
    }
    
    dropdown.classList.toggle('show');
}

function createNotificationDropdown() {
    const dropdown = document.createElement('div');
    dropdown.className = 'notification-dropdown';
    dropdown.innerHTML = `
        <div class="notification-header">
            <h4>Notifications</h4>
            <span class="mark-all-read">Mark all as read</span>
        </div>
        <div class="notification-list">
            <div class="notification-item">
                <div class="notification-icon">📅</div>
                <div class="notification-content">
                    <p>Booking confirmed for slot B-102</p>
                    <span class="notification-time">2 hours ago</span>
                </div>
            </div>
            <div class="notification-item">
                <div class="notification-icon">⏰</div>
                <div class="notification-content">
                    <p>Reminder: Parking expires in 30 minutes</p>
                    <span class="notification-time">30 minutes ago</span>
                </div>
            </div>
        </div>
        <div class="notification-footer">
            <a href="/customer/notifications">View all notifications</a>
        </div>
    `;
    
    return dropdown;
}

function toggleProfileMenu() {
    let menu = document.querySelector('.profile-menu');
    
    if (!menu) {
        menu = createProfileMenu();
        document.body.appendChild(menu);
    }
    
    menu.classList.toggle('show');
}

function createProfileMenu() {
    const menu = document.createElement('div');
    menu.className = 'profile-menu';
    menu.innerHTML = `
        <div class="profile-menu-item">
            <a href="/customer/profile">👤 My Profile</a>
        </div>
        <div class="profile-menu-item">
            <a href="/customer/settings">⚙️ Settings</a>
        </div>
        <div class="profile-menu-item">
            <a href="/customer/help">❓ Help & Support</a>
        </div>
        <div class="profile-menu-divider"></div>
        <div class="profile-menu-item">
            <a href="/logout">🚪 Logout</a>
        </div>
    `;
    
    return menu;
}

function loadRecentActivity() {
    // Recent activity is now loaded from the backend via Thymeleaf
    // The data is already rendered in the HTML template
    console.log('Recent activity loaded from database via backend');
}

function updateActivityList(activities) {
    const activityList = document.querySelector('.activity-list');
    if (!activityList) return;
    
    activityList.innerHTML = activities.map(activity => `
        <div class="activity-item">
            <div class="activity-icon ${activity.type}">
                ${activity.type === 'confirmed' ? '✓' : '✕'}
            </div>
            <div class="activity-content">
                <div class="activity-title">${activity.id}</div>
                <div class="activity-details">${activity.slot} • ${activity.date}</div>
            </div>
            <div class="activity-status status-${activity.type.toLowerCase()}">
                ${activity.status}
            </div>
        </div>
    `).join('');
}

function updateTimeDisplay() {
    const now = new Date();
    const options = { 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    };
    
    const timeElement = document.querySelector('.current-time');
    if (timeElement) {
        timeElement.textContent = now.toLocaleDateString('en-US', options);
    }
}

function initializeTooltips() {
    // Add tooltips to action cards
    document.querySelectorAll('.action-card').forEach(card => {
        card.addEventListener('mouseenter', function() {
            const tooltip = this.querySelector('.action-subtitle');
            if (tooltip) {
                tooltip.style.opacity = '1';
            }
        });
        
        card.addEventListener('mouseleave', function() {
            const tooltip = this.querySelector('.action-subtitle');
            if (tooltip) {
                tooltip.style.opacity = '0.7';
            }
        });
    });
}

// Close dropdowns when clicking outside
document.addEventListener('click', function(event) {
    const notificationDropdown = document.querySelector('.notification-dropdown');
    const profileMenu = document.querySelector('.profile-menu');
    
    if (notificationDropdown && !event.target.closest('.notifications') && !event.target.closest('.notification-dropdown')) {
        notificationDropdown.classList.remove('show');
    }
    
    if (profileMenu && !event.target.closest('.user-info') && !event.target.closest('.profile-menu')) {
        profileMenu.classList.remove('show');
    }
});

// Utility functions
function formatCurrency(amount) {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD'
    }).format(amount);
}

function formatDate(date) {
    return new Intl.DateTimeFormat('en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }).format(new Date(date));
}

// Add smooth scrolling for internal links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    });
});

// Notification System Functions
function initializeNotifications() {
    const notificationToggle = document.getElementById('notificationToggle');
    const notificationDropdown = document.getElementById('notificationDropdown');
    
    if (notificationToggle && notificationDropdown) {
        // Create overlay element
        let overlay = document.querySelector('.notification-overlay');
        if (!overlay) {
            overlay = document.createElement('div');
            overlay.className = 'notification-overlay';
            document.body.appendChild(overlay);
        }
        
        // Move dropdown to body to avoid positioning issues
        if (notificationDropdown.parentElement !== document.body) {
            document.body.appendChild(notificationDropdown);
        }
        
        // Toggle notification dropdown
        notificationToggle.addEventListener('click', function(e) {
            e.stopPropagation();
            
            const isActive = notificationDropdown.classList.contains('active');
            
            if (isActive) {
                // Close dropdown
                notificationDropdown.classList.remove('active');
                overlay.classList.remove('active');
            } else {
                // Open dropdown
                notificationDropdown.classList.add('active');
                overlay.classList.add('active');
                
                // Position dropdown relative to the bell icon
                const rect = notificationToggle.getBoundingClientRect();
                notificationDropdown.style.top = (rect.bottom + 10) + 'px';
                notificationDropdown.style.right = (window.innerWidth - rect.right) + 'px';
            }
        });
        
        // Close dropdown when clicking overlay
        overlay.addEventListener('click', function() {
            notificationDropdown.classList.remove('active');
            overlay.classList.remove('active');
        });
        
        // Close dropdown when clicking outside
        document.addEventListener('click', function(e) {
            if (!notificationToggle.contains(e.target) && !notificationDropdown.contains(e.target)) {
                notificationDropdown.classList.remove('active');
                overlay.classList.remove('active');
            }
        });
        
        // Prevent dropdown from closing when clicking inside
        notificationDropdown.addEventListener('click', function(e) {
            e.stopPropagation();
        });
    }
}

// Mark notification as read
function markAsRead(notificationId) {
    fetch(`/api/notifications/${notificationId}/read`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => {
        if (response.ok) {
            // Update UI to show as read
            const notificationItem = document.querySelector(`[data-notification-id="${notificationId}"]`);
            if (notificationItem) {
                notificationItem.classList.remove('unread');
                notificationItem.classList.add('read');
            }
            
            // Update badge count
            updateNotificationBadge();
        }
    })
    .catch(error => {
        console.error('Error marking notification as read:', error);
    });
}

// Mark all notifications as read
function markAllAsRead() {
    fetch('/api/notifications/mark-all-read', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => {
        if (response.ok) {
            // Update UI to show all as read
            document.querySelectorAll('.notification-item.unread').forEach(item => {
                item.classList.remove('unread');
                item.classList.add('read');
            });
            
            // Hide badge and update count
            const badge = document.querySelector('.notification-badge');
            if (badge) {
                badge.style.display = 'none';
            }
            
            const countElement = document.querySelector('.notification-count');
            if (countElement) {
                countElement.textContent = '0 unread';
            }
            
            // Hide mark all read button
            const markAllBtn = document.querySelector('.mark-all-read-btn');
            if (markAllBtn) {
                markAllBtn.style.display = 'none';
            }
        }
    })
    .catch(error => {
        console.error('Error marking all notifications as read:', error);
    });
}

// Update notification badge count
function updateNotificationBadge() {
    fetch('/api/notifications/unread-count')
    .then(response => response.json())
    .then(data => {
        const badge = document.querySelector('.notification-badge');
        const countElement = document.querySelector('.notification-count');
        
        if (data.count > 0) {
            if (badge) {
                badge.textContent = data.count;
                badge.style.display = 'flex';
            }
            if (countElement) {
                countElement.textContent = `${data.count} unread`;
            }
        } else {
            if (badge) {
                badge.style.display = 'none';
            }
            if (countElement) {
                countElement.textContent = '0 unread';
            }
        }
    })
    .catch(error => {
        console.error('Error updating notification badge:', error);
    });
}

// Show notification toast
function showNotificationToast(title, message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `notification-toast ${type}`;
    toast.innerHTML = `
        <div class="toast-icon">
            <i class="fas ${getToastIcon(type)}"></i>
        </div>
        <div class="toast-content">
            <div class="toast-title">${title}</div>
            <div class="toast-message">${message}</div>
        </div>
        <button class="toast-close" onclick="this.parentElement.remove()">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    document.body.appendChild(toast);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (toast.parentElement) {
            toast.remove();
        }
    }, 5000);
}

function getToastIcon(type) {
    switch(type) {
        case 'success': return 'fa-check-circle';
        case 'warning': return 'fa-exclamation-triangle';
        case 'error': return 'fa-times-circle';
        case 'payment': return 'fa-credit-card';
        case 'booking': return 'fa-parking';
        default: return 'fa-info-circle';
    }
}

// Refresh notifications periodically
setInterval(() => {
    updateNotificationBadge();
}, 30000); // Check every 30 seconds
