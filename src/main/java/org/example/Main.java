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
        Path filePath = Paths.get("/home/vinoth/greeter/main.bal");

        if (filePath == null || filePath.toString().isEmpty()) {
            System.out.println("File path is empty. Please provide a valid path");
            return;
        }

        try {
            // Use try-with-resources to automatically close file stream
            String fileContent = Files.readString(filePath);
            fileContent = fileContent.replaceAll("/\\*([^*]|[\\r\\n]|(\\*+([^*/]|[\\r\\n])))*\\*+/", ""); // remove multi-line comments
            fileContent = fileContent.replaceAll("//.*", ""); // remove single-line comments
            TextDocument textDocument = TextDocuments.from(fileContent);
            SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
            StatementVisitor visitor = new StatementVisitor();
            syntaxTree.rootNode().accept(visitor);
            visitor.writeToCSV("output.csv");
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    static class StatementVisitor extends NodeVisitor {
        private List<String> variableNames = new ArrayList<>();
        private List<String> variableLabels = new ArrayList<>();

        @Override
        public void visit(CaptureBindingPatternNode node) {
            Token variableToken = node.variableName();
            String variableLabel = variableToken.toSourceCode();

            variableLabel = variableLabel.replaceAll("\\s+", " "); // remove unnecessary spaces
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

        @Override
        public void visit(ConstantDeclarationNode node) {
            Token constantToken = node.variableName();
            String variableLabel = constantToken.toSourceCode();

            variableLabel = variableLabel.replaceAll("\\s+", " "); // remove unnecessary spaces
            variableLabels.add(variableLabel.trim());
            System.out.println(variableLabel.trim());

//            Node initializer = node.initializer();
//            if (initializer instanceof ExpressionStatementNode) {
//                ExpressionStatementNode expressionStatementNode = (ExpressionStatementNode) initializer;
//                ExpressionNode expressionNode = expressionStatementNode.expression();
//                String statementSourceCode = expressionNode.toSourceCode();
//                statementSourceCode = statementSourceCode.replaceAll("\\s+", " "); // remove unnecessary spaces
//                System.out.println(statementSourceCode.trim());
//                variableNames.add(statementSourceCode.trim());
//            }

            StatementNode parentStatement = getParentStatement(constantToken);

            if (parentStatement != null) {
                String statementSourceCode = parentStatement.toSourceCode();
                statementSourceCode = statementSourceCode.replaceAll("\\s+", " "); // remove unnecessary spaces
                System.out.println(statementSourceCode.trim());
                variableNames.add(statementSourceCode.trim());
            }
        }

//        @Override
//        public void visit(ModuleVariableDeclarationNode node) {
//            Token moduleToken = node.variableName();
//            String variableLabel = moduleToken.toSourceCode();
//
//            variableLabel = variableLabel.replaceAll("\\s+", " "); // remove unnecessary spaces
//            variableLabels.add(variableLabel.trim());
//            System.out.println(variableLabel.trim());
//
//            Node initializer = node.initializer();
//            if (initializer instanceof ExpressionStatementNode) {
//                ExpressionStatementNode expressionStatementNode = (ExpressionStatementNode) initializer;
//                ExpressionNode expressionNode = expressionStatementNode.expression();
//                String statementSourceCode = expressionNode.toSourceCode();
//                statementSourceCode = statementSourceCode.replaceAll("\\s+", " "); // remove unnecessary spaces
//                System.out.println(statementSourceCode.trim());
//                variableNames.add(statementSourceCode.trim());
//            }
//
//            StatementNode parentStatement = getParentStatement(constantToken);
//
//            if (parentStatement != null) {
//                String statementSourceCode = parentStatement.toSourceCode();
//                statementSourceCode = statementSourceCode.replaceAll("\\s+", " "); // remove unnecessary spaces
//                System.out.println(statementSourceCode.trim());
//                variableNames.add(statementSourceCode.trim());
//            }
//        }

        private StatementNode getParentStatement(Token token) {
            NonTerminalNode parent = token.parent();
            while (!(parent instanceof StatementNode)) {
                parent = parent.parent();
                if (parent == null) {
                    return null;
                }
            }
            return (StatementNode) parent;
        }

        private void writeToCSV(String fileName) {
            try {
                FileWriter writer = new FileWriter(new File(fileName));

                for (int i = 0; i <= variableLabels.size() ; i++) {
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