// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: core/metadata/src/builtins.debug.proto

package org.jetbrains.kotlin.metadata.builtins;

public final class DebugBuiltInsProtoBuf {
  private DebugBuiltInsProtoBuf() {}
  public static void registerAllExtensions(
      org.jetbrains.kotlin.protobuf.ExtensionRegistry registry) {
    registry.add(org.jetbrains.kotlin.metadata.builtins.DebugBuiltInsProtoBuf.packageFqName);
    registry.add(org.jetbrains.kotlin.metadata.builtins.DebugBuiltInsProtoBuf.classAnnotation);
    registry.add(org.jetbrains.kotlin.metadata.builtins.DebugBuiltInsProtoBuf.constructorAnnotation);
    registry.add(org.jetbrains.kotlin.metadata.builtins.DebugBuiltInsProtoBuf.functionAnnotation);
    registry.add(org.jetbrains.kotlin.metadata.builtins.DebugBuiltInsProtoBuf.propertyAnnotation);
    registry.add(org.jetbrains.kotlin.metadata.builtins.DebugBuiltInsProtoBuf.propertyGetterAnnotation);
    registry.add(org.jetbrains.kotlin.metadata.builtins.DebugBuiltInsProtoBuf.propertySetterAnnotation);
    registry.add(org.jetbrains.kotlin.metadata.builtins.DebugBuiltInsProtoBuf.compileTimeValue);
    registry.add(org.jetbrains.kotlin.metadata.builtins.DebugBuiltInsProtoBuf.enumEntryAnnotation);
    registry.add(org.jetbrains.kotlin.metadata.builtins.DebugBuiltInsProtoBuf.parameterAnnotation);
    registry.add(org.jetbrains.kotlin.metadata.builtins.DebugBuiltInsProtoBuf.typeAnnotation);
    registry.add(org.jetbrains.kotlin.metadata.builtins.DebugBuiltInsProtoBuf.typeParameterAnnotation);
  }
  public static final int PACKAGE_FQ_NAME_FIELD_NUMBER = 151;
  /**
   * <code>extend .org.jetbrains.kotlin.metadata.Package { ... }</code>
   */
  public static final
    org.jetbrains.kotlin.protobuf.GeneratedMessage.GeneratedExtension<
      org.jetbrains.kotlin.metadata.DebugProtoBuf.Package,
      java.lang.Integer> packageFqName = org.jetbrains.kotlin.protobuf.GeneratedMessage
          .newFileScopedGeneratedExtension(
        java.lang.Integer.class,
        null);
  public static final int CLASS_ANNOTATION_FIELD_NUMBER = 150;
  /**
   * <code>extend .org.jetbrains.kotlin.metadata.Class { ... }</code>
   */
  public static final
    org.jetbrains.kotlin.protobuf.GeneratedMessage.GeneratedExtension<
      org.jetbrains.kotlin.metadata.DebugProtoBuf.Class,
      java.util.List<org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation>> classAnnotation = org.jetbrains.kotlin.protobuf.GeneratedMessage
          .newFileScopedGeneratedExtension(
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.class,
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.getDefaultInstance());
  public static final int CONSTRUCTOR_ANNOTATION_FIELD_NUMBER = 150;
  /**
   * <code>extend .org.jetbrains.kotlin.metadata.Constructor { ... }</code>
   */
  public static final
    org.jetbrains.kotlin.protobuf.GeneratedMessage.GeneratedExtension<
      org.jetbrains.kotlin.metadata.DebugProtoBuf.Constructor,
      java.util.List<org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation>> constructorAnnotation = org.jetbrains.kotlin.protobuf.GeneratedMessage
          .newFileScopedGeneratedExtension(
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.class,
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.getDefaultInstance());
  public static final int FUNCTION_ANNOTATION_FIELD_NUMBER = 150;
  /**
   * <code>extend .org.jetbrains.kotlin.metadata.Function { ... }</code>
   */
  public static final
    org.jetbrains.kotlin.protobuf.GeneratedMessage.GeneratedExtension<
      org.jetbrains.kotlin.metadata.DebugProtoBuf.Function,
      java.util.List<org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation>> functionAnnotation = org.jetbrains.kotlin.protobuf.GeneratedMessage
          .newFileScopedGeneratedExtension(
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.class,
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.getDefaultInstance());
  public static final int PROPERTY_ANNOTATION_FIELD_NUMBER = 150;
  /**
   * <code>extend .org.jetbrains.kotlin.metadata.Property { ... }</code>
   */
  public static final
    org.jetbrains.kotlin.protobuf.GeneratedMessage.GeneratedExtension<
      org.jetbrains.kotlin.metadata.DebugProtoBuf.Property,
      java.util.List<org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation>> propertyAnnotation = org.jetbrains.kotlin.protobuf.GeneratedMessage
          .newFileScopedGeneratedExtension(
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.class,
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.getDefaultInstance());
  public static final int PROPERTY_GETTER_ANNOTATION_FIELD_NUMBER = 152;
  /**
   * <code>extend .org.jetbrains.kotlin.metadata.Property { ... }</code>
   */
  public static final
    org.jetbrains.kotlin.protobuf.GeneratedMessage.GeneratedExtension<
      org.jetbrains.kotlin.metadata.DebugProtoBuf.Property,
      java.util.List<org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation>> propertyGetterAnnotation = org.jetbrains.kotlin.protobuf.GeneratedMessage
          .newFileScopedGeneratedExtension(
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.class,
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.getDefaultInstance());
  public static final int PROPERTY_SETTER_ANNOTATION_FIELD_NUMBER = 153;
  /**
   * <code>extend .org.jetbrains.kotlin.metadata.Property { ... }</code>
   */
  public static final
    org.jetbrains.kotlin.protobuf.GeneratedMessage.GeneratedExtension<
      org.jetbrains.kotlin.metadata.DebugProtoBuf.Property,
      java.util.List<org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation>> propertySetterAnnotation = org.jetbrains.kotlin.protobuf.GeneratedMessage
          .newFileScopedGeneratedExtension(
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.class,
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.getDefaultInstance());
  public static final int COMPILE_TIME_VALUE_FIELD_NUMBER = 151;
  /**
   * <code>extend .org.jetbrains.kotlin.metadata.Property { ... }</code>
   */
  public static final
    org.jetbrains.kotlin.protobuf.GeneratedMessage.GeneratedExtension<
      org.jetbrains.kotlin.metadata.DebugProtoBuf.Property,
      org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.Argument.Value> compileTimeValue = org.jetbrains.kotlin.protobuf.GeneratedMessage
          .newFileScopedGeneratedExtension(
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.Argument.Value.class,
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.Argument.Value.getDefaultInstance());
  public static final int ENUM_ENTRY_ANNOTATION_FIELD_NUMBER = 150;
  /**
   * <code>extend .org.jetbrains.kotlin.metadata.EnumEntry { ... }</code>
   */
  public static final
    org.jetbrains.kotlin.protobuf.GeneratedMessage.GeneratedExtension<
      org.jetbrains.kotlin.metadata.DebugProtoBuf.EnumEntry,
      java.util.List<org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation>> enumEntryAnnotation = org.jetbrains.kotlin.protobuf.GeneratedMessage
          .newFileScopedGeneratedExtension(
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.class,
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.getDefaultInstance());
  public static final int PARAMETER_ANNOTATION_FIELD_NUMBER = 150;
  /**
   * <code>extend .org.jetbrains.kotlin.metadata.ValueParameter { ... }</code>
   */
  public static final
    org.jetbrains.kotlin.protobuf.GeneratedMessage.GeneratedExtension<
      org.jetbrains.kotlin.metadata.DebugProtoBuf.ValueParameter,
      java.util.List<org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation>> parameterAnnotation = org.jetbrains.kotlin.protobuf.GeneratedMessage
          .newFileScopedGeneratedExtension(
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.class,
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.getDefaultInstance());
  public static final int TYPE_ANNOTATION_FIELD_NUMBER = 150;
  /**
   * <code>extend .org.jetbrains.kotlin.metadata.Type { ... }</code>
   */
  public static final
    org.jetbrains.kotlin.protobuf.GeneratedMessage.GeneratedExtension<
      org.jetbrains.kotlin.metadata.DebugProtoBuf.Type,
      java.util.List<org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation>> typeAnnotation = org.jetbrains.kotlin.protobuf.GeneratedMessage
          .newFileScopedGeneratedExtension(
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.class,
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.getDefaultInstance());
  public static final int TYPE_PARAMETER_ANNOTATION_FIELD_NUMBER = 150;
  /**
   * <code>extend .org.jetbrains.kotlin.metadata.TypeParameter { ... }</code>
   */
  public static final
    org.jetbrains.kotlin.protobuf.GeneratedMessage.GeneratedExtension<
      org.jetbrains.kotlin.metadata.DebugProtoBuf.TypeParameter,
      java.util.List<org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation>> typeParameterAnnotation = org.jetbrains.kotlin.protobuf.GeneratedMessage
          .newFileScopedGeneratedExtension(
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.class,
        org.jetbrains.kotlin.metadata.DebugProtoBuf.Annotation.getDefaultInstance());

  public static org.jetbrains.kotlin.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static org.jetbrains.kotlin.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n&core/metadata/src/builtins.debug.proto" +
      "\022&org.jetbrains.kotlin.metadata.builtins" +
      "\032&core/metadata/src/metadata.debug.proto" +
      ":@\n\017package_fq_name\022&.org.jetbrains.kotl" +
      "in.metadata.Package\030\227\001 \001(\005:j\n\020class_anno" +
      "tation\022$.org.jetbrains.kotlin.metadata.C" +
      "lass\030\226\001 \003(\0132).org.jetbrains.kotlin.metad" +
      "ata.Annotation:v\n\026constructor_annotation" +
      "\022*.org.jetbrains.kotlin.metadata.Constru" +
      "ctor\030\226\001 \003(\0132).org.jetbrains.kotlin.metad",
      "ata.Annotation:p\n\023function_annotation\022\'." +
      "org.jetbrains.kotlin.metadata.Function\030\226" +
      "\001 \003(\0132).org.jetbrains.kotlin.metadata.An" +
      "notation:p\n\023property_annotation\022\'.org.je" +
      "tbrains.kotlin.metadata.Property\030\226\001 \003(\0132" +
      ").org.jetbrains.kotlin.metadata.Annotati" +
      "on:w\n\032property_getter_annotation\022\'.org.j" +
      "etbrains.kotlin.metadata.Property\030\230\001 \003(\013" +
      "2).org.jetbrains.kotlin.metadata.Annotat" +
      "ion:w\n\032property_setter_annotation\022\'.org.",
      "jetbrains.kotlin.metadata.Property\030\231\001 \003(" +
      "\0132).org.jetbrains.kotlin.metadata.Annota" +
      "tion:~\n\022compile_time_konstue\022\'.org.jetbrai" +
      "ns.kotlin.metadata.Property\030\227\001 \001(\01328.org" +
      ".jetbrains.kotlin.metadata.Annotation.Ar" +
      "gument.Value:s\n\025enum_entry_annotation\022(." +
      "org.jetbrains.kotlin.metadata.EnumEntry\030" +
      "\226\001 \003(\0132).org.jetbrains.kotlin.metadata.A" +
      "nnotation:w\n\024parameter_annotation\022-.org." +
      "jetbrains.kotlin.metadata.ValueParameter",
      "\030\226\001 \003(\0132).org.jetbrains.kotlin.metadata." +
      "Annotation:h\n\017type_annotation\022#.org.jetb" +
      "rains.kotlin.metadata.Type\030\226\001 \003(\0132).org." +
      "jetbrains.kotlin.metadata.Annotation:{\n\031" +
      "type_parameter_annotation\022,.org.jetbrain" +
      "s.kotlin.metadata.TypeParameter\030\226\001 \003(\0132)" +
      ".org.jetbrains.kotlin.metadata.Annotatio" +
      "nB\027B\025DebugBuiltInsProtoBuf"
    };
    org.jetbrains.kotlin.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new org.jetbrains.kotlin.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public org.jetbrains.kotlin.protobuf.ExtensionRegistry assignDescriptors(
              org.jetbrains.kotlin.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    org.jetbrains.kotlin.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new org.jetbrains.kotlin.protobuf.Descriptors.FileDescriptor[] {
          org.jetbrains.kotlin.metadata.DebugProtoBuf.getDescriptor(),
        }, assigner);
    packageFqName.internalInit(descriptor.getExtensions().get(0));
    classAnnotation.internalInit(descriptor.getExtensions().get(1));
    constructorAnnotation.internalInit(descriptor.getExtensions().get(2));
    functionAnnotation.internalInit(descriptor.getExtensions().get(3));
    propertyAnnotation.internalInit(descriptor.getExtensions().get(4));
    propertyGetterAnnotation.internalInit(descriptor.getExtensions().get(5));
    propertySetterAnnotation.internalInit(descriptor.getExtensions().get(6));
    compileTimeValue.internalInit(descriptor.getExtensions().get(7));
    enumEntryAnnotation.internalInit(descriptor.getExtensions().get(8));
    parameterAnnotation.internalInit(descriptor.getExtensions().get(9));
    typeAnnotation.internalInit(descriptor.getExtensions().get(10));
    typeParameterAnnotation.internalInit(descriptor.getExtensions().get(11));
    org.jetbrains.kotlin.metadata.DebugProtoBuf.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}