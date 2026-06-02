---
name: Zomato AI
colors:
  surface: '#121414'
  surface-dim: '#121414'
  surface-bright: '#38393a'
  surface-container-lowest: '#0c0f0f'
  surface-container-low: '#1a1c1c'
  surface-container: '#1e2020'
  surface-container-high: '#282a2b'
  surface-container-highest: '#333535'
  on-surface: '#e2e2e2'
  on-surface-variant: '#e4bebc'
  inverse-surface: '#e2e2e2'
  inverse-on-surface: '#2f3131'
  outline: '#ab8987'
  outline-variant: '#5b403f'
  surface-tint: '#ffb3b1'
  primary: '#ffb3b1'
  on-primary: '#680011'
  primary-container: '#ff535a'
  on-primary-container: '#5b000e'
  inverse-primary: '#bb162c'
  secondary: '#c8c6c5'
  on-secondary: '#313030'
  secondary-container: '#4a4949'
  on-secondary-container: '#bab8b7'
  tertiary: '#e9c349'
  on-tertiary: '#3c2f00'
  tertiary-container: '#cca830'
  on-tertiary-container: '#4f3e00'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#ffdad8'
  primary-fixed-dim: '#ffb3b1'
  on-primary-fixed: '#410007'
  on-primary-fixed-variant: '#92001c'
  secondary-fixed: '#e5e2e1'
  secondary-fixed-dim: '#c8c6c5'
  on-secondary-fixed: '#1c1b1b'
  on-secondary-fixed-variant: '#474646'
  tertiary-fixed: '#ffe088'
  tertiary-fixed-dim: '#e9c349'
  on-tertiary-fixed: '#241a00'
  on-tertiary-fixed-variant: '#574500'
  background: '#121414'
  on-background: '#e2e2e2'
  surface-variant: '#333535'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 48px
    fontWeight: '800'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.05em
  caption:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 8px
  container-margin: 24px
  gutter: 16px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 32px
  section-gap: 48px
---

## Brand & Style

The design system is engineered to evoke a sense of "Culinary Excellence meets Artificial Intelligence." It targets food enthusiasts who value speed, curation, and premium aesthetics. The personality is energetic, sophisticated, and forward-looking.

The visual style is a **Hybrid Glassmorphic-Modern** approach. It leverages deep charcoal surfaces to allow vibrant crimson and soft gold accents to "pop," creating a high-contrast, immersive environment. The UI utilizes translucent layers, background blurs, and high-fidelity typography to establish a premium feel that differentiates it from utilitarian delivery apps. The interface should feel like a digital concierge—intuitive, high-end, and responsive.

## Colors

The palette is centered around **Vibrant Crimson** (#E23744), a high-energy red that stimulates appetite and signals brand identity. This is paired with **Deep Charcoal** (#121212) for primary surfaces, creating a cinematic backdrop for food photography.

**Soft Gold** (#D4AF37) is reserved for "Premium" indicators, AI-generated recommendations, and high-tier rewards. The neutral palette consists of cool grays and off-whites to ensure legibility. Glassmorphism is achieved through a specific surface token (`surface_glass`) which uses 60% opacity Deep Charcoal with a 20px backdrop-blur.

## Typography

This design system uses **Plus Jakarta Sans** for headings and interactive labels to provide a soft, rounded, and welcoming modern feel. **Inter** is used for body text and descriptions to ensure maximum legibility and a systematic, utilitarian clarity.

Display and Headline styles use tight letter-spacing to feel impactful and editorial. On mobile devices, the `headline-lg` should scale down to `headline-lg-mobile` to maintain balanced hierarchy within the viewport. All text on dark surfaces should maintain a minimum contrast ratio of 4.5:1, utilizing light gray rather than pure white to reduce eye strain.

## Layout & Spacing

The layout follows a **Fluid Grid** model with a heavy emphasis on vertical rhythm. 
- **Mobile:** 4-column grid with 24px side margins and 16px gutters.
- **Desktop:** 12-column grid with a max-width of 1280px.

Generous whitespace (the "stack-lg" and "section-gap" tokens) is required to separate high-quality food imagery from textual data. AI-powered search results should appear in a staggered or masonry-inspired layout to emphasize discovery. Padding within cards must remain consistent at 20px to accommodate the rounded-2xl corners.

## Elevation & Depth

Visual hierarchy is achieved through **Tonal Stacking** and **Glassmorphism**.
1. **Level 0 (Base):** Deep Charcoal (#121212) background.
2. **Level 1 (Cards):** Surface at 5% lighter than base or the `surface_glass` token for floating elements.
3. **Level 2 (Active/Hover):** Cards "lift" using an ambient shadow: `0 20px 40px rgba(0,0,0,0.4)` and a 1px inner stroke of 10% white to define edges.

Overlays, such as AI chat sheets or filter menus, must use the glassmorphic blur to maintain context of the underlying restaurant feed.

## Shapes

The design system utilizes a **Rounded (2xl)** shape language.
- **Primary Cards:** 24px radius (rounded-xl/2xl) to create a friendly, organic feel for food-related content.
- **Buttons:** 12px radius for a sophisticated, slightly more structured look compared to the cards.
- **Inputs:** 16px radius to match the card aesthetic while maintaining functional clarity.

Avatars for restaurant logos and user profiles should be perfect circles to contrast against the broad, rounded rectangles of the UI.

## Components

### Buttons
- **Primary:** Vibrant Crimson background, white text. On hover, the button scales slightly (1.02x) and increases shadow intensity.
- **Secondary:** Soft Gold border with transparent background, gold text. Used for "Premium" or "Loyalty" actions.

### AI Search Cards
- Must use the `surface_glass` effect.
- Features a subtle pulsing crimson border when the AI is "thinking" or processing a query.
- Imagery within cards should have a subtle zoom-in transition on hover.

### Inputs & Selectors
- Backgrounds use a dark-tinted gray with a 1px stroke. 
- On focus, the stroke changes to Vibrant Crimson with a 4px soft outer glow.

### Skeleton Loaders
- Instead of static gray, use a shimmering gradient that moves from Deep Charcoal to a slightly lighter gray, mimicking a soft "pulse" of energy.

### Chips/Tags
- Small, pill-shaped elements for cuisine types.
- Background: 10% white opacity. Text: 80% white opacity.