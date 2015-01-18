package org.intellij.vala.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import org.intellij.vala.psi.ValaDeclaration;
import org.intellij.vala.psi.ValaDeclarationContainer;
import org.intellij.vala.psi.ValaFieldDeclaration;
import org.intellij.vala.psi.ValaLocalVariable;
import org.intellij.vala.psi.impl.ValaPsiElementUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.util.PsiTreeUtil.getParentOfType;
import static org.intellij.vala.psi.impl.LocalVariableUtil.getTypeReference;


public class ValaFieldReference extends PsiReferenceBase<PsiNamedElement> {

    private PsiElement objectVariable;

    public ValaFieldReference(PsiNamedElement psiNamedElement) {
        this(null, psiNamedElement);
    }

    public ValaFieldReference(PsiElement objectVariable, PsiNamedElement psiNamedElement) {
        super(psiNamedElement, new TextRange(0, psiNamedElement.getTextLength()));
        this.objectVariable = objectVariable;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        if (objectVariable != null) {
            return resolveAsOtherObjectFieldReference();
        }
        return resolveAsThisClassFieldReference(myElement);
    }

    private PsiElement resolveAsOtherObjectFieldReference() {
        ValaDeclaration objectTypeDeclaration = resolveObjectType();
        if (objectTypeDeclaration instanceof ValaDeclarationContainer) {
            return resolveAsClassFieldReference(myElement, (ValaDeclarationContainer) objectTypeDeclaration);
        }
        return null;
    }

    public static PsiElement resolveAsThisClassFieldReference(PsiNamedElement myElement) {
        ValaDeclarationContainer containingClass = getParentOfType(myElement, ValaDeclarationContainer.class, false);
        if (containingClass == null) {
            return null;
        }
        return resolveAsClassFieldReference(myElement, containingClass);
    }

    public static PsiElement resolveAsClassFieldReference(PsiNamedElement myElement, ValaDeclarationContainer containingClass) {
        for (ValaDeclaration declaration : containingClass.getDeclarations()) {
            if (declaration instanceof ValaFieldDeclaration) {
                ValaFieldDeclaration fieldDeclaration = (ValaFieldDeclaration) declaration;
                if (myElement.getName().equals(fieldDeclaration.getName())) {
                    return fieldDeclaration;
                }
            }
        }
        return null;
    }

    private ValaDeclaration resolveObjectType() {
        PsiReference parentRef = objectVariable.getReference();
        if (parentRef == null) {
            return null;
        }
        PsiElement resolved = parentRef.resolve();
        if (resolved != null) {
            return findTypeDeclaration(resolved);
        }
        return null;
    }

    private static ValaDeclaration findTypeDeclaration(PsiElement resolved) {
        if (resolved instanceof ValaLocalVariable) {
            return (ValaDeclaration) getTypeReference((ValaLocalVariable) resolved).resolve();
        } else if (resolved instanceof ValaFieldDeclaration) {
            return (ValaDeclaration) ValaPsiElementUtil.getTypeReference((ValaFieldDeclaration) resolved).resolve();
        }
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}