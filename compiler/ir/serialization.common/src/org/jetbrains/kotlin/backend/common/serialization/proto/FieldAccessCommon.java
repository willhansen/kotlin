// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: compiler/ir/serialization.common/src/KotlinIr.proto

package org.jetbrains.kotlin.backend.common.serialization.proto;

/**
 * Protobuf type {@code org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon}
 */
public final class FieldAccessCommon extends
    org.jetbrains.kotlin.protobuf.GeneratedMessageLite implements
    // @@protoc_insertion_point(message_implements:org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon)
    FieldAccessCommonOrBuilder {
  // Use FieldAccessCommon.newBuilder() to construct.
  private FieldAccessCommon(org.jetbrains.kotlin.protobuf.GeneratedMessageLite.Builder builder) {
    super(builder);
    this.unknownFields = builder.getUnknownFields();
  }
  private FieldAccessCommon(boolean noInit) { this.unknownFields = org.jetbrains.kotlin.protobuf.ByteString.EMPTY;}

  private static final FieldAccessCommon defaultInstance;
  public static FieldAccessCommon getDefaultInstance() {
    return defaultInstance;
  }

  public FieldAccessCommon getDefaultInstanceForType() {
    return defaultInstance;
  }

  private final org.jetbrains.kotlin.protobuf.ByteString unknownFields;
  private FieldAccessCommon(
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
          case 8: {
            bitField0_ |= 0x00000001;
            symbol_ = input.readInt64();
            break;
          }
          case 16: {
            bitField0_ |= 0x00000002;
            super_ = input.readInt64();
            break;
          }
          case 26: {
            org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.Builder subBuilder = null;
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
              subBuilder = receiver_.toBuilder();
            }
            receiver_ = input.readMessage(org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.PARSER, extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(receiver_);
              receiver_ = subBuilder.buildPartial();
            }
            bitField0_ |= 0x00000004;
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
  public static org.jetbrains.kotlin.protobuf.Parser<FieldAccessCommon> PARSER =
      new org.jetbrains.kotlin.protobuf.AbstractParser<FieldAccessCommon>() {
    public FieldAccessCommon parsePartialFrom(
        org.jetbrains.kotlin.protobuf.CodedInputStream input,
        org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
        throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
      return new FieldAccessCommon(input, extensionRegistry);
    }
  };

  @java.lang.Override
  public org.jetbrains.kotlin.protobuf.Parser<FieldAccessCommon> getParserForType() {
    return PARSER;
  }

  private int bitField0_;
  public static final int SYMBOL_FIELD_NUMBER = 1;
  private long symbol_;
  /**
   * <code>required int64 symbol = 1;</code>
   */
  public boolean hasSymbol() {
    return ((bitField0_ & 0x00000001) == 0x00000001);
  }
  /**
   * <code>required int64 symbol = 1;</code>
   */
  public long getSymbol() {
    return symbol_;
  }

  public static final int SUPER_FIELD_NUMBER = 2;
  private long super_;
  /**
   * <code>optional int64 super = 2;</code>
   */
  public boolean hasSuper() {
    return ((bitField0_ & 0x00000002) == 0x00000002);
  }
  /**
   * <code>optional int64 super = 2;</code>
   */
  public long getSuper() {
    return super_;
  }

  public static final int RECEIVER_FIELD_NUMBER = 3;
  private org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression receiver_;
  /**
   * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression receiver = 3;</code>
   */
  public boolean hasReceiver() {
    return ((bitField0_ & 0x00000004) == 0x00000004);
  }
  /**
   * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression receiver = 3;</code>
   */
  public org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression getReceiver() {
    return receiver_;
  }

  private void initFields() {
    symbol_ = 0L;
    super_ = 0L;
    receiver_ = org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.getDefaultInstance();
  }
  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    if (!hasSymbol()) {
      memoizedIsInitialized = 0;
      return false;
    }
    if (hasReceiver()) {
      if (!getReceiver().isInitialized()) {
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
      output.writeInt64(1, symbol_);
    }
    if (((bitField0_ & 0x00000002) == 0x00000002)) {
      output.writeInt64(2, super_);
    }
    if (((bitField0_ & 0x00000004) == 0x00000004)) {
      output.writeMessage(3, receiver_);
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
        .computeInt64Size(1, symbol_);
    }
    if (((bitField0_ & 0x00000002) == 0x00000002)) {
      size += org.jetbrains.kotlin.protobuf.CodedOutputStream
        .computeInt64Size(2, super_);
    }
    if (((bitField0_ & 0x00000004) == 0x00000004)) {
      size += org.jetbrains.kotlin.protobuf.CodedOutputStream
        .computeMessageSize(3, receiver_);
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

  public static org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon parseFrom(
      org.jetbrains.kotlin.protobuf.ByteString data)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon parseFrom(
      org.jetbrains.kotlin.protobuf.ByteString data,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon parseFrom(byte[] data)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon parseFrom(
      byte[] data,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return PARSER.parseFrom(input);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon parseFrom(
      java.io.InputStream input,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return PARSER.parseFrom(input, extensionRegistry);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return PARSER.parseDelimitedFrom(input);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon parseDelimitedFrom(
      java.io.InputStream input,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return PARSER.parseDelimitedFrom(input, extensionRegistry);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon parseFrom(
      org.jetbrains.kotlin.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return PARSER.parseFrom(input);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon parseFrom(
      org.jetbrains.kotlin.protobuf.CodedInputStream input,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return PARSER.parseFrom(input, extensionRegistry);
  }

  public static Builder newBuilder() { return Builder.create(); }
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder(org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon prototype) {
    return newBuilder().mergeFrom(prototype);
  }
  public Builder toBuilder() { return newBuilder(this); }

  /**
   * Protobuf type {@code org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon}
   */
  public static final class Builder extends
      org.jetbrains.kotlin.protobuf.GeneratedMessageLite.Builder<
        org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon, Builder>
      implements
      // @@protoc_insertion_point(builder_implements:org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon)
      org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommonOrBuilder {
    // Construct using org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon.newBuilder()
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
      symbol_ = 0L;
      bitField0_ = (bitField0_ & ~0x00000001);
      super_ = 0L;
      bitField0_ = (bitField0_ & ~0x00000002);
      receiver_ = org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.getDefaultInstance();
      bitField0_ = (bitField0_ & ~0x00000004);
      return this;
    }

    public Builder clone() {
      return create().mergeFrom(buildPartial());
    }

    public org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon getDefaultInstanceForType() {
      return org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon.getDefaultInstance();
    }

    public org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon build() {
      org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon buildPartial() {
      org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon result = new org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
        to_bitField0_ |= 0x00000001;
      }
      result.symbol_ = symbol_;
      if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
        to_bitField0_ |= 0x00000002;
      }
      result.super_ = super_;
      if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
        to_bitField0_ |= 0x00000004;
      }
      result.receiver_ = receiver_;
      result.bitField0_ = to_bitField0_;
      return result;
    }

    public Builder mergeFrom(org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon other) {
      if (other == org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon.getDefaultInstance()) return this;
      if (other.hasSymbol()) {
        setSymbol(other.getSymbol());
      }
      if (other.hasSuper()) {
        setSuper(other.getSuper());
      }
      if (other.hasReceiver()) {
        mergeReceiver(other.getReceiver());
      }
      setUnknownFields(
          getUnknownFields().concat(other.unknownFields));
      return this;
    }

    public final boolean isInitialized() {
      if (!hasSymbol()) {
        
        return false;
      }
      if (hasReceiver()) {
        if (!getReceiver().isInitialized()) {
          
          return false;
        }
      }
      return true;
    }

    public Builder mergeFrom(
        org.jetbrains.kotlin.protobuf.CodedInputStream input,
        org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException e) {
        parsedMessage = (org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon) e.getUnfinishedMessage();
        throw e;
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private long symbol_ ;
    /**
     * <code>required int64 symbol = 1;</code>
     */
    public boolean hasSymbol() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>required int64 symbol = 1;</code>
     */
    public long getSymbol() {
      return symbol_;
    }
    /**
     * <code>required int64 symbol = 1;</code>
     */
    public Builder setSymbol(long konstue) {
      bitField0_ |= 0x00000001;
      symbol_ = konstue;
      
      return this;
    }
    /**
     * <code>required int64 symbol = 1;</code>
     */
    public Builder clearSymbol() {
      bitField0_ = (bitField0_ & ~0x00000001);
      symbol_ = 0L;
      
      return this;
    }

    private long super_ ;
    /**
     * <code>optional int64 super = 2;</code>
     */
    public boolean hasSuper() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>optional int64 super = 2;</code>
     */
    public long getSuper() {
      return super_;
    }
    /**
     * <code>optional int64 super = 2;</code>
     */
    public Builder setSuper(long konstue) {
      bitField0_ |= 0x00000002;
      super_ = konstue;
      
      return this;
    }
    /**
     * <code>optional int64 super = 2;</code>
     */
    public Builder clearSuper() {
      bitField0_ = (bitField0_ & ~0x00000002);
      super_ = 0L;
      
      return this;
    }

    private org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression receiver_ = org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.getDefaultInstance();
    /**
     * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression receiver = 3;</code>
     */
    public boolean hasReceiver() {
      return ((bitField0_ & 0x00000004) == 0x00000004);
    }
    /**
     * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression receiver = 3;</code>
     */
    public org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression getReceiver() {
      return receiver_;
    }
    /**
     * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression receiver = 3;</code>
     */
    public Builder setReceiver(org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression konstue) {
      if (konstue == null) {
        throw new NullPointerException();
      }
      receiver_ = konstue;

      bitField0_ |= 0x00000004;
      return this;
    }
    /**
     * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression receiver = 3;</code>
     */
    public Builder setReceiver(
        org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.Builder builderForValue) {
      receiver_ = builderForValue.build();

      bitField0_ |= 0x00000004;
      return this;
    }
    /**
     * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression receiver = 3;</code>
     */
    public Builder mergeReceiver(org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression konstue) {
      if (((bitField0_ & 0x00000004) == 0x00000004) &&
          receiver_ != org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.getDefaultInstance()) {
        receiver_ =
          org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.newBuilder(receiver_).mergeFrom(konstue).buildPartial();
      } else {
        receiver_ = konstue;
      }

      bitField0_ |= 0x00000004;
      return this;
    }
    /**
     * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression receiver = 3;</code>
     */
    public Builder clearReceiver() {
      receiver_ = org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression.getDefaultInstance();

      bitField0_ = (bitField0_ & ~0x00000004);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon)
  }

  static {
    defaultInstance = new FieldAccessCommon(true);
    defaultInstance.initFields();
  }

  // @@protoc_insertion_point(class_scope:org.jetbrains.kotlin.backend.common.serialization.proto.FieldAccessCommon)
}
