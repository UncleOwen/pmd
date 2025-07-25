/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import net.sourceforge.pmd.lang.ast.impl.javacc.JavaccToken;
import net.sourceforge.pmd.lang.document.FileLocation;
import net.sourceforge.pmd.lang.document.TextRegion;

/**
 * Package declaration at the top of a {@linkplain ASTCompilationUnit source file}.
 * Since 7.0, there is no Name node anymore. Use {@link #getName()} instead.
 *
 *
 * <pre class="grammar">
 *
 * PackageDeclaration ::= {@link ASTModifierList AnnotationList} "package" Name ";"
 *
 * </pre>
 *
 */
public final class ASTPackageDeclaration extends AbstractJavaNode implements Annotatable, ASTTopLevelDeclaration, JavadocCommentOwner {

    private TextRegion reportRegion;

    ASTPackageDeclaration(int id) {
        super(id);
    }


    @Override
    protected <P, R> R acceptVisitor(JavaVisitor<? super P, ? extends R> visitor, P data) {
        return visitor.visit(this, data);
    }

    void setNameRegion(JavaccToken startTok, JavaccToken endTok) {
        reportRegion = TextRegion.union(startTok.getRegion(), endTok.getRegion());
    }

    @Override
    public FileLocation getReportLocation() {
        // the report location is the name of the package
        return getAstInfo().getTextDocument().toLocation(reportRegion);
    }

    /**
     * Returns the name of the package.
     *
     * @since 6.30.0
     */
    public String getName() {
        return super.getImage();
    }

    @Override
    public String getImage() {
        // the image was null before 7.0, best keep it that way
        return null;
    }
}
