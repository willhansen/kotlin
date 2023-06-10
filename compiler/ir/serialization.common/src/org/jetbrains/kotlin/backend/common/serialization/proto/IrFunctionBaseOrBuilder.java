// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: compiler/ir/serialization.common/src/KotlinIr.proto

package org.jetbrains.kotlin.backend.common.serialization.proto;

public interface IrFunctionBaseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase)
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
   * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter type_parameter = 3;</code>
   */
  java.util.List<org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter> 
      getTypeParameterList();
  /**
   * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter type_parameter = 3;</code>
   */
  org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter getTypeParameter(int index);
  /**
   * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter type_parameter = 3;</code>
   */
  int getTypeParameterCount();

  /**
   * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrValueParameter dispatch_receiver = 4;</code>
   */
  boolean hasDispatchReceiver();
  /**
   * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrValueParameter dispatch_receiver = 4;</code>
   */
  org.jetbrains.kotlin.backend.common.serialization.proto.IrValueParameter getDispatchReceiver();

  /**
   * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrValueParameter extension_receiver = 5;</code>
   */
  boolean hasExtensionReceiver();
  /**
   * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrValueParameter extension_receiver = 5;</code>
   */
  org.jetbrains.kotlin.backend.common.serialization.proto.IrValueParameter getExtensionReceiver();

  /**
   * <code>optional int32 context_receiver_parameters_count = 8;</code>
   */
  boolean hasContextReceiverParametersCount();
  /**
   * <code>optional int32 context_receiver_parameters_count = 8;</code>
   */
  int getContextReceiverParametersCount();

  /**
   * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrValueParameter konstue_parameter = 6;</code>
   */
  java.util.List<org.jetbrains.kotlin.backend.common.serialization.proto.IrValueParameter> 
      getValueParameterList();
  /**
   * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrValueParameter konstue_parameter = 6;</code>
   */
  org.jetbrains.kotlin.backend.common.serialization.proto.IrValueParameter getValueParameter(int index);
  /**
   * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrValueParameter konstue_parameter = 6;</code>
   */
  int getValueParameterCount();

  /**
   * <code>optional int32 body = 7;</code>
   */
  boolean hasBody();
  /**
   * <code>optional int32 body = 7;</code>
   */
  int getBody();
}