/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson, École Polytechnique de Montréal, and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Florian Wininger - Add Extension and Leaf Node
 *******************************************************************************/

package fr.inria.linuxtools.internal.statesystem.core.backend.historytree;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import fr.inria.linuxtools.statesystem.core.exceptions.TimeRangeException;
import fr.inria.linuxtools.statesystem.core.interval.ITmfStateInterval;
import fr.inria.linuxtools.statesystem.core.statevalue.TmfStateValue;

/**
 * The base class for all the types of nodes that go in the History Tree.
 *
 * @author Alexandre Montplaisir
 */
public abstract class HTNode {

    // ------------------------------------------------------------------------
    // Class fields
    // ------------------------------------------------------------------------

    /**
     * The type of node
     */
    public static enum NodeType {
        /**
         * Core node, which is a "front" node, at any level of the tree except
         * the bottom-most one. It has children, and may have extensions.
         */
        CORE,
        /**
         * Leaf node, which is a node at the last bottom level of the tree. It
         * cannot have any children or extensions.
         */
        LEAF;

        /**
         * Determine a node type by reading a serialized byte.
         *
         * @param rep
         *            The byte representation of the node type
         * @return The corresponding NodeType
         * @throws IOException
         *             If the NodeType is unrecognized
         */
        public static NodeType fromByte(byte rep) throws IOException {
            switch (rep) {
            case 1:
                return CORE;
            case 2:
                return LEAF;
            default:
                throw new IOException();
            }
        }

        /**
         * Get the byte representation of this node type. It can then be read
         * with {@link #fromByte}.
         *
         * @return The byte matching this node type
         */
        public byte toByte() {
            switch (this) {
            case CORE:
                return 1;
            case LEAF:
                return 2;
            default:
                throw new IllegalStateException();
            }
        }
    }

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /* Configuration of the History Tree to which belongs this node */
    private final HTConfig config;

    /* Time range of this node */
    private final long nodeStart;
    private long nodeEnd;

    /* Sequence number = position in the node section of the file */
    private final int sequenceNumber;
    private int parentSequenceNumber; /* = -1 if this node is the root node */

    /* Where the Strings section begins (from the start of the node */
    private int stringSectionOffset;

    /* Sum of bytes of all intervals in the node */
    private int sizeOfIntervalSection;

    /* True if this node was read from disk (meaning its end time is now fixed) */
    private volatile boolean isOnDisk;

    /* Vector containing all the intervals contained in this node */
    private final List<HTInterval> intervals;

    /* Lock used to protect the accesses to intervals, nodeEnd and such */
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(false);

    /**
     * Constructor
     *
     * @param config
     *            Configuration of the History Tree
     * @param seqNumber
     *            The (unique) sequence number assigned to this particular node
     * @param parentSeqNumber
     *            The sequence number of this node's parent node
     * @param start
     *            The earliest timestamp stored in this node
     */
    protected HTNode(HTConfig config, int seqNumber, int parentSeqNumber, long start) {
        this.config = config;
        this.nodeStart = start;
        this.sequenceNumber = seqNumber;
        this.parentSequenceNumber = parentSeqNumber;

        this.stringSectionOffset = config.getBlockSize();
        this.sizeOfIntervalSection = 0;
        this.isOnDisk = false;
        this.intervals = new ArrayList<>();
    }

    /**
     * Reader factory method. Build a Node object (of the right type) by reading
     * a block in the file.
     *
     * @param config
     *            Configuration of the History Tree
     * @param fc
     *            FileChannel to the history file, ALREADY SEEKED at the start
     *            of the node.
     * @return The node object
     * @throws IOException
     *             If there was an error reading from the file channel
     */
    public static final HTNode readNode(HTConfig config, FileChannel fc)
            throws IOException {
        HTNode newNode = null;
        int res, i;

        ByteBuffer buffer = ByteBuffer.allocate(config.getBlockSize());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.clear();
        res = fc.read(buffer);
        assert (res == config.getBlockSize());
        buffer.flip();

        /* Read the common header part */
        byte typeByte = buffer.get();
        NodeType type = NodeType.fromByte(typeByte);
        long start = buffer.getLong();
        long end = buffer.getLong();
        int seqNb = buffer.getInt();
        int parentSeqNb = buffer.getInt();
        int intervalCount = buffer.getInt();
        int stringSectionOffset = buffer.getInt();
        buffer.get(); // TODO Used to be "isDone", to be removed from the header

        /* Now the rest of the header depends on the node type */
        switch (type) {
        case CORE:
            /* Core nodes */
            newNode = new CoreNode(config, seqNb, parentSeqNb, start);
            newNode.readSpecificHeader(buffer);
            break;

        case LEAF:
            /* Leaf nodes */
            newNode = new LeafNode(config, seqNb, parentSeqNb, start);
            newNode.readSpecificHeader(buffer);
            break;

        default:
            /* Unrecognized node type */
            throw new IOException();
        }

        /*
         * At this point, we should be done reading the header and 'buffer'
         * should only have the intervals left
         */
        for (i = 0; i < intervalCount; i++) {
            newNode.intervals.add(HTInterval.readFrom(buffer));
        }

        /* Assign the node's other information we have read previously */
        newNode.nodeEnd = end;
        newNode.stringSectionOffset = stringSectionOffset;
        newNode.isOnDisk = true;

        return newNode;
    }

    /**
     * Write this node to the given file channel.
     *
     * @param fc
     *            The file channel to write to (should be sought to be correct
     *            position)
     * @throws IOException
     *             If there was an error writing
     */
    public final void writeSelf(FileChannel fc) throws IOException {
        /*
         * Yes, we are taking the *read* lock here, because we are reading the
         * information in the node to write it to disk.
         */
        rwl.readLock().lock();
        try {
            final int blockSize = config.getBlockSize();
            int curStringsEntryEndPos = blockSize;

            ByteBuffer buffer = ByteBuffer.allocate(blockSize);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.clear();

            /* Write the common header part */
            buffer.put(this.getNodeType().toByte());
            buffer.putLong(nodeStart);
            buffer.putLong(nodeEnd);
            buffer.putInt(sequenceNumber);
            buffer.putInt(parentSequenceNumber);
            buffer.putInt(intervals.size());
            buffer.putInt(stringSectionOffset);
            buffer.put((byte) 1); // TODO Used to be "isDone", to be removed from header

            /* Now call the inner method to write the specific header part */
            this.writeSpecificHeader(buffer);

            /* Back to us, we write the intervals */
            for (HTInterval interval : intervals) {
                int size = interval.writeInterval(buffer, curStringsEntryEndPos);
                curStringsEntryEndPos -= size;
            }

            /*
             * Write padding between the end of the Data section and the start
             * of the Strings section (needed to fill the node in case there is
             * no Strings section)
             */
            while (buffer.position() < stringSectionOffset) {
                buffer.put((byte) 0);
            }

            /*
             * If the offsets were right, the size of the Strings section should
             * be == to the expected size
             */
            assert (curStringsEntryEndPos == stringSectionOffset);

            /* Finally, write everything in the Buffer to disk */

            // if we don't do this, flip() will lose what's after.
            buffer.position(blockSize);

            buffer.flip();
            int res = fc.write(buffer);
            assert (res == blockSize);

        } finally {
            rwl.readLock().unlock();
        }
        isOnDisk = true;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Retrieve the history tree configuration used for this node.
     *
     * @return The history tree config
     */
    protected HTConfig getConfig() {
        return config;
    }

    /**
     * Get the start time of this node.
     *
     * @return The start time of this node
     */
    public long getNodeStart() {
        return nodeStart;
    }

    /**
     * Get the end time of this node.
     *
     * @return The end time of this node
     */
    public long getNodeEnd() {
        if (this.isOnDisk) {
            return nodeEnd;
        }
        return 0;
    }

    /**
     * Get the sequence number of this node.
     *
     * @return The sequence number of this node
     */
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Get the sequence number of this node's parent.
     *
     * @return The parent sequence number
     */
    public int getParentSequenceNumber() {
        return parentSequenceNumber;
    }

    /**
     * Change this node's parent. Used when we create a new root node for
     * example.
     *
     * @param newParent
     *            The sequence number of the node that is the new parent
     */
    public void setParentSequenceNumber(int newParent) {
        parentSequenceNumber = newParent;
    }

    /**
     * Return if this node is "done" (full and written to disk).
     *
     * @return If this node is done or not
     */
    public boolean isOnDisk() {
        return isOnDisk;
    }

    /**
     * Add an interval to this node
     *
     * @param newInterval
     *            Interval to add to this node
     */
    public void addInterval(HTInterval newInterval) {
        rwl.writeLock().lock();
        try {
            /* Just in case, should be checked before even calling this function */
            assert (newInterval.getIntervalSize() <= this.getNodeFreeSpace());

            intervals.add(newInterval);
            sizeOfIntervalSection += newInterval.getIntervalSize();

            /* Update the in-node offset "pointer" */
            stringSectionOffset -= (newInterval.getStringsEntrySize());
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * We've received word from the containerTree that newest nodes now exist to
     * our right. (Puts isDone = true and sets the endtime)
     *
     * @param endtime
     *            The nodeEnd time that the node will have
     */
    public void closeThisNode(long endtime) {
        rwl.writeLock().lock();
        try {
            assert (endtime >= this.nodeStart);

            if (!intervals.isEmpty()) {
                /*
                 * Sort the intervals by ascending order of their end time. This
                 * speeds up lookups a bit
                 */
                Collections.sort(intervals);

                /*
                 * Make sure there are no intervals in this node with their
                 * EndTime > the one requested. Only need to check the last one
                 * since they are now sorted
                 */
                assert (endtime >= intervals.get(intervals.size() - 1).getEndTime());
            }

            this.nodeEnd = endtime;
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * The method to fill up the stateInfo (passed on from the Current State
     * Tree when it does a query on the SHT). We'll replace the data in that
     * vector with whatever relevant we can find from this node
     *
     * @param stateInfo
     *            The same stateInfo that comes from SHT's doQuery()
     * @param t
     *            The timestamp for which the query is for. Only return
     *            intervals that intersect t.
     * @throws TimeRangeException
     *             If 't' is invalid
     */
    public void writeInfoFromNode(List<ITmfStateInterval> stateInfo, long t)
            throws TimeRangeException {
        /* This is from a state system query, we are "reading" this node */
        rwl.readLock().lock();
        try {
            for (int i = getStartIndexFor(t); i < intervals.size(); i++) {
                /*
                 * Now we only have to compare the Start times, since we now the
                 * End times necessarily fit.
                 *
                 * Second condition is to ignore new attributes that might have
                 * been created after stateInfo was instantiated (they would be
                 * null anyway).
                 */
                ITmfStateInterval interval = intervals.get(i);
                if (interval.getStartTime() <= t &&
                        interval.getAttribute() < stateInfo.size()) {
                    stateInfo.set(interval.getAttribute(), interval);
                }
            }
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * Get a single Interval from the information in this node If the
     * key/timestamp pair cannot be found, we return null.
     *
     * @param key
     *            The attribute quark to look for
     * @param t
     *            The timestamp
     * @return The Interval containing the information we want, or null if it
     *         wasn't found
     * @throws TimeRangeException
     *             If 't' is invalid
     */
    public HTInterval getRelevantInterval(int key, long t) throws TimeRangeException {
        rwl.readLock().lock();
        try {
            for (int i = getStartIndexFor(t); i < intervals.size(); i++) {
                HTInterval curInterval = intervals.get(i);
                if (curInterval.getAttribute() == key
                        && curInterval.getStartTime() <= t
                        && curInterval.getEndTime() >= t) {
                    return curInterval;
                }
            }

            /* We didn't find the relevant information in this node */
            return null;

        } finally {
            rwl.readLock().unlock();
        }
    }

    private int getStartIndexFor(long t) throws TimeRangeException {
        /* Should only be called by methods with the readLock taken */

        if (intervals.isEmpty()) {
            return 0;
        }
        /*
         * Since the intervals are sorted by end time, we can skip all the ones
         * at the beginning whose end times are smaller than 't'. Java does
         * provides a .binarySearch method, but its API is quite weird...
         */
        HTInterval dummy = new HTInterval(0, t, 0, TmfStateValue.nullValue());
        int index = Collections.binarySearch(intervals, dummy);

        if (index < 0) {
            /*
             * .binarySearch returns a negative number if the exact value was
             * not found. Here we just want to know where to start searching, we
             * don't care if the value is exact or not.
             */
            index = -index - 1;

        }

        /* Sometimes binarySearch yields weird stuff... */
        if (index < 0) {
            index = 0;
        }
        if (index >= intervals.size()) {
            index = intervals.size() - 1;
        }

        /*
         * Another API quirkiness, the returned index is the one of the *last*
         * element of a series of equal endtimes, which happens sometimes. We
         * want the *first* element of such a series, to read through them
         * again.
         */
        while (index > 0
                && intervals.get(index - 1).compareTo(intervals.get(index)) == 0) {
            index--;
        }

        return index;
    }

    /**
     * <pre>
     *  1 - byte (type)
     * 16 - 2x long (start time, end time)
     * 16 - 4x int (seq number, parent seq number, intervalcount,
     *              strings section pos.)
     *  1 - byte (done or not)
     * </pre>
     */
    private static final int COMMON_HEADER_SIZE = 34;

    /**
     * Return the total header size of this node (will depend on the node type).
     *
     * @return The total header size
     */
    public final int getTotalHeaderSize() {
        return COMMON_HEADER_SIZE + getSpecificHeaderSize();
    }

    /**
     * @return The offset, within the node, where the Data section ends
     */
    private int getDataSectionEndOffset() {
        return this.getTotalHeaderSize() + sizeOfIntervalSection;
    }

    /**
     * Returns the free space in the node, which is simply put, the
     * stringSectionOffset - dataSectionOffset
     *
     * @return The amount of free space in the node (in bytes)
     */
    public int getNodeFreeSpace() {
        rwl.readLock().lock();
        int ret = stringSectionOffset - this.getDataSectionEndOffset();
        rwl.readLock().unlock();

        return ret;
    }

    /**
     * Returns the current space utilization of this node, as a percentage.
     * (used space / total usable space, which excludes the header)
     *
     * @return The percentage (value between 0 and 100) of space utilization in
     *         in this node.
     */
    public long getNodeUsagePercent() {
        rwl.readLock().lock();
        try {
            final int blockSize = config.getBlockSize();
            float freePercent = (float) this.getNodeFreeSpace()
                    / (float) (blockSize - this.getTotalHeaderSize())
                    * 100F;
            return (long) (100L - freePercent);

        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * @name Debugging functions
     */

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        /* Only used for debugging, shouldn't be externalized */
        StringBuffer buf = new StringBuffer("Node #" + sequenceNumber + ", ");
        buf.append(this.toStringSpecific());
        buf.append(intervals.size() + " intervals (" + this.getNodeUsagePercent()
                + "% used), ");

        buf.append("[" + this.nodeStart + " - ");
        if (this.isOnDisk) {
            buf = buf.append("" + this.nodeEnd + "]");
        } else {
            buf = buf.append("...]");
        }
        return buf.toString();
    }

    /**
     * Debugging function that prints out the contents of this node
     *
     * @param writer
     *            PrintWriter in which we will print the debug output
     */
    @SuppressWarnings("nls")
    public void debugPrintIntervals(PrintWriter writer) {
        /* Only used for debugging, shouldn't be externalized */
        writer.println("Node #" + sequenceNumber + ":");

        /* Array of children */
        if (this.getNodeType() == NodeType.CORE) { /* Only Core Nodes can have children */
            CoreNode thisNode = (CoreNode) this;
            writer.print("  " + thisNode.getNbChildren() + " children");
            if (thisNode.getNbChildren() >= 1) {
                writer.print(": [ " + thisNode.getChild(0));
                for (int i = 1; i < thisNode.getNbChildren(); i++) {
                    writer.print(", " + thisNode.getChild(i));
                }
                writer.print(']');
            }
            writer.print('\n');
        }

        /* List of intervals in the node */
        writer.println("  Intervals contained:");
        for (int i = 0; i < intervals.size(); i++) {
            writer.println(intervals.get(i).toString());
        }
        writer.println('\n');
    }

    // ------------------------------------------------------------------------
    // Abstract methods
    // ------------------------------------------------------------------------

    /**
     * Get the byte value representing the node type.
     *
     * @return The node type
     */
    public abstract NodeType getNodeType();

    /**
     * Return the specific header size of this node. This means the size
     * occupied by the type-specific section of the header (not counting the
     * common part).
     *
     * @return The specific header size
     */
    protected abstract int getSpecificHeaderSize();

    /**
     * Read the type-specific part of the node header from a byte buffer.
     *
     * @param buffer
     *            The byte buffer to read from. It should be already positioned
     *            correctly.
     */
    protected abstract void readSpecificHeader(ByteBuffer buffer);

    /**
     * Write the type-specific part of the header in a byte buffer.
     *
     * @param buffer
     *            The buffer to write to. It should already be at the correct
     *            position.
     */
    protected abstract void writeSpecificHeader(ByteBuffer buffer);

    /**
     * Node-type-specific toString method. Used for debugging.
     *
     * @return A string representing the node
     */
    protected abstract String toStringSpecific();
}
