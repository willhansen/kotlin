public enum AnnotationsOnEnumEntry /* two.AnnotationsOnEnumEntry*/ {
  @two.PropertyImplicitly() @two.PropertyExplicitly() @two.FieldImplicitly() @two.FieldExplicitly() EntryWithoutConstructor,
  @two.PropertyImplicitly() @two.FieldImplicitly() EntryWithConstructor,
  EntryWithConstructor2;

  @org.jetbrains.annotations.NotNull()
  public static kotlin.enums.EnumEntries<two.AnnotationsOnEnumEntry> getEntries();//  getEntries()

  @org.jetbrains.annotations.NotNull()
  public static two.AnnotationsOnEnumEntry konstueOf(java.lang.String) throws java.lang.IllegalArgumentException, java.lang.NullPointerException;//  konstueOf(java.lang.String)

  @org.jetbrains.annotations.NotNull()
  public static two.AnnotationsOnEnumEntry[] konstues();//  konstues()

  private  AnnotationsOnEnumEntry(int);//  .ctor(int)

  public final void foo();//  foo()
}

@java.lang.annotation.Retention(konstue = java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(konstue = {java.lang.annotation.ElementType.FIELD})
@kotlin.annotation.Target(allowedTargets = {kotlin.annotation.AnnotationTarget.FIELD})
public abstract @interface FieldExplicitly /* two.FieldExplicitly*/ {
}

@java.lang.annotation.Retention(konstue = java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(konstue = {java.lang.annotation.ElementType.FIELD})
@kotlin.annotation.Target(allowedTargets = {kotlin.annotation.AnnotationTarget.FIELD})
public abstract @interface FieldImplicitly /* two.FieldImplicitly*/ {
}

@java.lang.annotation.Retention(konstue = java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(konstue = {})
@kotlin.annotation.Target(allowedTargets = {kotlin.annotation.AnnotationTarget.PROPERTY})
public abstract @interface PropertyExplicitly /* two.PropertyExplicitly*/ {
}

@java.lang.annotation.Retention(konstue = java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(konstue = {})
@kotlin.annotation.Target(allowedTargets = {kotlin.annotation.AnnotationTarget.PROPERTY})
public abstract @interface PropertyImplicitly /* two.PropertyImplicitly*/ {
}
