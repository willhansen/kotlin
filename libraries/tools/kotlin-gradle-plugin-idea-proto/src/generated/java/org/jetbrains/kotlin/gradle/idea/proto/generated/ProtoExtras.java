// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto_extras.proto

package org.jetbrains.kotlin.gradle.idea.proto.generated;

public final class ProtoExtras {
  private ProtoExtras() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_IdeaExtrasProto_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_IdeaExtrasProto_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_IdeaExtrasProto_ValuesEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_IdeaExtrasProto_ValuesEntry_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\022proto_extras.proto\0220org.jetbrains.kotl" +
      "in.gradle.idea.proto.generated\"\237\001\n\017IdeaE" +
      "xtrasProto\022]\n\006konstues\030\001 \003(\0132M.org.jetbrai" +
      "ns.kotlin.gradle.idea.proto.generated.Id" +
      "eaExtrasProto.ValuesEntry\032-\n\013ValuesEntry" +
      "\022\013\n\003key\030\001 \001(\t\022\r\n\005konstue\030\002 \001(\014:\0028\001B\002P\001b\006pr" +
      "oto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_IdeaExtrasProto_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_IdeaExtrasProto_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_IdeaExtrasProto_descriptor,
        new java.lang.String[] { "Values", });
    internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_IdeaExtrasProto_ValuesEntry_descriptor =
      internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_IdeaExtrasProto_descriptor.getNestedTypes().get(0);
    internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_IdeaExtrasProto_ValuesEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_org_jetbrains_kotlin_gradle_idea_proto_generated_IdeaExtrasProto_ValuesEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
