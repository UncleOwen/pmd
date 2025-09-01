/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.test.schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import net.sourceforge.pmd.lang.PlainTextLanguage;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.AbstractRule;
import net.sourceforge.pmd.reporting.RuleContext;

import com.github.stefanbirkner.systemlambda.SystemLambda;

/**
 * @author Clément Fournier
 */
class TestSchemaParserTest {

    @Test
    void testSchemaSimple() throws IOException {
        String file = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                      + "<test-data\n"
                      + "        xmlns=\"http://pmd.sourceforge.net/rule-tests\"\n"
                      + "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                      + "        xsi:schemaLocation=\"http://pmd.sourceforge.net/rule-tests net/sourceforge/pmd/test/schema/rule-tests_1_0_0.xsd\">\n"
                      + "    <test-code>\n"
                      + "        <description>equality operators with Double.NaN</description>\n"
                      + "        <expected-problems>4</expected-problems>\n"
                      + "        <code><![CDATA[\n"
                      + "            public class Foo {\n"
                      + "                private int i;\n"
                      + "            }\n"
                      + "            ]]></code>\n"
                      + "    </test-code>\n"
                      + "    <test-code>\n"
                      + "        <description>equality operators with Float.NaN</description>\n"
                      + "        <expected-problems>4</expected-problems>\n"
                      + "        <code><![CDATA[\n"
                      + "            public class Foo {\n"
                      + "            }\n"
                      + "            ]]></code>\n"
                      + "    </test-code>\n"
                      + "</test-data>\n";

        RuleTestCollection parsed = parseFile(file);

        assertEquals(2, parsed.getTests().size());
        assertThat("Indentation should be removed",
                parsed.getTests().get(0).getCode(), equalTo("public class Foo {\n    private int i;\n}"));
    }

    @Test
    void testSharedCodeFragment() throws IOException {
        String file = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<test-data\n"
                + "        xmlns=\"http://pmd.sourceforge.net/rule-tests\"\n"
                + "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "        xsi:schemaLocation=\"http://pmd.sourceforge.net/rule-tests net/sourceforge/pmd/test/schema/rule-tests_1_0_0.xsd\">\n"
                + "    <code-fragment id=\"code1\"><![CDATA[\n"
                + "        public class Foo {\n"
                + "            private int i;\n"
                + "        }\n"
                + "        ]]></code-fragment>\n"
                + "    <test-code>\n"
                + "        <description>equality operators with Double.NaN</description>\n"
                + "        <expected-problems>4</expected-problems>\n"
                + "        <code-ref id=\"code1\" />\n"
                + "    </test-code>\n"
                + "</test-data>\n";

        RuleTestCollection parsed = parseFile(file);

        assertEquals(1, parsed.getTests().size());
        assertThat("Indentation should be removed",
                parsed.getTests().get(0).getCode(), equalTo("public class Foo {\n    private int i;\n}"));
    }

    @Test
    void testSchemaDeprecatedAttr() throws Exception {
        String file = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                      + "<test-data\n"
                      + "        xmlns=\"http://pmd.sourceforge.net/rule-tests\"\n"
                      + "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                      + "        xsi:schemaLocation=\"http://pmd.sourceforge.net/rule-tests net/sourceforge/pmd/test/schema/rule-tests_1_0_0.xsd\">\n"
                      + "    <test-code regressionTest='false'>\n"
                      + "        <description>equality operators with Double.NaN</description>\n"
                      + "        <expected-problems>4</expected-problems>\n"
                      + "        <code><![CDATA[\n"
                      + "            public class Foo {\n"
                      + "            }\n"
                      + "            ]]></code>\n"
                      + "    </test-code>\n"
                      + "</test-data>\n";

        String log = SystemLambda.tapSystemErr(() -> {
            RuleTestCollection parsed = parseFile(file);
            assertEquals(1, parsed.getTests().size());
        });

        assertThat(log, containsString(" 6|     <test-code regressionTest='false'>\n"
                                              + "                   ^^^^^^^^^^^^^^ Attribute 'regressionTest' is deprecated, use 'disabled' with inverted value\n"));
    }

    @Test
    void testUnknownProperty() throws Exception {
        String file = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<test-data\n"
                + "        xmlns=\"http://pmd.sourceforge.net/rule-tests\"\n"
                + "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "        xsi:schemaLocation=\"http://pmd.sourceforge.net/rule-tests net/sourceforge/pmd/test/schema/rule-tests_1_0_0.xsd\">\n"
                + "    <test-code>\n"
                + "        <description>equality operators with Double.NaN</description>\n"
                + "        <rule-property name='invalid_property'>foo</rule-property>\n"
                + "        <expected-problems>0</expected-problems>\n"
                + "        <code><![CDATA[\n"
                + "            public class Foo {\n"
                + "            }\n"
                + "            ]]></code>\n"
                + "    </test-code>\n"
                + "</test-data>\n";

        String log = SystemLambda.tapSystemErr(() -> {
            assertThrows(IllegalStateException.class, () -> parseFile(file));
        });

        assertThat(log, containsString("  8|         <rule-property name='invalid_property'>foo</rule-property>\n"
                                             + "                            ^^^^ Unknown property, known property names are violationSuppressRegex, violationSuppressXPath\n"));
    }

    @Test
    void withExpectedSuppressions() throws IOException {
        String file = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<test-data>\n"
                + "        xmlns=\"http://pmd.sourceforge.net/rule-tests\"\n"
                + "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "        xsi:schemaLocation=\"http://pmd.sourceforge.net/rule-tests net/sourceforge/pmd/test/schema/rule-tests_1_1_0.xsd\">\n"
                + "    <test-code>\n"
                + "        <description>Test case with suppression</description>\n"
                + "        <expected-problems>0</expected-problems>\n"
                + "        <expected-suppressions>\n"
                + "            <suppressor line=\"1\">@SuppressWarnings</suppressor>\n"
                + "            <suppressor line=\"2\">NOPMD</suppressor>\n"
                + "            <suppressor line=\"3\"></suppressor>\n"
                + "            <suppressor line=\"4\"/>\n"
                + "        </expected-suppressions>\n"
                + "        <code><![CDATA[\n"
                + "            public class Foo { }\n"
                + "            ]]></code>\n"
                + "    </test-code>\n"
                + "</test-data>\n";
        RuleTestCollection testCollection = parseFile(file);
        assertEquals(1, testCollection.getTests().size());
        RuleTestDescriptor test = testCollection.getTests().get(0);
        assertTrue(test.hasExpectedSuppressions());
        assertEquals(4, test.getExpectedSuppressions().size());
        assertEquals(1, test.getExpectedSuppressions().get(0).getLine());
        assertEquals("@SuppressWarnings", test.getExpectedSuppressions().get(0).getSuppressorId());
        assertEquals(2, test.getExpectedSuppressions().get(1).getLine());
        assertEquals("NOPMD", test.getExpectedSuppressions().get(1).getSuppressorId());
        assertEquals(3, test.getExpectedSuppressions().get(2).getLine());
        assertEquals("", test.getExpectedSuppressions().get(2).getSuppressorId());
        assertEquals(4, test.getExpectedSuppressions().get(3).getLine());
        assertEquals("", test.getExpectedSuppressions().get(3).getSuppressorId());
    }

    @Test
    void withExpectedEmptySuppressions() throws IOException {
        String file = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<test-data>\n"
                + "        xmlns=\"http://pmd.sourceforge.net/rule-tests\"\n"
                + "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "        xsi:schemaLocation=\"http://pmd.sourceforge.net/rule-tests net/sourceforge/pmd/test/schema/rule-tests_1_1_0.xsd\">\n"
                + "    <test-code>\n"
                + "        <description>Test case with suppression</description>\n"
                + "        <expected-problems>0</expected-problems>\n"
                + "        <expected-suppressions>\n"
                + "        </expected-suppressions>\n"
                + "        <code><![CDATA[\n"
                + "            public class Foo { }\n"
                + "            ]]></code>\n"
                + "    </test-code>\n"
                + "</test-data>\n";
        RuleTestCollection testCollection = parseFile(file);
        assertEquals(1, testCollection.getTests().size());
        RuleTestDescriptor test = testCollection.getTests().get(0);
        assertTrue(test.hasExpectedSuppressions());
        assertEquals(0, test.getExpectedSuppressions().size());
    }

    @Test
    void withExpectedNoSuppressions() throws IOException {
        String file = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<test-data>\n"
                + "        xmlns=\"http://pmd.sourceforge.net/rule-tests\"\n"
                + "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "        xsi:schemaLocation=\"http://pmd.sourceforge.net/rule-tests net/sourceforge/pmd/test/schema/rule-tests_1_1_0.xsd\">\n"
                + "    <test-code>\n"
                + "        <description>Test case with suppression</description>\n"
                + "        <expected-problems>0</expected-problems>\n"
                + "        <code><![CDATA[\n"
                + "            public class Foo { }\n"
                + "            ]]></code>\n"
                + "    </test-code>\n"
                + "</test-data>\n";
        RuleTestCollection testCollection = parseFile(file);
        assertEquals(1, testCollection.getTests().size());
        RuleTestDescriptor test = testCollection.getTests().get(0);
        assertFalse(test.hasExpectedSuppressions());
    }

    private RuleTestCollection parseFile(String file) throws IOException {
        MockRule mockRule = new MockRule();
        mockRule.setLanguage(PlainTextLanguage.getInstance());

        InputSource is = new InputSource();
        is.setSystemId("a/file.xml");
        is.setCharacterStream(new StringReader(file));

        return new TestSchemaParser().parse(mockRule, is);
    }

    public static final class MockRule extends AbstractRule {
        @Override
        public void apply(Node target, RuleContext ctx) {
            // do nothing
        }
    }

}
