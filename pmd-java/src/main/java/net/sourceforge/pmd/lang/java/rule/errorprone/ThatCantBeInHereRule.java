/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.errorprone;

import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRulechainRule;

/**
 * Detects method calls on collections where the passed object cannot possibly be in the collection
 * due to type mismatch. This helps catch potential programming errors where incompatible types
 * are used with collection methods like contains(), remove(), indexOf(), etc.
 * 
 * Examples of violations:
 * - List&lt;Integer&gt; list; list.remove("string"); // String cannot be in Integer list
 * - Map&lt;String, Integer&gt; map; map.get(42); // Integer key cannot be in String-keyed map
 */
public class ThatCantBeInHereRule extends AbstractJavaRulechainRule {

    public ThatCantBeInHereRule() {
        super(ASTMethodCall.class);
    }

    @Override
    public Object visit(ASTMethodCall node, Object data) {
        return null;
    }
}
