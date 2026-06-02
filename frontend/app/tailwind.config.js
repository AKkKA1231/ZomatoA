/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: "class",
  theme: {
    extend: {
      "colors": {
        "tertiary": "#e9c349",
        "on-tertiary-container": "#4f3e00",
        "surface-tint": "#ffb3b1",
        "secondary-fixed-dim": "#c8c6c5",
        "surface-bright": "#38393a",
        "primary-container": "#ff535a",
        "on-secondary": "#313030",
        "surface-container-highest": "#333535",
        "secondary-container": "#4a4949",
        "tertiary-container": "#cca830",
        "secondary-fixed": "#e5e2e1",
        "tertiary-fixed": "#ffe088",
        "on-error": "#690005",
        "primary-fixed-dim": "#ffb3b1",
        "on-tertiary-fixed-variant": "#574500",
        "on-secondary-fixed": "#1c1b1b",
        "surface-container-lowest": "#0c0f0f",
        "secondary": "#c8c6c5",
        "on-secondary-fixed-variant": "#474646",
        "error-container": "#93000a",
        "on-primary": "#680011",
        "inverse-surface": "#e2e2e2",
        "surface-dim": "#121414",
        "on-error-container": "#ffdad6",
        "on-tertiary": "#3c2f00",
        "surface-container": "#1e2020",
        "outline": "#ab8987",
        "on-primary-fixed": "#410007",
        "on-primary-container": "#5b000e",
        "outline-variant": "#5b403f",
        "surface": "#121414",
        "surface-container-low": "#1a1c1c",
        "primary": "#ffb3b1",
        "error": "#ffb4ab",
        "inverse-primary": "#bb162c",
        "background": "#121414",
        "primary-fixed": "#ffdad8",
        "inverse-on-surface": "#2f3131",
        "surface-variant": "#333535",
        "surface-container-high": "#282a2b",
        "on-secondary-container": "#bab8b7",
        "on-surface": "#e2e2e2",
        "on-primary-fixed-variant": "#92001c",
        "tertiary-fixed-dim": "#e9c349",
        "on-tertiary-fixed": "#241a00",
        "on-background": "#e2e2e2",
        "on-surface-variant": "#e4bebc"
      },
      "borderRadius": {
        "DEFAULT": "0.25rem",
        "lg": "0.5rem",
        "xl": "0.75rem",
        "full": "9999px"
      },
      "spacing": {
        "stack-sm": "8px",
        "container-margin": "24px",
        "stack-lg": "32px",
        "unit": "8px",
        "gutter": "16px",
        "stack-md": "16px",
        "section-gap": "48px"
      },
      "fontFamily": {
        "label-md": ["Plus Jakarta Sans", "sans-serif"],
        "caption": ["Inter", "sans-serif"],
        "body-lg": ["Inter", "sans-serif"],
        "headline-lg": ["Plus Jakarta Sans", "sans-serif"],
        "headline-md": ["Plus Jakarta Sans", "sans-serif"],
        "display-lg": ["Plus Jakarta Sans", "sans-serif"],
        "headline-lg-mobile": ["Plus Jakarta Sans", "sans-serif"],
        "body-md": ["Inter", "sans-serif"]
      },
      "fontSize": {
        "label-md": [
          "14px",
          {
            "lineHeight": "20px",
            "letterSpacing": "0.05em",
            "fontWeight": "600"
          }
        ],
        "caption": [
          "12px",
          {
            "lineHeight": "16px",
            "fontWeight": "500"
          }
        ],
        "body-lg": [
          "18px",
          {
            "lineHeight": "28px",
            "fontWeight": "400"
          }
        ],
        "headline-lg": [
          "32px",
          {
            "lineHeight": "40px",
            "letterSpacing": "-0.01em",
            "fontWeight": "700"
          }
        ],
        "headline-md": [
          "24px",
          {
            "lineHeight": "32px",
            "fontWeight": "600"
          }
        ],
        "display-lg": [
          "48px",
          {
            "lineHeight": "56px",
            "letterSpacing": "-0.02em",
            "fontWeight": "800"
          }
        ],
        "headline-lg-mobile": [
          "28px",
          {
            "lineHeight": "36px",
            "fontWeight": "700"
          }
        ],
        "body-md": [
          "16px",
          {
            "lineHeight": "24px",
            "fontWeight": "400"
          }
        ]
      }
    },
  },
  plugins: [],
}
