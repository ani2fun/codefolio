// Side-effect import: Tailwind base styles.
import "./tailwind.css";

// Side-effect import: d3-transition patches `selection.prototype.transition`
// and `.interrupt` when its top-level module body runs — without this, any
// `D3.select(...).transition().attr(...)` chain inside the Scala.js widgets
// silently no-ops (the element's `__transition__` scheduler slot stays
// undefined). It MUST live here, not in the Scala.js facade: d3's barrel
// re-exports d3-transition via `export *`, but d3-selection has
// `"sideEffects": false` so the bundler tree-shakes the path that would
// touch d3-transition's index.js; and a Scala.js `@JSImport(_, Namespace)`
// reference inside `D3.scala` gets DCE'd by the JS linker because the val
// is private and never read. A bare ESM import here is the one form Vite
// guarantees to preserve.
import "d3-transition";

// Side-effect import: triggers the Scala.js MainModuleInitializer
// (codefolio.client.Main.main), which mounts the React tree.
import "scalajs:main.js";
