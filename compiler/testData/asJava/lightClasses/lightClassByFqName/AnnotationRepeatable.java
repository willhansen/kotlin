@java.lang.annotation.Repeatable(konstue = simple.One.Container.class)
@java.lang.annotation.Retention(konstue = java.lang.annotation.RetentionPolicy.RUNTIME)
@kotlin.annotation.Repeatable()
public abstract @interface One /* simple.One*/ {
  public abstract java.lang.String konstue();//  konstue()

  @java.lang.annotation.Retention(konstue = java.lang.annotation.RetentionPolicy.RUNTIME)
  @kotlin.jvm.internal.RepeatableContainer()
  public static abstract @interface Container /* simple.One.Container*/ {
    public abstract simple.One[] konstue();//  konstue()
  }
}
