package org.example;

import com.sun.source.tree.ExpressionStatementTree;
import io.ballerina.compiler.syntax.tree.*;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {
    public static void main(String[] args) {
        try {
            List<String> baseUrls = Arrays.asList(
                    "https://github.com/ballerina-platform/module-ballerinax-twitter/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-oracledb/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerina-soap/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-mssql.driver/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-aws.dynamodb/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-ai.agent/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-aws.s3/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-azure-cosmosdb/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerina-toml/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerina-yaml/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/persist-tools/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-netsuite/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-sfdc/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/openapi-connectors/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerina-http/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerina-time/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerina-crypto/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerina-persist/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerina-xmldata/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerina-uuid/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerina-url/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerina-file/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerina-serdes/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-mssql/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-guides/gcp-microservices-demo/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-guides/ai-samples/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-guides/ftgo-microservices-example/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-guides/integration-samples/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-guides/azure-functions-demo/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-guides/ballerina-in-action-samples/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-java.jdbc/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/ballerina-grammar/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/nballerina/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/ballerina-distribution/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerina-grpc/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-github/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-postgresql/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-nats/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerina-oauth2/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-mysql/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-twilio/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-slack/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-rabbitmq/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerinai-transaction/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerina-log/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerina-jwt/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-kafka/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerina-websubhub/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerina-graphql/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerina-websub/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerina-cache/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerina-constraint/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/ballerina-spec/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-health.fhir.r4/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerina-regex/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/edi-tools/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerina-random/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-googleapis.calendar/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-azure-storage-service/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-microsoft.onedrive/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/playground/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-aws.sqs/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-mysql.driver/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-peoplehr/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-persist.inmemory/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-persist.googlesheets/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerina-edi/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-health.fhir.templates/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-health.hl7v2/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerina-math.vector/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/asyncapi-triggers/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-azure.eventhub/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-oracledb.driver/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-health.clients/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-health.base/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-client.config/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-scim/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/protoc-tools/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-cdata.connect.driver/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-postgresql.driver/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-aws.simpledb/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-aws.sns/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-snowflake.driver/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-microsoft.onenote/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-microsoft.outlook.mail/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-microsoft.outlook.calendar/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-microsoft.teams/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-googleapis.drive/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-googleapis.people/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-aws.ses/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-azure.ad/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-microsoft.excel/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/ballerina-lang/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/ballerina-custom-jre/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/ballerina-lang/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerina-io/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/openapi-tools/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/ballerina-dev-website/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerina-auth/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerina-udp/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerinax-persist.sql/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/fhir-tools/archive/refs/heads/main.zip",
                    "https://github.com/ballerina-platform/module-ballerina-tcp/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/ballerina-update-tool/archive/refs/heads/master.zip",
                    "https://github.com/ballerina-platform/module-ballerina-ftp/archive/refs/heads/master.zip"
            );
            Path savePath = Paths.get("/home/vinoth/Music");

            for (String baseUrl : baseUrls) {
                // Download the repo and extract the zip file
                URL url = new URL(baseUrl);
                try (ZipInputStream zipStream = new ZipInputStream(url.openStream())) {
                    ZipEntry entry;
                    byte[] buffer = new byte[4096];
                    while ((entry = zipStream.getNextEntry()) != null) {
                        Path filePath = savePath.resolve(entry.getName());
                        if (!entry.isDirectory()) {
                            // Create directories if necessary
                            Files.createDirectories(filePath.getParent());
                            // Write the file to disk
                            try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                                int bytesRead;
                                while ((bytesRead = zipStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, bytesRead);
                                }
                            }
                        }
                        zipStream.closeEntry();
                    }
                }

                // Process the downloaded files
                Files.walk(savePath)
                        .filter(path -> path.toString().endsWith(".bal"))
                        .forEach(filePath -> {
                            try {
                                String fileContent = Files.readString(filePath);
                                fileContent = removeComments(fileContent); // Remove comments from the content
                                TextDocument textDocument = TextDocuments.from(fileContent);
                                SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
                                StatementVisitor visitor = new StatementVisitor();
                                syntaxTree.rootNode().accept(visitor);
                                visitor.writeToCSV();
                            } catch (Exception e) {
                                System.out.println("Error reading file " + filePath + ": " + e.getMessage());
                            }
                        });
            }
        } catch (MalformedURLException e) {
            System.out.println("Invalid URL: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error downloading file: " + e.getMessage());
        }
    }

    // Helper method to remove comments from Ballerina source code
    private static String removeComments(String content) {
        // Remove single-line comments (// ...)
        content = content.replaceAll("//.*", "");

        // Remove multi-line comments (/* ... */)
        content = content.replaceAll("/\\*.*?\\*/", "");

        return content;
    }

    static class StatementVisitor extends NodeVisitor {
        private static final String OUTPUT_CSV_FILE = "output.csv";
        private List<String> variableNames = new ArrayList<>();
        private List<String> sourceStatements = new ArrayList<>();
        private boolean isInsideLocalVar;
        private boolean isListBindingPattern;

//         private static final Pattern BASIC_LITERAL_PATTERN =
//                 Pattern.compile("(\\b(nil|true|false|[0-9]+|0x[0-9a-fA-F]+|\".*\"|\\[.*\\]|\\{.*\\}|\\(.*\\))\\b)");

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
            System.out.println(variableName.trim());

            if (parentStatement != null) {
                String sourceStatement = parentStatement.toSourceCode();
                sourceStatement = sourceStatement.replaceAll("\\s+", " "); // remove unnecessary spaces

                // skip the processing of the node if the parent statement contains a basic literal value
//                 if (BASIC_LITERAL_PATTERN.matcher(sourceStatement).find()) {
//                     return;
//                 }

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

        @Override
        public void visit(ListBindingPatternNode listBindingPatternNode) {
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

                FileWriter writer = new FileWriter(new File(OUTPUT_CSV_FILE), true);

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
