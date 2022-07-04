package com.linkedpipes.etl.executor.plugin.osgi;

/**
 * List of packages exported by the executor.
 */
class OsgiPackageList {

    private OsgiPackageList() {
    }

    private static final String JAVAX = ""
            + "" // javax additional - FIND BUNDLE WITH THIS !
            + "javax.servlet;version=\"2.4.0\","
            + "javax.servlet.http;version=\"2.4.0\","
            + "javax.xml.bind;version=\"2.3.0\","
            + "javax.xml.bind.util;version=\"2.3.0\","
            + "javax.xml.bind.annotation;version=\"2.3.0\","
            + "javax.xml.bind.annotation.adapters;version=\"2.3.0\","
            + "javax.annotation;version=\"1.3.2\"";

    private static final String SLF4J = ""
            + "org.slf4j;version=\"1.7.21\","
            + "org.slf4j.helpers;version=\"1.7.21\","
            + "org.slf4j.spi;version=\"1.7.21\"";

    private static final String LOGBACK = ""
            + "ch.qos.logback.classic;version=\"1.1.7\","
            + "ch.qos.logback.classic.joran;version=\"1.1.7\","
            + "ch.qos.logback.core;version=\"1.1.7\","
            + "ch.qos.logback.core.joran.action;version=\"1.1.7\","
            + "ch.qos.logback.core.joran.spi;version=\"1.1.7\","
            + "ch.qos.logback.core.rolling;version=\"1.1.7\","
            + "ch.qos.logback.core.util;version=\"1.1.7\"";

    private static final String LOG4J = ""
            + "org.apache.log4j;version=\"1.7.18\","
            + "org.apache.log4j.helpers;version=\"1.7.18\","
            + "org.apache.log4j.api;version=\"1.7.18\","
            + "org.apache.log4j.xml;version=\"1.7.18\"";

    private static final String LP_PACKAGE =
            "com.linkedpipes.etl.executor.api.v1";

    private static final String LP = ""
            + LP_PACKAGE + ";version=\"0.0.0\","
            + LP_PACKAGE + ".component;version=\"0.0.0\","
            + LP_PACKAGE + ".component.chunk;version=\"0.0.0\","
            + LP_PACKAGE + ".component.task;version=\"0.0.0\","
            + LP_PACKAGE + ".dataunit;version=\"0.0.0\","
            + LP_PACKAGE + ".event;version=\"0.0.0\","
            + LP_PACKAGE + ".rdf;version=\"0.0.0\","
            + LP_PACKAGE + ".rdf.model;version=\"0.0.0\","
            + LP_PACKAGE + ".rdf.pojo;version=\"0.0.0\","
            + LP_PACKAGE + ".service;version=\"0.0.0\","
            + LP_PACKAGE + ".report;version=\"0.0.0\","
            + LP_PACKAGE + ".vocabulary;version=\"0.0.0\","
            + "com.linkedpipes.etl.rdf.utils;version=\"0.0.0\"";

    public static final String EXPORT_PACKAGE_LIST = ""
            + JAVAX + ","
            + SLF4J + ","
            + LOGBACK + ","
            + LOG4J + ","
            + LP;

}
