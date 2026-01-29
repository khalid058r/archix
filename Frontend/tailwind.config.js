/** @type {import('tailwindcss').Config} */
export default {
    content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
    theme: {
        extend: {
            colors: {
                // Couleurs principales
                primary: {
                    DEFAULT: '#8E1616',
                    light: '#B21E1E',
                    dark: '#6B1010',
                    bg: '#F4E6E6',
                },
                secondary: {
                    DEFAULT: '#E8C999',
                    light: '#F0D4AA',
                    dark: '#DDB888',
                    bg: '#F6F0E6',
                },
                cream: '#F8EEDF',

                // Gris
                gray: {
                    100: '#F5F5F5',
                    300: '#CCCCCC',
                    500: '#999999',
                    700: '#666666',
                    900: '#333333',
                },

                // Fonctionnelles
                success: '#2E7D32',
                warning: '#F57F17',
                error: '#8E1616',
                info: '#1565C0',

                // Surfaces (from prompt logic)
                background: '#F8EEDF',
                surface: '#FFFFFF',
            },
            fontFamily: {
                sans: ['Inter', 'system-ui', 'sans-serif'],
                mono: ['JetBrains Mono', 'monospace'],
            },
            fontSize: {
                'title-xl': ['32px', { lineHeight: '1.2', fontWeight: '600' }],
                'title-lg': ['24px', { lineHeight: '1.3', fontWeight: '600' }],
                'title-md': ['20px', { lineHeight: '1.4', fontWeight: '600' }],
                'title-sm': ['16px', { lineHeight: '1.4', fontWeight: '600' }],
                'body-lg': ['16px', { lineHeight: '1.6' }],
                'body-md': ['14px', { lineHeight: '1.5' }],
                'body-sm': ['12px', { lineHeight: '1.5' }],
                'caption': ['11px', { lineHeight: '1.4', fontWeight: '500', letterSpacing: '0.5px' }],
            },
            spacing: {
                '18': '4.5rem',
                '88': '22rem',
                '128': '32rem',
            },
            borderRadius: {
                DEFAULT: '8px',
                'lg': '12px',
                'xl': '16px',
            },
            boxShadow: {
                'card': '0 2px 8px rgba(0, 0, 0, 0.08)',
                'card-hover': '0 4px 16px rgba(0, 0, 0, 0.12)',
                'dropdown': '0 4px 12px rgba(0, 0, 0, 0.15)',
            },
            animation: {
                'fade-in': 'fadeIn 0.2s ease-out',
                'slide-up': 'slideUp 0.3s ease-out',
                'spin-slow': 'spin 1.5s linear infinite',
            },
            keyframes: {
                fadeIn: {
                    '0%': { opacity: '0' },
                    '100%': { opacity: '1' },
                },
                slideUp: {
                    '0%': { opacity: '0', transform: 'translateY(10px)' },
                    '100%': { opacity: '1', transform: 'translateY(0)' },
                },
            },
        },
    },
    plugins: [],
}
