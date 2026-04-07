/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.test;

import static net.sourceforge.pmd.lang.rule.InternalApiBridge.loadRuleSetsWithoutException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.rule.RuleSetLoader;

import com.github.stefanbirkner.systemlambda.Statement;
import com.github.stefanbirkner.systemlambda.SystemLambda;

public class AbstractExecutionTest {
    protected List<Path> files;
    protected List<String> rulesetFiles;
    protected LanguageVersion languageVersion;

    private String output;

    protected final void execute() {
        try {
            restoreLocale(
                    // restoring system properties: Test might change file.encoding or might change logging properties
                    // See Slf4jSimpleConfigurationForAnt and resetLogging
                    () -> SystemLambda.restoreSystemProperties(
                            () -> output = tapSystemOut(
                                    this::doExecute
                            )
                    ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void doExecute() {
        PMDConfiguration configuration = new PMDConfiguration();

        configuration.setReportFormat("text");

        if (languageVersion != null) {
            configuration.setDefaultLanguageVersion(languageVersion);
        }

        try (PmdAnalysis pmd = PmdAnalysis.create(configuration)) {
            RuleSetLoader ruleSetLoader = pmd.newRuleSetLoader();
            pmd.addRuleSets(loadRuleSetsWithoutException(ruleSetLoader, rulesetFiles));

            files.forEach(p -> pmd.files().addFile(p));

            pmd.performAnalysis();
        }
    }

    private static void restoreLocale(Statement statement) throws Exception {
        Locale originalLocale = Locale.getDefault();
        try {
            statement.execute();
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    /**
     * This is similar to {@link SystemLambda#tapSystemOut(Statement)}. But this
     * method doesn't use the platform default charset as it was when the JVM started.
     * Instead, it uses the current system property {@code file.encoding}. This allows
     * tests to change the encoding.
     *
     * @param statement an arbitrary piece of code.
     * @return text that is written to stdout. Lineendings are normalized to {@code \n}.
     * @throws Exception any exception thrown by the statement
     */
    private static String tapSystemOut(Statement statement) throws Exception {
        @SuppressWarnings("PMD.CloseResource") // we don't want to close System.out
        PrintStream originalOut = System.out;
        ByteArrayOutputStream text = new ByteArrayOutputStream();
        String currentDefaultCharset = System.getProperty("file.encoding");
        try {
            PrintStream replacement = new PrintStream(text, true, currentDefaultCharset);
            System.setOut(replacement);
            statement.execute();
        } finally {
            System.setOut(originalOut);
        }
        String result = text.toString(currentDefaultCharset);
        return result.replace(System.lineSeparator(), "\n");
    }

    public void assertOutputContaining(String text) {
        assertThat(output, containsString(text));
    }
}
