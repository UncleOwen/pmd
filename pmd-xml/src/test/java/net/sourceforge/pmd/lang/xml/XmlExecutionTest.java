/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xml;

import java.nio.file.Paths;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.test.AbstractExecutionTest;

class XmlExecutionTest extends AbstractExecutionTest {

    @Test
    void testXml() {
        files = Collections.singletonList(Paths.get("src/test/resources/net/sourceforge/pmd/lang/xml/mistypedcdata.xml"));
        rulesetFiles = Collections.singletonList("src/main/resources/category/xml/errorprone.xml");
        languageVersion = LanguageRegistry.PMD.getLanguageById("xml").getVersion("1.0");

        execute();

        assertOutputContaining("Potentially mistyped CDATA section with extra [ at beginning or ] at the end.");
    }
}
