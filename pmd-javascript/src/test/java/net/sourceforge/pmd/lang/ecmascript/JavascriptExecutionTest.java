/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ecmascript;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import net.sourceforge.pmd.test.AbstractExecutionTest;

class JavascriptExecutionTest extends AbstractExecutionTest {

    @Test
    void testEcmascript() {
        files = Arrays.asList(
                Paths.get("src/test/resources/net/sourceforge/pmd/lang/ecmascript/JavascriptExecutionTestFcoltable.js"),
                Paths.get("src/test/resources/net/sourceforge/pmd/lang/ecmascript/JavascriptExecutionTestSorttable.js")
        );
        rulesetFiles = Collections.singletonList("src/test/resources/net/sourceforge/pmd/lang/ecmascript/JavascriptExecutionTestRuleset.xml");

        execute();

        assertOutputContaining("A 'return', 'break', 'continue', or 'throw' statement should be the last in a block.");
        assertOutputContaining("Avoid using global variables");
        assertOutputContaining("Use ===/!== to compare with true/false or Numbers");
    }
}
