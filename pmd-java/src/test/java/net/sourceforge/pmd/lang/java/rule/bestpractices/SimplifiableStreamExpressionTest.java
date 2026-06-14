/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.bestpractices;

import static net.sourceforge.pmd.lang.java.rule.bestpractices.SimplifiableStreamExpressionRule.isIdentityLambda;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.JavaParsingHelper;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.test.PmdRuleTst;

class SimplifiableStreamExpressionTest extends PmdRuleTst {

    private final JavaParsingHelper java = JavaParsingHelper.DEFAULT.withResourceContext(getClass());

    @Nested
    class IsIdentityLambda {
        @Test
        @DisplayName("Lambda Expression x -> x => true")
        void testXX() {
            assertTrue(isIdentityLambda(createLambdaNode("x -> x")));
        }

        @Test
        @DisplayName("Lambda Expression x -> y => false")
        void testXY() {
            assertFalse(isIdentityLambda(createLambdaNode("x -> y")));
        }

        @Test
        @DisplayName("Lambda Expression x -> { return x; } => true")
        void testXreturnX() {
            assertTrue(isIdentityLambda(createLambdaNode("x -> { return x; }")));
        }

        @Test
        @DisplayName("Lambda Expression x -> { return y; } => false")
        void testXreturnY() {
            assertFalse(isIdentityLambda(createLambdaNode("x -> { return y; }")));
        }

        @Test
        @DisplayName("Lambda Expression x -> { multiple lines } => false")
        void testMultipleLines() {
            assertFalse(isIdentityLambda(createLambdaNode("x -> { x = x+x; return x; }")));
        }

        @Test
        @DisplayName("Lambda Expression x -> endless loop => false")
        void testEndlessLoop() {
            assertFalse(isIdentityLambda(createLambdaNode("x -> { while(true){} }")));
        }

        @Test
        @DisplayName("Any function reference => false")
        void testNeither() {
            assertFalse(isIdentityLambda(createLambdaNode("java.util.function.IntFunction::apply")));
        }

        private JavaNode createLambdaNode(String expr) {
            String source = "import java.util.function.IntFunction;\n"
                    + "class Foo {\n"
                    + "    private static void foo(IntFunction<Integer> func){};\n"
                    + "    private int y;\n"
                    + "    { foo(" + expr + "); }\n"
                    + "}";
            ASTCompilationUnit root = java.parse(source);
            return root.descendants(ASTMethodCall.class)
                    .first(call -> call.getQualifier() == null && "foo".equals(call.getMethodName()))
                    .getArguments().get(0);
        }
    }
}
