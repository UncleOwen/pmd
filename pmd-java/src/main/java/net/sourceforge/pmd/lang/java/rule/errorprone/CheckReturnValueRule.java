/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.errorprone;

import net.sourceforge.pmd.lang.java.ast.ASTExpressionStatement;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRulechainRule;
import net.sourceforge.pmd.lang.java.rule.internal.JavaRuleUtil;
import net.sourceforge.pmd.lang.java.symbols.AnnotableSymbol;
import net.sourceforge.pmd.lang.java.symbols.JExecutableSymbol;
import net.sourceforge.pmd.lang.java.symbols.JTypeDeclSymbol;
import net.sourceforge.pmd.lang.java.types.InvocationMatcher;
import net.sourceforge.pmd.lang.java.types.InvocationMatcher.CompoundInvocationMatcher;
import net.sourceforge.pmd.lang.java.types.JMethodSig;
import net.sourceforge.pmd.lang.java.types.TypeSystem;
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

    private static final String CHECK_RETURN_VALUE_ANNOTATION = "CheckReturnValue";
    private static final String CAN_IGNORE_RETURN_VALUE_ANNOTATION = "CanIgnoreReturnValue";

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
                || isCheckReturnValueAnnotated(call)
                || MATCHERS.anyMatch(call);
    }

    private boolean isCheckReturnValueAnnotated(ASTMethodCall call) {
        JExecutableSymbol methodSymbol = call.getMethodType().getSymbol();
        if (methodSymbol.isUnresolved()) {
            return false;
        }
        if (isAnnotatedWith(methodSymbol, CHECK_RETURN_VALUE_ANNOTATION)) {
            return true;
        }

        JTypeDeclSymbol classSymbol = call.getMethodType().getDeclaringType().getSymbol();
        if (isAnnotatedWith(classSymbol, CHECK_RETURN_VALUE_ANNOTATION)
                && !isAnnotatedWith(methodSymbol, CAN_IGNORE_RETURN_VALUE_ANNOTATION)
        ) {
            return true;
        }

        TypeSystem typeSystem = classSymbol.getTypeSystem();
        AnnotableSymbol packageSymbol = typeSystem.getPackageSymbol(classSymbol.getPackageName());
        return packageSymbol != null
                && isAnnotatedWith(packageSymbol, CHECK_RETURN_VALUE_ANNOTATION)
                && !isAnnotatedWith(methodSymbol, CAN_IGNORE_RETURN_VALUE_ANNOTATION);
    }

    private boolean isAnnotatedWith(AnnotableSymbol symbol, String name) {
        return symbol.getDeclaredAnnotations().stream().anyMatch(annotation -> name.equals(annotation.getSimpleName()));
    }

    private boolean isResultUsed(ASTMethodCall call) {
        return !(call.getParent() instanceof ASTExpressionStatement);
    }
}
