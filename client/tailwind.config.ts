import type { Config } from "tailwindcss"

// Tailwind 3.4 config ported from portfolio-app. The `content` paths cover:
//   - the HTML entry and any TS bridges
//   - Scala source files (Tailwind scans them for class-name string literals)
//   - the Scala.js linker output (the emitted JS contains the same class
//     strings that scalajs-react renders into the DOM)
const config = {
  darkMode: ["class"],
  content: [
    "./index.html",
    "./src/**/*.{ts,tsx,js,mjs,html}",
    "./src/main/scala/**/*.scala",
    // Scala.js fastLinkJS / fullLinkJS emit JS files under client/target/.
    "../client/target/scala-*/codefolio-client-*/**/*.js",
  ],
  prefix: "",
  theme: {
    container: {
      center: true,
      padding: {
        "sm_x": "1.5rem",
        "sm_y": "3rem",
        "x":    "5rem",
        "y":    "5rem",
        "md_x": "7rem",
        "md_y": "13rem"
      },
      screens: {
        "xss":  "375px",
        "xs":   "475px",
        "sm":   "640px",
        "md":   "768px",
        "lg":   "1024px",
        "xl":   "1280px",
        "2xl":  "1536px",
      },
    },
    extend: {
      colors: {
        border:      "hsl(var(--border))",
        input:       "hsl(var(--input))",
        ring:        "hsl(var(--ring))",
        background:  "hsl(var(--background))",
        foreground:  "hsl(var(--foreground))",
        primary: {
          DEFAULT:    "hsl(var(--primary))",
          foreground: "hsl(var(--primary-foreground))",
        },
        secondary: {
          DEFAULT:    "hsl(var(--secondary))",
          foreground: "hsl(var(--secondary-foreground))",
        },
        destructive: {
          DEFAULT:    "hsl(var(--destructive))",
          foreground: "hsl(var(--destructive-foreground))",
        },
        muted: {
          DEFAULT:    "hsl(var(--muted))",
          foreground: "hsl(var(--muted-foreground))",
        },
        accent: {
          DEFAULT:    "hsl(var(--accent))",
          foreground: "hsl(var(--accent-foreground))",
        },
        popover: {
          DEFAULT:    "hsl(var(--popover))",
          foreground: "hsl(var(--popover-foreground))",
        },
        card: {
          DEFAULT:    "hsl(var(--card))",
          foreground: "hsl(var(--card-foreground))",
        },
      },
      borderRadius: {
        lg: "var(--radius)",
        md: "calc(var(--radius) - 2px)",
        sm: "calc(var(--radius) - 4px)",
      },
      keyframes: {
        "accordion-down": {
          from: { height: "0" },
          to:   { height: "var(--radix-accordion-content-height)" },
        },
        "accordion-up": {
          from: { height: "var(--radix-accordion-content-height)" },
          to:   { height: "0" },
        },
      },
      animation: {
        "accordion-down": "accordion-down 0.2s ease-out",
        "accordion-up":   "accordion-up 0.2s ease-out",
      },
    },
  },
  plugins: [require("tailwindcss-animate"), require("@tailwindcss/typography")],
} satisfies Config

export default config
