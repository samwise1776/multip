<p align="center">
  <img src="assets/multip.svg" width="120" alt="Multip Logo">
</p>

<h1 align="center">Multip</h1>

<p align="center">
  <strong>A unified programming language for building apps, websites, and backends</strong>
</p>

<p align="center">
  <a href="#quick-start">Quick Start</a> •
  <a href="#features">Features</a> •
  <a href="#examples">Examples</a> •
  <a href="#installation">Installation</a> •
  <a href="https://github.com/samwise1776/multip">GitHub</a>
</p>

---

### Hello World

```multip
page Home

window {
    title = "My First App"

    column {
        padding = 40

        heading {
            text = "Hello, World!"
            size = 36
            color = #7C3AED
        }

        text {
            value = "Welcome to Multip"
            size = 18
        }

        button {
            text = "Click Me"

            on click {
                print("Hello from Multip!")
            }
        }
    }
}
```

### Variables & Functions

```multip
const appName = "Multip Calculator"
var result = 0

function add(a, b) {
    return a + b
}

function factorial(n) {
    if n <= 1 {
        return 1
    }
    return n * factorial(n - 1)
}

page Calculator

window {
    title = appName

    column {
        padding = 30

        heading {
            text = "Calculator"
            size = 32
            color = #7C3AED
        }

        text {
            value = "Result: " + result
            size = 24
        }

        row {
            button {
                text = "10 + 5"

                on click {
                    result = add(10, 5)
                    print("10 + 5 = " + result)
                }
            }

            button {
                text = "5! = 120"

                on click {
                    result = factorial(5)
                    print("5! = " + result)
                }
            }
        }
    }
}
```

### Components

```multip
component Card(title, body) {
    column {
        heading {
            text = title
            size = 24
        }

        paragraph {
            text = body
            size = 16
        }
    }
}

page Home

window {
    title = "Components"

    column {
        padding = 30

        Card("Welcome", "Build reusable UI components")
        Card("Learn", "Components support props and children")
        Card("Create", "Build complex apps with simple components")
    }
}
```

### Server & API

```multip
server API {
    port = 8080

    route "/api/users" {
        method = "GET"

        on request {
            var users = [
                { id: 1, name: "Alice" },
                { id: 2, name: "Bob" }
            ]
            response.json(users)
        }
    }

    route "/api/health" {
        method = "GET"

        on request {
            response.json({ status: "ok" })
        }
    }
}
```

### Browser UI

```multip
page Browser

window {
    title = "Multip Browser"

    column {
        toolbar {
            backButton()
            forwardButton()
            refreshButton()
            homeButton()

            urlbox {
                placeholder = "Enter URL..."
            }

            goButton {
                text = "Go"
            }
        }

        browserView {
            url = "multip://home"
        }
    }
}
```

---

## Quick Start

```bash
# Install
git clone https://github.com/samwise1776/multip.git
cd multip

# Run a file
./multip run examples/hello.multip

# Open browser
./multip browser examples/hello.multip

# Compile and show AST
./multip compile examples/hello.multip

# Create new project
./multip new my-app
```

## Features

| Feature | Description |
|---------|-------------|
| **Unified Language** | HTML, CSS, JS, and backend in one file |
| **Browser Engine** | Built-in browser with tabs and navigation |
| **Components** | Reusable UI components with props |
| **Server** | Built-in HTTP server with routing |
| **Database** | Built-in database operations |
| **Animations** | Declarative animation system |
| **Package Manager** | Install and publish packages |
| **VS Code Extension** | Syntax highlighting and IntelliSense |
| **Standard Library** | Math, strings, files, HTTP, JSON, encryption |

## Installation

### VS Code Extension

```bash
code --install-extension vscode-extension/multip-1.0.0.vsix
```

### CLI Tool

```bash
# Add to PATH
export PATH=$PATH:$(pwd)

# Or symlink
ln -s $(pwd)/multip ~/.local/bin/multip
```

## Examples

| Example | Description |
|---------|-------------|
| `hello.multip` | Basic hello world |
| `calculator.multip` | Calculator with functions |
| `browser.multip` | Browser UI demo |
| `server.multip` | REST API server |
| `advanced.multip` | Components and animations |
| `variables.multip` | Variables and control flow |

## CLI Commands

```bash
multip run <file.multip>           # Run a file
multip browser [file.multip]       # Open in browser
multip compile <file.multip>       # Show tokens and AST
multip new <project>               # Create new project
multip build                       # Build project
multip pkg install <name>          # Install package
multip publish <file.multip> <url> # Publish to URL
multip format <file.multip>        # Format file
multip test <file.multip>          # Run tests
multip docs <file.multip>          # Generate docs
```

## Language Syntax

### Keywords
`page` `window` `column` `row` `text` `heading` `button` `component` `route` `function` `var` `const` `if` `else` `for` `while` `return` `import` `server` `database` `animate` `on` `click`

### Types
String `"hello"` • Number `42` • Boolean `true` • Color `#7C3AED` • Array `[1, 2, 3]` • Map `{ key: value }`

### Operators
`+` `-` `*` `/` `%` `==` `!=` `<` `>` `<=` `>=` `&&` `||` `=` `+=` `-=` `->` `=>`

---

<p align="center">
  Built with Java Swing • MIT License
</p>
