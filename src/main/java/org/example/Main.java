package org.example;

import com.sun.source.tree.ExpressionStatementTree;
import io.ballerinalang.compiler.syntax.tree.*;
import io.ballerinalang.compiler.text.TextDocument;
import io.ballerinalang.compiler.text.TextDocuments;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {
    public static void main(String[] args) {
            try {
                String baseUrl = "https://github.com/ballerina-platform/nballerina/archive/refs/heads/main.zip";
                Path savePath = Paths.get("/home/vinoth/Music");
                String pathString = savePath.toString();
        
                // Download and extract the zip file
                URL url = new URL(baseUrl);
                ZipInputStream zipStream = new ZipInputStream(url.openStream());
                ZipEntry entry = zipStream.getNextEntry();
                while (entry != null) {
                    Path filePath = Paths.get(pathString, entry.getName());
                    if (!entry.isDirectory()) {
                        // Create directories if necessary
                        Files.createDirectories(filePath.getParent());
                        // Write the file to disk
                        FileOutputStream outputStream = new FileOutputStream(filePath.toFile());
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = zipStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.close();
                    }
                    zipStream.closeEntry();
                    entry = zipStream.getNextEntry();
                }
                zipStream.close();

            // continue with the rest of the code
            Path dirPath = Paths.get(pathString);

            if (dirPath == null || !Files.isDirectory(dirPath)) {
                System.out.println("Directory path is empty or invalid. Please provide a valid path");
                return;
            }

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
                            visitor.writeToCSV();
                        } catch (IOException e) {
                            System.out.println("Error reading file " + filePath + ": " + e.getMessage());
                        }
                    });
        } catch (MalformedURLException e) {
            System.out.println("Invalid URL: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error downloading file: " + e.getMessage());
        }
    }

    static class StatementVisitor extends NodeVisitor {
        private List<String> variableNames = new ArrayList<>();
        private List<String> sourceStatements = new ArrayList<>();

        private boolean isInsideLocalVar;
        private boolean isListBindingPattern;
        private static final String OUTPUT_CSV_FILE = "output.csv";

        // private static final Pattern BASIC_LITERAL_PATTERN =
        //         Pattern.compile("(\\b(nil|true|false|[0-9]+|0x[0-9a-fA-F]+|\".*\"|\\[.*\\]|\\{.*\\}|\\(.*\\))\\b)");

        @Override
        public void visit(CaptureBindingPatternNode node) {

            if (!isInsideLocalVar) {
                return;
            }

            if (!isListBindingPattern) {
                return;
            }

            // Check if the right-hand side of the parent statement is a basic literal value
            StatementNode parentStatement = getParentStatement(node.variableName());

            if (parentStatement != null && parentStatement instanceof ExpressionStatementTree) {
                System.out.println(parentStatement);
                ExpressionNode expressionNode = ((ExpressionStatementNode) parentStatement).expression();
                System.out.println(expressionNode);

//                if (expressionNode instanceof BasicLiteralNode) {
//                    BasicLiteralNode basicLiteralNode = (BasicLiteralNode) expressionNode;
//                    if (basicLiteralNode.literalToken() == null || basicLiteralNode.literalToken().equals("[]") ||
//                            basicLiteralNode.literalToken().equals("{}") || basicLiteralNode.literalToken().equals("0")) {
//                        return;
//                    }
//                }
            }

            Token variableToken = node.variableName();
            String variableName = variableToken.toSourceCode();

            variableName = variableName.replaceAll("\\s+", " "); // remove unnecessary spaces

            variableNames.add(variableName.trim());
           // System.out.println(variableName.trim());

            if (parentStatement != null) {
                String sourceStatement = parentStatement.toSourceCode();
                sourceStatement = sourceStatement.replaceAll("\\s+", " "); // remove unnecessary spaces

                // skip the processing of the node if the parent statement contains a basic literal value
                // if (BASIC_LITERAL_PATTERN.matcher(sourceStatement).find()) {
                //     return;
                // }

                sourceStatements.add(sourceStatement.trim());
                //System.out.println(sourceStatement.trim());

            }
        }

        @Override
        public void visit(VariableDeclarationNode variableDeclarationNode) {
            isInsideLocalVar = true;

            visitSyntaxNode(variableDeclarationNode.typedBindingPattern());

            isInsideLocalVar = false;
        }

        @Override
        public void visit(ListBindingPatternNode listBindingPatternNode){
            isListBindingPattern = false;

            listBindingPatternNode.children().forEach(child -> child.accept(this));

            isListBindingPattern = true;
        }

        private StatementNode getParentStatement(Token token) {
            NonTerminalNode parent = token.parent();
            while (parent != null && !(parent instanceof StatementNode)) {
                parent = parent.parent();
            }
            return (StatementNode) parent;
        }

        private void writeToCSV() {
            try {

                FileWriter writer = new FileWriter(new File(OUTPUT_CSV_FILE),true);

                // read existing entries from the output.csv file into a set
                Set<String> existingEntries = Files.lines(Paths.get(OUTPUT_CSV_FILE))
                        .map(String::trim)
                        .collect(Collectors.toSet());

                for (int i = 0; i < sourceStatements.size(); i++) {
                    if (i >= variableNames.size()) {
                        break;
                    }
                    String variableLabel = variableNames.get(i).trim();
                    String statementSourceCode = "\"" + sourceStatements.get(i).trim().replace("\"", "\\\"") + "\"";
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