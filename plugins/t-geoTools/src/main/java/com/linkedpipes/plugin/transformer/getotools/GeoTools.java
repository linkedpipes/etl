package com.linkedpipes.plugin.transformer.getotools;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.ChunkedStatements;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableChunkedStatements;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transform geo coordinates.
 */
public final class GeoTools implements Component.Sequential {

    private static class Point {

        /**
         * If true the subject has required type.
         */
        boolean hasType = false;

        Value coord = null;

        String coordType = null;

    }

    private static final Logger LOG = LoggerFactory.getLogger(GeoTools.class);

    @Component.InputPort(id = "InputRdf")
    public ChunkedStatements inputRdf;

    @Component.InputPort(id = "OutputRdf")
    public WritableChunkedStatements outputRdf;

    @Component.Configuration
    public GeoToolsConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    private CoordinateReferenceSystem targetCrs = null;

    private ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private boolean printTypeWarning = false;

    @Override
    public void execute() throws LpException {
        //
        try {
            targetCrs = CRS.decode(configuration.getOutputCoordType());
        } catch (Exception ex) {
            throw exceptionFactory.failure("Can't create output CRS: {}",
                    configuration.getOutputCoordType(), ex);
        }
        //
        progressReport.start(inputRdf.size());
        // We need to load statements.
        final Map<Resource, Point> resources = new HashMap<>();
        final List<Statement> outputBuffer = new ArrayList<>(50000);
        for (ChunkedStatements.Chunk chunk : inputRdf) {
            resources.clear();
            outputBuffer.clear();
            for (Statement s : chunk.toStatements()) {
                LOG.info("{}", s.getPredicate());
                // Check for type.
                if (RDF.TYPE.equals(s.getPredicate())) {
                    final String typeAsStr = s.getObject().stringValue();
                    if (typeAsStr.equals(configuration.getType())) {
                        final Point point = getOrCreate(
                                resources, s.getSubject());
                        point.hasType = true;
                        if (isValid(point)) {
                            process(s.getSubject(), point, outputBuffer);
                            resources.remove(s.getSubject());
                        }
                    }
                }
                // Check for other values.
                final String predicate = s.getPredicate().stringValue();
                if (predicate.equals(configuration.getCoord())) {
                    final Point point = getOrCreate(resources, s.getSubject());
                    point.coord = s.getObject();
                    if (isValid(point)) {
                        process(s.getSubject(), point, outputBuffer);
                        resources.remove(s.getSubject());
                    }
                } else if (predicate.equals(configuration.getCoordType())) {
                    final Point point = getOrCreate(resources, s.getSubject());
                    point.coordType = getType(s.getObject());
                    if (isValid(point)) {
                        process(s.getSubject(), point, outputBuffer);
                        resources.remove(s.getSubject());
                    }
                }
            }
            // Check all remaining resources and process them.
            for (Map.Entry<Resource, Point> entry : resources.entrySet()) {
                final Point point = entry.getValue();
                if (!point.hasType || point.coord == null) {
                    continue;
                }
                //
                if (point.coordType == null) {
                    process(entry.getKey(), point.coord,
                            configuration.getCoordType(), outputBuffer);
                } else {
                    process(entry.getKey(), point.coord,
                            point.coordType, outputBuffer);
                }
            }
            //
            outputRdf.submit(outputBuffer);
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    protected void process(Resource subject, Point point,
            List<Statement> outputBuffer) throws LpException {
        process(subject, point.coord, point.coordType,
                outputBuffer);
    }

    /**
     * Create new output entity and connect it.
     *
     * @param subject
     * @param coord
     * @param coordType
     * @param outputBuffer
     */
    protected void process(Resource subject, Value coord, String coordType,
            List<Statement> outputBuffer) throws LpException {
        if (coordType == null || coordType.isEmpty()) {
            coordType = configuration.getDefaultCoordType();
        }
        double transX;
        double transY;
        // Transform.
        try {
            String[] coordSplit = coord.stringValue().split(" ");
            double x = Double.parseDouble(coordSplit[0]);
            double y = Double.parseDouble(coordSplit[1]);
            //
            final DirectPosition2D srcPosition =
                    createDirectPosition(x, y, coordType);
            final DirectPosition2D dstPosition = new DirectPosition2D();
            boolean lenient = true;
            final MathTransform mathTransform = CRS.findMathTransform(
                    srcPosition.getCoordinateReferenceSystem(),
                    targetCrs, lenient);
            mathTransform.transform(srcPosition, dstPosition);
            transX = dstPosition.x;
            transY = dstPosition.y;
        } catch (Exception ex) {
            if (configuration.isFailOnError()) {
                throw exceptionFactory.failure("Can't convert coordinate: {}",
                        subject.stringValue(), ex);
            } else {
                LOG.error("Can't convert coordinate: {} ('{}','{}')",
                        subject.stringValue(), coord.stringValue(),
                        coordType, ex);
                return;
            }
        }
        // Create output.
        final Resource entity = valueFactory.createIRI(
                subject.stringValue() + "/wgs84");

        outputBuffer.add(valueFactory.createStatement(subject,
                valueFactory.createIRI(configuration.getOutputPredicate()),
                entity));

        outputBuffer.add(valueFactory.createStatement(entity, RDF.TYPE,
                valueFactory.createIRI("http://schema.org/GeoCoordinates")));

        outputBuffer.add(valueFactory.createStatement(entity,
                valueFactory.createIRI("http://schema.org/longitude"),
                valueFactory.createLiteral(doubleToStr(transY),
                        "http://www.w3.org/2001/XMLSchema#double")));

        outputBuffer.add(valueFactory.createStatement(entity,
                valueFactory.createIRI("http://schema.org/latitude"),
                valueFactory.createLiteral(doubleToStr(transX),
                        "http://www.w3.org/2001/XMLSchema#double")));

    }

    private static String doubleToStr(double value) {
        return Double.toString(value);
    }

    private static String getType(Value type) {
        // urn:ogc:def:crs:EPSG::5514 --> EPSG:5514
        String typeAsStr = type.stringValue();
        typeAsStr = typeAsStr.substring(typeAsStr.lastIndexOf("crs:") + 4);
        return typeAsStr.replaceFirst("::", ":");
    }

    private DirectPosition2D createDirectPosition(double x, double y,
            String coordType) throws FactoryException {
        if ("EPSG:5514".equals(coordType.toUpperCase())) {
            if (!printTypeWarning) {
                LOG.warn("EPSG:2065 with conversion used instead of EPSG:5514");
                printTypeWarning = true;
            }
            // Convert 5541 to 2065. The 5514 does not have full support
            // in geo tools.
            // Another solution is to introduce a custom provider.
            coordType = "EPSG:2065";
            double swap = x;
            x = -y;
            y = -swap;
        }
        final CoordinateReferenceSystem sourceCrs = CRS.decode(coordType);
        return new DirectPosition2D(sourceCrs, x, y);
    }

    private static Point getOrCreate(Map<Resource, Point> map,
            Resource resource) {
        Point point = map.get(resource);
        if (point == null) {
            point = new Point();
            map.put(resource, point);
        }
        return point;
    }

    private static boolean isValid(Point point) {
        return point.hasType && point.coord != null && point.coordType != null;
    }

}
