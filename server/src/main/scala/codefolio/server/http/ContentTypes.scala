package codefolio.server.http

/**
 * Maps a filename extension to a Content-Type header value.
 *
 * The browser refuses to load `.js` ESM modules with the wrong type ("Failed to fetch dynamically imported
 * module"), and `woff`/`woff2` trigger CORS warnings without a `font/...` type, so the table covers
 * everything [[StaticRoutes]] serves — the Vite bundle plus the `img/` and `certificates/` trees. An unknown
 * extension falls back to `application/octet-stream`.
 */
object ContentTypes:

  def forName(name: String): String =
    val lower = name.toLowerCase
    if lower.endsWith(".js") || lower.endsWith(".mjs") then "application/javascript; charset=utf-8"
    else if lower.endsWith(".css") then "text/css; charset=utf-8"
    else if lower.endsWith(".html") || lower.endsWith(".htm") then "text/html; charset=utf-8"
    else if lower.endsWith(".json") || lower.endsWith(".map") then "application/json; charset=utf-8"
    else if lower.endsWith(".svg") then "image/svg+xml"
    else if lower.endsWith(".png") then "image/png"
    else if lower.endsWith(".jpg") || lower.endsWith(".jpeg") then "image/jpeg"
    else if lower.endsWith(".webp") then "image/webp"
    else if lower.endsWith(".gif") then "image/gif"
    else if lower.endsWith(".ico") then "image/x-icon"
    else if lower.endsWith(".woff2") then "font/woff2"
    else if lower.endsWith(".woff") then "font/woff"
    else if lower.endsWith(".ttf") then "font/ttf"
    else if lower.endsWith(".otf") then "font/otf"
    else if lower.endsWith(".pdf") then "application/pdf"
    else if lower.endsWith(".wasm") then "application/wasm"
    else if lower.endsWith(".txt") then "text/plain; charset=utf-8"
    else "application/octet-stream"
