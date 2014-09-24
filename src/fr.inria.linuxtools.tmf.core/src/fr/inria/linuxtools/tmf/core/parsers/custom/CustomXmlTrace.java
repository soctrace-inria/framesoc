/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package fr.inria.linuxtools.tmf.core.parsers.custom;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import fr.inria.linuxtools.internal.tmf.core.Activator;
import fr.inria.linuxtools.tmf.core.event.ITmfEvent;
import fr.inria.linuxtools.tmf.core.exceptions.TmfTraceException;
import fr.inria.linuxtools.tmf.core.io.BufferedRandomAccessFile;
import fr.inria.linuxtools.tmf.core.parsers.custom.CustomXmlTraceDefinition.InputAttribute;
import fr.inria.linuxtools.tmf.core.parsers.custom.CustomXmlTraceDefinition.InputElement;
import fr.inria.linuxtools.tmf.core.timestamp.TmfTimestamp;
import fr.inria.linuxtools.tmf.core.trace.ITmfContext;
import fr.inria.linuxtools.tmf.core.trace.ITmfEventParser;
import fr.inria.linuxtools.tmf.core.trace.TmfContext;
import fr.inria.linuxtools.tmf.core.trace.TmfTrace;
import fr.inria.linuxtools.tmf.core.trace.TraceValidationStatus;
import fr.inria.linuxtools.tmf.core.trace.indexer.ITmfPersistentlyIndexable;
import fr.inria.linuxtools.tmf.core.trace.indexer.ITmfTraceIndexer;
import fr.inria.linuxtools.tmf.core.trace.indexer.TmfBTreeTraceIndexer;
import fr.inria.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import fr.inria.linuxtools.tmf.core.trace.indexer.checkpoint.TmfCheckpoint;
import fr.inria.linuxtools.tmf.core.trace.location.ITmfLocation;
import fr.inria.linuxtools.tmf.core.trace.location.TmfLongLocation;

/**
 * Trace object for custom XML trace parsers.
 *
 * @author Patrick Tassé
 * @since 3.0
 */
public class CustomXmlTrace extends TmfTrace implements ITmfEventParser, ITmfPersistentlyIndexable {

    private static final TmfLongLocation NULL_LOCATION = new TmfLongLocation(-1L);
    private static final int DEFAULT_CACHE_SIZE = 100;
    private static final int MAX_LINES = 100;
    private static final int CONFIDENCE = 100;

    private final CustomXmlTraceDefinition fDefinition;
    private final CustomXmlEventType fEventType;
    private final InputElement fRecordInputElement;
    private BufferedRandomAccessFile fFile;

    /**
     * Basic constructor
     *
     * @param definition
     *            Trace definition
     */
    public CustomXmlTrace(final CustomXmlTraceDefinition definition) {
        fDefinition = definition;
        fEventType = new CustomXmlEventType(fDefinition);
        fRecordInputElement = getRecordInputElement(fDefinition.rootInputElement);
        setCacheSize(DEFAULT_CACHE_SIZE);
    }

    /**
     * Full constructor
     *
     * @param resource
     *            Trace resource
     * @param definition
     *            Trace definition
     * @param path
     *            Path to the trace/log file
     * @param pageSize
     *            Page size to use
     * @throws TmfTraceException
     *             If the trace/log couldn't be opened
     */
    public CustomXmlTrace(final IResource resource,
            final CustomXmlTraceDefinition definition, final String path,
            final int pageSize) throws TmfTraceException {
        this(definition);
        setCacheSize((pageSize > 0) ? pageSize : DEFAULT_CACHE_SIZE);
        initTrace(resource, path, CustomXmlEvent.class);
    }

    @Override
    public void initTrace(final IResource resource, final String path, final Class<? extends ITmfEvent> eventType) throws TmfTraceException {
        super.initTrace(resource, path, eventType);
        try {
            fFile = new BufferedRandomAccessFile(getPath(), "r"); //$NON-NLS-1$
        } catch (IOException e) {
            throw new TmfTraceException(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void dispose() {
        super.dispose();
        if (fFile != null) {
            try {
                fFile.close();
            } catch (IOException e) {
            } finally {
                fFile = null;
            }
        }
    }

    @Override
    public ITmfTraceIndexer getIndexer() {
        return super.getIndexer();
    }

    @Override
    public synchronized TmfContext seekEvent(final ITmfLocation location) {
        final CustomXmlTraceContext context = new CustomXmlTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
        if (NULL_LOCATION.equals(location) || fFile == null) {
            return context;
        }
        try {
            if (location == null) {
                fFile.seek(0);
            } else if (location.getLocationInfo() instanceof Long) {
                fFile.seek((Long) location.getLocationInfo());
            }
            long rawPos = fFile.getFilePointer();
            String line = fFile.getNextLine();
            while (line != null) {
                final int idx = indexOfElement(fRecordInputElement.elementName, line, 0);
                if (idx != -1) {
                    context.setLocation(new TmfLongLocation(rawPos + idx));
                    return context;
                }
                rawPos = fFile.getFilePointer();
                line = fFile.getNextLine();
            }
            return context;
        } catch (final IOException e) {
            Activator.logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
            return context;
        }

    }

    @Override
    public synchronized TmfContext seekEvent(final double ratio) {
        if (fFile == null) {
            return new CustomTxtTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
        }
        try {
            long pos = Math.round(ratio * fFile.length());
            while (pos > 0) {
                fFile.seek(pos - 1);
                if (fFile.read() == '\n') {
                    break;
                }
                pos--;
            }
            final ITmfLocation location = new TmfLongLocation(pos);
            final TmfContext context = seekEvent(location);
            context.setRank(ITmfContext.UNKNOWN_RANK);
            return context;
        } catch (final IOException e) {
            Activator.logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
            return new CustomXmlTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
        }
    }

    @Override
    public synchronized double getLocationRatio(final ITmfLocation location) {
        if (fFile == null) {
            return 0;
        }
        try {
            if (location.getLocationInfo() instanceof Long) {
                return ((Long) location.getLocationInfo()).doubleValue() / fFile.length();
            }
        } catch (final IOException e) {
            Activator.logError("Error getting location ration. File: " + getPath(), e); //$NON-NLS-1$
        }
        return 0;
    }

    @Override
    public ITmfLocation getCurrentLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public synchronized CustomXmlEvent parseEvent(final ITmfContext tmfContext) {
        ITmfContext context = seekEvent(tmfContext.getLocation());
        return parse(context);
    }

    @Override
    public synchronized CustomXmlEvent getNext(final ITmfContext context) {
        final ITmfContext savedContext = new TmfContext(context.getLocation(), context.getRank());
        final CustomXmlEvent event = parse(context);
        if (event != null) {
            updateAttributes(savedContext, event.getTimestamp());
            context.increaseRank();
        }
        return event;
    }

    private synchronized CustomXmlEvent parse(final ITmfContext tmfContext) {
        if (fFile == null) {
            return null;
        }
        if (!(tmfContext instanceof CustomXmlTraceContext)) {
            return null;
        }

        final CustomXmlTraceContext context = (CustomXmlTraceContext) tmfContext;
        if (context.getLocation() == null || !(context.getLocation().getLocationInfo() instanceof Long) || NULL_LOCATION.equals(context.getLocation())) {
            return null;
        }

        CustomXmlEvent event = null;
        try {
            if (fFile.getFilePointer() != (Long) context.getLocation().getLocationInfo() + 1)
            {
                fFile.seek((Long) context.getLocation().getLocationInfo() + 1); // +1 is for the <
            }
            final StringBuffer elementBuffer = new StringBuffer("<"); //$NON-NLS-1$
            readElement(elementBuffer, fFile);
            final Element element = parseElementBuffer(elementBuffer);

            event = extractEvent(element, fRecordInputElement);
            ((StringBuffer) event.getContent().getValue()).append(elementBuffer);

            long rawPos = fFile.getFilePointer();
            String line = fFile.getNextLine();
            while (line != null) {
                final int idx = indexOfElement(fRecordInputElement.elementName, line, 0);
                if (idx != -1) {
                    context.setLocation(new TmfLongLocation(rawPos + idx));
                    return event;
                }
                rawPos = fFile.getFilePointer();
                line = fFile.getNextLine();
            }
        } catch (final IOException e) {
            Activator.logError("Error parsing event. File: " + getPath(), e); //$NON-NLS-1$

        }
        context.setLocation(NULL_LOCATION);
        return event;
    }

    private Element parseElementBuffer(final StringBuffer elementBuffer) {
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder db = dbf.newDocumentBuilder();

            // The following allows xml parsing without access to the dtd
            final EntityResolver resolver = new EntityResolver() {
                @Override
                public InputSource resolveEntity(final String publicId, final String systemId) {
                    final String empty = ""; //$NON-NLS-1$
                    final ByteArrayInputStream bais = new ByteArrayInputStream(empty.getBytes());
                    return new InputSource(bais);
                }
            };
            db.setEntityResolver(resolver);

            // The following catches xml parsing exceptions
            db.setErrorHandler(new ErrorHandler() {
                @Override
                public void error(final SAXParseException saxparseexception) throws SAXException {}

                @Override
                public void warning(final SAXParseException saxparseexception) throws SAXException {}

                @Override
                public void fatalError(final SAXParseException saxparseexception) throws SAXException {
                    throw saxparseexception;
                }
            });

            final Document doc = db.parse(new ByteArrayInputStream(elementBuffer.toString().getBytes()));
            return doc.getDocumentElement();
        } catch (final ParserConfigurationException e) {
            Activator.logError("Error parsing element buffer. File:" + getPath(), e); //$NON-NLS-1$
        } catch (final SAXException e) {
            Activator.logError("Error parsing element buffer. File:" + getPath(), e); //$NON-NLS-1$
        } catch (final IOException e) {
            Activator.logError("Error parsing element buffer. File: " + getPath(), e); //$NON-NLS-1$
        }
        return null;
    }

    private static int indexOfElement(String elementName, String line, int fromIndex) {
        final String recordElementStart = '<' + elementName;
        int index = line.indexOf(recordElementStart, fromIndex);
        if (index == -1) {
            return index;
        }
        int nextCharIndex = index + recordElementStart.length();
        if (nextCharIndex < line.length()) {
            char c = line.charAt(nextCharIndex);
            // Check that the match is not just a substring of another element
            if (Character.isLetterOrDigit(c)) {
                return indexOfElement(elementName, line, nextCharIndex);
            }
        }
        return index;
    }

    private void readElement(final StringBuffer buffer, final RandomAccessFile raFile) {
        try {
            int numRead = 0;
            boolean startTagClosed = false;
            int i;
            while ((i = raFile.read()) != -1) {
                numRead++;
                final char c = (char) i;
                buffer.append(c);
                if (c == '"') {
                    readQuote(buffer, raFile, '"');
                } else if (c == '\'') {
                    readQuote(buffer, raFile, '\'');
                } else if (c == '<') {
                    readElement(buffer, raFile);
                } else if (c == '/' && numRead == 1) {
                    break; // found "</"
                } else if (c == '-' && numRead == 3 && buffer.substring(buffer.length() - 3, buffer.length() - 1).equals("!-")) { //$NON-NLS-1$
                    readComment(buffer, raFile); // found "<!--"
                } else if (i == '>') {
                    if (buffer.charAt(buffer.length() - 2) == '/') {
                        break; // found "/>"
                    } else if (startTagClosed) {
                        break; // found "<...>...</...>"
                    }
                    else {
                        startTagClosed = true; // found "<...>"
                    }
                }
            }
            return;
        } catch (final IOException e) {
            return;
        }
    }

    private static void readQuote(final StringBuffer buffer,
            final RandomAccessFile raFile, final char eq) {
        try {
            int i;
            while ((i = raFile.read()) != -1) {
                final char c = (char) i;
                buffer.append(c);
                if (c == eq)
                {
                    break; // found matching end-quote
                }
            }
            return;
        } catch (final IOException e) {
            return;
        }
    }

    private static void readComment(final StringBuffer buffer,
            final RandomAccessFile raFile) {
        try {
            int numRead = 0;
            int i;
            while ((i = raFile.read()) != -1) {
                numRead++;
                final char c = (char) i;
                buffer.append(c);
                if (c == '>' && numRead >= 2 && buffer.substring(buffer.length() - 3, buffer.length() - 1).equals("--")) //$NON-NLS-1$
                {
                    break; // found "-->"
                }
            }
            return;
        } catch (final IOException e) {
            return;
        }
    }

    /**
     * Parse an XML element.
     *
     * @param parentElement
     *            The parent element
     * @param buffer
     *            The contents to parse
     * @return The parsed content
     */
    public static StringBuffer parseElement(final Element parentElement, final StringBuffer buffer) {
        final NodeList nodeList = parentElement.getChildNodes();
        String separator = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (separator == null) {
                    separator = " | "; //$NON-NLS-1$
                } else {
                    buffer.append(separator);
                }
                final Element element = (Element) node;
                if (!element.hasChildNodes()) {
                    buffer.append(element.getNodeName());
                } else if (element.getChildNodes().getLength() == 1 && element.getFirstChild().getNodeType() == Node.TEXT_NODE) {
                    buffer.append(element.getNodeName() + ":" + element.getFirstChild().getNodeValue().trim()); //$NON-NLS-1$
                } else {
                    buffer.append(element.getNodeName());
                    buffer.append(" [ "); //$NON-NLS-1$
                    parseElement(element, buffer);
                    buffer.append(" ]"); //$NON-NLS-1$
                }
            } else if (node.getNodeType() == Node.TEXT_NODE) {
                if (node.getNodeValue().trim().length() != 0) {
                    buffer.append(node.getNodeValue().trim());
                }
            }
        }
        return buffer;
    }

    /**
     * Get an input element if it is a valid record input. If not, we will look
     * into its children for valid inputs.
     *
     * @param inputElement
     *            The main element to check for.
     * @return The record element
     */
    public InputElement getRecordInputElement(final InputElement inputElement) {
        if (inputElement.logEntry) {
            return inputElement;
        } else if (inputElement.childElements != null) {
            for (final InputElement childInputElement : inputElement.childElements) {
                final InputElement recordInputElement = getRecordInputElement(childInputElement);
                if (recordInputElement != null) {
                    return recordInputElement;
                }
            }
        }
        return null;
    }

    /**
     * Extract a trace event from an XML element.
     *
     * @param element
     *            The element
     * @param inputElement
     *            The input element
     * @return The extracted event
     */
    public CustomXmlEvent extractEvent(final Element element, final InputElement inputElement) {
        final CustomXmlEvent event = new CustomXmlEvent(fDefinition, this, TmfTimestamp.ZERO, "", fEventType, ""); //$NON-NLS-1$ //$NON-NLS-2$
        event.setContent(new CustomEventContent(event, new StringBuffer()));
        parseElement(element, event, inputElement);
        return event;
    }

    private void parseElement(final Element element, final CustomXmlEvent event, final InputElement inputElement) {
        if (inputElement.inputName != null && !inputElement.inputName.equals(CustomXmlTraceDefinition.TAG_IGNORE)) {
            event.parseInput(parseElement(element, new StringBuffer()).toString(), inputElement.inputName, inputElement.inputAction, inputElement.inputFormat);
        }
        if (inputElement.attributes != null) {
            for (final InputAttribute attribute : inputElement.attributes) {
                event.parseInput(element.getAttribute(attribute.attributeName), attribute.inputName, attribute.inputAction, attribute.inputFormat);
            }
        }
        final NodeList childNodes = element.getChildNodes();
        if (inputElement.childElements != null) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node node = childNodes.item(i);
                if (node instanceof Element) {
                    for (final InputElement child : inputElement.childElements) {
                        if (node.getNodeName().equals(child.elementName)) {
                            parseElement((Element) node, event, child);
                            break;
                        }
                    }
                }
            }
        }
        return;
    }

    /**
     * Retrieve the trace definition.
     *
     * @return The trace definition
     */
    public CustomTraceDefinition getDefinition() {
        return fDefinition;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation sets the confidence to 100 if any of the first
     * 100 lines of the file contains a valid record input element, and 0
     * otherwise.
     */
    @Override
    public IStatus validate(IProject project, String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.CustomTrace_FileNotFound + ": " + path); //$NON-NLS-1$
        }
        try (BufferedRandomAccessFile rafile = new BufferedRandomAccessFile(path, "r")) { //$NON-NLS-1$
            int lineCount = 0;
            long rawPos = 0;
            String line = rafile.getNextLine();
            while ((line != null) && (lineCount++ < MAX_LINES)) {
                final int idx = indexOfElement(fRecordInputElement.elementName, line, 0);
                if (idx != -1) {
                    rafile.seek(rawPos + idx + 1); // +1 is for the <
                    final StringBuffer elementBuffer = new StringBuffer("<"); //$NON-NLS-1$
                    readElement(elementBuffer, rafile);
                    final Element element = parseElementBuffer(elementBuffer);
                    if (element != null) {
                        rafile.close();
                        return new TraceValidationStatus(CONFIDENCE, Activator.PLUGIN_ID);
                    }
                }
                rawPos = rafile.getFilePointer();
                line = rafile.getNextLine();
            }
        } catch (IOException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "IOException validating file: " + path, e); //$NON-NLS-1$
        }
        return new TraceValidationStatus(0, Activator.PLUGIN_ID);
    }

    private static int fCheckpointSize = -1;

    @Override
    public synchronized int getCheckpointSize() {
        if (fCheckpointSize == -1) {
            TmfCheckpoint c = new TmfCheckpoint(TmfTimestamp.ZERO, new TmfLongLocation(0L), 0);
            ByteBuffer b = ByteBuffer.allocate(ITmfCheckpoint.MAX_SERIALIZE_SIZE);
            b.clear();
            c.serialize(b);
            fCheckpointSize = b.position();
        }

        return fCheckpointSize;
    }

    @Override
    public ITmfLocation restoreLocation(ByteBuffer bufferIn) {
        return new TmfLongLocation(bufferIn);
    }

    @Override
    protected ITmfTraceIndexer createIndexer(int interval) {
        return new TmfBTreeTraceIndexer(this, interval);
    }
}
