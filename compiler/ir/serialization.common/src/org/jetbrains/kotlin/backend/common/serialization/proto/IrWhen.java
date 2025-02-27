// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: compiler/ir/serialization.common/src/KotlinIr.proto

package org.jetbrains.kotlin.backend.common.serialization.proto;

/**
 * Protobuf type {@code org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen}
 */
public final class IrWhen extends
    org.jetbrains.kotlin.protobuf.GeneratedMessageLite implements
    // @@protoc_insertion_point(message_implements:org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen)
    IrWhenOrBuilder {
  // Use IrWhen.newBuilder() to construct.
  private IrWhen(org.jetbrains.kotlin.protobuf.GeneratedMessageLite.Builder builder) {
    super(builder);
    this.unknownFields = builder.getUnknownFields();
  }
  private IrWhen(boolean noInit) { this.unknownFields = org.jetbrains.kotlin.protobuf.ByteString.EMPTY;}

  private static final IrWhen defaultInstance;
  public static IrWhen getDefaultInstance() {
    return defaultInstance;
  }

  public IrWhen getDefaultInstanceForType() {
    return defaultInstance;
  }

  private final org.jetbrains.kotlin.protobuf.ByteString unknownFields;
  private IrWhen(
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
            if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
              branch_ = new java.util.ArrayList<org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement>();
              mutable_bitField0_ |= 0x00000001;
            }
            branch_.add(input.readMessage(org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement.PARSER, extensionRegistry));
            break;
          }
          case 16: {
            bitField0_ |= 0x00000001;
            originName_ = input.readInt32();
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
      if (((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
        branch_ = java.util.Collections.unmodifiableList(branch_);
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
  public static org.jetbrains.kotlin.protobuf.Parser<IrWhen> PARSER =
      new org.jetbrains.kotlin.protobuf.AbstractParser<IrWhen>() {
    public IrWhen parsePartialFrom(
        org.jetbrains.kotlin.protobuf.CodedInputStream input,
        org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
        throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
      return new IrWhen(input, extensionRegistry);
    }
  };

  @java.lang.Override
  public org.jetbrains.kotlin.protobuf.Parser<IrWhen> getParserForType() {
    return PARSER;
  }

  private int bitField0_;
  public static final int BRANCH_FIELD_NUMBER = 1;
  private java.util.List<org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement> branch_;
  /**
   * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement branch = 1;</code>
   */
  public java.util.List<org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement> getBranchList() {
    return branch_;
  }
  /**
   * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement branch = 1;</code>
   */
  public java.util.List<? extends org.jetbrains.kotlin.backend.common.serialization.proto.IrStatementOrBuilder> 
      getBranchOrBuilderList() {
    return branch_;
  }
  /**
   * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement branch = 1;</code>
   */
  public int getBranchCount() {
    return branch_.size();
  }
  /**
   * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement branch = 1;</code>
   */
  public org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement getBranch(int index) {
    return branch_.get(index);
  }
  /**
   * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement branch = 1;</code>
   */
  public org.jetbrains.kotlin.backend.common.serialization.proto.IrStatementOrBuilder getBranchOrBuilder(
      int index) {
    return branch_.get(index);
  }

  public static final int ORIGIN_NAME_FIELD_NUMBER = 2;
  private int originName_;
  /**
   * <code>optional int32 origin_name = 2;</code>
   */
  public boolean hasOriginName() {
    return ((bitField0_ & 0x00000001) == 0x00000001);
  }
  /**
   * <code>optional int32 origin_name = 2;</code>
   */
  public int getOriginName() {
    return originName_;
  }

  private void initFields() {
    branch_ = java.util.Collections.emptyList();
    originName_ = 0;
  }
  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    for (int i = 0; i < getBranchCount(); i++) {
      if (!getBranch(i).isInitialized()) {
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
    for (int i = 0; i < branch_.size(); i++) {
      output.writeMessage(1, branch_.get(i));
    }
    if (((bitField0_ & 0x00000001) == 0x00000001)) {
      output.writeInt32(2, originName_);
    }
    output.writeRawBytes(unknownFields);
  }

  private int memoizedSerializedSize = -1;
  public int getSerializedSize() {
    int size = memoizedSerializedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < branch_.size(); i++) {
      size += org.jetbrains.kotlin.protobuf.CodedOutputStream
        .computeMessageSize(1, branch_.get(i));
    }
    if (((bitField0_ & 0x00000001) == 0x00000001)) {
      size += org.jetbrains.kotlin.protobuf.CodedOutputStream
        .computeInt32Size(2, originName_);
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

  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen parseFrom(
      org.jetbrains.kotlin.protobuf.ByteString data)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen parseFrom(
      org.jetbrains.kotlin.protobuf.ByteString data,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen parseFrom(byte[] data)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen parseFrom(
      byte[] data,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return PARSER.parseFrom(input);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen parseFrom(
      java.io.InputStream input,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return PARSER.parseFrom(input, extensionRegistry);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return PARSER.parseDelimitedFrom(input);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen parseDelimitedFrom(
      java.io.InputStream input,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return PARSER.parseDelimitedFrom(input, extensionRegistry);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen parseFrom(
      org.jetbrains.kotlin.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return PARSER.parseFrom(input);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen parseFrom(
      org.jetbrains.kotlin.protobuf.CodedInputStream input,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return PARSER.parseFrom(input, extensionRegistry);
  }

  public static Builder newBuilder() { return Builder.create(); }
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder(org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen prototype) {
    return newBuilder().mergeFrom(prototype);
  }
  public Builder toBuilder() { return newBuilder(this); }

  /**
   * Protobuf type {@code org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen}
   */
  public static final class Builder extends
      org.jetbrains.kotlin.protobuf.GeneratedMessageLite.Builder<
        org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen, Builder>
      implements
      // @@protoc_insertion_point(builder_implements:org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen)
      org.jetbrains.kotlin.backend.common.serialization.proto.IrWhenOrBuilder {
    // Construct using org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen.newBuilder()
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
      branch_ = java.util.Collections.emptyList();
      bitField0_ = (bitField0_ & ~0x00000001);
      originName_ = 0;
      bitField0_ = (bitField0_ & ~0x00000002);
      return this;
    }

    public Builder clone() {
      return create().mergeFrom(buildPartial());
    }

    public org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen getDefaultInstanceForType() {
      return org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen.getDefaultInstance();
    }

    public org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen build() {
      org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen buildPartial() {
      org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen result = new org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        branch_ = java.util.Collections.unmodifiableList(branch_);
        bitField0_ = (bitField0_ & ~0x00000001);
      }
      result.branch_ = branch_;
      if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
        to_bitField0_ |= 0x00000001;
      }
      result.originName_ = originName_;
      result.bitField0_ = to_bitField0_;
      return result;
    }

    public Builder mergeFrom(org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen other) {
      if (other == org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen.getDefaultInstance()) return this;
      if (!other.branch_.isEmpty()) {
        if (branch_.isEmpty()) {
          branch_ = other.branch_;
          bitField0_ = (bitField0_ & ~0x00000001);
        } else {
          ensureBranchIsMutable();
          branch_.addAll(other.branch_);
        }
        
      }
      if (other.hasOriginName()) {
        setOriginName(other.getOriginName());
      }
      setUnknownFields(
          getUnknownFields().concat(other.unknownFields));
      return this;
    }

    public final boolean isInitialized() {
      for (int i = 0; i < getBranchCount(); i++) {
        if (!getBranch(i).isInitialized()) {
          
          return false;
        }
      }
      return true;
    }

    public Builder mergeFrom(
        org.jetbrains.kotlin.protobuf.CodedInputStream input,
        org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException e) {
        parsedMessage = (org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen) e.getUnfinishedMessage();
        throw e;
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private java.util.List<org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement> branch_ =
      java.util.Collections.emptyList();
    private void ensureBranchIsMutable() {
      if (!((bitField0_ & 0x00000001) == 0x00000001)) {
        branch_ = new java.util.ArrayList<org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement>(branch_);
        bitField0_ |= 0x00000001;
       }
    }

    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement branch = 1;</code>
     */
    public java.util.List<org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement> getBranchList() {
      return java.util.Collections.unmodifiableList(branch_);
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement branch = 1;</code>
     */
    public int getBranchCount() {
      return branch_.size();
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement branch = 1;</code>
     */
    public org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement getBranch(int index) {
      return branch_.get(index);
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement branch = 1;</code>
     */
    public Builder setBranch(
        int index, org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement konstue) {
      if (konstue == null) {
        throw new NullPointerException();
      }
      ensureBranchIsMutable();
      branch_.set(index, konstue);

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement branch = 1;</code>
     */
    public Builder setBranch(
        int index, org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement.Builder builderForValue) {
      ensureBranchIsMutable();
      branch_.set(index, builderForValue.build());

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement branch = 1;</code>
     */
    public Builder addBranch(org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement konstue) {
      if (konstue == null) {
        throw new NullPointerException();
      }
      ensureBranchIsMutable();
      branch_.add(konstue);

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement branch = 1;</code>
     */
    public Builder addBranch(
        int index, org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement konstue) {
      if (konstue == null) {
        throw new NullPointerException();
      }
      ensureBranchIsMutable();
      branch_.add(index, konstue);

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement branch = 1;</code>
     */
    public Builder addBranch(
        org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement.Builder builderForValue) {
      ensureBranchIsMutable();
      branch_.add(builderForValue.build());

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement branch = 1;</code>
     */
    public Builder addBranch(
        int index, org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement.Builder builderForValue) {
      ensureBranchIsMutable();
      branch_.add(index, builderForValue.build());

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement branch = 1;</code>
     */
    public Builder addAllBranch(
        java.lang.Iterable<? extends org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement> konstues) {
      ensureBranchIsMutable();
      org.jetbrains.kotlin.protobuf.AbstractMessageLite.Builder.addAll(
          konstues, branch_);

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement branch = 1;</code>
     */
    public Builder clearBranch() {
      branch_ = java.util.Collections.emptyList();
      bitField0_ = (bitField0_ & ~0x00000001);

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement branch = 1;</code>
     */
    public Builder removeBranch(int index) {
      ensureBranchIsMutable();
      branch_.remove(index);

      return this;
    }

    private int originName_ ;
    /**
     * <code>optional int32 origin_name = 2;</code>
     */
    public boolean hasOriginName() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>optional int32 origin_name = 2;</code>
     */
    public int getOriginName() {
      return originName_;
    }
    /**
     * <code>optional int32 origin_name = 2;</code>
     */
    public Builder setOriginName(int konstue) {
      bitField0_ |= 0x00000002;
      originName_ = konstue;
      
      return this;
    }
    /**
     * <code>optional int32 origin_name = 2;</code>
     */
    public Builder clearOriginName() {
      bitField0_ = (bitField0_ & ~0x00000002);
      originName_ = 0;
      
      return this;
    }

    // @@protoc_insertion_point(builder_scope:org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen)
  }

  static {
    defaultInstance = new IrWhen(true);
    defaultInstance.initFields();
  }

  // @@protoc_insertion_point(class_scope:org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen)
}
