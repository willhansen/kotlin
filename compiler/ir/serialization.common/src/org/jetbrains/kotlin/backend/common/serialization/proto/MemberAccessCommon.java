// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: compiler/ir/serialization.common/src/KotlinIr.proto

package org.jetbrains.kotlin.backend.common.serialization.proto;

/**
 * Protobuf type {@code org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon}
 */
public final class MemberAccessCommon extends
    org.jetbrains.kotlin.protobuf.GeneratedMessageLite implements
    // @@protoc_insertion_point(message_implements:org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon)
    MemberAccessCommonOrBuilder {
  // Use MemberAccessCommon.newBuilder() to construct.
  private MemberAccessCommon(org.jetbrains.kotlin.protobuf.GeneratedMessageLite.Builder builder) {
    super(builder);
    this.unknownFields = builder.getUnknownFields();
  }
  private MemberAccessCommon(boolean noInit) { this.unknownFields = org.jetbrains.kotlin.protobuf.ByteString.EMPTY;}

  private static final MemberAccessCommon defaultInstance;
  public static MemberAccessCommon getDefaultInstance() {
    return defaultInstance;
  }

  public MemberAccessCommon getDefaultInstanceForType() {
    return defaultInstance;
  }

  private final org.jetbrains.kotlin.protobuf.ByteString unknownFields;
  private MemberAccessCommon(
      org.jetbrains.kotlin.protobuf.CodedInputStream input,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    initFields();
    int mutable_bitField0_ = 0;
    org.jetbrains.kotlin.protobuf.ByteString.Output unknownFieldsOutput =
        org.jetbrains.kotlin.protobuf.ByteString.newOutput();
    org.jetbrains.kotlin.protobuf.CodedOutputStream unknownFieldsCodedOutput =
        org.jetbrains.kotlin.protobuf.CodedOutputStream.newInstance(
            unknownFieldsOutput, 1);
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          default: {
            if (!parseUnknownField(input, unknownFieldsCodedOutput,
                                   extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
          case 10: {
            org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.Builder subBuilder = null;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
              subBuilder = dispatchReceiver_.toBuilder();
            }
            dispatchReceiver_ = input.readMessage(org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.PARSER, extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(dispatchReceiver_);
              dispatchReceiver_ = subBuilder.buildPartial();
            }
            bitField0_ |= 0x00000001;
            break;
          }
          case 18: {
            org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.Builder subBuilder = null;
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
              subBuilder = extensionReceiver_.toBuilder();
            }
            extensionReceiver_ = input.readMessage(org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.PARSER, extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(extensionReceiver_);
              extensionReceiver_ = subBuilder.buildPartial();
            }
            bitField0_ |= 0x00000002;
            break;
          }
          case 26: {
            if (!((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
              konstueArgument_ = new java.util.ArrayList<org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression>();
              mutable_bitField0_ |= 0x00000004;
            }
            konstueArgument_.add(input.readMessage(org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression.PARSER, extensionRegistry));
            break;
          }
          case 32: {
            if (!((mutable_bitField0_ & 0x00000008) == 0x00000008)) {
              typeArgument_ = new java.util.ArrayList<java.lang.Integer>();
              mutable_bitField0_ |= 0x00000008;
            }
            typeArgument_.add(input.readInt32());
            break;
          }
          case 34: {
            int length = input.readRawVarint32();
            int limit = input.pushLimit(length);
            if (!((mutable_bitField0_ & 0x00000008) == 0x00000008) && input.getBytesUntilLimit() > 0) {
              typeArgument_ = new java.util.ArrayList<java.lang.Integer>();
              mutable_bitField0_ |= 0x00000008;
            }
            while (input.getBytesUntilLimit() > 0) {
              typeArgument_.add(input.readInt32());
            }
            input.popLimit(limit);
            break;
          }
        }
      }
    } catch (org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException(
          e.getMessage()).setUnfinishedMessage(this);
    } finally {
      if (((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
        konstueArgument_ = java.util.Collections.unmodifiableList(konstueArgument_);
      }
      if (((mutable_bitField0_ & 0x00000008) == 0x00000008)) {
        typeArgument_ = java.util.Collections.unmodifiableList(typeArgument_);
      }
      try {
        unknownFieldsCodedOutput.flush();
      } catch (java.io.IOException e) {
      // Should not happen
      } finally {
        unknownFields = unknownFieldsOutput.toByteString();
      }
      makeExtensionsImmutable();
    }
  }
  public static org.jetbrains.kotlin.protobuf.Parser<MemberAccessCommon> PARSER =
      new org.jetbrains.kotlin.protobuf.AbstractParser<MemberAccessCommon>() {
    public MemberAccessCommon parsePartialFrom(
        org.jetbrains.kotlin.protobuf.CodedInputStream input,
        org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
        throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
      return new MemberAccessCommon(input, extensionRegistry);
    }
  };

  @java.lang.Override
  public org.jetbrains.kotlin.protobuf.Parser<MemberAccessCommon> getParserForType() {
    return PARSER;
  }

  private int bitField0_;
  public static final int DISPATCH_RECEIVER_FIELD_NUMBER = 1;
  private org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression dispatchReceiver_;
  /**
   * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression dispatch_receiver = 1;</code>
   */
  public boolean hasDispatchReceiver() {
    return ((bitField0_ & 0x00000001) == 0x00000001);
  }
  /**
   * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression dispatch_receiver = 1;</code>
   */
  public org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression getDispatchReceiver() {
    return dispatchReceiver_;
  }

  public static final int EXTENSION_RECEIVER_FIELD_NUMBER = 2;
  private org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression extensionReceiver_;
  /**
   * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression extension_receiver = 2;</code>
   */
  public boolean hasExtensionReceiver() {
    return ((bitField0_ & 0x00000002) == 0x00000002);
  }
  /**
   * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression extension_receiver = 2;</code>
   */
  public org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression getExtensionReceiver() {
    return extensionReceiver_;
  }

  public static final int VALUE_ARGUMENT_FIELD_NUMBER = 3;
  private java.util.List<org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression> konstueArgument_;
  /**
   * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression konstue_argument = 3;</code>
   */
  public java.util.List<org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression> getValueArgumentList() {
    return konstueArgument_;
  }
  /**
   * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression konstue_argument = 3;</code>
   */
  public java.util.List<? extends org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpressionOrBuilder> 
      getValueArgumentOrBuilderList() {
    return konstueArgument_;
  }
  /**
   * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression konstue_argument = 3;</code>
   */
  public int getValueArgumentCount() {
    return konstueArgument_.size();
  }
  /**
   * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression konstue_argument = 3;</code>
   */
  public org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression getValueArgument(int index) {
    return konstueArgument_.get(index);
  }
  /**
   * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression konstue_argument = 3;</code>
   */
  public org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpressionOrBuilder getValueArgumentOrBuilder(
      int index) {
    return konstueArgument_.get(index);
  }

  public static final int TYPE_ARGUMENT_FIELD_NUMBER = 4;
  private java.util.List<java.lang.Integer> typeArgument_;
  /**
   * <code>repeated int32 type_argument = 4 [packed = true];</code>
   */
  public java.util.List<java.lang.Integer>
      getTypeArgumentList() {
    return typeArgument_;
  }
  /**
   * <code>repeated int32 type_argument = 4 [packed = true];</code>
   */
  public int getTypeArgumentCount() {
    return typeArgument_.size();
  }
  /**
   * <code>repeated int32 type_argument = 4 [packed = true];</code>
   */
  public int getTypeArgument(int index) {
    return typeArgument_.get(index);
  }
  private int typeArgumentMemoizedSerializedSize = -1;

  private void initFields() {
    dispatchReceiver_ = org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.getDefaultInstance();
    extensionReceiver_ = org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.getDefaultInstance();
    konstueArgument_ = java.util.Collections.emptyList();
    typeArgument_ = java.util.Collections.emptyList();
  }
  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    if (hasDispatchReceiver()) {
      if (!getDispatchReceiver().isInitialized()) {
        memoizedIsInitialized = 0;
        return false;
      }
    }
    if (hasExtensionReceiver()) {
      if (!getExtensionReceiver().isInitialized()) {
        memoizedIsInitialized = 0;
        return false;
      }
    }
    for (int i = 0; i < getValueArgumentCount(); i++) {
      if (!getValueArgument(i).isInitialized()) {
        memoizedIsInitialized = 0;
        return false;
      }
    }
    memoizedIsInitialized = 1;
    return true;
  }

  public void writeTo(org.jetbrains.kotlin.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    getSerializedSize();
    if (((bitField0_ & 0x00000001) == 0x00000001)) {
      output.writeMessage(1, dispatchReceiver_);
    }
    if (((bitField0_ & 0x00000002) == 0x00000002)) {
      output.writeMessage(2, extensionReceiver_);
    }
    for (int i = 0; i < konstueArgument_.size(); i++) {
      output.writeMessage(3, konstueArgument_.get(i));
    }
    if (getTypeArgumentList().size() > 0) {
      output.writeRawVarint32(34);
      output.writeRawVarint32(typeArgumentMemoizedSerializedSize);
    }
    for (int i = 0; i < typeArgument_.size(); i++) {
      output.writeInt32NoTag(typeArgument_.get(i));
    }
    output.writeRawBytes(unknownFields);
  }

  private int memoizedSerializedSize = -1;
  public int getSerializedSize() {
    int size = memoizedSerializedSize;
    if (size != -1) return size;

    size = 0;
    if (((bitField0_ & 0x00000001) == 0x00000001)) {
      size += org.jetbrains.kotlin.protobuf.CodedOutputStream
        .computeMessageSize(1, dispatchReceiver_);
    }
    if (((bitField0_ & 0x00000002) == 0x00000002)) {
      size += org.jetbrains.kotlin.protobuf.CodedOutputStream
        .computeMessageSize(2, extensionReceiver_);
    }
    for (int i = 0; i < konstueArgument_.size(); i++) {
      size += org.jetbrains.kotlin.protobuf.CodedOutputStream
        .computeMessageSize(3, konstueArgument_.get(i));
    }
    {
      int dataSize = 0;
      for (int i = 0; i < typeArgument_.size(); i++) {
        dataSize += org.jetbrains.kotlin.protobuf.CodedOutputStream
          .computeInt32SizeNoTag(typeArgument_.get(i));
      }
      size += dataSize;
      if (!getTypeArgumentList().isEmpty()) {
        size += 1;
        size += org.jetbrains.kotlin.protobuf.CodedOutputStream
            .computeInt32SizeNoTag(dataSize);
      }
      typeArgumentMemoizedSerializedSize = dataSize;
    }
    size += unknownFields.size();
    memoizedSerializedSize = size;
    return size;
  }

  private static final long serialVersionUID = 0L;
  @java.lang.Override
  protected java.lang.Object writeReplace()
      throws java.io.ObjectStreamException {
    return super.writeReplace();
  }

  public static org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon parseFrom(
      org.jetbrains.kotlin.protobuf.ByteString data)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon parseFrom(
      org.jetbrains.kotlin.protobuf.ByteString data,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon parseFrom(byte[] data)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon parseFrom(
      byte[] data,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return PARSER.parseFrom(input);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon parseFrom(
      java.io.InputStream input,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return PARSER.parseFrom(input, extensionRegistry);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return PARSER.parseDelimitedFrom(input);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon parseDelimitedFrom(
      java.io.InputStream input,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return PARSER.parseDelimitedFrom(input, extensionRegistry);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon parseFrom(
      org.jetbrains.kotlin.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return PARSER.parseFrom(input);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon parseFrom(
      org.jetbrains.kotlin.protobuf.CodedInputStream input,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return PARSER.parseFrom(input, extensionRegistry);
  }

  public static Builder newBuilder() { return Builder.create(); }
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder(org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon prototype) {
    return newBuilder().mergeFrom(prototype);
  }
  public Builder toBuilder() { return newBuilder(this); }

  /**
   * Protobuf type {@code org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon}
   */
  public static final class Builder extends
      org.jetbrains.kotlin.protobuf.GeneratedMessageLite.Builder<
        org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon, Builder>
      implements
      // @@protoc_insertion_point(builder_implements:org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon)
      org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommonOrBuilder {
    // Construct using org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private void maybeForceBuilderInitialization() {
    }
    private static Builder create() {
      return new Builder();
    }

    public Builder clear() {
      super.clear();
      dispatchReceiver_ = org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.getDefaultInstance();
      bitField0_ = (bitField0_ & ~0x00000001);
      extensionReceiver_ = org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.getDefaultInstance();
      bitField0_ = (bitField0_ & ~0x00000002);
      konstueArgument_ = java.util.Collections.emptyList();
      bitField0_ = (bitField0_ & ~0x00000004);
      typeArgument_ = java.util.Collections.emptyList();
      bitField0_ = (bitField0_ & ~0x00000008);
      return this;
    }

    public Builder clone() {
      return create().mergeFrom(buildPartial());
    }

    public org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon getDefaultInstanceForType() {
      return org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon.getDefaultInstance();
    }

    public org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon build() {
      org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon buildPartial() {
      org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon result = new org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
        to_bitField0_ |= 0x00000001;
      }
      result.dispatchReceiver_ = dispatchReceiver_;
      if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
        to_bitField0_ |= 0x00000002;
      }
      result.extensionReceiver_ = extensionReceiver_;
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        konstueArgument_ = java.util.Collections.unmodifiableList(konstueArgument_);
        bitField0_ = (bitField0_ & ~0x00000004);
      }
      result.konstueArgument_ = konstueArgument_;
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        typeArgument_ = java.util.Collections.unmodifiableList(typeArgument_);
        bitField0_ = (bitField0_ & ~0x00000008);
      }
      result.typeArgument_ = typeArgument_;
      result.bitField0_ = to_bitField0_;
      return result;
    }

    public Builder mergeFrom(org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon other) {
      if (other == org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon.getDefaultInstance()) return this;
      if (other.hasDispatchReceiver()) {
        mergeDispatchReceiver(other.getDispatchReceiver());
      }
      if (other.hasExtensionReceiver()) {
        mergeExtensionReceiver(other.getExtensionReceiver());
      }
      if (!other.konstueArgument_.isEmpty()) {
        if (konstueArgument_.isEmpty()) {
          konstueArgument_ = other.konstueArgument_;
          bitField0_ = (bitField0_ & ~0x00000004);
        } else {
          ensureValueArgumentIsMutable();
          konstueArgument_.addAll(other.konstueArgument_);
        }
        
      }
      if (!other.typeArgument_.isEmpty()) {
        if (typeArgument_.isEmpty()) {
          typeArgument_ = other.typeArgument_;
          bitField0_ = (bitField0_ & ~0x00000008);
        } else {
          ensureTypeArgumentIsMutable();
          typeArgument_.addAll(other.typeArgument_);
        }
        
      }
      setUnknownFields(
          getUnknownFields().concat(other.unknownFields));
      return this;
    }

    public final boolean isInitialized() {
      if (hasDispatchReceiver()) {
        if (!getDispatchReceiver().isInitialized()) {
          
          return false;
        }
      }
      if (hasExtensionReceiver()) {
        if (!getExtensionReceiver().isInitialized()) {
          
          return false;
        }
      }
      for (int i = 0; i < getValueArgumentCount(); i++) {
        if (!getValueArgument(i).isInitialized()) {
          
          return false;
        }
      }
      return true;
    }

    public Builder mergeFrom(
        org.jetbrains.kotlin.protobuf.CodedInputStream input,
        org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException e) {
        parsedMessage = (org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon) e.getUnfinishedMessage();
        throw e;
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression dispatchReceiver_ = org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.getDefaultInstance();
    /**
     * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression dispatch_receiver = 1;</code>
     */
    public boolean hasDispatchReceiver() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression dispatch_receiver = 1;</code>
     */
    public org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression getDispatchReceiver() {
      return dispatchReceiver_;
    }
    /**
     * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression dispatch_receiver = 1;</code>
     */
    public Builder setDispatchReceiver(org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression konstue) {
      if (konstue == null) {
        throw new NullPointerException();
      }
      dispatchReceiver_ = konstue;

      bitField0_ |= 0x00000001;
      return this;
    }
    /**
     * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression dispatch_receiver = 1;</code>
     */
    public Builder setDispatchReceiver(
        org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.Builder builderForValue) {
      dispatchReceiver_ = builderForValue.build();

      bitField0_ |= 0x00000001;
      return this;
    }
    /**
     * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression dispatch_receiver = 1;</code>
     */
    public Builder mergeDispatchReceiver(org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression konstue) {
      if (((bitField0_ & 0x00000001) == 0x00000001) &&
          dispatchReceiver_ != org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.getDefaultInstance()) {
        dispatchReceiver_ =
          org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.newBuilder(dispatchReceiver_).mergeFrom(konstue).buildPartial();
      } else {
        dispatchReceiver_ = konstue;
      }

      bitField0_ |= 0x00000001;
      return this;
    }
    /**
     * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression dispatch_receiver = 1;</code>
     */
    public Builder clearDispatchReceiver() {
      dispatchReceiver_ = org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.getDefaultInstance();

      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }

    private org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression extensionReceiver_ = org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.getDefaultInstance();
    /**
     * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression extension_receiver = 2;</code>
     */
    public boolean hasExtensionReceiver() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression extension_receiver = 2;</code>
     */
    public org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression getExtensionReceiver() {
      return extensionReceiver_;
    }
    /**
     * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression extension_receiver = 2;</code>
     */
    public Builder setExtensionReceiver(org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression konstue) {
      if (konstue == null) {
        throw new NullPointerException();
      }
      extensionReceiver_ = konstue;

      bitField0_ |= 0x00000002;
      return this;
    }
    /**
     * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression extension_receiver = 2;</code>
     */
    public Builder setExtensionReceiver(
        org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.Builder builderForValue) {
      extensionReceiver_ = builderForValue.build();

      bitField0_ |= 0x00000002;
      return this;
    }
    /**
     * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression extension_receiver = 2;</code>
     */
    public Builder mergeExtensionReceiver(org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression konstue) {
      if (((bitField0_ & 0x00000002) == 0x00000002) &&
          extensionReceiver_ != org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.getDefaultInstance()) {
        extensionReceiver_ =
          org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.newBuilder(extensionReceiver_).mergeFrom(konstue).buildPartial();
      } else {
        extensionReceiver_ = konstue;
      }

      bitField0_ |= 0x00000002;
      return this;
    }
    /**
     * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression extension_receiver = 2;</code>
     */
    public Builder clearExtensionReceiver() {
      extensionReceiver_ = org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.getDefaultInstance();

      bitField0_ = (bitField0_ & ~0x00000002);
      return this;
    }

    private java.util.List<org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression> konstueArgument_ =
      java.util.Collections.emptyList();
    private void ensureValueArgumentIsMutable() {
      if (!((bitField0_ & 0x00000004) == 0x00000004)) {
        konstueArgument_ = new java.util.ArrayList<org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression>(konstueArgument_);
        bitField0_ |= 0x00000004;
       }
    }

    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression konstue_argument = 3;</code>
     */
    public java.util.List<org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression> getValueArgumentList() {
      return java.util.Collections.unmodifiableList(konstueArgument_);
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression konstue_argument = 3;</code>
     */
    public int getValueArgumentCount() {
      return konstueArgument_.size();
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression konstue_argument = 3;</code>
     */
    public org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression getValueArgument(int index) {
      return konstueArgument_.get(index);
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression konstue_argument = 3;</code>
     */
    public Builder setValueArgument(
        int index, org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression konstue) {
      if (konstue == null) {
        throw new NullPointerException();
      }
      ensureValueArgumentIsMutable();
      konstueArgument_.set(index, konstue);

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression konstue_argument = 3;</code>
     */
    public Builder setValueArgument(
        int index, org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression.Builder builderForValue) {
      ensureValueArgumentIsMutable();
      konstueArgument_.set(index, builderForValue.build());

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression konstue_argument = 3;</code>
     */
    public Builder addValueArgument(org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression konstue) {
      if (konstue == null) {
        throw new NullPointerException();
      }
      ensureValueArgumentIsMutable();
      konstueArgument_.add(konstue);

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression konstue_argument = 3;</code>
     */
    public Builder addValueArgument(
        int index, org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression konstue) {
      if (konstue == null) {
        throw new NullPointerException();
      }
      ensureValueArgumentIsMutable();
      konstueArgument_.add(index, konstue);

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression konstue_argument = 3;</code>
     */
    public Builder addValueArgument(
        org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression.Builder builderForValue) {
      ensureValueArgumentIsMutable();
      konstueArgument_.add(builderForValue.build());

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression konstue_argument = 3;</code>
     */
    public Builder addValueArgument(
        int index, org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression.Builder builderForValue) {
      ensureValueArgumentIsMutable();
      konstueArgument_.add(index, builderForValue.build());

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression konstue_argument = 3;</code>
     */
    public Builder addAllValueArgument(
        java.lang.Iterable<? extends org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression> konstues) {
      ensureValueArgumentIsMutable();
      org.jetbrains.kotlin.protobuf.AbstractMessageLite.Builder.addAll(
          konstues, konstueArgument_);

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression konstue_argument = 3;</code>
     */
    public Builder clearValueArgument() {
      konstueArgument_ = java.util.Collections.emptyList();
      bitField0_ = (bitField0_ & ~0x00000004);

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.NullableIrExpression konstue_argument = 3;</code>
     */
    public Builder removeValueArgument(int index) {
      ensureValueArgumentIsMutable();
      konstueArgument_.remove(index);

      return this;
    }

    private java.util.List<java.lang.Integer> typeArgument_ = java.util.Collections.emptyList();
    private void ensureTypeArgumentIsMutable() {
      if (!((bitField0_ & 0x00000008) == 0x00000008)) {
        typeArgument_ = new java.util.ArrayList<java.lang.Integer>(typeArgument_);
        bitField0_ |= 0x00000008;
       }
    }
    /**
     * <code>repeated int32 type_argument = 4 [packed = true];</code>
     */
    public java.util.List<java.lang.Integer>
        getTypeArgumentList() {
      return java.util.Collections.unmodifiableList(typeArgument_);
    }
    /**
     * <code>repeated int32 type_argument = 4 [packed = true];</code>
     */
    public int getTypeArgumentCount() {
      return typeArgument_.size();
    }
    /**
     * <code>repeated int32 type_argument = 4 [packed = true];</code>
     */
    public int getTypeArgument(int index) {
      return typeArgument_.get(index);
    }
    /**
     * <code>repeated int32 type_argument = 4 [packed = true];</code>
     */
    public Builder setTypeArgument(
        int index, int konstue) {
      ensureTypeArgumentIsMutable();
      typeArgument_.set(index, konstue);
      
      return this;
    }
    /**
     * <code>repeated int32 type_argument = 4 [packed = true];</code>
     */
    public Builder addTypeArgument(int konstue) {
      ensureTypeArgumentIsMutable();
      typeArgument_.add(konstue);
      
      return this;
    }
    /**
     * <code>repeated int32 type_argument = 4 [packed = true];</code>
     */
    public Builder addAllTypeArgument(
        java.lang.Iterable<? extends java.lang.Integer> konstues) {
      ensureTypeArgumentIsMutable();
      org.jetbrains.kotlin.protobuf.AbstractMessageLite.Builder.addAll(
          konstues, typeArgument_);
      
      return this;
    }
    /**
     * <code>repeated int32 type_argument = 4 [packed = true];</code>
     */
    public Builder clearTypeArgument() {
      typeArgument_ = java.util.Collections.emptyList();
      bitField0_ = (bitField0_ & ~0x00000008);
      
      return this;
    }

    // @@protoc_insertion_point(builder_scope:org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon)
  }

  static {
    defaultInstance = new MemberAccessCommon(true);
    defaultInstance.initFields();
  }

  // @@protoc_insertion_point(class_scope:org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon)
}
