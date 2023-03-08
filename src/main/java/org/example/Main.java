package org.example;

import io.ballerinalang.compiler.syntax.tree.*;
import io.ballerinalang.compiler.text.TextDocument;
import io.ballerinalang.compiler.text.TextDocuments;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        Path dirPath = Paths.get("/home/vinoth/Documents/nballerina-main");

        if (dirPath == null || !Files.isDirectory(dirPath)) {
            System.out.println("Directory path is empty or invalid. Please provide a valid path");
            return;
        }

        try {
            Files.walk(dirPath)
                    .filter(path -> path.toString().endsWith(".bal"))
                    .forEach(filePath -> {
                        try {
                            String fileContent = Files.readString(filePath);
                            fileContent = fileContent.replaceAll("/\\*([^*]|[\\r\\n]|(\\*+([^*/]|[\\r\\n])))*\\*+/", ""); // remove multi-line comments
                            fileContent = fileContent.replaceAll("//.*", ""); // remove single-line comments
                            TextDocument textDocument = TextDocuments.from(fileContent);
                            SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
                            StatementVisitor visitor = new StatementVisitor();
                            syntaxTree.rootNode().accept(visitor);
                            visitor.writeToCSV("output.csv");
                        } catch (IOException e) {
                            System.out.println("Error reading file " + filePath + ": " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.out.println("Error walking directory " + dirPath + ": " + e.getMessage());
        }
    }

    static class StatementVisitor extends NodeVisitor {
        private List<String> variableNames = new ArrayList<>();
        private List<String> sourceStatements = new ArrayList<>();

        private boolean isInsideLocalVar;

        @Override
        public void visit(CaptureBindingPatternNode node) {

            if (!isInsideLocalVar) {
                return;
            }

            Token variableToken = node.variableName();
            String variableName = variableToken.toSourceCode();

            variableName = variableName.replaceAll("\\s+", " "); // remove unnecessary spaces
            variableNames.add(variableName.trim());
            System.out.println(variableName.trim());

            StatementNode parentStatement = getParentStatement(variableToken);

            if (parentStatement != null) {
                String sourceStatement = parentStatement.toSourceCode();
                sourceStatement = sourceStatement.replaceAll("\\s+", " "); // remove unnecessary spaces
                sourceStatements.add(sourceStatement.trim());
                System.out.println(sourceStatement.trim());
            }
        }

        @Override
        public void visit(VariableDeclarationNode variableDeclarationNode) {

            isInsideLocalVar = true;

            visitSyntaxNode(variableDeclarationNode.typedBindingPattern());

            isInsideLocalVar = false;
        }

        private StatementNode getParentStatement(Token token) {
            NonTerminalNode parent = token.parent();
            while (parent != null && !(parent instanceof StatementNode)) {
                parent = parent.parent();
            }
            return (StatementNode) parent;
        }

        private void writeToCSV(String fileName) {
            try {

                FileWriter writer = new FileWriter((fileName),true);

                // read existing entries from the output.csv file into a set
                Set<String> existingEntries = Files.lines(Paths.get(fileName))
                        .map(String::trim)
                        .collect(Collectors.toSet());

                for (int i = 0; i < sourceStatements.size(); i++) {
                    if (i >= variableNames.size()) {
                        break;
                    }
                    String variableLabel = variableNames.get(i).trim();
                    String statementSourceCode = sourceStatements.get(i).trim();
                    String combinedEntry = variableLabel + "," + statementSourceCode;

                    // check if the combined entry already exists in the set of existing entries
                    if (!existingEntries.contains(combinedEntry)) {
                        // write the combined entry to the output.csv file
                        writer.write(combinedEntry + "\n");
                        // add the combined entry to the set of existing entries
                        existingEntries.add(combinedEntry);
                    }
                }

                writer.flush();
                writer.close();

            } catch (IOException e) {
                System.out.println("Error writing to CSV file: " + e.getMessage());
            }
        }
    }
}