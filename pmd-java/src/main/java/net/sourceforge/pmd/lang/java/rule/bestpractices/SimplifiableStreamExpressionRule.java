/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.bestpractices;

import static net.sourceforge.pmd.lang.java.ast.ASTList.singleOrNull;
import static net.sourceforge.pmd.lang.java.ast.internal.JavaAstUtils.isReferenceToVar;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTLambdaExpression;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.ast.ASTMethodReference;
import net.sourceforge.pmd.lang.java.ast.ASTReturnStatement;
import net.sourceforge.pmd.lang.java.ast.ASTStatement;
import net.sourceforge.pmd.lang.java.ast.ASTThrowStatement;
import net.sourceforge.pmd.lang.java.ast.ASTVariableAccess;
import net.sourceforge.pmd.lang.java.ast.ASTVariableId;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.ast.internal.JavaAstUtils;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRulechainRule;
import net.sourceforge.pmd.lang.java.types.InvocationMatcher;
import net.sourceforge.pmd.lang.java.types.InvocationMatcher.CompoundInvocationMatcher;
import net.sourceforge.pmd.reporting.RuleContext;

public class SimplifiableStreamExpressionRule extends AbstractJavaRulechainRule {

    private static final InvocationMatcher COLLECTION_STREAM = InvocationMatcher.parse("java.util.Collection#stream()");

    private static final InvocationMatcher COLLECTIONS_EMPTYLIST = InvocationMatcher.parse("java.util.Collections#emptyList()");
    private static final InvocationMatcher COLLECTIONS_SINGLETON = InvocationMatcher.parse("java.util.Collections#singleton(_)");

    private static final InvocationMatcher OPTIONAL_ISPRESENT = InvocationMatcher.parse("java.util.Optional#isPresent()");
    private static final InvocationMatcher OPTIONAL_ORELSEGET = InvocationMatcher.parse("java.util.Optional#orElseGet(_)");

    private static final InvocationMatcher FUNCTION_IDENTITY = InvocationMatcher.parse("java.util.function.Function#identity()");

    private static final InvocationMatcher COLLECTORS_COUNTING = InvocationMatcher.parse("java.util.stream.Collectors#counting()");
    private static final InvocationMatcher COLLECTORS_MAPPING = InvocationMatcher.parse("java.util.stream.Collectors#mapping(_,_)");
    private static final InvocationMatcher COLLECTORS_MAXBY = InvocationMatcher.parse("java.util.stream.Collectors#maxBy(_)");
    private static final InvocationMatcher COLLECTORS_REDUCING = InvocationMatcher.parse("java.util.stream.Collectors#reducing(_)");
    private static final InvocationMatcher COLLECTORS_SUMMINGINT = InvocationMatcher.parse("java.util.stream.Collectors#summingInt(_)");

    private static final InvocationMatcher STREAM_COLLECT = InvocationMatcher.parse("java.util.stream.Stream#collect(_)");
    private static final InvocationMatcher STREAM_FILTER = InvocationMatcher.parse("java.util.stream.Stream#filter(_)");
    private static final InvocationMatcher STREAM_FINDFIRST = InvocationMatcher.parse("java.util.stream.Stream#findFirst()");
    private static final InvocationMatcher STREAM_FOREACH = InvocationMatcher.parse("java.util.stream.Stream#forEach(_)");
    private static final InvocationMatcher STREAM_TOARRAY = InvocationMatcher.parse("java.util.stream.Stream#toArray(_)");
    private static final InvocationMatcher STREAM_SORTED = InvocationMatcher.parse("java.util.stream.Stream#sorted()");
    private static final InvocationMatcher STREAM_SORTED1 = InvocationMatcher.parse("java.util.stream.Stream#sorted(_)");

    private static final CompoundInvocationMatcher PRIMITIVEOPTIONAL_ISPRESENT = InvocationMatcher.parseAll(
            "java.util.OptionalDouble#isPresent()",
            "java.util.OptionalInt#isPresent()",
            "java.util.OptionalLong#isPresent()"
    );
    private static final CompoundInvocationMatcher PRIMITIVEOPTIONAL_ORELSEGET = InvocationMatcher.parseAll(
            "java.util.OptionalDouble#orElseGet(_)",
            "java.util.OptionalInt#orElseGet(_)",
            "java.util.OptionalLong#orElseGet(_)"
    );

    private static final CompoundInvocationMatcher PRIMITIVESTREAM_FILTER = InvocationMatcher.parseAll(
            "java.util.stream.DoubleStream#filter(_)",
            "java.util.stream.IntStream#filter(_)",
            "java.util.stream.LongStream#filter(_)"
    );
    private static final CompoundInvocationMatcher PRIMITIVESTREAM_FINDFIRST = InvocationMatcher.parseAll(
            "java.util.stream.DoubleStream#findFirst()",
            "java.util.stream.IntStream#findFirst()",
            "java.util.stream.LongStream#findFirst()"
    );
    private static final CompoundInvocationMatcher PRIMITIVESTREAM_MAPTOOBJ = InvocationMatcher.parseAll(
            "java.util.stream.DoubleStream#mapToObj(_)",
            "java.util.stream.IntStream#mapToObj(_)",
            "java.util.stream.LongStream#mapToObj(_)"
    );

    private static final CompoundInvocationMatcher PRIMITIVESTREAM_SORTED = InvocationMatcher.parseAll(
            "java.util.stream.DoubleStream#sorted()",
            "java.util.stream.IntStream#sorted()",
            "java.util.stream.LongStream#sorted()"
    );

    public SimplifiableStreamExpressionRule() {
        super(ASTMethodCall.class);
    }

    @Override
    public Object visit(ASTMethodCall node, Object data) {
        RuleContext ctx = (RuleContext) data;

        if (STREAM_FOREACH.matchesCall(node) && COLLECTION_STREAM.matchesCall(node.getQualifier())) {
            reportViolation(node, ctx, ".stream().forEach(..)", ".forEach(..)");
        } else if (STREAM_TOARRAY.matchesCall(node) && COLLECTION_STREAM.matchesCall(node.getQualifier())) {
            reportViolation(node, ctx, ".stream().toArray(..)", ".toArray(..)");
        } else if (COLLECTION_STREAM.matchesCall(node) && COLLECTIONS_SINGLETON.matchesCall(node.getQualifier())) {
            reportViolation(node, ctx, "Collections.singleton(..).stream()", "Stream.of(..)");
        } else if (COLLECTION_STREAM.matchesCall(node) && COLLECTIONS_EMPTYLIST.matchesCall(node.getQualifier())) {
            reportViolation(node, ctx, "Collections.emptyList().stream()", "Stream.empty()");
        } else if (OPTIONAL_ISPRESENT.matchesCall(node) && STREAM_FINDFIRST.matchesCall(node.getQualifier())
                && STREAM_FILTER.matchesCall(((ASTMethodCall) node.getQualifier()).getQualifier())) {
            reportViolation(node, ctx, ".filter(..).findFirst().isPresent()", ".anyMatch(..)");
        } else if (PRIMITIVEOPTIONAL_ISPRESENT.anyMatch(node) && PRIMITIVESTREAM_FINDFIRST.anyMatch(node.getQualifier())
                && PRIMITIVESTREAM_FILTER.anyMatch(((ASTMethodCall) node.getQualifier()).getQualifier())) {
            reportViolation(node, ctx, ".filter(..).findFirst().isPresent()", ".anyMatch(..)");
        } else if (STREAM_COLLECT.matchesCall(node) && COLLECTORS_COUNTING.matchesCall(node.getArguments().get(0))) {
            reportViolation(node, ctx, ".collect(counting())", ".count()");
        } else if (STREAM_COLLECT.matchesCall(node) && COLLECTORS_MAXBY.matchesCall(node.getArguments().get(0))) {
            reportViolation(node, ctx, ".collect(maxBy(..))", ".max(..)");
        } else if (STREAM_COLLECT.matchesCall(node) && COLLECTORS_MAPPING.matchesCall(node.getArguments().get(0))) {
            reportViolation(node, ctx, ".collect(mapping(..))", ".map(..).collect(..)");
        } else if (STREAM_COLLECT.matchesCall(node) && COLLECTORS_REDUCING.matchesCall(node.getArguments().get(0))) {
            reportViolation(node, ctx, ".collect(reducing(..))", ".reduce(..)");
        } else if (STREAM_COLLECT.matchesCall(node) && COLLECTORS_SUMMINGINT.matchesCall(node.getArguments().get(0))) {
            reportViolation(node, ctx, ".collect(summingInt(..))", ".mapToInt(..).sum()");
        } else if (PRIMITIVESTREAM_MAPTOOBJ.anyMatch(node) && isIdentityLambda(node.getArguments().get(0))) {
            reportViolation(node, ctx, ".mapToObj(x -> x)", ".boxed()");
        } else if (STREAM_FINDFIRST.matchesCall(node) && STREAM_SORTED1.matchesCall(node.getQualifier())) {
            reportViolation(node, ctx, ".sorted(..).findFirst()", ".min(..)");
        } else if (STREAM_FINDFIRST.matchesCall(node) && STREAM_SORTED.matchesCall(node.getQualifier())) {
            reportViolation(node, ctx, ".sorted().findFirst()", ".min()");
        } else if (PRIMITIVESTREAM_FINDFIRST.anyMatch(node) && PRIMITIVESTREAM_SORTED.anyMatch(node.getQualifier())) {
            reportViolation(node, ctx, ".sorted().findFirst()", ".min()");
        } else if (isOptionalThrowsCase(node)) {
            reportViolation(node, ctx, ".orElseGet(() -> { throw new ...; })", ".orElseThrow()");
        } else if (isPrimitiveOptionalThrowsCase(node)) {
            reportViolation(node, ctx, ".orElseGet(() -> { throw new ...; })", ".orElseThrow()");
        }

        return null;
    }

    // checks for this pattern:
    // optional.orElseGet(() -> { throw new ...; })
    private boolean isOptionalThrowsCase(ASTMethodCall node) {
        if (!OPTIONAL_ORELSEGET.matchesCall(node)) {
            return false;
        }
        ASTExpression orElseGetParameter = node.getArguments().get(0);
        if (!(orElseGetParameter instanceof ASTLambdaExpression)) {
            return false;
        }
        ASTLambdaExpression lambda = (ASTLambdaExpression) orElseGetParameter;
        return !lambda.isExpressionBody() && singleOrNull(lambda.getBlockBody()) instanceof ASTThrowStatement;
    }

    // checks for these patterns:
    // optionalint.orElseGet(() -> { throw new ...; })
    // optionallong.orElseGet(() -> { throw new ...; })
    // optionaldouble.orElseGet(() -> { throw new ...; })
    private boolean isPrimitiveOptionalThrowsCase(ASTMethodCall node) {
        if (!PRIMITIVEOPTIONAL_ORELSEGET.anyMatch(node)) {
            return false;
        }
        ASTExpression orElseGetParameter = node.getArguments().get(0);
        if (!(orElseGetParameter instanceof ASTLambdaExpression)) {
            return false;
        }
        ASTLambdaExpression lambda = (ASTLambdaExpression) orElseGetParameter;
        return !lambda.isExpressionBody() && singleOrNull(lambda.getBlockBody()) instanceof ASTThrowStatement;
    }

    private void reportViolation(ASTMethodCall node, RuleContext ctx, String original, String replacement) {
        ctx.addViolation(node, original, replacement);
    }

    // visible for testing
    /* private */ static boolean isIdentityLambda(JavaNode node) {
        if (!(node instanceof ASTLambdaExpression)) {
            return false;
        }
        ASTLambdaExpression lambda = (ASTLambdaExpression) node;
        ASTVariableId varId = lambda.getParameters().get(0).getVarId();
        if (lambda.isExpressionBody()) {
            ASTExpression expr = lambda.getExpressionBody();
            return isReferenceToVar(expr, varId.getSymbol());
        } else {
            ASTBlock block = lambda.getBlockBody();
            if (block.size() != 1) {
                return false;
            }
            ASTStatement stmt = block.get(0);
            if (!(stmt instanceof ASTReturnStatement)) {
                return false;
            }
            ASTReturnStatement ret = (ASTReturnStatement) stmt;
            return isReferenceToVar(ret.getExpr(), varId.getSymbol());
        }
    }
}
