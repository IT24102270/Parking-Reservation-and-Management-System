/**
 * City Park Solutions Pvt Ltd - Dark Mode System
 * Comprehensive dark mode functionality for the entire parking management system
 */

class DarkModeManager {
    constructor() {
        this.storageKey = 'cps-dark-mode';
        this.init();
    }

    init() {
        // Initialize dark mode based on saved preference or system preference
        const savedTheme = this.getSavedTheme();
        const systemPrefersDark = this.getSystemPreference();
        
        const initialTheme = savedTheme || (systemPrefersDark ? 'dark' : 'light');
        this.setTheme(initialTheme);
        
        // Create and insert toggle button
        this.createToggleButton();
        
        // Listen for system theme changes
        this.listenForSystemChanges();
        
        // Add keyboard shortcut (Ctrl/Cmd + Shift + D)
        this.addKeyboardShortcut();
    }

    getSavedTheme() {
        return localStorage.getItem(this.storageKey);
    }

    getSystemPreference() {
        return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    }

    setTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem(this.storageKey, theme);
        
        // Update toggle button icon
        this.updateToggleIcon(theme);
        
        // Dispatch custom event for other components to listen to
        window.dispatchEvent(new CustomEvent('themeChanged', { 
            detail: { theme } 
        }));
        
        // Update meta theme-color for mobile browsers
        this.updateMetaThemeColor(theme);
    }

    toggleTheme() {
        const currentTheme = document.documentElement.getAttribute('data-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        this.setTheme(newTheme);
        
        // Add visual feedback
        this.addToggleAnimation();
    }

    createToggleButton() {
        // Check if toggle already exists
        if (document.querySelector('.dark-mode-toggle')) {
            return;
        }

        const toggle = document.createElement('button');
        toggle.className = 'dark-mode-toggle';
        toggle.setAttribute('aria-label', 'Toggle dark mode');
        toggle.setAttribute('title', 'Toggle dark mode (Ctrl+Shift+D)');
        
        toggle.innerHTML = `
            <i class="fas fa-moon dark-mode-toggle-icon moon" aria-hidden="true"></i>
            <i class="fas fa-sun dark-mode-toggle-icon sun" aria-hidden="true"></i>
        `;
        
        toggle.addEventListener('click', () => this.toggleTheme());
        
        // Insert into DOM
        document.body.appendChild(toggle);
    }

    updateToggleIcon(theme) {
        const toggle = document.querySelector('.dark-mode-toggle');
        if (toggle) {
            const moonIcon = toggle.querySelector('.moon');
            const sunIcon = toggle.querySelector('.sun');
            
            if (theme === 'dark') {
                moonIcon.style.display = 'none';
                sunIcon.style.display = 'block';
            } else {
                moonIcon.style.display = 'block';
                sunIcon.style.display = 'none';
            }
        }
    }

    updateMetaThemeColor(theme) {
        let metaThemeColor = document.querySelector('meta[name="theme-color"]');
        
        if (!metaThemeColor) {
            metaThemeColor = document.createElement('meta');
            metaThemeColor.name = 'theme-color';
            document.head.appendChild(metaThemeColor);
        }
        
        const colors = {
            light: '#f8f9fb',
            dark: '#0f1419'
        };
        
        metaThemeColor.content = colors[theme];
    }

    listenForSystemChanges() {
        if (window.matchMedia) {
            const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
            mediaQuery.addEventListener('change', (e) => {
                // Only auto-switch if user hasn't manually set a preference
                if (!this.getSavedTheme()) {
                    this.setTheme(e.matches ? 'dark' : 'light');
                }
            });
        }
    }

    addKeyboardShortcut() {
        document.addEventListener('keydown', (e) => {
            // Ctrl+Shift+D or Cmd+Shift+D
            if ((e.ctrlKey || e.metaKey) && e.shiftKey && e.key === 'D') {
                e.preventDefault();
                this.toggleTheme();
            }
        });
    }

    addToggleAnimation() {
        const toggle = document.querySelector('.dark-mode-toggle');
        if (toggle) {
            toggle.style.transform = 'scale(0.9)';
            setTimeout(() => {
                toggle.style.transform = '';
            }, 150);
        }
    }

    // Public API methods
    getCurrentTheme() {
        return document.documentElement.getAttribute('data-theme');
    }

    isDarkMode() {
        return this.getCurrentTheme() === 'dark';
    }

    setDarkMode() {
        this.setTheme('dark');
    }

    setLightMode() {
        this.setTheme('light');
    }

    // Method to update specific elements that need custom dark mode handling
    updateCustomElements() {
        const currentTheme = this.getCurrentTheme();
        
        // Update charts and graphs
        this.updateCharts(currentTheme);
        
        // Update maps
        this.updateMaps(currentTheme);
        
        // Update any third-party components
        this.updateThirdPartyComponents(currentTheme);
    }

    updateCharts(theme) {
        // Update Chart.js charts if they exist
        if (window.Chart && window.Chart.instances) {
            Object.values(window.Chart.instances).forEach(chart => {
                if (chart && chart.options) {
                    const textColor = theme === 'dark' ? '#f7fafc' : '#1a2332';
                    const gridColor = theme === 'dark' ? '#2d3748' : '#e1e5e9';
                    
                    if (chart.options.scales) {
                        Object.values(chart.options.scales).forEach(scale => {
                            if (scale.ticks) scale.ticks.color = textColor;
                            if (scale.grid) scale.grid.color = gridColor;
                        });
                    }
                    
                    chart.update();
                }
            });
        }
    }

    updateMaps(theme) {
        // Update map themes if maps are present
        // This would be implemented based on the mapping library used
    }

    updateThirdPartyComponents(theme) {
        // Update any other third-party components that need theme updates
        // This can be extended as needed
    }
}

// Initialize dark mode when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.darkModeManager = new DarkModeManager();
});

// Also initialize immediately if DOM is already loaded
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        window.darkModeManager = new DarkModeManager();
    });
} else {
    window.darkModeManager = new DarkModeManager();
}

// Export for use in other scripts
if (typeof module !== 'undefined' && module.exports) {
    module.exports = DarkModeManager;
}
