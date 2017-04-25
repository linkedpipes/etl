package com.linkedpipes.plugin.transformer.xmltochunks;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sun.rmi.runtime.Log;

import javax.xml.namespace.QName;
import javax.xml.parsers.*;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.io.*;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * Created by patzo on 18.04.17.
 */
public class XMLtoChunks implements Component, SequentialExecution {
    private static final Logger LOG = LoggerFactory.getLogger(XMLtoChunks.class);
    XMLInputFactory inFactory = XMLInputFactory.newInstance();
    XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
    OutputStream outputStream;
    int chunk_number = 1;
    XMLEventReader eventReader;
    int bytes = 0;
    Document doc;
    Transformer transformer;
    DocumentBuilder docBuilder;


    @Component.InputPort(iri = "FilesInput")
    public FilesDataUnit inputFiles;

    @Component.OutputPort(iri = "FilesOutput")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public XMLtoChunksConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        initializeProperties();

        LOG.debug("Executing XML to chunks");
        progressReport.start(inputFiles.size());
        for (FilesDataUnit.Entry entry : inputFiles) {

            File outputFile = outputFiles.createFile(entry.getFileName());
            File input = entry.toFile();

            try {

                eventReader =
                        inFactory.createXMLEventReader(
                                new FileReader(input));

                iterateXML(entry.toFile());

                progressReport.entryProcessed();
            } catch (IOException ex) {
                throw exceptionFactory.failure("Can't write output to file.",
                        ex);
            } catch (XMLStreamException ex){
                throw exceptionFactory.failure("Exception when parsing XML. ",
                        ex);
            } catch (TransformerException ex){
                throw exceptionFactory.failure("Exception when transforming XML. ",
                        ex);
            }
        }
        progressReport.done();
    }

    private void initializeProperties() throws LpException{
        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = docBuilder.newDocument();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformer = transformerFactory.newTransformer();
        }catch(ParserConfigurationException | TransformerConfigurationException ex) {
            throw exceptionFactory.failure("Can't load XML doc builder.",
                    ex);
        }
    }

    private void iterateXML(File outputFile)throws
            LpException, XMLStreamException, IOException, TransformerException {
        Node context = doc;


        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();

            bytes = bytes + event.toString().length();
            if (bytes > configuration.getChunk_size() * 3 * 1000 * 1000 &&
                    event.getEventType() == XMLStreamConstants.END_ELEMENT &&
                    !(isWithinNodes(context.getParentNode()))) {

                setNextOutputStream(outputFile.getName());
                context = cleanDoc(context.getParentNode());
                continue;
            }
            switch (event.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT:
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    setNextOutputStream(outputFile.getName());
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    QName name = event.asStartElement().getName();

                    Element curr = doc.createElementNS(name.getNamespaceURI(),
                            name.getLocalPart());
                    context.appendChild(curr);
                    context = curr;

                    Iterator<Attribute> iterator = event.asStartElement().getAttributes();
                    while(iterator.hasNext()){
                        Attribute attr = iterator.next();
                        ((Element) context).setAttributeNS(attr.getName().getNamespaceURI(),
                                attr.getName().getLocalPart(), attr.getValue());
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    context = context.getParentNode();
                    break;
                case XMLStreamConstants.CHARACTERS:
                    context.appendChild(doc.createTextNode(event.asCharacters().getData()));
                    break;
            }
        }
    }

    private void setNextOutputStream(String outputFile) throws IOException, XMLStreamException, LpException,
            TransformerException {
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outputFiles.createFile(chunk_number + "_" + outputFile));
        transformer.transform(source, result);
        bytes = 0;
        chunk_number ++;
    }

    private boolean isWithinNodes(Node current){
        for (XMLtoChunksConfiguration.Reference r : configuration.getReferences()){
            if(isWithinNode(current, r.getPrefix(), r.getLocal()))
                return true;
        }
        return false;
    }

    private boolean isWithinNode(Node current, String parentNamespaceURI, String parentLocal){
        while(current.getParentNode() != doc){
            if (current.getNamespaceURI().equals(parentNamespaceURI) &&
                    current.getLocalName().equals(parentLocal))
                return true;
            current = current.getParentNode();
        }
        return false;
    }

    private Node cleanDoc(Node reach_elem){
        Document doc2 = docBuilder.newDocument();

        Node current = reach_elem;
        Node prev = null;
        Node out = null;
        while(current != doc){
            Node temp = current.cloneNode(false);

            if (out == null)
                out = temp;
            current = current.getParentNode();
            if (prev != null)
                temp.appendChild(prev);
            prev = temp;
        }

        current = doc2.adoptNode(prev);
        doc2.appendChild(current);
        doc = doc2;
        return out;
    }

}
