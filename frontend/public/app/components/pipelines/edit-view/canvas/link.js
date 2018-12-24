((definition) => {
    if (typeof define === "function" && define.amd) {
        define(["jointjs"], definition);
    }
})((joint) => {

    function createDataLink(source, sourcePort, target, targetPort, vertices) {
        return new joint.dia.Link({
            "source": {"id": source, "port": sourcePort},
            "target": {"id": target, "port": targetPort},
            "type": "link",
            "attrs": {
                ".marker-target": {
                    "d": "M 10 0 L 0 5 L 10 10 z"
                }
            },
            "vertices": vertices
        });
    }

    function createRunAfter(source, target, vertices) {
        return new joint.dia.Link({
            "source": {"id": source},
            "target": {"id": target},
            "type": "control",
            "attrs": {
                ".connection": {
                    "stroke": "blue"
                },
                ".marker-target": {
                    "fill": "yellow",
                    "d": "M 10 0 L 0 5 L 10 10 z"
                }
            },
            "vertices": vertices
        });
    }

    return {
        "createDataLink": createDataLink,
        "createRunAfter": createRunAfter
    }

});