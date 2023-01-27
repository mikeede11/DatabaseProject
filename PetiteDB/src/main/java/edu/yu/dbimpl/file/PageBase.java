package edu.yu.dbimpl.file;

/** Specifies the public API for the Page implementation by requiring all
 * Page implementations to extend this base class.
 *
 * Students MAY NOT modify this class in any way, they must suppport EXACTLY
 * the constructor signatures specified in the base class (and NO OTHER
 * signatures).
 *
 * A Page represents the main-memory region used to hold the contents of a
 * block, and thus is used by the FileMgr in tandem with a BlockId A Page can
 * hold three value types: ints, strings, and "blobs" (i.e., arbitrary arrays
 * of bytes).  A client can store a value at any offset of the page but is
 * responsible for knowing what values have been stored where.  The result of
 * trying to retrieve a value from the wrong offset is undefined.
 *
 * The design for storing values of a given type is discussed in lecture and
 * MUST BE FOLLOWED in your implementation (see the Javadoc on maxLength()
 * below).
 *
 * @author Avraham Leff
 */

import java.nio.charset.*;

public abstract class PageBase {
    public static Charset CHARSET = StandardCharsets.US_ASCII;

    /** A charset chooses how many bytes each character encodes to. ASCII uses
     * one byte per character, whereas Unicode-16 uses between 2 bytes and 4
     * bytes per character.  This implies that the DBMS cannot know exactly how
     * many bytes a given string will encode to.  The maxLength method calculates
     * the maximum size of the blob for a string having a speciﬁed number of
     * characters. It does so by multiplying the number of characters by the max
     * number of bytes per character and adding 4 bytes for the integer that is
     * written with the bytes.(#of bytes?)
     */
    public static int maxLength(int strlen) {
        float bytesPerChar = CHARSET.newEncoder().maxBytesPerChar();
        return Integer.BYTES + (strlen * (int)bytesPerChar);
    }

    /** Use this constructor when creating data buffers.
     *
     * @param blocksize specifies the size of the blocks stored by a single Page:
     * must match the value supplied to the FileMgr constructor.
     */
    public PageBase(int blocksize) {
        // fill me in in your implementation class!
    }

    /** Use this constructor when creating log pages.
     */
    public PageBase(byte[] b) {
        // fill me in in your implementation class!
    }

    public abstract int getInt(int offset);

    public abstract void setInt(int offset, int n);//allocate a block

    public abstract byte[] getBytes(int offset);

    public abstract void setBytes(int offset, byte[] b);//allocate a block

    public abstract String getString(int offset);

    public abstract void setString(int offset, String s);//allocate a block
}