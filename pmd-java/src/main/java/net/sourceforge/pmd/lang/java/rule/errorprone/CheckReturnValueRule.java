/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.errorprone;

import net.sourceforge.pmd.lang.java.ast.ASTExpressionStatement;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRulechainRule;
import net.sourceforge.pmd.lang.java.rule.internal.JavaRuleUtil;
import net.sourceforge.pmd.lang.java.types.InvocationMatcher;
import net.sourceforge.pmd.lang.java.types.InvocationMatcher.CompoundInvocationMatcher;
import net.sourceforge.pmd.lang.java.types.JMethodSig;
import net.sourceforge.pmd.reporting.RuleContext;

/**
 * Reports when a return value is ignored that shouldn't be.
 *
 * @since 7.25.0
 */
public class CheckReturnValueRule extends AbstractJavaRulechainRule {

    private static final CompoundInvocationMatcher MATCHERS = InvocationMatcher.parseAll(
            "java.io.InputStream#skip(long)",
            "java.io.InputStream#read(byte[])",
            "java.io.InputStream#read(byte[],int,int)"
    );

    public CheckReturnValueRule() {
        super(ASTMethodCall.class);
    }

    @Override
    public RuleContext visit(ASTMethodCall call, Object data) {
        RuleContext ctx = (RuleContext) data;

        if (shouldCheckResult(call) && !isResultUsed(call)) {
            ctx.addViolation(call, formatCall(call));
        }

        return ctx;
    }

    private String formatCall(ASTMethodCall call) {
        JMethodSig methodSig = call.getMethodType();

        return methodSig.getDeclaringType() + "." + methodSig.getName() + "()";
    }

    private boolean shouldCheckResult(ASTMethodCall call) {
        return JavaRuleUtil.isKnownPure(call)
                || MATCHERS.anyMatch(call);
    }

    private boolean isResultUsed(ASTMethodCall call) {
        return !(call.getParent() instanceof ASTExpressionStatement);
    }
}
