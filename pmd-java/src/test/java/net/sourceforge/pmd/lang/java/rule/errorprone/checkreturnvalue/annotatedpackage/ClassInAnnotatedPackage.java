/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.errorprone.checkreturnvalue.annotatedpackage;

import net.sourceforge.pmd.lang.java.rule.errorprone.checkreturnvalue.CanIgnoreReturnValue;

public class ClassInAnnotatedPackage {
    public int shouldBeChecked() {
        return 42;
    }

    @CanIgnoreReturnValue
    public int doesNotNeedToBeChecked() {
        return 4;
    }
}
