import { ref, watchEffect } from 'vue'

const THEME_KEY = 'smart-campus-theme'
const isDark = ref(false)

export function useTheme() {
  const initTheme = () => {
    const saved = localStorage.getItem(THEME_KEY)
    if (saved !== null) {
      isDark.value = saved === 'dark'
    } else {
      isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
    }
  }

  const applyTheme = () => {
    document.documentElement.classList.toggle('dark', isDark.value)
  }

  const toggleTheme = () => {
    isDark.value = !isDark.value
    localStorage.setItem(THEME_KEY, isDark.value ? 'dark' : 'light')
  }

  initTheme()

  watchEffect(() => {
    applyTheme()
  })

  return { isDark, toggleTheme }
}