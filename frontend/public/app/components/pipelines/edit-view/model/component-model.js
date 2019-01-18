((definition) => {
    if (typeof define === "function" && define.amd) {
        define(["jsonld", "vocabulary"], definition);
    }
})((jsonld, vocabulary) => {

    const LP = vocabulary.LP;

    const SKOS = vocabulary.SKOS;

    const DCTERMS = vocabulary.DCTERMS;

    const service = {
        "component": {},
        "connection": {},
        "pipeline": {}
    };

    service.getIri = (component) => {
        return jsonld.r.getId(component);
    };

    service.getLabel = (component) => {
        return jsonld.r.getPlainString(component, SKOS.PREF_LABEL);
    };

    service.getDescription = (component) => {
        return jsonld.r.getPlainString(component, DCTERMS.DESCRIPTION);
    };

    service.getX = (component) => {
        return jsonld.r.getInteger(component, LP.HAS_X);
    };

    service.getY = (component) => {
        return jsonld.r.getInteger(component, LP.HAS_Y);
    };

    service.getColor = (component) => {
        return jsonld.r.getPlainString(component, LP.HAS_COLOR);
    };

    service.setColor = (component, color) => {
        if (color) {
            jsonld.r.setStrings(component, LP.HAS_COLOR, color);
        } else if (component[LP.HAS_COLOR]) {
            delete component[LP.HAS_COLOR];
        }
    };

    service.getTemplateIri = (component) => {
        return jsonld.r.getIRI(component, LP.HAS_TEMPLATE);
    };

    service.setPosition = (component, x, y) => {
        component[LP.HAS_X] = x;
        component[LP.HAS_Y] = y;
    };

    service.setTemplate = (component, iri) => {
        component[LP.HAS_TEMPLATE] = {"@id": iri};
    };

    service.setDisabled = (component, disabled) => {
        if (disabled) {
            component[LP.HAS_DISABLED] = true;
        } else {
            delete component[LP.HAS_DISABLED];
        }
    };

    service.isDisabled = (component) => {
        const disabled = jsonld.r.getBoolean(component, LP.HAS_DISABLED);
        if (disabled === undefined || disabled === false) {
            return false;
        } else {
            return true;
        }
    };

    return service;
});