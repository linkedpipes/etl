package com.linkedpipes.plugin.transformer.xmltochunks;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class XmlToChunks implements Component, SequentialExecution {

    @Component.InputPort(iri = "FilesInput")
    public FilesDataUnit inputFiles;

    @Component.OutputPort(iri = "FilesOutput")
    public WritableFilesDataUnit outputFiles;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.Configuration
    public XmlToChunksConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    private final XMLInputFactory inFactory = XMLInputFactory.newInstance();

    private int chunkNumber = 1;

    private XMLEventReader eventReader;

    private Document document;

    private Transformer transformer;

    private DocumentBuilder docBuilder;

    @Override
    public void execute() throws LpException {
        initializeProperties();
        progressReport.start(inputFiles.size());
        for (FilesDataUnit.Entry entry : inputFiles) {
            splitEntry(entry);
            progressReport.entryProcessed();
        }
        progressReport.done();
    }


    private void initializeProperties() throws LpException {
        DocumentBuilderFactory builderFactory =
                DocumentBuilderFactory.newInstance();
        TransformerFactory transformerFactory =
                TransformerFactory.newInstance();
        try {
            docBuilder = builderFactory.newDocumentBuilder();
            document = docBuilder.newDocument();
            transformer = transformerFactory.newTransformer();
        } catch (ParserConfigurationException |
                TransformerConfigurationException ex) {
            throw exceptionFactory.failure(
                    "Can't load XML document builder.", ex);
        }
    }

    private void splitEntry(FilesDataUnit.Entry entry) throws LpException {
        File input = entry.toFile();
        try {
            eventReader = inFactory.createXMLEventReader(new FileReader(input));
            splitXmlFile(entry.getFileName());
        } catch (IOException ex) {
            throw exceptionFactory.failure(
                    "Can't write output to file.", ex);
        } catch (XMLStreamException ex) {
            throw exceptionFactory.failure(
                    "Exception when parsing XML. ", ex);
        } catch (TransformerException ex) {
            throw exceptionFactory.failure(
                    "Exception when transforming XML. ", ex);
        }
    }

    private void splitXmlFile(String inputFileName) throws
            LpException, XMLStreamException, IOException, TransformerException {
        Node context = document;
        int bytes = 0;
        int maximumChunkSize = configuration.getChunk_size() * 1024 ; //* 1024;
        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();
            bytes = bytes + event.toString().length();

            boolean tooBig  = bytes > maximumChunkSize;
            boolean isClosing =
                    event.getEventType() == XMLStreamConstants.END_ELEMENT;
            boolean canSplitOnThisElement =
                    !(isWithinReferenceNodes(context.getParentNode()));
            if (tooBig && isClosing && canSplitOnThisElement) {
                bytes = 0;
                setNextOutputStream(inputFileName);
                context = createCleanDocument(context.getParentNode());
                continue;
            }

            switch (event.getEventType()) {
                case XMLStreamConstants.END_DOCUMENT:
                    setNextOutputStream(inputFileName);
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    context = addNode(context, event);
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    context = context.getParentNode();
                    break;
                case XMLStreamConstants.CHARACTERS:
                    context.appendChild(document.createTextNode(
                            event.asCharacters().getData()));
                    break;
                default:
                    break;
            }
        }
    }

    private Node addNode(Node context, XMLEvent event) {
        QName name = event.asStartElement().getName();

        Element curr = document.createElementNS(name.getNamespaceURI(),
                name.getLocalPart());
        context.appendChild(curr);
        context = curr;

        Iterator<Attribute> iterator = event.asStartElement().getAttributes();
        while (iterator.hasNext()) {
            Attribute attr = iterator.next();
            ((Element) context).setAttributeNS(
                    attr.getName().getNamespaceURI(),
                    attr.getName().getLocalPart(), attr.getValue());
        }
        return context;
    }

    private boolean isWithinReferenceNodes(Node current) {
        for (XmlToChunksConfiguration.Reference reference :
                configuration.getReferences()) {
            if (isWithinReferenceNode(current, reference)) {
                return true;
            }
        }
        return false;
    }

    private boolean isWithinReferenceNode(Node node,
            XmlToChunksConfiguration.Reference reference) {
        while (node != null && node != document) {
            if (nodeMatchToReference(node, reference)) {
                return true;
            }
            node = node.getParentNode();
        }
        return false;
    }

    private boolean nodeMatchToReference(Node node,
            XmlToChunksConfiguration.Reference reference) {
        if (!node.getLocalName().equals(reference.getLocal())) {
            return false;
        }
        if (reference.getPrefix() == null || reference.getPrefix().isEmpty()) {
            if (node.getPrefix() == null) {
                return true;
            } else {
                return false;
            }
        } else {
            return reference.getPrefix().equals(node.getPrefix());
        }
    }

    private Node createCleanDocument(Node reachElement) {
        Document newDocument = docBuilder.newDocument();

        Node current = reachElement;
        Node prev = null;
        Node output = null;
        while (current != document) {
            Node temp = current.cloneNode(false);
            if (output == null) {
                output = temp;
            }
            current = current.getParentNode();
            if (prev != null) {
                temp.appendChild(prev);
            }
            prev = temp;
        }

        current = newDocument.adoptNode(prev);
        newDocument.appendChild(current);
        document = newDocument;
        return output;
    }

    private void setNextOutputStream(String outputFile)
            throws IOException, XMLStreamException, LpException,
            TransformerException {
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(
                outputFiles.createFile(chunkNumber + "_" + outputFile));
        transformer.transform(source, result);
        chunkNumber++;
    }


}
