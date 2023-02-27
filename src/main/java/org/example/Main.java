package org.example;

import io.ballerinalang.compiler.syntax.tree.*;
import io.ballerinalang.compiler.text.TextDocument;
import io.ballerinalang.compiler.text.TextDocuments;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

//    public static void main(String[] args) {
//        Path filePath = Paths.get("/home/vinoth/greeter/main.bal");
//
//        if (filePath == null || filePath.toString().isEmpty()) {
//            System.out.println("File path is empty. Please provide a valid path");
//            return;
//        }
//
//        try {
//            // Use try-with-resources to automatically close file stream
//            String fileContent = Files.readString(filePath);
//            fileContent = fileContent.replaceAll("/\\*([^*]|[\\r\\n]|(\\*+([^*/]|[\\r\\n])))*\\*+/", ""); // remove multi-line comments
//            fileContent = fileContent.replaceAll("//.*", ""); // remove single-line comments
//            TextDocument textDocument = TextDocuments.from(fileContent);
//            SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
//            StatementVisitor visitor = new StatementVisitor();
//            syntaxTree.rootNode().accept(visitor);
//            visitor.writeToCSV("output.csv");
//        } catch (IOException e) {
//            System.out.println("Error reading file: " + e.getMessage());
//        }
//    }

    static class StatementVisitor extends NodeVisitor{
        private List<String> variableNames = new ArrayList<>();
        private List<String> variableLabels = new ArrayList<>();

        private boolean isInsideLocalVar;

        @Override
        public void visit(CaptureBindingPatternNode node) {

            if(!isInsideLocalVar){
                return;
            }

            Token variableToken = node.variableName();
            String variableLabel = variableToken.toSourceCode();

            variableLabel = variableLabel.replaceAll("\\s+", " "); // remove unnecessary spaces
            if (!variableLabels.contains(variableLabel.trim())) { // check if label already exists
                variableLabels.add(variableLabel.trim());
                System.out.println(variableLabel.trim());

                StatementNode parentStatement = getParentStatement(variableToken);

                if (parentStatement != null) {
                    String statementSourceCode = parentStatement.toSourceCode();
                    statementSourceCode = statementSourceCode.replaceAll("\\s+", " "); // remove unnecessary spaces
                    System.out.println(statementSourceCode.trim());
                    variableNames.add(statementSourceCode.trim());
                }
            }
        }

        @Override
        public void visit(VariableDeclarationNode variableDeclarationNode) {

            isInsideLocalVar = true;

            visitSyntaxNode(variableDeclarationNode.typedBindingPattern());

//            String variableLabel = variableDeclarationNode.toSourceCode();
//
//            variableLabel = variableLabel.replaceAll("\\s+", " "); // remove unnecessary spaces
//            if (!variableLabels.contains(variableLabel.trim())) { // check if label already exists
//                variableLabels.add(variableLabel.trim());
//                System.out.println(variableLabel.trim());
//            }

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
                FileWriter writer = new FileWriter(new File(fileName));

                for (int i = 0; i < variableLabels.size() ; i++) {
                    if (i >= variableNames.size()) {
                        break;
                    }
                    String variableLabel = variableLabels.get(i).trim();
                    String statementSourceCode = variableNames.get(i).trim();
                    writer.write(variableLabel + "," + statementSourceCode + "\n");
                }

                writer.flush();
                writer.close();
            } catch (IOException e) {
                System.out.println("Error writing to CSV file: " + e.getMessage());
            }
        }
    }
}