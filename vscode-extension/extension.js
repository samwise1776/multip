const { exec } = require('child_process');
const path = require('path');
const fs = require('fs');

/**
 * Multip VS Code Extension
 * Provides language support, commands, and tooling for .multip files
 */
function activate(context) {
    console.log('Multip extension activated');

    // Register commands
    context.subscriptions.push(
        vscode.commands.registerCommand('multip.newProject', () => {
            const workspaceFolder = vscode.workspace.workspaceFolders?.[0];
            if (!workspaceFolder) {
                vscode.window.showErrorMessage('No workspace folder open');
                return;
            }
            const name = vscode.window.showInputBox({ prompt: 'Project name' });
            if (name) {
                const cmd = `multip new ${name}`;
                exec(cmd, { cwd: workspaceFolder.uri.fsPath }, (err, stdout) => {
                    if (err) vscode.window.showErrorMessage(`Error: ${err.message}`);
                    else vscode.window.showInformationMessage(`Project "${name}" created`);
                });
            }
        })
    );

    context.subscriptions.push(
        vscode.commands.registerCommand('multip.build', () => {
            runMultip('build', 'Building project...', 'Project built successfully');
        })
    );

    context.subscriptions.push(
        vscode.commands.registerCommand('multip.run', () => {
            runMultip('run', 'Running project...', 'Project running');
        })
    );

    context.subscriptions.push(
        vscode.commands.registerCommand('multip.compile', () => {
            const editor = vscode.window.activeTextEditor;
            if (editor && editor.document.fileName.endsWith('.multip')) {
                const file = editor.document.fileName;
                exec(`multip compile ${file}`, (err, stdout) => {
                    if (err) vscode.window.showErrorMessage(`Error: ${err.message}`);
                    else {
                        const panel = vscode.window.createWebviewPanel(
                            'multipCompile', 'Multip Compile Output',
                            vscode.ViewColumn.Beside, {}
                        );
                        panel.webview.html = `<html><body><pre>${stdout}</pre></body></html>`;
                    }
                });
            }
        })
    );

    context.subscriptions.push(
        vscode.commands.registerCommand('multip.format', () => {
            const editor = vscode.window.activeTextEditor;
            if (editor) {
                vscode.window.showInformationMessage('Formatting document...');
                // Format logic would go here
            }
        })
    );

    context.subscriptions.push(
        vscode.commands.registerCommand('multip.browser', () => {
            exec('multip browser', (err) => {
                if (err) vscode.window.showErrorMessage(`Error: ${err.message}`);
            });
        })
    );

    // Auto-detect .multip files
    vscode.workspace.onDidOpenTextDocument((doc) => {
        if (doc.fileName.endsWith('.multip')) {
            vscode.languages.setTextDocumentLanguage(doc, 'multip');
        }
    });

    // Diagnostics provider
    const diagnosticCollection = vscode.languages.createDiagnosticCollection('multip');
    context.subscriptions.push(diagnosticCollection);

    vscode.workspace.onDidSaveTextDocument((doc) => {
        if (doc.fileName.endsWith('.multip')) {
            lintDocument(doc, diagnosticCollection);
        }
    });
}

function runMultip(command, running, done) {
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

function lintDocument(doc, collection) {
    const diagnostics = [];
    const text = doc.getText();
    const lines = text.split('\n');

    lines.forEach((line, i) => {
        // Check for missing closing braces
        if (line.trim().endsWith('{') && i === lines.length - 1) {
            diagnostics.push(new vscode.Diagnostic(
                new vscode.Range(i, line.length - 1, i, line.length),
                'Missing closing brace',
                vscode.DiagnosticSeverity.Warning
            ));
        }
        // Check for undefined variables (simplified)
        const match = line.match(/=\s*([a-zA-Z_]\w*)\s*[;,\n]/);
        if (match && !['true', 'false', 'null', 'self', 'this'].includes(match[1])) {
            // Simplified check
        }
    });

    collection.set(doc.uri, diagnostics);
}

function deactivate() {}

module.exports = { activate, deactivate };
