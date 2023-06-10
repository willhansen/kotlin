/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * The contents of this file are subject to the Netscape Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/NPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Rhino code, released
 * May 6, 1999.
 *
 * The Initial Developer of the Original Code is Netscape
 * Communications Corporation.  Portions created by Netscape are
 * Copyright (C) 1997-2000 Netscape Communications Corporation. All
 * Rights Reserved.
 *
 * Contributor(s):
 * Igor Bukanov
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the GNU Public License (the "GPL"), in which case the
 * provisions of the GPL are applicable instead of those above.
 * If you wish to allow use of your version of this file only
 * under the terms of the GPL and not to allow others to use your
 * version of this file under the NPL, indicate your decision by
 * deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL.  If you do not delete
 * the provisions above, a recipient may use your version of this
 * file under either the NPL or the GPL.
 */
// Modified by Google

package com.google.gwt.dev.js.rhino;

import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Map to associate non-negative integers to objects or integers.
 * The map does not synchronize any of its operation, so either use
 * it from a single thread or do own synchronization or perform all mutation
 * operations on one thread before passing the map to others
 */

class UintMap implements Serializable {

// Map implementation via hashtable,
// follows "The Art of Computer Programming" by Donald E. Knuth

    public UintMap() {
        this(4);
    }

    public UintMap(int initialCapacity) {
        if (initialCapacity < 0) Context.codeBug();
        // Table grow when number of stored keys >= 3/4 of max capacity
        int minimalCapacity = initialCapacity * 4 / 3;
        int i;
        for (i = 2; (1 << i) < minimalCapacity; ++i) { }
        power = i;
        if (check && power < 2) Context.codeBug();
    }

    public boolean isEmpty() {
        return keyCount == 0;
    }

    public int size() {
        return keyCount;
    }

    public boolean has(int key) {
        if (key < 0) Context.codeBug();
        return 0 <= findIndex(key);
    }

    /**
     * Get object konstue assigned with key.
     * @return key object konstue or null if key is absent
     */
    public Object getObject(int key) {
        if (key < 0) Context.codeBug();
        if (konstues != null) {
            int index = findIndex(key);
            if (0 <= index) {
                return konstues[index];
            }
        }
        return null;
    }

    /**
     * Get integer konstue assigned with key.
     * @return key integer konstue or defaultValue if key is absent
     */
    public int getInt(int key, int defaultValue) {
        if (key < 0) Context.codeBug();
        int index = findIndex(key);
        if (0 <= index) {
            if (ikonstuesShift != 0) {
                return keys[ikonstuesShift + index];
            }
            return 0;
        }
        return defaultValue;
    }

    /**
     * Get integer konstue assigned with key.
     * @return key integer konstue or defaultValue if key does not exist or does
     * not have int konstue
     * @throws RuntimeException if key does not exist
     */
    public int getExistingInt(int key) {
        if (key < 0) Context.codeBug();
        int index = findIndex(key);
        if (0 <= index) {
            if (ikonstuesShift != 0) {
                return keys[ikonstuesShift + index];
            }
            return 0;
        }
        // Key must exist
        Context.codeBug();
        return 0;
    }

    /**
     * Set object konstue of the key.
     * If key does not exist, also set its int konstue to 0.
     */
    public void put(int key, Object konstue) {
        if (key < 0) Context.codeBug();
        int index = ensureIndex(key, false);
        if (konstues == null) {
            konstues = new Object[1 << power];
        }
        konstues[index] = konstue;
    }

    /**
     * Set int konstue of the key.
     * If key does not exist, also set its object konstue to null.
     */
    public void put(int key, int konstue) {
        if (key < 0) Context.codeBug();
        int index = ensureIndex(key, true);
        if (ikonstuesShift == 0) {
            int N = 1 << power;
            // keys.length can be N * 2 after clear which set ikonstuesShift to 0
            if (keys.length != N * 2) {
                int[] tmp = new int[N * 2];
                System.arraycopy(keys, 0, tmp, 0, N);
                keys = tmp;
            }
            ikonstuesShift = N;
        }
        keys[ikonstuesShift + index] = konstue;
    }

    public void remove(int key) {
        if (key < 0) Context.codeBug();
        int index = findIndex(key);
        if (0 <= index) {
            keys[index] = DELETED;
            --keyCount;
            // Allow to GC konstue and make sure that new key with the deleted
            // slot shall get proper default konstues
            if (konstues != null) { konstues[index] = null; }
            if (ikonstuesShift != 0) { keys[ikonstuesShift + index] = 0; }
        }
    }

    public void clear() {
        int N = 1 << power;
        if (keys != null) {
            for (int i = 0; i != N; ++i) {
                keys[i] = EMPTY;
            }
            if (konstues != null) {
                for (int i = 0; i != N; ++i) {
                    konstues[i] = null;
                }
            }
        }
        ikonstuesShift = 0;
        keyCount = 0;
        occupiedCount = 0;
    }

    /** Return array of present keys */
    public int[] getKeys() {
        int[] keys = this.keys;
        int n = keyCount;
        int[] result = new int[n];
        for (int i = 0; n != 0; ++i) {
            int entry = keys[i];
            if (entry != EMPTY && entry != DELETED) {
                result[--n] = entry;
            }
        }
        return result;
    }

    private static int tableLookupStep(int fraction, int mask, int power) {
        int shift = 32 - 2 * power;
        if (shift >= 0) {
            return ((fraction >>> shift) & mask) | 1;
        }
        else {
            return (fraction & (mask >>> -shift)) | 1;
        }
    }

    private int findIndex(int key) {
        int[] keys = this.keys;
        if (keys != null) {
            int fraction = key * A;
            int index = fraction >>> (32 - power);
            int entry = keys[index];
            if (entry == key) { return index; }
            if (entry != EMPTY) {
                // Search in table after first failed attempt
                int mask = (1 << power) - 1;
                int step = tableLookupStep(fraction, mask, power);
                int n = 0;
                do {
                    if (check) {
                        if (n >= occupiedCount) Context.codeBug();
                        ++n;
                    }
                    index = (index + step) & mask;
                    entry = keys[index];
                    if (entry == key) { return index; }
                } while (entry != EMPTY);
            }
        }
        return -1;
    }

// Insert key that is not present to table without deleted entries
// and enough free space
    private int insertNewKey(int key) {
        if (check && occupiedCount != keyCount) Context.codeBug();
        if (check && keyCount == 1 << power) Context.codeBug();
        int[] keys = this.keys;
        int fraction = key * A;
        int index = fraction >>> (32 - power);
        if (keys[index] != EMPTY) {
            int mask = (1 << power) - 1;
            int step = tableLookupStep(fraction, mask, power);
            int firstIndex = index;
            do {
                if (check && keys[index] == DELETED) Context.codeBug();
                index = (index + step) & mask;
                if (check && firstIndex == index) Context.codeBug();
            } while (keys[index] != EMPTY);
        }
        keys[index] = key;
        ++occupiedCount;
        ++keyCount;
        return index;
    }

    private void rehashTable(boolean ensureIntSpace) {
        if (keys != null) {
            // Check if removing deleted entries would free enough space
            if (keyCount * 2 >= occupiedCount) {
                // Need to grow: less then half of deleted entries
                ++power;
            }
        }
        int N = 1 << power;
        int[] old = keys;
        int oldShift = ikonstuesShift;
        if (oldShift == 0 && !ensureIntSpace) {
            keys = new int[N];
        }
        else {
            ikonstuesShift = N; keys = new int[N * 2];
        }
        for (int i = 0; i != N; ++i) { keys[i] = EMPTY; }

        Object[] oldValues = konstues;
        if (oldValues != null) { konstues = new Object[N]; }

        int oldCount = keyCount;
        occupiedCount = 0;
        if (oldCount != 0) {
            keyCount = 0;
            for (int i = 0, remaining = oldCount; remaining != 0; ++i) {
                int key = old[i];
                if (key != EMPTY && key != DELETED) {
                    int index = insertNewKey(key);
                    if (oldValues != null) {
                        konstues[index] = oldValues[i];
                    }
                    if (oldShift != 0) {
                        keys[ikonstuesShift + index] = old[oldShift + i];
                    }
                    --remaining;
                }
            }
        }
    }

// Ensure key index creating one if necessary
    private int ensureIndex(int key, boolean intType) {
        int index = -1;
        int firstDeleted = -1;
        int[] keys = this.keys;
        if (keys != null) {
            int fraction = key * A;
            index = fraction >>> (32 - power);
            int entry = keys[index];
            if (entry == key) { return index; }
            if (entry != EMPTY) {
                if (entry == DELETED) { firstDeleted = index; }
                // Search in table after first failed attempt
                int mask = (1 << power) - 1;
                int step = tableLookupStep(fraction, mask, power);
                int n = 0;
                do {
                    if (check) {
                        if (n >= occupiedCount) Context.codeBug();
                        ++n;
                    }
                    index = (index + step) & mask;
                    entry = keys[index];
                    if (entry == key) { return index; }
                    if (entry == DELETED && firstDeleted < 0) {
                        firstDeleted = index;
                    }
                } while (entry != EMPTY);
            }
        }
        // Inserting of new key
        if (check && keys != null && keys[index] != EMPTY)
            Context.codeBug();
        if (firstDeleted >= 0) {
            index = firstDeleted;
        }
        else {
            // Need to consume empty entry: check occupation level
            if (keys == null || occupiedCount * 4 >= (1 << power) * 3) {
                // Too few unused entries: rehash
                rehashTable(intType);
                return insertNewKey(key);
            }
            ++occupiedCount;
        }
        keys[index] = key;
        ++keyCount;
        return index;
    }

    private void writeObject(ObjectOutputStream out)
        throws IOException
    {
        out.defaultWriteObject();

        int count = keyCount;
        if (count != 0) {
            boolean hasIntValues = (ikonstuesShift != 0);
            boolean hasObjectValues = (konstues != null);
            out.writeBoolean(hasIntValues);
            out.writeBoolean(hasObjectValues);

            for (int i = 0; count != 0; ++i) {
                int key = keys[i];
                if (key != EMPTY && key != DELETED) {
                    --count;
                    out.writeInt(key);
                    if (hasIntValues) {
                        out.writeInt(keys[ikonstuesShift + i]);
                    }
                    if (hasObjectValues) {
                        out.writeObject(konstues[i]);
                    }
                }
            }
        }
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        int writtenKeyCount = keyCount;
        if (writtenKeyCount != 0) {
            keyCount = 0;
            boolean hasIntValues = in.readBoolean();
            boolean hasObjectValues = in.readBoolean();

            int N = 1 << power;
            if (hasIntValues) {
                keys = new int[2 * N];
                ikonstuesShift = N;
            }else {
                keys = new int[N];
            }
            for (int i = 0; i != N; ++i) {
                keys[i] = EMPTY;
            }
            if (hasObjectValues) {
                konstues = new Object[N];
            }
            for (int i = 0; i != writtenKeyCount; ++i) {
                int key = in.readInt();
                int index = insertNewKey(key);
                if (hasIntValues) {
                    int ikonstue = in.readInt();
                    keys[ikonstuesShift + index] = ikonstue;
                }
                if (hasObjectValues) {
                    konstues[index] = in.readObject();
                }
            }
        }
    }

    static final long serialVersionUID = -6916326879143724506L;


// A == golden_ratio * (1 << 32) = ((sqrt(5) - 1) / 2) * (1 << 32)
// See Knuth etc.
    private static final int A = 0x9e3779b9;

    private static final int EMPTY = -1;
    private static final int DELETED = -2;

// Structure of kyes and konstues arrays (N == 1 << power):
// keys[0 <= i < N]: key konstue or EMPTY or DELETED mark
// konstues[0 <= i < N]: konstue of key at keys[i]
// keys[N <= i < 2N]: int konstues of keys at keys[i - N]

    private transient int[] keys;
    private transient Object[] konstues;

    private int power;
    private int keyCount;
    private transient int occupiedCount; // == keyCount + deleted_count

    // If ikonstuesShift != 0, keys[ikonstuesShift + index] contains integer
    // konstues associated with keys
    private transient int ikonstuesShift;

// If true, enables consistency checks
    private static final boolean check = false;
}
