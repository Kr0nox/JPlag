package de.jplag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import de.jplag.util.FileUtils;

public class TokenPrinterUtils {
    private TokenPrinterUtils() {
    }

    public static String printTokensByFile(List<Token> tokens) {
        return printTokensByFile(tokens, Function.identity());
    }

    public static String printTokensByFile(List<Token> tokens, Function<File, File> fileMapper) {
        Map<File, List<Token>> groups = groupByFile(tokens);

        StringBuilder outputBuilder = new StringBuilder();

        for (File file : groups.keySet()) {
            outputBuilder.append(file.getAbsolutePath()).append(":").append(System.lineSeparator());
            try {
                outputBuilder.append(printTokensForFile(groups.get(file), fileMapper.apply(file))).append(System.lineSeparator())
                        .append(System.lineSeparator());
            } catch (IOException e) {
                outputBuilder.append("Could not print tokens: ").append(System.lineSeparator());
                outputBuilder.append(e.getMessage()).append(System.lineSeparator()).append(System.lineSeparator());
            }
        }

        return outputBuilder.toString();
    }

    public static String printTokensForFile(List<Token> tokens, File file) throws IOException {
        return new TokenPrinter(List.of(FileUtils.readFileContent(file).split(System.lineSeparator())), tokens).printTokens();
    }

    private static Map<File, List<Token>> groupByFile(List<Token> tokens) {
        Map<File, List<Token>> groups = new HashMap<>();

        for (Token token : tokens) {
            groups.computeIfAbsent(token.getFile(), (_) -> new ArrayList<>());
            groups.get(token.getFile()).add(token);
        }

        return groups;
    }
}
