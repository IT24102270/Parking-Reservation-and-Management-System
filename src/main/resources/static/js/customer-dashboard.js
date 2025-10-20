// Modern Customer Dashboard JavaScript
// City Park Solutions - Enhanced User Experience

document.addEventListener('DOMContentLoaded', function() {
    
    // Initialize dashboard
    initializeDashboard();
    
    // Update current date and time
    updateDateTime();
    setInterval(updateDateTime, 60000); // Update every minute
    
    // Initialize animations
    initializeAnimations();
    
    // Initialize interactive elements
    initializeInteractiveElements();
});

/**
 * Initialize dashboard functionality
 */
function initializeDashboard() {
    console.log('🚗 City Park Solutions Dashboard Initialized');
    
    // Add loading states
    document.body.classList.add('dashboard-loaded');
    
    // Initialize tooltips for stats
    initializeStatTooltips();
    
    // Initialize action card interactions
    initializeActionCards();
    
    // Ensure navigation links work properly
    ensureNavigationWorks();
}

/**
 * Update current date and time display
 */
function updateDateTime() {
    const now = new Date();
    const options = { 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        hour12: true
    };
    
    const dateTimeString = now.toLocaleDateString('en-US', options).replace(',', ' •');
    const dateTimeElement = document.getElementById('currentDateTime');
    
    if (dateTimeElement) {
        dateTimeElement.textContent = dateTimeString;
    }
}

/**
 * Initialize smooth animations for dashboard elements
 */
function initializeAnimations() {
    // Stagger animation for stat cards
    const statCards = document.querySelectorAll('.cps-stat-card');
    statCards.forEach((card, index) => {
        card.style.animationDelay = `${index * 0.1}s`;
    });
    
    // Stagger animation for action cards
    const actionCards = document.querySelectorAll('.cps-action-card');
    actionCards.forEach((card, index) => {
        card.style.animationDelay = `${(index * 0.1) + 0.3}s`;
    });
    
    // Stagger animation for activity items
    const activityItems = document.querySelectorAll('.cps-activity-item');
    activityItems.forEach((item, index) => {
        item.style.animationDelay = `${(index * 0.1) + 0.6}s`;
    });
}

/**
 * Initialize interactive elements
 */
function initializeInteractiveElements() {
    // Notification toggle
    const notificationToggle = document.getElementById('notificationToggle');
    if (notificationToggle) {
        notificationToggle.addEventListener('click', toggleNotifications);
    }
    
    // Profile toggle
    const profileToggle = document.getElementById('profileToggle');
    if (profileToggle) {
        profileToggle.addEventListener('click', toggleProfile);
    }
    
    // Close dropdowns when clicking outside
    document.addEventListener('click', function(event) {
        if (!event.target.closest('.cps-notifications') && !event.target.closest('.cps-user-profile')) {
            closeAllDropdowns();
        }
    });
    
    // Add keyboard navigation
    initializeKeyboardNavigation();
}

/**
 * Initialize stat card tooltips
 */
function initializeStatTooltips() {
    const statCards = document.querySelectorAll('.cps-stat-card');
    
    statCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            // Add subtle glow effect
            this.style.boxShadow = 'var(--shadow-lg), 0 0 20px rgba(52, 152, 219, 0.1)';
        });
        
        card.addEventListener('mouseleave', function() {
            // Remove glow effect
            this.style.boxShadow = 'var(--color-card-shadow)';
        });
    });
}

/**
 * Initialize action card interactions
 */
function initializeActionCards() {
    const actionCards = document.querySelectorAll('.cps-action-card');
    
    actionCards.forEach(card => {
        // Add click animation
        card.addEventListener('click', function(e) {
            // Create ripple effect
            createRippleEffect(e, this);
        });
        
        // Add keyboard support
        card.addEventListener('keydown', function(e) {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                this.click();
            }
        });
    });
}

/**
 * Create ripple effect on click
 */
function createRippleEffect(event, element) {
    const ripple = document.createElement('span');
    const rect = element.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height);
    const x = event.clientX - rect.left - size / 2;
    const y = event.clientY - rect.top - size / 2;
    
    ripple.style.cssText = `
        position: absolute;
        width: ${size}px;
        height: ${size}px;
        left: ${x}px;
        top: ${y}px;
        background: rgba(52, 152, 219, 0.3);
        border-radius: 50%;
        transform: scale(0);
        animation: ripple 0.6s ease-out;
        pointer-events: none;
        z-index: 1;
    `;
    
    element.style.position = 'relative';
    element.style.overflow = 'hidden';
    element.appendChild(ripple);
    
    // Remove ripple after animation
    setTimeout(() => {
        if (ripple.parentNode) {
            ripple.parentNode.removeChild(ripple);
        }
    }, 600);
}

/**
 * Toggle notifications dropdown
 */
function toggleNotifications() {
    const dropdown = document.getElementById('notificationDropdown');
    if (dropdown) {
        dropdown.classList.toggle('show');
        
        // Close profile dropdown if open
        const profileDropdown = document.getElementById('profileDropdown');
        if (profileDropdown) {
            profileDropdown.classList.remove('show');
        }
    }
}

/**
 * Toggle profile dropdown
 */
function toggleProfile() {
    const dropdown = document.getElementById('profileDropdown');
    if (dropdown) {
        dropdown.classList.toggle('show');
        
        // Close notification dropdown if open
        const notificationDropdown = document.getElementById('notificationDropdown');
        if (notificationDropdown) {
            notificationDropdown.classList.remove('show');
        }
    }
}

/**
 * Close all dropdowns
 */
function closeAllDropdowns() {
    const dropdowns = document.querySelectorAll('.notification-dropdown, .profile-dropdown');
    dropdowns.forEach(dropdown => {
        dropdown.classList.remove('show');
    });
}

/**
 * Ensure navigation links work properly
 */
function ensureNavigationWorks() {
    // Find all navigation links and ensure they work
    const navLinks = document.querySelectorAll('.cps-nav a, .cps-breadcrumb a');
    
    navLinks.forEach(link => {
        // Remove any existing event listeners that might interfere
        const newLink = link.cloneNode(true);
        link.parentNode.replaceChild(newLink, link);
        
        // Add explicit click handler to ensure navigation
        newLink.addEventListener('click', function(e) {
            const href = this.getAttribute('href') || this.getAttribute('th:href');
            if (href && !href.startsWith('#')) {
                // Allow normal navigation
                console.log('Navigating to:', href);
                return true;
            }
        });
    });
    
    console.log('Navigation links initialized:', navLinks.length);
}

/**
 * Initialize keyboard navigation
 */
function initializeKeyboardNavigation() {
    // Add tabindex to interactive elements
    const interactiveElements = document.querySelectorAll('.cps-action-card, .cps-stat-card');
    interactiveElements.forEach((element, index) => {
        element.setAttribute('tabindex', '0');
    });
    
    // Add keyboard shortcuts
    document.addEventListener('keydown', function(e) {
        // Alt + N for new booking
        if (e.altKey && e.key === 'n') {
            e.preventDefault();
            const newBookingLink = document.querySelector('a[href*="/customer/booking/new"]');
            if (newBookingLink) {
                newBookingLink.click();
            }
        }
        
        // Alt + B for bookings
        if (e.altKey && e.key === 'b') {
            e.preventDefault();
            const bookingsLink = document.querySelector('a[href*="/customer/bookings"]');
            if (bookingsLink) {
                bookingsLink.click();
            }
        }
        
        // Escape to close dropdowns
        if (e.key === 'Escape') {
            closeAllDropdowns();
        }
    });
}

/**
 * Animate number counting for stats
 */
function animateStatNumbers() {
    const statNumbers = document.querySelectorAll('.cps-stat-number');
    
    statNumbers.forEach(number => {
        const finalValue = parseInt(number.textContent) || 0;
        const duration = 1000; // 1 second
        const increment = finalValue / (duration / 16); // 60fps
        let currentValue = 0;
        
        const timer = setInterval(() => {
            currentValue += increment;
            if (currentValue >= finalValue) {
                currentValue = finalValue;
                clearInterval(timer);
            }
            number.textContent = Math.floor(currentValue);
        }, 16);
    });
}

/**
 * Show success message
 */
function showSuccessMessage(message) {
    const toast = document.createElement('div');
    toast.className = 'cps-toast cps-toast-success';
    toast.innerHTML = `
        <i class="fas fa-check-circle"></i>
        <span>${message}</span>
    `;
    
    document.body.appendChild(toast);
    
    // Animate in
    setTimeout(() => toast.classList.add('show'), 100);
    
    // Remove after 3 seconds
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => document.body.removeChild(toast), 300);
    }, 3000);
}

/**
 * Handle action card clicks with loading states
 */
function handleActionClick(actionType, element) {
    // Add loading state
    element.classList.add('loading');
    
    // Simulate loading (remove in production)
    setTimeout(() => {
        element.classList.remove('loading');
        showSuccessMessage(`${actionType} action completed!`);
    }, 1000);
}

// Add CSS for ripple animation
const rippleCSS = `
    @keyframes ripple {
        to {
            transform: scale(2);
            opacity: 0;
        }
    }
    
    .cps-toast {
        position: fixed;
        top: 20px;
        right: 20px;
        background: var(--gradient-card);
        border: 1px solid var(--color-border-light);
        border-radius: var(--radius-lg);
        padding: var(--spacing-lg);
        box-shadow: var(--shadow-lg);
        display: flex;
        align-items: center;
        gap: var(--spacing-md);
        transform: translateX(100%);
        transition: transform 0.3s ease;
        z-index: 10000;
    }
    
    .cps-toast.show {
        transform: translateX(0);
    }
    
    .cps-toast-success {
        border-left: 4px solid var(--color-status-success);
    }
    
    .cps-toast i {
        color: var(--color-status-success);
    }
    
    .loading {
        opacity: 0.7;
        pointer-events: none;
    }
`;

// Inject CSS
const style = document.createElement('style');
style.textContent = rippleCSS;
document.head.appendChild(style);

// Export functions for global use
window.CityParkDashboard = {
    showSuccessMessage,
    handleActionClick,
    animateStatNumbers,
    updateDateTime
};
