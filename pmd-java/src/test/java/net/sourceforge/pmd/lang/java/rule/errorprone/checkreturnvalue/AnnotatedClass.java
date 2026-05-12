/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.errorprone.checkreturnvalue;

@CheckReturnValue
public class AnnotatedClass {
    public int shouldBeChecked() {
        return 42;
    }

    @CanIgnoreReturnValue
    public int doesNotNeedToBeChecked() {
        return 4;
    }
}
