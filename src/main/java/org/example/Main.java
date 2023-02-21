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

            System.out.println(variableLabel);
            variableLabels.add(variableLabel);

            StatementNode parentStatement = getParentStatement(variableToken);

            if (parentStatement != null) {
                String statementSourceCode = parentStatement.toSourceCode();
                System.out.println(statementSourceCode);
                variableNames.add(statementSourceCode);
            }
        }

//        @Override
//        public void visit(CaptureBindingPatternNode node) {
//            Token variableToken = node.variableName();
//            String variableLabel = variableToken.toSourceCode();
//
//            System.out.println(variableLabel);
//            variableLabels.add(variableLabel);
//
//            Node parent = node.parent();
//            if (parent instanceof VariableDeclarationNode) {
//                VariableDeclarationNode variableDeclaration = (VariableDeclarationNode) parent;
//                String statementSourceCode = variableDeclaration.toSourceCode();
//                System.out.println(statementSourceCode);
//                variableNames.add(statementSourceCode);
//            }
//        }

        /**
         * Returns the parent statement node of the given token
         *
         * @param token the token to find the parent statement for
         * @return the parent statement node
         */
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

                for (int i = 0; i < variableLabels.size()-1; i++) {
                    String variableLabel = variableLabels.get(i);
                    String statementSourceCode = variableNames.get(i);
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