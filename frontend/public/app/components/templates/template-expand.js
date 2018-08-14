((definition) => {
    if (typeof define === "function" && define.amd) {
        define([], definition);
    }
})(() => {

    function expandTemplates(data) {
        let templateList = [];
        const templateMap = {};
        populateWithJarTemplates(data, templateList, templateMap);
        if (Object.keys(data.refTemplate).length === 0) {
            // No reference templates.
            data.templateList = templateList;
            return;
        }

        let toExpand = {...data.refTemplate};
        let toExpandNext = {};
        let maxLineageLength = 0;
        while (true) {
            let wasTemplateExpanded = false;
            for (let iri in toExpand) {
                if (!toExpand.hasOwnProperty(iri)) {
                    continue;
                }
                // Check for parent.
                const instance = toExpand[iri];
                const parent = templateMap[instance.template];
                if (instance.template === undefined || parent === undefined) {
                    // Missing parent try to expand later.
                    toExpandNext[iri] = instance;
                    continue;
                }
                wasTemplateExpanded = true;
                initiateWithParent(instance, parent);
                templateList.push(instance);
                templateMap[iri] = instance;
                // Keep track of lineage size.
                maxLineageLength = Math.max(
                    maxLineageLength, instance._parents.length);
            }
            if (Object.keys(toExpandNext).length === 0) {
                break;
            }
            if (!wasTemplateExpanded) {
                const invalid = handleNonExpandedTemplates(
                    toExpand, templateMap);
                templateList = [...invalid, ...templateList];
                break;
            }
            toExpand = toExpandNext;
            toExpandNext = []
        }
        setChildren(templateList, maxLineageLength);
        setCoreTemplates(data);
        data.templateList = templateList;
    }

    function populateWithJarTemplates(data, templateList, templateMap) {
        for (let iri in data.jarTemplate) {
            if (!data.jarTemplate.hasOwnProperty(iri)) {
                continue;
            }
            const instance = data.jarTemplate[iri];
            instance._parents = [];
            instance._children = [];
            instance._coreReference = instance;
            templateList.push(instance);
            templateMap[iri] = instance;
        }
    }

    function initiateWithParent(instance, parent) {
        instance._parents = [...parent._parents, parent];
        instance._children = [];
    }

    function handleNonExpandedTemplates(templates, templateMap) {
        const invalid = [];
        console.error("Missing parent for templates:");
        for (let iri in templates) {
            if (!templates.hasOwnProperty(iri)) {
                continue;
            }
            const instance = templates[iri];
            console.info("  ", instance.label, " (",
                iri, ") template: ", instance.template);
            instance._parents = [];
            instance._children = [];
            instance.isInvalid = true;
            invalid.push(instance);
            templateMap[iri] = instance;
        }
        return invalid;
    }

    function setChildren(templateList, maxLineageLength) {
        // Use parent to start from the first level under core and then proceed.
        for (let depth = 1; depth <= maxLineageLength; depth++) {
            for (let index = 0; index < templateList.length; index++) {
                const template = templateList[index];
                if (template._parents.length === depth) {
                    setParentForChildren(template);
                }
            }
        }
    }

    function setParentForChildren(template) {
        for (let parent = 0; parent < template._parents.length; parent++) {
            template._parents[parent]._children.push(template);
        }
    }

    function setCoreTemplates(data) {
        // Use children property in jar templates.
        for (let iri in data.jarTemplate) {
            if (!data.jarTemplate.hasOwnProperty(iri)) {
                continue;
            }
            const instance = data.jarTemplate[iri];
            for (let i = 0; i < instance._children.length; ++i) {
                instance._children[i]._coreReference = instance;
            }
        }
    }

    return {
        "expandTemplates": expandTemplates
    };

});