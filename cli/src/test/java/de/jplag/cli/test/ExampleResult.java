package de.jplag.cli.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

import de.jplag.JPlag;
import de.jplag.JPlagResult;
import de.jplag.exceptions.ExitException;
import de.jplag.java.JavaLanguage;
import de.jplag.options.JPlagOptions;
import de.jplag.util.FileUtils;

public class ExampleResult {
    private static final String exampleCode = """
            public class A {
            }
            """.stripIndent();

    private static JPlagResult result;

    public static JPlagResult getExampleResult() throws IOException, ExitException {
        if (result == null) {
            File dir = Files.createTempDirectory("jplagCode").toFile();
            FileUtils.write(new File(dir, "A.java"), exampleCode);
            FileUtils.write(new File(dir, "B.java"), exampleCode);

            result = JPlag.run(new JPlagOptions(new JavaLanguage(), Set.of(dir), Set.of()).withMinimumTokenMatch(0));
        }
        return result;
    }
}
