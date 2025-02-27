/* The following code was generated by JFlex 1.7.0 tweaked for IntelliJ platform */

package org.jetbrains.kotlin.kdoc.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.text.CharArrayUtil;
import java.lang.Character;
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag;


/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.7.0
 * from the specification file <tt>/Users/victor.petukhov/IdeaProjects/kotlin-jps/compiler/psi/src/org/jetbrains/kotlin/kdoc/lexer/KDoc.flex</tt>
 */
class _KDocLexer implements FlexLexer {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;
  public static final int LINE_BEGINNING = 2;
  public static final int CONTENTS_BEGINNING = 4;
  public static final int TAG_BEGINNING = 6;
  public static final int TAG_TEXT_BEGINNING = 8;
  public static final int CONTENTS = 10;
  public static final int CODE_BLOCK = 12;
  public static final int CODE_BLOCK_LINE_BEGINNING = 14;
  public static final int CODE_BLOCK_CONTENTS_BEGINNING = 16;
  public static final int INDENTED_CODE_BLOCK = 18;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0,  0,  1,  1,  2,  2,  3,  3,  4,  4,  5,  5,  6,  6,  7,  7, 
     8,  8,  6, 6
  };

  /** 
   * Translates characters to character classes
   * Chosen bits are [12, 6, 3]
   * Total runtime size is 12752 bytes
   */
  public static int ZZ_CMAP(int ch) {
    return ZZ_CMAP_A[(ZZ_CMAP_Y[(ZZ_CMAP_Z[ch>>9]<<6)|((ch>>3)&0x3f)]<<3)|(ch&0x7)];
  }

  /* The ZZ_CMAP_Z table has 2176 entries */
  static final char ZZ_CMAP_Z[] = zzUnpackCMap(
    "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17\1"+
    "\20\5\21\1\22\1\23\1\24\1\21\14\25\1\26\50\25\1\27\2\25\1\30\1\31\1\32\1\33"+
    "\25\25\1\34\20\21\1\35\1\36\1\37\1\40\1\41\1\42\1\43\1\21\1\44\1\45\1\46\1"+
    "\21\1\47\2\21\1\50\4\21\1\25\1\51\1\52\5\21\2\25\1\53\31\21\1\25\1\54\1\21"+
    "\1\55\40\21\1\56\21\21\1\57\1\60\13\21\1\61\10\21\123\25\1\62\7\25\1\63\1"+
    "\64\37\21\1\25\1\64\u0702\21");

  /* The ZZ_CMAP_Y table has 3392 entries */
  static final char ZZ_CMAP_Y[] = zzUnpackCMap(
    "\1\0\1\1\2\0\1\2\1\3\1\4\1\5\1\6\2\7\1\10\1\11\2\7\1\12\1\13\3\0\1\14\1\15"+
    "\1\16\1\15\2\7\1\17\3\7\1\17\71\7\1\20\1\7\1\21\1\0\1\22\1\23\20\0\1\24\1"+
    "\14\1\25\1\26\2\7\1\27\11\7\1\30\21\7\1\21\1\31\23\7\1\0\1\32\3\7\1\17\1\33"+
    "\1\32\4\7\1\34\10\0\3\7\2\35\2\0\1\36\2\0\5\7\1\35\3\0\1\37\1\32\13\7\1\40"+
    "\1\0\1\41\1\37\1\0\1\42\2\0\1\43\3\7\3\0\1\44\12\7\1\45\1\0\1\33\2\0\1\31"+
    "\3\7\1\35\1\46\1\15\2\7\1\45\1\15\1\47\1\50\2\0\3\7\1\50\10\0\1\43\1\22\12"+
    "\0\1\51\6\7\1\52\2\0\1\50\1\7\1\21\1\0\2\32\1\44\1\53\1\54\2\7\1\43\1\55\1"+
    "\52\1\0\1\25\1\0\1\56\1\21\1\0\1\57\1\36\1\44\1\60\1\54\2\7\1\43\1\61\1\21"+
    "\3\0\1\62\2\0\1\63\1\0\1\44\1\30\1\27\2\7\1\43\1\64\1\52\2\0\1\50\1\0\1\21"+
    "\1\0\1\33\1\0\1\44\1\53\1\54\2\7\1\43\1\64\1\52\3\0\1\56\1\21\1\0\1\33\1\0"+
    "\1\65\1\66\1\67\1\70\1\71\1\66\1\7\1\21\2\0\1\50\4\0\1\33\1\44\1\24\1\43\2"+
    "\7\1\43\1\72\1\52\3\0\2\21\3\0\1\44\1\24\1\43\2\7\1\43\1\72\1\52\3\0\1\25"+
    "\1\21\1\0\1\73\1\0\1\44\1\24\1\43\4\7\1\74\1\0\1\25\2\0\1\21\2\0\1\31\1\44"+
    "\1\7\1\17\1\31\2\7\1\27\1\40\1\17\7\0\1\32\5\7\1\75\1\34\1\17\7\0\1\76\1\77"+
    "\1\51\1\32\1\100\1\101\1\75\1\16\1\102\2\0\1\51\4\0\1\50\7\0\1\7\1\32\3\7"+
    "\1\22\3\0\1\22\16\0\5\7\1\35\1\0\1\34\2\0\1\45\1\14\1\103\1\37\1\104\1\7\1"+
    "\21\1\25\2\0\4\7\1\30\1\16\5\7\1\105\51\7\1\67\1\17\1\67\5\7\1\67\4\7\1\67"+
    "\1\17\1\67\1\7\1\17\7\7\1\67\10\7\1\35\4\0\2\7\2\0\12\7\1\22\1\0\1\32\114"+
    "\7\1\53\2\7\1\32\2\7\1\35\11\7\1\66\1\50\1\0\1\7\1\24\1\21\1\0\2\7\1\21\1"+
    "\0\2\7\1\21\1\0\1\7\1\24\1\50\1\0\6\7\1\57\3\0\1\34\1\71\10\0\13\7\1\0\5\7"+
    "\1\106\10\7\1\45\1\0\3\7\1\22\6\0\3\7\1\45\1\22\1\0\5\7\1\57\2\0\1\32\7\0"+
    "\2\7\1\17\1\0\6\7\1\22\11\0\1\34\13\0\1\44\5\7\1\57\1\0\1\44\1\57\6\0\1\107"+
    "\3\7\1\50\1\37\1\0\1\31\4\7\1\45\3\0\4\7\1\57\4\0\1\44\1\0\1\31\3\7\1\45\15"+
    "\0\1\110\1\111\1\0\30\7\10\0\42\7\2\45\4\7\2\45\1\7\1\112\3\7\1\45\6\7\1\24"+
    "\1\102\1\113\1\22\1\114\1\57\1\7\1\22\1\113\1\22\5\0\1\115\1\0\1\34\1\50\1"+
    "\0\1\47\3\0\1\33\1\34\2\0\1\7\1\22\3\7\1\35\10\0\1\116\1\31\1\40\1\117\1\23"+
    "\1\120\1\7\1\121\1\44\1\122\2\0\5\7\1\50\116\0\5\7\1\17\5\7\1\17\20\7\1\22"+
    "\1\123\1\124\1\0\4\7\1\30\1\16\7\7\1\34\2\0\2\7\1\17\1\0\10\17\11\0\1\34\72"+
    "\0\1\44\3\0\1\32\1\21\1\117\1\22\1\32\11\7\1\17\1\44\1\32\12\7\1\105\1\44"+
    "\4\7\1\45\1\32\12\7\1\17\2\0\3\7\1\35\6\0\170\7\1\45\11\0\71\7\1\22\6\0\21"+
    "\7\1\22\10\0\5\7\1\45\41\7\1\22\2\7\1\0\1\124\2\0\5\7\1\17\1\0\1\34\3\7\1"+
    "\0\12\7\4\0\1\34\1\7\1\31\14\7\1\125\1\57\1\0\1\7\1\35\11\0\1\7\1\126\1\105"+
    "\2\7\1\35\2\0\1\50\6\7\1\57\1\0\1\31\5\7\1\57\7\0\1\31\1\36\1\0\1\31\2\7\1"+
    "\45\1\0\2\7\1\17\3\0\3\7\1\22\1\51\5\7\1\35\2\0\1\34\6\0\5\7\1\50\2\0\1\105"+
    "\1\57\2\0\2\7\1\17\1\15\6\7\1\103\1\117\1\106\2\0\1\127\1\7\1\35\1\63\1\0"+
    "\3\130\1\0\2\17\22\0\4\7\1\35\3\0\64\7\1\57\1\0\2\7\1\17\1\107\5\7\1\57\40"+
    "\0\55\7\1\45\15\7\1\21\4\0\1\17\1\0\1\107\1\131\1\7\1\43\1\17\1\102\1\132"+
    "\15\7\1\21\3\0\1\107\54\7\1\45\2\0\10\7\1\31\6\7\5\0\1\7\1\22\6\0\1\71\2\0"+
    "\1\44\3\0\1\33\1\24\20\7\1\22\1\47\3\0\1\32\2\7\1\60\1\32\2\7\1\35\1\37\12"+
    "\7\1\17\3\31\1\63\1\111\3\0\1\7\1\72\2\7\1\17\2\7\1\133\1\7\1\45\1\7\1\45"+
    "\4\0\17\7\1\35\10\0\6\7\1\22\41\0\3\7\1\22\6\7\1\50\5\0\3\7\1\17\2\0\3\7\1"+
    "\35\6\0\3\7\1\45\4\7\1\57\1\7\1\117\5\0\23\7\1\45\54\0\1\45\1\43\4\7\1\30"+
    "\1\134\2\7\1\45\25\0\2\7\1\45\1\0\3\7\1\21\10\0\7\7\1\37\10\0\1\50\1\0\1\72"+
    "\1\32\2\7\1\57\5\0\3\7\1\22\20\0\6\7\1\45\1\0\2\7\1\45\1\0\2\7\1\35\21\0\11"+
    "\7\1\50\66\0\1\107\6\7\11\0\1\107\5\7\4\0\3\7\1\50\2\0\1\107\3\7\1\17\13\0"+
    "\1\107\5\7\1\35\1\0\1\135\27\0\5\7\1\35\52\0\55\7\1\17\22\0\14\7\1\35\63\0"+
    "\5\7\1\17\72\0\7\7\1\50\130\0\10\7\1\22\1\0\1\50\7\0\1\107\1\7\14\0\1\21\77"+
    "\0\12\7\1\24\10\7\1\24\1\136\1\110\1\7\1\137\1\72\7\7\1\30\1\140\2\24\3\7"+
    "\1\141\1\102\1\31\1\43\51\7\1\45\3\7\1\43\2\7\1\105\3\7\1\105\2\7\1\24\3\7"+
    "\1\24\2\7\1\17\3\7\1\17\3\7\1\43\3\7\1\43\2\7\1\105\1\57\6\0\1\72\3\7\1\76"+
    "\1\32\1\105\1\142\1\116\1\143\1\76\1\112\1\76\2\105\1\62\1\7\1\27\1\7\1\57"+
    "\1\144\1\27\1\7\1\57\50\0\32\7\1\17\5\0\106\7\1\22\1\0\33\7\1\45\74\0");

  /* The ZZ_CMAP_A table has 808 entries */
  static final char ZZ_CMAP_A[] = zzUnpackCMap(
    "\11\0\1\1\1\11\1\12\1\13\1\12\2\0\1\17\3\0\1\3\3\0\1\21\1\22\1\15\3\0\1\4"+
    "\1\14\12\2\6\0\1\16\22\3\1\5\1\20\1\6\1\0\1\3\1\7\12\3\3\0\1\10\6\0\1\12\4"+
    "\0\4\3\4\0\1\3\12\0\1\3\2\0\7\3\1\0\2\3\4\0\4\3\6\0\5\3\7\0\1\3\1\0\1\3\1"+
    "\0\5\3\1\0\2\3\6\0\1\3\1\0\3\3\1\0\1\3\1\0\4\3\1\0\13\3\1\0\1\3\2\0\6\3\1"+
    "\0\7\3\1\0\1\3\15\0\4\3\10\0\1\3\12\0\6\3\1\0\1\3\7\0\2\3\3\0\3\3\2\0\2\3"+
    "\1\0\6\3\5\0\11\3\6\0\2\3\6\0\1\3\3\0\1\3\13\0\6\3\3\0\1\3\2\0\5\3\2\0\2\3"+
    "\2\0\6\3\1\0\1\3\3\0\2\3\4\0\2\3\1\0\5\3\4\0\3\3\4\0\2\3\1\0\2\3\1\0\2\3\2"+
    "\0\4\3\1\0\1\3\3\0\3\3\3\0\1\3\1\0\2\3\1\0\3\3\3\0\1\3\1\0\6\3\3\0\3\3\1\0"+
    "\4\3\3\0\2\3\1\0\1\3\1\0\2\3\3\0\2\3\3\0\4\3\1\0\3\3\1\0\2\3\5\0\3\3\2\0\1"+
    "\3\2\0\1\3\1\0\2\3\5\0\2\3\1\0\1\3\2\0\2\3\1\0\1\3\2\0\1\3\3\0\3\3\1\0\1\3"+
    "\1\0\1\3\2\0\2\3\1\0\10\3\1\0\1\3\2\0\1\3\3\0\2\3\1\0\1\3\4\0\6\3\1\0\5\3"+
    "\1\0\1\3\10\0\5\3\1\0\4\3\1\0\4\3\3\0\2\3\2\0\1\3\1\0\1\3\1\0\1\3\1\0\1\3"+
    "\2\0\3\3\1\0\6\3\2\0\2\3\2\12\10\0\1\3\4\0\1\3\1\0\5\3\2\0\1\3\1\0\4\3\1\0"+
    "\3\3\2\0\6\3\4\0\1\3\4\0\4\3\3\0\2\3\4\0\1\3\2\0\4\3\1\0\2\3\1\0\3\3\1\0\1"+
    "\3\3\0\3\3\3\0\6\3\6\0\1\3\1\0\3\3\1\0\2\3\1\0\5\3\1\0\2\3\1\0\2\3\3\0\1\3"+
    "\2\0\1\3\1\0\4\3\5\0\1\3\2\0\2\3\1\0\2\3\1\0\1\3\1\0\6\3\2\0\5\3\1\0\4\3\2"+
    "\0\1\3\1\0\1\3\5\0\1\3\1\0\1\3\1\0\3\3\1\0\3\3\1\0\3\3");

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\11\0\3\1\1\2\1\3\3\2\1\4\1\5\1\4"+
    "\3\2\1\6\1\7\2\2\1\10\1\11\3\10\1\12"+
    "\1\0\1\13\6\0\1\4\1\14\1\15\4\0\1\16"+
    "\2\0\1\17\1\4\1\20\1\21\1\0\2\22\1\0"+
    "\1\23\1\3\1\24\1\23";

  private static int [] zzUnpackAction() {
    int [] result = new int[63];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int konstue = packed.charAt(i++);
      do result[j++] = konstue; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\23\0\46\0\71\0\114\0\137\0\162\0\205"+
    "\0\230\0\253\0\276\0\321\0\253\0\344\0\367\0\u010a"+
    "\0\u011d\0\u0130\0\u0143\0\u0156\0\u0169\0\321\0\u017c\0\u018f"+
    "\0\u01a2\0\u01b5\0\u01c8\0\253\0\u01db\0\321\0\u01ee\0\u0201"+
    "\0\u0214\0\u0227\0\253\0\321\0\u023a\0\u024d\0\u0260\0\u0273"+
    "\0\u0286\0\u0299\0\253\0\u02ac\0\u02bf\0\u02d2\0\u02e5\0\u02f8"+
    "\0\253\0\u030b\0\u031e\0\u0331\0\u0344\0\253\0\253\0\u0357"+
    "\0\u023a\0\u0260\0\u036a\0\u023a\0\u0344\0\u037d\0\253";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[63];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\14\12\1\13\1\14\5\12\1\15\1\16\3\15\1\17"+
    "\1\15\1\20\1\21\1\22\1\15\1\22\1\15\1\23"+
    "\1\15\1\24\1\25\3\15\1\16\3\15\1\17\1\15"+
    "\1\20\1\21\1\22\1\15\1\22\1\15\1\26\1\27"+
    "\1\24\1\25\3\15\1\30\1\15\1\31\1\15\1\32"+
    "\3\15\1\30\1\15\1\30\1\15\1\26\1\15\1\30"+
    "\4\15\1\30\3\15\1\33\3\15\1\30\1\15\1\30"+
    "\1\15\1\26\1\15\1\30\4\15\1\16\3\15\1\17"+
    "\1\15\1\20\1\21\1\22\1\15\1\22\1\15\1\26"+
    "\1\15\1\24\1\25\2\15\1\34\1\35\7\34\1\35"+
    "\1\34\1\35\1\34\1\36\1\34\1\35\4\34\1\35"+
    "\5\34\1\37\1\40\1\35\1\34\1\35\1\34\1\41"+
    "\1\34\1\35\4\34\1\35\5\34\1\37\1\40\1\35"+
    "\1\34\1\35\1\34\1\36\1\34\1\35\3\34\40\0"+
    "\1\42\21\0\1\43\1\44\6\0\1\16\7\0\1\22"+
    "\1\0\1\22\3\0\1\22\3\0\3\45\1\46\1\45"+
    "\1\0\1\47\14\45\7\0\1\50\23\0\1\51\13\0"+
    "\1\22\7\0\1\22\1\0\1\22\3\0\1\22\17\0"+
    "\1\43\1\23\6\0\1\22\7\0\1\22\1\0\1\22"+
    "\3\0\1\52\10\0\2\53\17\0\1\54\20\0\1\30"+
    "\7\0\1\30\1\0\1\30\3\0\1\30\5\0\3\31"+
    "\21\0\1\55\22\0\1\56\20\0\1\35\7\0\1\35"+
    "\1\0\1\35\3\0\1\35\12\0\1\57\23\0\1\60"+
    "\26\0\1\43\1\41\22\0\1\61\5\0\5\45\1\0"+
    "\1\47\16\45\3\46\1\0\1\62\21\45\1\0\1\47"+
    "\12\45\1\63\1\45\7\0\1\64\23\0\1\64\13\0"+
    "\1\22\7\0\1\22\1\0\1\22\3\0\1\65\5\0"+
    "\2\54\21\0\3\55\1\0\1\66\16\0\3\56\1\0"+
    "\1\67\23\0\1\70\23\0\1\70\12\0\5\71\1\0"+
    "\1\72\12\71\1\63\1\71\5\63\1\73\14\63\1\74"+
    "\11\64\3\0\7\64\1\0\1\22\7\0\1\22\1\0"+
    "\1\22\3\0\1\75\4\0\1\70\7\0\1\76\1\0"+
    "\1\70\3\0\1\70\3\0\22\73\1\77\1\0\1\76"+
    "\11\0\1\76\3\0\1\76\3\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[912];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int konstue = packed.charAt(i++);
      konstue--;
      do result[j++] = konstue; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String[] ZZ_ERROR_MSG = {
    "Unknown internal scanner error",
    "Error: could not match input",
    "Error: pushback konstue was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\11\0\1\11\2\1\1\11\16\1\1\11\5\1\1\0"+
    "\1\11\6\0\1\1\1\11\1\1\4\0\1\11\2\0"+
    "\2\1\2\11\1\0\2\1\1\0\3\1\1\11";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[63];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int konstue = packed.charAt(i++);
      do result[j++] = konstue; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private CharSequence zzBuffer = "";

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /**
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean zzEOFDone;

  /* user code: */
  public _KDocLexer() {
    this((java.io.Reader)null);
  }

  private boolean isLastToken() {
    return zzMarkedPos == zzBuffer.length();
  }

  private boolean yytextContainLineBreaks() {
    return CharArrayUtil.containLineBreaks(zzBuffer, zzStartRead, zzMarkedPos);
  }

  private boolean nextIsNotWhitespace() {
    return zzMarkedPos <= zzBuffer.length() && !Character.isWhitespace(zzBuffer.charAt(zzMarkedPos + 1));
  }

  private boolean prevIsNotWhitespace() {
    return zzMarkedPos != 0 && !Character.isWhitespace(zzBuffer.charAt(zzMarkedPos - 1));
  }


  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  _KDocLexer(java.io.Reader in) {
    this.zzReader = in;
  }


  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    int size = 0;
    for (int i = 0, length = packed.length(); i < length; i += 2) {
      size += packed.charAt(i);
    }
    char[] map = new char[size];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < packed.length()) {
      int  count = packed.charAt(i++);
      char konstue = packed.charAt(i++);
      do map[j++] = konstue; while (--count > 0);
    }
    return map;
  }

  public final int getTokenStart() {
    return zzStartRead;
  }

  public final int getTokenEnd() {
    return getTokenStart() + yylength();
  }

  public void reset(CharSequence buffer, int start, int end, int initialState) {
    zzBuffer = buffer;
    zzCurrentPos = zzMarkedPos = zzStartRead = start;
    zzAtEOF  = false;
    zzAtBOL = true;
    zzEndRead = end;
    yybegin(initialState);
  }

  /**
   * Refills the input buffer.
   *
   * @return      {@code false}, iff there was new input.
   *
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {
    return true;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final CharSequence yytext() {
    return zzBuffer.subSequence(zzStartRead, zzMarkedPos);
  }


  /**
   * Returns the character at position {@code pos} from the
   * matched text.
   *
   * It is equikonstent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch.
   *            A konstue from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer.charAt(zzStartRead+pos);
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occurred while scanning.
   *
   * In a wellformed scanner (no or only correct usage of
   * yypushback(int) and a match-all fallback rule) this method
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Contains user EOF-code, which will be executed exactly once,
   * when the end of file is reached
   */
  private void zzDoEOF() {
    if (!zzEOFDone) {
      zzEOFDone = true;
      return;

    }
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public IElementType advance() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    CharSequence zzBufferL = zzBuffer;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

      zzState = ZZ_LEXSTATE[zzLexicalState];

      // set up zzAction for empty match case:
      int zzAttributes = zzAttrL[zzState];
      if ( (zzAttributes & 1) == 1 ) {
        zzAction = zzState;
      }


      zzForAction: {
        while (true) {

          if (zzCurrentPosL < zzEndReadL) {
            zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL/*, zzEndReadL*/);
            zzCurrentPosL += Character.charCount(zzInput);
          }
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL/*, zzEndReadL*/);
              zzCurrentPosL += Character.charCount(zzInput);
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + ZZ_CMAP(zzInput) ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
        zzAtEOF = true;
        zzDoEOF();
        return null;
      }
      else {
        switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
          case 1: 
            { return TokenType.BAD_CHARACTER;
            } 
            // fall through
          case 21: break;
          case 2: 
            { yybegin(CONTENTS);
        return KDocTokens.TEXT;
            } 
            // fall through
          case 22: break;
          case 3: 
            { if(yystate() == CONTENTS_BEGINNING) {
            yybegin(INDENTED_CODE_BLOCK);
            return KDocTokens.CODE_BLOCK_TEXT;
        }
            } 
            // fall through
          case 23: break;
          case 4: 
            { if (yytextContainLineBreaks()) {
            yybegin(LINE_BEGINNING);
            return TokenType.WHITE_SPACE;
        }  else {
            yybegin(yystate() == CONTENTS_BEGINNING ? CONTENTS_BEGINNING : CONTENTS);
            return KDocTokens.TEXT;  // internal white space
        }
            } 
            // fall through
          case 24: break;
          case 5: 
            { yybegin(CONTENTS_BEGINNING);
                                            return KDocTokens.LEADING_ASTERISK;
            } 
            // fall through
          case 25: break;
          case 6: 
            { if (yytextContainLineBreaks()) {
            yybegin(LINE_BEGINNING);
        }
        return TokenType.WHITE_SPACE;
            } 
            // fall through
          case 26: break;
          case 7: 
            { yybegin(TAG_TEXT_BEGINNING);
        return KDocTokens.MARKDOWN_LINK;
            } 
            // fall through
          case 27: break;
          case 8: 
            { yybegin(yystate() == INDENTED_CODE_BLOCK ? INDENTED_CODE_BLOCK : CODE_BLOCK);
        return KDocTokens.CODE_BLOCK_TEXT;
            } 
            // fall through
          case 28: break;
          case 9: 
            { if (yytextContainLineBreaks()) {
            yybegin(yystate() == INDENTED_CODE_BLOCK ? LINE_BEGINNING : CODE_BLOCK_LINE_BEGINNING);
            return TokenType.WHITE_SPACE;
        }
        return KDocTokens.CODE_BLOCK_TEXT;
            } 
            // fall through
          case 29: break;
          case 10: 
            { yybegin(CODE_BLOCK_CONTENTS_BEGINNING);
        return KDocTokens.LEADING_ASTERISK;
            } 
            // fall through
          case 30: break;
          case 11: 
            { if (isLastToken()) return KDocTokens.END;
                                            else return KDocTokens.TEXT;
            } 
            // fall through
          case 31: break;
          case 12: 
            { yybegin(CONTENTS);
        return KDocTokens.MARKDOWN_ESCAPED_CHAR;
            } 
            // fall through
          case 32: break;
          case 13: 
            { KDocKnownTag tag = KDocKnownTag.Companion.findByTagName(zzBuffer.subSequence(zzStartRead, zzMarkedPos));
    yybegin(tag != null && tag.isReferenceRequired() ? TAG_BEGINNING : TAG_TEXT_BEGINNING);
    return KDocTokens.TAG_NAME;
            } 
            // fall through
          case 33: break;
          case 14: 
            { yybegin(CONTENTS_BEGINNING);
                                            return KDocTokens.START;
            } 
            // fall through
          case 34: break;
          case 15: 
            { yybegin(CODE_BLOCK_LINE_BEGINNING);
        return KDocTokens.TEXT;
            } 
            // fall through
          case 35: break;
          case 16: 
            { yybegin(TAG_TEXT_BEGINNING);
                  return KDocTokens.MARKDOWN_LINK;
            } 
            // fall through
          case 36: break;
          case 17: 
            { yybegin(CONTENTS);
                  return KDocTokens.MARKDOWN_LINK;
            } 
            // fall through
          case 37: break;
          case 18: 
            // lookahead expression with fixed lookahead length
            zzMarkedPos = Character.offsetByCodePoints
                (zzBufferL/*, zzStartRead, zzEndRead - zzStartRead*/, zzMarkedPos, -1);
            { yybegin(CONTENTS);
        return KDocTokens.MARKDOWN_LINK;
            } 
            // fall through
          case 38: break;
          case 19: 
            { yybegin(CONTENTS);
        return KDocTokens.MARKDOWN_INLINE_LINK;
            } 
            // fall through
          case 39: break;
          case 20: 
            // lookahead expression with fixed base length
            zzMarkedPos = Character.offsetByCodePoints
                (zzBufferL/*, zzStartRead, zzEndRead - zzStartRead*/, zzStartRead, 3);
            { // Code fence end
        yybegin(CONTENTS);
        return KDocTokens.TEXT;
            } 
            // fall through
          case 40: break;
          default:
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
