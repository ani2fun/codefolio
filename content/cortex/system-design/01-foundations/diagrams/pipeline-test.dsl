workspace "System Design Track — Diagram Pipeline" "Proof-of-pipeline C4 model. Renders to pipeline-test.svg via `make diagrams`." {

    model {
        author = person "Author" "Writes a lesson in Markdown."

        notebook = softwareSystem "note-book mdBook" "The published System Design track." {
            mdbook        = container "mdbook"        "Static-site generator that renders the book." "Rust"
            d2pp          = container "mdbook-d2"     "Preprocessor that renders inline d2 fences."   "Rust"
            mermaidJs     = container "mermaid-init.js" "Client-side script that renders mermaid blocks." "JavaScript"
            structurizrCli = container "Structurizr CLI" "Renders DSL workspaces to SVG (run via Docker by `make diagrams`)." "Java / Docker"
            html          = container "HTML output"   "What the reader sees in their browser."        "HTML / CSS / JS"
        }

        author        -> mdbook        "writes lesson.md"
        mdbook        -> d2pp          "delegates `d2` fences to"
        mdbook        -> mermaidJs     "embeds the mermaid script into"
        mdbook        -> structurizrCli "embeds pre-rendered SVG produced by"
        d2pp          -> html          "outputs SVG into"
        mermaidJs     -> html          "renders mermaid into"
        structurizrCli -> html         "ships SVG that goes into"
    }

    views {
        systemContext notebook "PipelineContext" {
            include *
            autolayout lr
        }

        container notebook "PipelineContainers" {
            include *
            autolayout lr
        }

        styles {
            element "Person" {
                background "#dbeafe"
                color      "#1e3a5f"
                shape      Person
            }
            element "Software System" {
                background "#ede9fe"
                color      "#1e3a5f"
            }
            element "Container" {
                background "#fef9c3"
                color      "#1e3a5f"
            }
        }
    }
}
