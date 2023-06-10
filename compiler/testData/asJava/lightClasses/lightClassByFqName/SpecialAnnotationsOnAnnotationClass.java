@java.lang.annotation.Documented()
@java.lang.annotation.Repeatable(konstue = Anno.Container.class)
@java.lang.annotation.Retention(konstue = java.lang.annotation.RetentionPolicy.SOURCE)
@java.lang.annotation.Target(konstue = {java.lang.annotation.ElementType.TYPE_PARAMETER, java.lang.annotation.ElementType.TYPE_USE})
@kotlin.annotation.MustBeDocumented()
@kotlin.annotation.Repeatable()
@kotlin.annotation.Retention(konstue = kotlin.annotation.AnnotationRetention.SOURCE)
@kotlin.annotation.Target(allowedTargets = {kotlin.annotation.AnnotationTarget.TYPE_PARAMETER, kotlin.annotation.AnnotationTarget.TYPE})
public abstract @interface Anno /* Anno*/ {
  public abstract int i();//  i()

  @java.lang.annotation.Retention(konstue = java.lang.annotation.RetentionPolicy.SOURCE)
  @java.lang.annotation.Target(konstue = {java.lang.annotation.ElementType.TYPE_PARAMETER, java.lang.annotation.ElementType.TYPE_USE})
  @kotlin.annotation.Retention(konstue = kotlin.annotation.AnnotationRetention.SOURCE)
  @kotlin.annotation.Target(allowedTargets = {kotlin.annotation.AnnotationTarget.TYPE_PARAMETER, kotlin.annotation.AnnotationTarget.TYPE})
  @kotlin.jvm.internal.RepeatableContainer()
  public static abstract @interface Container /* Anno.Container*/ {
    public abstract Anno[] konstue();//  konstue()
  }
}
