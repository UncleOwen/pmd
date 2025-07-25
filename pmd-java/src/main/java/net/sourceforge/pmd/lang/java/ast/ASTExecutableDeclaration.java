/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.java.symbols.JExecutableSymbol;
import net.sourceforge.pmd.lang.java.types.JClassType;
import net.sourceforge.pmd.lang.java.types.JMethodSig;


/**
 * Groups method and constructor declarations under a common type.
 *
 * <pre class="grammar">
 *
 * ExecutableDeclaration ::= {@link ASTMethodDeclaration MethodDeclaration}
 *                         | {@link ASTConstructorDeclaration ConstructorDeclaration}
 *
 * </pre>
 *
 * <p>Note: This interface was called ASTMethodOrConstructorDeclaration in PMD 6.
 *
 * @author Clément Fournier
 * @since 5.8.1
 */
public interface ASTExecutableDeclaration
    extends ModifierOwner,
            ASTBodyDeclaration,
            TypeParamOwnerNode,
            JavadocCommentOwner,
            ReturnScopeNode {


    @Override
    JExecutableSymbol getSymbol();


    /**
     * Returns the generic signature for the method. This is a {@link JMethodSig}
     * declared in the {@linkplain JClassType#getGenericTypeDeclaration() generic type declaration}
     * of the enclosing type. The signature may mention type parameters
     * of the enclosing types, and its own type parameters.
     */
    JMethodSig getGenericSignature();


    /**
     * Returns the name of the method, or the simple name of the declaring class for
     * a constructor declaration.
     */
    String getName();


    /**
     * Returns true if this method is abstract, so doesn't
     * declare a body. Interface members are
     * implicitly abstract, whether they declare the
     * {@code abstract} modifier or not. Default interface
     * methods are not abstract though, consistently with the
     * standard reflection API.
     */
    // TODO is this relevant?
    default boolean isAbstract() {
        return hasModifiers(JModifier.ABSTRACT);
    }

    /**
     * Returns the formal parameters node of this method or constructor.
     */
    @NonNull
    default ASTFormalParameters getFormalParameters() {
        return firstChild(ASTFormalParameters.class);
    }

    /**
     * Returns the number of formal parameters expected by this declaration.
     * This excludes any receiver parameter, which is irrelevant to arity.
     */
    default int getArity() {
        return getFormalParameters().size();
    }


    /**
     * Returns the body of this method or constructor. Returns null if
     * this is the declaration of an abstract method.
     */
    @Override
    default @Nullable ASTBlock getBody() {
        JavaNode last = getLastChild();
        return last instanceof ASTBlock ? (ASTBlock) last : null;
    }

    /**
     * Returns the {@code throws} clause of this declaration, or null
     * if there is none.
     */
    @Nullable
    default ASTThrowsList getThrowsList() {
        return firstChild(ASTThrowsList.class);
    }

    /**
     * Returns true if this node's last formal parameter is varargs.
     */
    default boolean isVarargs() {
        JavaNode lastFormal = getFormalParameters().getLastChild();
        return lastFormal instanceof ASTFormalParameter && ((ASTFormalParameter) lastFormal).isVarargs();
    }


    /**
     * Returns true if this is a static method.
     * If this is a constructor, return false.
     *
     * @since 7.1.0
     */
    default boolean isStatic() {
        return hasModifiers(JModifier.STATIC);
    }


    /**
     * Returns true if this is a final method.
     * If this is a constructor, return false.
     *
     * @since 7.1.0
     */
    default boolean isFinal() {
        return hasModifiers(JModifier.FINAL);
    }

}
