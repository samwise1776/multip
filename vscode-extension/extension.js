const vscode = require('vscode');
const { exec } = require('child_process');
const path = require('path');
const fs = require('fs');

class MultipExtension {
    constructor(context) {
        this.context = context;
        this.diagnosticCollection = vscode.languages.createDiagnosticCollection('multip');
        this.outputChannel = vscode.window.createOutputChannel('Multip');
        this.statusBarItem = vscode.window.createStatusBarItem(vscode.StatusBarAlignment.Left, 100);
        this.statusBarItem.text = '$(zap) Multip';
        this.statusBarItem.tooltip = 'Multip Language';
        this.statusBarItem.command = 'multip.showMenu';
        this.statusBarItem.show();
    }

    registerCommands() {
        this.context.subscriptions.push(
            vscode.commands.registerCommand('multip.newProject', this.newProject, this),
            vscode.commands.registerCommand('multip.build', this.build, this),
            vscode.commands.registerCommand('multip.run', this.run, this),
            vscode.commands.registerCommand('multip.compile', this.compile, this),
            vscode.commands.registerCommand('multip.format', this.format, this),
            vscode.commands.registerCommand('multip.browser', this.browser, this),
            vscode.commands.registerCommand('multip.publish', this.publish, this),
            vscode.commands.registerCommand('multip.test', this.test, this),
            vscode.commands.registerCommand('multip.showMenu', this.showMenu, this),
            vscode.commands.registerCommand('multip.openDocs', this.openDocs, this)
        );
    }

    registerProviders() {
        this.context.subscriptions.push(
            vscode.languages.registerHoverProvider('multip', {
                provideHover(document, position) {
                    const word = document.getWordRangeAtPosition(position);
                    if (!word) return null;
                    const text = document.getText(word);
                    const hovers = {
                        'page': 'Defines a page component\n\n```multip\npage Home\n```',
                        'window': 'Defines a window container\n\n```multip\nwindow { title = "My App" }\n```',
                        'route': 'Defines a URL route\n\n```multip\nroute "/" { Home() }\n```',
                        'component': 'Creates a reusable component\n\n```multip\ncomponent Card(title) { ... }\n```',
                        'column': 'Vertical layout container',
                        'row': 'Horizontal layout container',
                        'button': 'Clickable button element',
                        'text': 'Text display element',
                        'heading': 'Heading text element',
                        'const': 'Declares an immutable variable',
                        'var': 'Declares a mutable variable',
                        'function': 'Defines a function',
                        'return': 'Returns a value from a function',
                        'if': 'Conditional statement',
                        'else': 'Alternative branch',
                        'for': 'Loop iteration',
                        'while': 'While loop',
                        'in': 'Used in for-in loops',
                        'print': 'Prints output to console\n\n```multip\nprint("Hello")\n```',
                        'import': 'Imports a module\n\n```multip\nimport math\n```',
                        'fetch': 'Makes an HTTP request',
                        'server': 'Defines a server',
                        'database': 'Defines a database connection',
                        'animate': 'Defines an animation',
                        'async': 'Marks a function as asynchronous',
                        'await': 'Waits for an async operation',
                        'on': 'Event handler',
                        'click': 'Click event'
                    };
                    if (hovers[text]) {
                        return new vscode.Hover(hovers[text]);
                    }
                    return null;
                }
            })
        );

        this.context.subscriptions.push(
            vscode.languages.registerCompletionItemProvider('multip', {
                provideCompletionItems(document, position) {
                    const items = [];
                    const keywords = [
                        { label: 'page', kind: vscode.CompletionItemKind.Keyword, detail: 'page Name' },
                        { label: 'window', kind: vscode.CompletionItemKind.Keyword, detail: 'window { ... }' },
                        { label: 'route', kind: vscode.CompletionItemKind.Keyword, detail: 'route "/path" { ... }' },
                        { label: 'component', kind: vscode.CompletionItemKind.Keyword, detail: 'component Name(args) { ... }' },
                        { label: 'column', kind: vscode.CompletionItemKind.Keyword, detail: 'column { ... }' },
                        { label: 'row', kind: vscode.CompletionItemKind.Keyword, detail: 'row { ... }' },
                        { label: 'button', kind: vscode.CompletionItemKind.Keyword, detail: 'button { text = "..." }' },
                        { label: 'text', kind: vscode.CompletionItemKind.Keyword, detail: 'text { value = "..." }' },
                        { label: 'heading', kind: vscode.CompletionItemKind.Keyword, detail: 'heading { text = "..." }' },
                        { label: 'const', kind: vscode.CompletionItemKind.Keyword, detail: 'const name = value' },
                        { label: 'var', kind: vscode.CompletionItemKind.Keyword, detail: 'var name = value' },
                        { label: 'function', kind: vscode.CompletionItemKind.Keyword, detail: 'function name() { ... }' },
                        { label: 'return', kind: vscode.CompletionItemKind.Keyword, detail: 'return value' },
                        { label: 'if', kind: vscode.CompletionItemKind.Keyword, detail: 'if condition { ... }' },
                        { label: 'else', kind: vscode.CompletionItemKind.Keyword, detail: 'else { ... }' },
                        { label: 'for', kind: vscode.CompletionItemKind.Keyword, detail: 'for item in list { ... }' },
                        { label: 'while', kind: vscode.CompletionItemKind.Keyword, detail: 'while condition { ... }' },
                        { label: 'print', kind: vscode.CompletionItemKind.Function, detail: 'print("...")' },
                        { label: 'import', kind: vscode.CompletionItemKind.Keyword, detail: 'import module' },
                        { label: 'fetch', kind: vscode.CompletionItemKind.Function, detail: 'fetch(url)' },
                        { label: 'server', kind: vscode.CompletionItemKind.Keyword, detail: 'server Name { ... }' },
                        { label: 'database', kind: vscode.CompletionItemKind.Keyword, detail: 'database Name' },
                        { label: 'animate', kind: vscode.CompletionItemKind.Keyword, detail: 'animate element { ... }' },
                        { label: 'on', kind: vscode.CompletionItemKind.Keyword, detail: 'on event { ... }' },
                        { label: 'async', kind: vscode.CompletionItemKind.Keyword, detail: 'async function' },
                        { label: 'await', kind: vscode.CompletionItemKind.Keyword, detail: 'await expression' }
                    ];

                    const snippets = [
                        { label: 'newpage', kind: vscode.CompletionItemKind.Snippet, insertText: 'page ${1:Name}\n\nwindow {\n\ttitle = "${2:Title}"\n\n\tcolumn {\n\t\t${3}\n\t}\n}', detail: 'New page template' },
                        { label: 'newcomponent', kind: vscode.CompletionItemKind.Snippet, insertText: 'component ${1:Name}(${2:args}) {\n\t${3}\n}', detail: 'New component template' },
                        { label: 'newroute', kind: vscode.CompletionItemKind.Snippet, insertText: 'route "${1:/}" {\n\t${2:Component}()\n}', detail: 'New route template' },
                        { label: 'newbutton', kind: vscode.CompletionItemKind.Snippet, insertText: 'button {\n\ttext = "${1:Click me}"\n\n\ton click {\n\t\t${2}\n\t}\n}', detail: 'New button with click handler' },
                        { label: 'newserver', kind: vscode.CompletionItemKind.Snippet, insertText: 'server ${1:API} {\n\tport = ${2:8080}\n\n\troute "${3:/api}" {\n\t\ton request {\n\t\t\t${4}\n\t\t}\n\t}\n}', detail: 'New server template' },
                        { label: 'newdb', kind: vscode.CompletionItemKind.Snippet, insertText: 'database ${1:Name}', detail: 'New database declaration' }
                    ];

                    keywords.forEach(k => {
                        const item = new vscode.CompletionItem(k.label, k.kind);
                        item.detail = k.detail;
                        item.insertText = k.label + ' ';
                        items.push(item);
                    });

                    snippets.forEach(s => {
                        const item = new vscode.CompletionItem(s.label, s.kind);
                        item.detail = s.detail;
                        item.insertText = new vscode.SnippetString(s.insertText);
                        items.push(item);
                    });

                    return items;
                }
            }, ' ', '{', '\n')
        );
    }

    registerEvents() {
        this.context.subscriptions.push(
            vscode.workspace.onDidOpenTextDocument((doc) => {
                if (doc.fileName.endsWith('.multip')) {
                    vscode.languages.setTextDocumentLanguage(doc, 'multip');
                }
            })
        );

        this.context.subscriptions.push(
            vscode.workspace.onDidSaveTextDocument((doc) => {
                if (doc.fileName.endsWith('.multip')) {
                    this.lintDocument(doc);
                }
            })
        );

        this.context.subscriptions.push(
            vscode.workspace.onDidChangeTextDocument((e) => {
                if (e.document.fileName.endsWith('.multip')) {
                    this.lintDocument(e.document);
                }
            })
        );
    }

    newProject() {
        const workspaceFolder = vscode.workspace.workspaceFolders?.[0];
        if (!workspaceFolder) {
            vscode.window.showErrorMessage('No workspace folder open');
            return;
        }
        vscode.window.showInputBox({ prompt: 'Project name', placeHolder: 'my-app' }).then(name => {
            if (name) {
                const cmd = `multip new ${name}`;
                exec(cmd, { cwd: workspaceFolder.uri.fsPath }, (err, stdout) => {
                    if (err) vscode.window.showErrorMessage(`Error: ${err.message}`);
                    else vscode.window.showInformationMessage(`Project "${name}" created`);
                });
            }
        });
    }

    build() {
        this.runMultipCommand('build', 'Building project...', 'Project built');
    }

    run() {
        this.runMultipCommand('run', 'Running project...', 'Project running');
    }

    compile() {
        const editor = vscode.window.activeTextEditor;
        if (editor && editor.document.fileName.endsWith('.multip')) {
            const file = editor.document.fileName;
            exec(`multip compile ${file}`, (err, stdout, stderr) => {
                if (err) {
                    vscode.window.showErrorMessage(`Compile error: ${err.message}`);
                } else {
                    const panel = vscode.window.createWebviewPanel(
                        'multipCompile', 'Multip Compile Output',
                        vscode.ViewColumn.Beside, {}
                    );
                    panel.webview.html = `<html><body style="background:#1e1e1e;color:#d4d4d4;font-family:monospace;padding:20px;"><pre>${stdout}</pre></body></html>`;
                }
            });
        } else {
            vscode.window.showErrorMessage('Open a .multip file first');
        }
    }

    format() {
        const editor = vscode.window.activeTextEditor;
        if (editor && editor.document.fileName.endsWith('.multip')) {
            const file = editor.document.fileName;
            exec(`multip format ${file}`, (err) => {
                if (err) vscode.window.showErrorMessage(`Format error: ${err.message}`);
                else vscode.window.showInformationMessage('Document formatted');
            });
        }
    }

    browser() {
        exec('multip browser', (err) => {
            if (err) vscode.window.showErrorMessage(`Error: ${err.message}`);
            else vscode.window.showInformationMessage('Multip Browser opened');
        });
    }

    publish() {
        const editor = vscode.window.activeTextEditor;
        if (editor && editor.document.fileName.endsWith('.multip')) {
            vscode.window.showInputBox({ prompt: 'Publish URL', placeHolder: 'https://example.com' }).then(url => {
                if (url) {
                    const file = editor.document.fileName;
                    exec(`multip publish ${file} ${url}`, (err) => {
                        if (err) vscode.window.showErrorMessage(`Publish error: ${err.message}`);
                        else vscode.window.showInformationMessage(`Published to ${url}`);
                    });
                }
            });
        }
    }

    test() {
        this.runMultipCommand('test', 'Running tests...', 'Tests passed');
    }

    showMenu() {
        const items = [
            'New Project', 'Build', 'Run', 'Compile',
            'Format', 'Browser', 'Publish', 'Test', 'Docs'
        ];
        vscode.window.showQuickPick(items, { placeHolder: 'Multip commands' }).then(selected => {
            if (!selected) return;
            const cmd = selected.toLowerCase().replace(/\s+/g, '');
            vscode.commands.executeCommand(`multip.${cmd}`);
        });
    }

    openDocs() {
        vscode.env.openExternal(vscode.Uri.parse('https://github.com/samwise1776/multip'));
    }

    runMultipCommand(command, running, done) {
        const editor = vscode.window.activeTextEditor;
        const file = editor?.document.fileName;
        if (!file || !file.endsWith('.multip')) {
            vscode.window.showErrorMessage('No .multip file open');
            return;
        }
        vscode.window.withProgress({
            location: vscode.ProgressLocation.Notification,
            title: running
        }, () => new Promise((resolve) => {
            exec(`multip ${command} ${file}`, (err) => {
                if (err) vscode.window.showErrorMessage(`Error: ${err.message}`);
                else vscode.window.showInformationMessage(done);
                resolve();
            });
        }));
    }

    lintDocument(doc) {
        const diagnostics = [];
        const text = doc.getText();
        const lines = text.split('\n');
        let braceCount = 0;

        lines.forEach((line, i) => {
            for (const ch of line) {
                if (ch === '{') braceCount++;
                if (ch === '}') braceCount--;
            }
            if (i === lines.length - 1 && braceCount > 0) {
                diagnostics.push(new vscode.Diagnostic(
                    new vscode.Range(i, line.length - 1, i, line.length),
                    `Missing ${braceCount} closing brace(s)`,
                    vscode.DiagnosticSeverity.Warning
                ));
            }
            const trimmed = line.trim();
            if (trimmed.startsWith('//') || trimmed.startsWith('#')) return;
            if (trimmed === '') return;

            const assignMatch = trimmed.match(/^(\w+)\s*=\s*(.*)/);
            if (assignMatch && ['const', 'var'].includes(assignMatch[1])) {
                const val = assignMatch[2].trim();
                if (val === '' || val === '=' || val === ';') {
                    diagnostics.push(new vscode.Diagnostic(
                        new vscode.Range(i, line.length - 1, i, line.length),
                        'Incomplete assignment',
                        vscode.DiagnosticSeverity.Warning
                    ));
                }
            }
        });

        if (braceCount !== 0 && diagnostics.length === 0) {
            diagnostics.push(new vscode.Diagnostic(
                new vscode.Range(lines.length - 1, 0, lines.length - 1, 0),
                `Unbalanced braces (${braceCount > 0 ? 'missing ' + braceCount + ' closing' : 'extra closing'})`,
                vscode.DiagnosticSeverity.Error
            ));
        }

        this.diagnosticCollection.set(doc.uri, diagnostics);
    }

    dispose() {
        this.statusBarItem.dispose();
        this.outputChannel.dispose();
        this.diagnosticCollection.dispose();
    }
}

let extension;

function activate(context) {
    extension = new MultipExtension(context);
    extension.registerCommands();
    extension.registerProviders();
    extension.registerEvents();
    console.log('Multip extension activated');
}

function deactivate() {
    if (extension) extension.dispose();
}

module.exports = { activate, deactivate };
