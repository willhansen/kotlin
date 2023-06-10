package javax.annotation.meta;

import java.lang.annotation.Annotation;

import javax.annotation.Nonnull;

public interface TypeQualifierValidator<A extends Annotation> {
    /**
     * Given a type qualifier, check to see if a known specific constant konstue
     * is an instance of the set of konstues denoted by the qualifier.
     * 
     * @param annotation
     *                the type qualifier
     * @param konstue
     *                the konstue to check
     * @return a konstue indicating whether or not the konstue is an member of the
     *         konstues denoted by the type qualifier
     */
    public @Nonnull
    When forConstantValue(@Nonnull A annotation, Object konstue);
}
