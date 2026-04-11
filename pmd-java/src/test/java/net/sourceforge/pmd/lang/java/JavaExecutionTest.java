package net.sourceforge.pmd.lang.java;

import net.sourceforge.pmd.renderers.HTMLRenderer;
import net.sourceforge.pmd.renderers.SummaryHTMLRenderer;
import net.sourceforge.pmd.test.AbstractExecutionTest;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

public class JavaExecutionTest extends AbstractExecutionTest {

    @Test
    void testNoFormattersValidation() {
        files = Arrays.asList(
                Paths.get("src/test/resources/rulesets/ant/java/EncodingTestClass.java"),
                Paths.get("src/test/resources/ant/java/MoreThanThousandLinesOfCodeWithDuplicateLiterals.java"),
                Paths.get("src/test/resources/ant/java/PMDTaskTestExample.java")
        );
        rulesetFiles = Collections.singletonList("src/test/resources/rulesets/testing/test-rset-1.xml");

        execute();

        assertOutputContaining("Violation from test-rset-1.xml");
    }

    @Test
    void testNestedRuleset() {
        files = Arrays.asList(
                Paths.get("src/test/resources/rulesets/ant/java/EncodingTestClass.java"),
                Paths.get("src/test/resources/ant/java/MoreThanThousandLinesOfCodeWithDuplicateLiterals.java"),
                Paths.get("src/test/resources/ant/java/PMDTaskTestExample.java")
        );
        rulesetFiles = Arrays.asList(
                "src/test/resources/rulesets/testing/test-rset-1.xml",
                "src/test/resources/rulesets/testing/test-rset-2.xml"
        );

        execute();

        assertOutputContaining("Violation from test-rset-1.xml");
        assertOutputContaining("Violation from test-rset-2.xml");
    }

    @Test
    void testFormatterWithProperties() {
        files = Arrays.asList(
                Paths.get("src/test/resources/rulesets/ant/java/EncodingTestClass.java"),
                Paths.get("src/test/resources/ant/java/MoreThanThousandLinesOfCodeWithDuplicateLiterals.java"),
                Paths.get("src/test/resources/ant/java/PMDTaskTestExample.java")
        );
        rulesetFiles = Arrays.asList(
                "src/test/resources/rulesets/testing/test-rset-1.xml",
                "src/test/resources/rulesets/testing/test-rset-2.xml"
        );

        reportFormat = SummaryHTMLRenderer.NAME;
        reportProperties.setProperty(HTMLRenderer.LINK_PREFIX.name(), "link_prefix");
        reportProperties.setProperty(HTMLRenderer.LINE_PREFIX.name(), "line_prefix");

        execute();

        assertOutputContaining("Violation from test-rset-1.xml");
        assertOutputContaining("link_prefix");
        assertOutputContaining("line_prefix");
    }

}
