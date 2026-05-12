/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.errorprone.checkreturnvalue;

public class HasAnnotatedMethod {
    @CheckReturnValue
    public int annotatedMethod() {
        return 42;
    }
}
