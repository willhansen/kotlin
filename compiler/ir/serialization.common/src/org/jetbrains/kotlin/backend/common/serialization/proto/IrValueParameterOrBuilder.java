// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: compiler/ir/serialization.common/src/KotlinIr.proto

package org.jetbrains.kotlin.backend.common.serialization.proto;

public interface IrValueParameterOrBuilder extends
    // @@protoc_insertion_point(interface_extends:org.jetbrains.kotlin.backend.common.serialization.proto.IrValueParameter)
    org.jetbrains.kotlin.protobuf.MessageLiteOrBuilder {

  /**
   * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase base = 1;</code>
   */
  boolean hasBase();
  /**
   * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase base = 1;</code>
   */
  org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase getBase();

  /**
   * <code>required int64 name_type = 2;</code>
   */
  boolean hasNameType();
  /**
   * <code>required int64 name_type = 2;</code>
   */
  long getNameType();

  /**
   * <code>optional int32 vararg_element_type = 3;</code>
   */
  boolean hasVarargElementType();
  /**
   * <code>optional int32 vararg_element_type = 3;</code>
   */
  int getVarargElementType();

  /**
   * <code>optional int32 default_konstue = 4;</code>
   */
  boolean hasDefaultValue();
  /**
   * <code>optional int32 default_konstue = 4;</code>
   */
  int getDefaultValue();
}