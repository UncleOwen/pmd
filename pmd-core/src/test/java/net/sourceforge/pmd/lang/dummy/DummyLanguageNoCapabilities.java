/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.dummy;

import java.util.Objects;

import net.sourceforge.pmd.lang.LanguageModuleBase;
import net.sourceforge.pmd.lang.LanguageRegistry;

/**
 * Dummy language used for testing PMD.
 */
public class DummyLanguageNoCapabilities extends LanguageModuleBase {

    public static final String NAME = "DummyNoCapabilities";
    public static final String TERSE_NAME = "dummy_no_capabilities";

    public DummyLanguageNoCapabilities() {
        super(LanguageMetadata.withId(TERSE_NAME).name(NAME).extensions("dummyxxx", "txt")
                              .addDefaultVersion("1.7", "7"));
    }

    public static DummyLanguageNoCapabilities getInstance() {
        return (DummyLanguageNoCapabilities) Objects.requireNonNull(RegistryHolder.REGISTRY.getLanguageByFullName(NAME));
    }

    private static final class RegistryHolder {
        private static final LanguageRegistry REGISTRY =
            LanguageRegistry.loadLanguages(DummyLanguageNoCapabilities.class.getClassLoader());
    }

}
