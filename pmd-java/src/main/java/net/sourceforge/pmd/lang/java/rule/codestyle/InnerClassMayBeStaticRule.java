/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.codestyle;

import net.sourceforge.pmd.lang.java.ast.ASTClassDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTClassType;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRulechainRule;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;
import net.sourceforge.pmd.reporting.RuleContext;


/**
 * @since 7.26.0
 */
public class InnerClassMayBeStaticRule extends AbstractJavaRulechainRule {

    public InnerClassMayBeStaticRule() {
        super(ASTClassDeclaration.class);
    }

    @Override
    public Object visit(ASTClassDeclaration node, Object data) {
        RuleContext ctx = (RuleContext) data;

        if (!node.isNested() || node.isStatic()) {
            return null;
        }

        JTypeMirror outer = node.ancestors(ASTClassDeclaration.class).first().getTypeMirror();

        if (node.descendants(ASTClassType.class).any(c -> c.getTypeMirror().equals(outer))) {
            return null;
        }

        ctx.addViolation(node);

        return null;
    }
}
