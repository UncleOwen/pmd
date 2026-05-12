/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.errorprone.checkreturnvalue;

public class ClassWithUnrelatedAnnotation {
    public int doesNotNeedToBeChecked() {
        return 4;
    }
}
