/**
 * Converts a Designer-authored pipeline (components referencing templates by
 * IRI, unresolved connections) into a fully resolved pipeline that executor
 * runs directly - templates expanded, configuration merged, execution order
 * and data unit groups computed.
 */
package com.linkedpipes.etl.unpacker;
