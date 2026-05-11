# Makefile — system-design diagram pipeline for codefolio
#
# codefolio is built by sbt + Vite (see build.sbt and client/vite.config.mjs).
# This Makefile only adds a `diagrams` target that renders Structurizr DSL
# workspaces under content/cortex/system-design/ into committed SVGs that the
# Cortex viewer embeds via <img src="./diagrams/..."> references.
#
# Why pre-render the SVGs? Cortex renders Mermaid and D2 client-side, but it
# has no Structurizr renderer. Pre-rendering keeps the runtime dependency-free
# and the SVGs reviewable in PRs.

SHELL := /bin/bash

# Every Structurizr workspace under the system-design track.
DSL_FILES := $(shell find content/cortex/system-design -name '*.dsl' 2>/dev/null)

.PHONY: help diagrams diagrams-local diagrams-docker diagrams-compose clean-diagrams rebuild-diagrams

help: ## List available targets
	@echo "Available targets:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-20s %s\n", $$1, $$2}'

# -----------------------------------------------------------------------------
# Diagram pipeline
# -----------------------------------------------------------------------------
#
# Rendering Structurizr DSL → SVG is two steps:
#   1. structurizr-cli export -format plantuml/c4plantuml  (DSL → .puml)
#   2. plantuml -tsvg                                       (.puml → .svg)
#
# We support two paths:
#   * `make diagrams-local`  — uses locally-installed `structurizr-cli` and
#                              `plantuml` (recommended on a dev box, fast)
#   * `make diagrams-docker` — uses Docker images directly (no local install)
#   * `make diagrams-compose`— uses docker-compose.diagrams.yml (canonical path)
#
# `make diagrams` picks whichever is available, preferring local CLIs.

diagrams: ## Render all Structurizr DSL workspaces to SVG (auto-detects local vs docker)
	@if command -v structurizr-cli >/dev/null && command -v plantuml >/dev/null; then \
		$(MAKE) diagrams-local; \
	elif command -v docker >/dev/null && docker info >/dev/null 2>&1; then \
		$(MAKE) diagrams-compose; \
	else \
		echo "ERROR: need either local (structurizr-cli + plantuml) or a running Docker daemon" >&2; \
		exit 1; \
	fi

diagrams-compose: ## Render diagrams via docker compose (the canonical Docker path)
	docker compose -f docker-compose.diagrams.yml --profile render run --rm diagrams

diagrams-local: $(DSL_FILES) ## Render diagrams using locally-installed CLIs
	@for dsl in $(DSL_FILES); do \
		dir=$$(dirname "$$dsl"); \
		echo "→ rendering $$dsl"; \
		structurizr-cli export -workspace "$$dsl" -format plantuml/c4plantuml -output "$$dir" >/dev/null; \
		plantuml -tsvg "$$dir"/*.puml >/dev/null; \
	done
	@echo "✓ all diagrams rendered"

diagrams-docker: $(DSL_FILES) ## Render diagrams using Docker (structurizr/cli + plantuml/plantuml)
	@for dsl in $(DSL_FILES); do \
		dir=$$(dirname "$$dsl"); \
		dsl_name=$$(basename "$$dsl"); \
		echo "→ rendering $$dsl (docker)"; \
		docker run --rm -v "$$PWD/$$dir:/usr/local/structurizr" structurizr/cli \
			export -workspace "$$dsl_name" -format plantuml/c4plantuml -output . >/dev/null; \
		docker run --rm -v "$$PWD/$$dir:/work" -w /work plantuml/plantuml \
			-tsvg "*.puml" >/dev/null; \
	done
	@echo "✓ all diagrams rendered"

clean-diagrams: ## Remove .puml intermediates only (keeps .dsl source and .svg outputs)
	@find content/cortex/system-design -name '*.puml' -delete
	@echo "✓ cleaned .puml intermediates"

rebuild-diagrams: ## Force a full rebuild: removes .puml + .svg, then regenerates from .dsl
	@find content/cortex/system-design -name '*.puml' -delete
	@find content/cortex/system-design -name 'structurizr-*.svg' -delete
	@echo "✓ cleaned .puml + .svg; regenerating..."
	@$(MAKE) diagrams
