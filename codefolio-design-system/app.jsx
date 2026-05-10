// Entrypoint — wires sections, theme persistence, and a tiny Tweaks panel for accent + density.
const { useState, useEffect } = React;

const TWEAK_DEFAULTS = /*EDITMODE-BEGIN*/{
  "accent": "terracotta",
  "density": "regular"
}/*EDITMODE-END*/;

const ACCENT_PRESETS = {
  terracotta: { light: "oklch(0.62 0.13 50)",  dark: "oklch(0.74 0.13 60)"  },
  forest:     { light: "oklch(0.50 0.10 150)", dark: "oklch(0.72 0.13 145)" },
  plum:       { light: "oklch(0.45 0.12 320)", dark: "oklch(0.74 0.13 330)" },
  ink:        { light: "oklch(0.32 0.04 250)", dark: "oklch(0.85 0.06 250)" }
};

function App() {
  const [dark, setDark] = useState(() => document.documentElement.classList.contains("dark"));
  const useT = window.useTweaks || ((d) => [d, () => {}]);
  const [tweaks, setTweak] = useT(TWEAK_DEFAULTS);

  useEffect(() => {
    document.documentElement.classList.toggle("dark", dark);
  }, [dark]);

  useEffect(() => {
    const preset = ACCENT_PRESETS[tweaks.accent] || ACCENT_PRESETS.terracotta;
    const value = dark ? preset.dark : preset.light;
    document.documentElement.style.setProperty("--accent", value);
    document.documentElement.style.setProperty("--accent-soft", value.replace(/\)\s*$/, " / 0.14)"));
  }, [tweaks.accent, dark]);

  useEffect(() => {
    const w = tweaks.density === "compact" ? "1080px" : tweaks.density === "wide" ? "1280px" : "1180px";
    document.documentElement.style.setProperty("--maxw", w);
  }, [tweaks.density]);

  const TweaksPanel  = window.TweaksPanel;
  const TweakSection = window.TweakSection;
  const TweakRadio   = window.TweakRadio;
  const TweakSelect  = window.TweakSelect;
  const TweakToggle  = window.TweakToggle;

  return (
    <React.Fragment>
      <Header dark={dark} setDark={setDark} />
      <main>
        <Hero />
        <SelectedWork />
        <About />
        <Experience />
        <Projects />
        <Cortex />
      </main>
      <Footer />

      {TweaksPanel && (
        <TweaksPanel>
          <TweakSection label="Accent" />
          <TweakSelect
            label="Color"
            value={tweaks.accent}
            options={[
              { value: "terracotta", label: "Terracotta" },
              { value: "forest", label: "Forest" },
              { value: "plum", label: "Plum" },
              { value: "ink", label: "Ink (mono)" }
            ]}
            onChange={(v) => setTweak("accent", v)}
          />
          <TweakSection label="Layout" />
          <TweakRadio
            label="Density"
            value={tweaks.density}
            options={["compact", "regular", "wide"]}
            onChange={(v) => setTweak("density", v)}
          />
          <TweakSection label="Theme" />
          <TweakToggle
            label="Dark mode"
            value={dark}
            onChange={(v) => { setDark(v); try { localStorage.setItem("theme-v2", v ? "dark" : "light"); } catch (_) {} }}
          />
        </TweaksPanel>
      )}
    </React.Fragment>
  );
}

ReactDOM.createRoot(document.getElementById("root")).render(<App />);
