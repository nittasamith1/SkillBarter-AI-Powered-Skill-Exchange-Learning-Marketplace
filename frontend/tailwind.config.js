/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          950: '#0B1220', // Deep Navy
          900: '#111827',
          800: '#1E293B',
        },
        teal: {
          500: '#14B8A6', // Primary Teal
          400: '#2DD4BF',
          300: '#5EEAD4', // Mint Accent
          600: '#0F9F91', // Hover Teal
        },
        surface: {
          50: '#F8FAFC',  // App Background
          100: '#F1F5F9', // Secondary Surface
          200: '#E2E8F0', // Border
        },
        slateText: {
          900: '#0F172A', // Primary Text
          600: '#475569',
          500: '#64748B', // Secondary Text
          400: '#94A3B8', // Muted Text
        },
        aiBlue: '#3B82F6',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
      },
      borderRadius: {
        'btn': '8px',
        'input': '8px',
        'card': '12px',
        'panel': '16px',
      },
    },
  },
  plugins: [],
}
