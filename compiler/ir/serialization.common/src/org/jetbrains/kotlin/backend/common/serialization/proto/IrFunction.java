// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: compiler/ir/serialization.common/src/KotlinIr.proto

package org.jetbrains.kotlin.backend.common.serialization.proto;

/**
 * Protobuf type {@code org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction}
 */
public final class IrFunction extends
    org.jetbrains.kotlin.protobuf.GeneratedMessageLite implements
    // @@protoc_insertion_point(message_implements:org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction)
    IrFunctionOrBuilder {
  // Use IrFunction.newBuilder() to construct.
  private IrFunction(org.jetbrains.kotlin.protobuf.GeneratedMessageLite.Builder builder) {
    super(builder);
    this.unknownFields = builder.getUnknownFields();
  }
  private IrFunction(boolean noInit) { this.unknownFields = org.jetbrains.kotlin.protobuf.ByteString.EMPTY;}

  private static final IrFunction defaultInstance;
  public static IrFunction getDefaultInstance() {
    return defaultInstance;
  }

  public IrFunction getDefaultInstanceForType() {
    return defaultInstance;
  }

  private final org.jetbrains.kotlin.protobuf.ByteString unknownFields;
  private IrFunction(
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
            org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase.Builder subBuilder = null;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
              subBuilder = base_.toBuilder();
            }
            base_ = input.readMessage(org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase.PARSER, extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(base_);
              base_ = subBuilder.buildPartial();
            }
            bitField0_ |= 0x00000001;
            break;
          }
          case 16: {
            if (!((mutable_bitField0_ & 0x00000002) == 0x00000002)) {
              overridden_ = new java.util.ArrayList<java.lang.Long>();
              mutable_bitField0_ |= 0x00000002;
            }
            overridden_.add(input.readInt64());
            break;
          }
          case 18: {
            int length = input.readRawVarint32();
            int limit = input.pushLimit(length);
            if (!((mutable_bitField0_ & 0x00000002) == 0x00000002) && input.getBytesUntilLimit() > 0) {
              overridden_ = new java.util.ArrayList<java.lang.Long>();
              mutable_bitField0_ |= 0x00000002;
            }
            while (input.getBytesUntilLimit() > 0) {
              overridden_.add(input.readInt64());
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
      if (((mutable_bitField0_ & 0x00000002) == 0x00000002)) {
        overridden_ = java.util.Collections.unmodifiableList(overridden_);
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
  public static org.jetbrains.kotlin.protobuf.Parser<IrFunction> PARSER =
      new org.jetbrains.kotlin.protobuf.AbstractParser<IrFunction>() {
    public IrFunction parsePartialFrom(
        org.jetbrains.kotlin.protobuf.CodedInputStream input,
        org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
        throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
      return new IrFunction(input, extensionRegistry);
    }
  };

  @java.lang.Override
  public org.jetbrains.kotlin.protobuf.Parser<IrFunction> getParserForType() {
    return PARSER;
  }

  private int bitField0_;
  public static final int BASE_FIELD_NUMBER = 1;
  private org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase base_;
  /**
   * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase base = 1;</code>
   */
  public boolean hasBase() {
    return ((bitField0_ & 0x00000001) == 0x00000001);
  }
  /**
   * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase base = 1;</code>
   */
  public org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase getBase() {
    return base_;
  }

  public static final int OVERRIDDEN_FIELD_NUMBER = 2;
  private java.util.List<java.lang.Long> overridden_;
  /**
   * <code>repeated int64 overridden = 2 [packed = true];</code>
   *
   * <pre>
   * TODO: supposed to be deleted
   * </pre>
   */
  public java.util.List<java.lang.Long>
      getOverriddenList() {
    return overridden_;
  }
  /**
   * <code>repeated int64 overridden = 2 [packed = true];</code>
   *
   * <pre>
   * TODO: supposed to be deleted
   * </pre>
   */
  public int getOverriddenCount() {
    return overridden_.size();
  }
  /**
   * <code>repeated int64 overridden = 2 [packed = true];</code>
   *
   * <pre>
   * TODO: supposed to be deleted
   * </pre>
   */
  public long getOverridden(int index) {
    return overridden_.get(index);
  }
  private int overriddenMemoizedSerializedSize = -1;

  private void initFields() {
    base_ = org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase.getDefaultInstance();
    overridden_ = java.util.Collections.emptyList();
  }
  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    if (!hasBase()) {
      memoizedIsInitialized = 0;
      return false;
    }
    if (!getBase().isInitialized()) {
      memoizedIsInitialized = 0;
      return false;
    }
    memoizedIsInitialized = 1;
    return true;
  }

  public void writeTo(org.jetbrains.kotlin.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    getSerializedSize();
    if (((bitField0_ & 0x00000001) == 0x00000001)) {
      output.writeMessage(1, base_);
    }
    if (getOverriddenList().size() > 0) {
      output.writeRawVarint32(18);
      output.writeRawVarint32(overriddenMemoizedSerializedSize);
    }
    for (int i = 0; i < overridden_.size(); i++) {
      output.writeInt64NoTag(overridden_.get(i));
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
        .computeMessageSize(1, base_);
    }
    {
      int dataSize = 0;
      for (int i = 0; i < overridden_.size(); i++) {
        dataSize += org.jetbrains.kotlin.protobuf.CodedOutputStream
          .computeInt64SizeNoTag(overridden_.get(i));
      }
      size += dataSize;
      if (!getOverriddenList().isEmpty()) {
        size += 1;
        size += org.jetbrains.kotlin.protobuf.CodedOutputStream
            .computeInt32SizeNoTag(dataSize);
      }
      overriddenMemoizedSerializedSize = dataSize;
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

  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction parseFrom(
      org.jetbrains.kotlin.protobuf.ByteString data)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction parseFrom(
      org.jetbrains.kotlin.protobuf.ByteString data,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction parseFrom(byte[] data)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction parseFrom(
      byte[] data,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return PARSER.parseFrom(input);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction parseFrom(
      java.io.InputStream input,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return PARSER.parseFrom(input, extensionRegistry);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return PARSER.parseDelimitedFrom(input);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction parseDelimitedFrom(
      java.io.InputStream input,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return PARSER.parseDelimitedFrom(input, extensionRegistry);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction parseFrom(
      org.jetbrains.kotlin.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return PARSER.parseFrom(input);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction parseFrom(
      org.jetbrains.kotlin.protobuf.CodedInputStream input,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return PARSER.parseFrom(input, extensionRegistry);
  }

  public static Builder newBuilder() { return Builder.create(); }
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder(org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction prototype) {
    return newBuilder().mergeFrom(prototype);
  }
  public Builder toBuilder() { return newBuilder(this); }

  /**
   * Protobuf type {@code org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction}
   */
  public static final class Builder extends
      org.jetbrains.kotlin.protobuf.GeneratedMessageLite.Builder<
        org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction, Builder>
      implements
      // @@protoc_insertion_point(builder_implements:org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction)
      org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionOrBuilder {
    // Construct using org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction.newBuilder()
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
      base_ = org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase.getDefaultInstance();
      bitField0_ = (bitField0_ & ~0x00000001);
      overridden_ = java.util.Collections.emptyList();
      bitField0_ = (bitField0_ & ~0x00000002);
      return this;
    }

    public Builder clone() {
      return create().mergeFrom(buildPartial());
    }

    public org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction getDefaultInstanceForType() {
      return org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction.getDefaultInstance();
    }

    public org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction build() {
      org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction buildPartial() {
      org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction result = new org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
        to_bitField0_ |= 0x00000001;
      }
      result.base_ = base_;
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        overridden_ = java.util.Collections.unmodifiableList(overridden_);
        bitField0_ = (bitField0_ & ~0x00000002);
      }
      result.overridden_ = overridden_;
      result.bitField0_ = to_bitField0_;
      return result;
    }

    public Builder mergeFrom(org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction other) {
      if (other == org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction.getDefaultInstance()) return this;
      if (other.hasBase()) {
        mergeBase(other.getBase());
      }
      if (!other.overridden_.isEmpty()) {
        if (overridden_.isEmpty()) {
          overridden_ = other.overridden_;
          bitField0_ = (bitField0_ & ~0x00000002);
        } else {
          ensureOverriddenIsMutable();
          overridden_.addAll(other.overridden_);
        }
        
      }
      setUnknownFields(
          getUnknownFields().concat(other.unknownFields));
      return this;
    }

    public final boolean isInitialized() {
      if (!hasBase()) {
        
        return false;
      }
      if (!getBase().isInitialized()) {
        
        return false;
      }
      return true;
    }

    public Builder mergeFrom(
        org.jetbrains.kotlin.protobuf.CodedInputStream input,
        org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException e) {
        parsedMessage = (org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction) e.getUnfinishedMessage();
        throw e;
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase base_ = org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase.getDefaultInstance();
    /**
     * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase base = 1;</code>
     */
    public boolean hasBase() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase base = 1;</code>
     */
    public org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase getBase() {
      return base_;
    }
    /**
     * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase base = 1;</code>
     */
    public Builder setBase(org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase konstue) {
      if (konstue == null) {
        throw new NullPointerException();
      }
      base_ = konstue;

      bitField0_ |= 0x00000001;
      return this;
    }
    /**
     * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase base = 1;</code>
     */
    public Builder setBase(
        org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase.Builder builderForValue) {
      base_ = builderForValue.build();

      bitField0_ |= 0x00000001;
      return this;
    }
    /**
     * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase base = 1;</code>
     */
    public Builder mergeBase(org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase konstue) {
      if (((bitField0_ & 0x00000001) == 0x00000001) &&
          base_ != org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase.getDefaultInstance()) {
        base_ =
          org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase.newBuilder(base_).mergeFrom(konstue).buildPartial();
      } else {
        base_ = konstue;
      }

      bitField0_ |= 0x00000001;
      return this;
    }
    /**
     * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase base = 1;</code>
     */
    public Builder clearBase() {
      base_ = org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase.getDefaultInstance();

      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }

    private java.util.List<java.lang.Long> overridden_ = java.util.Collections.emptyList();
    private void ensureOverriddenIsMutable() {
      if (!((bitField0_ & 0x00000002) == 0x00000002)) {
        overridden_ = new java.util.ArrayList<java.lang.Long>(overridden_);
        bitField0_ |= 0x00000002;
       }
    }
    /**
     * <code>repeated int64 overridden = 2 [packed = true];</code>
     *
     * <pre>
     * TODO: supposed to be deleted
     * </pre>
     */
    public java.util.List<java.lang.Long>
        getOverriddenList() {
      return java.util.Collections.unmodifiableList(overridden_);
    }
    /**
     * <code>repeated int64 overridden = 2 [packed = true];</code>
     *
     * <pre>
     * TODO: supposed to be deleted
     * </pre>
     */
    public int getOverriddenCount() {
      return overridden_.size();
    }
    /**
     * <code>repeated int64 overridden = 2 [packed = true];</code>
     *
     * <pre>
     * TODO: supposed to be deleted
     * </pre>
     */
    public long getOverridden(int index) {
      return overridden_.get(index);
    }
    /**
     * <code>repeated int64 overridden = 2 [packed = true];</code>
     *
     * <pre>
     * TODO: supposed to be deleted
     * </pre>
     */
    public Builder setOverridden(
        int index, long konstue) {
      ensureOverriddenIsMutable();
      overridden_.set(index, konstue);
      
      return this;
    }
    /**
     * <code>repeated int64 overridden = 2 [packed = true];</code>
     *
     * <pre>
     * TODO: supposed to be deleted
     * </pre>
     */
    public Builder addOverridden(long konstue) {
      ensureOverriddenIsMutable();
      overridden_.add(konstue);
      
      return this;
    }
    /**
     * <code>repeated int64 overridden = 2 [packed = true];</code>
     *
     * <pre>
     * TODO: supposed to be deleted
     * </pre>
     */
    public Builder addAllOverridden(
        java.lang.Iterable<? extends java.lang.Long> konstues) {
      ensureOverriddenIsMutable();
      org.jetbrains.kotlin.protobuf.AbstractMessageLite.Builder.addAll(
          konstues, overridden_);
      
      return this;
    }
    /**
     * <code>repeated int64 overridden = 2 [packed = true];</code>
     *
     * <pre>
     * TODO: supposed to be deleted
     * </pre>
     */
    public Builder clearOverridden() {
      overridden_ = java.util.Collections.emptyList();
      bitField0_ = (bitField0_ & ~0x00000002);
      
      return this;
    }

    // @@protoc_insertion_point(builder_scope:org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction)
  }

  static {
    defaultInstance = new IrFunction(true);
    defaultInstance.initFields();
  }

  // @@protoc_insertion_point(class_scope:org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction)
}
