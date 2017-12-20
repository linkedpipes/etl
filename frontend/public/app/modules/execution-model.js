(function () {
    "use-strict";

    function loadModel(data) {
        modelLoader.loadModelFromJsonLd(
            this.data, data, this.jsonldService.jsonld());
    }

    function getComponents() {
        return this.data.components;
    }

    function getComponentStatus(component) {
        return component.status;
    }

    function enableMapping(component) {
        switch (component.mapping) {
            case MAPPING_STATUS.FINISHED:
                component.mapping = MAPPING_STATUS.FINISHED_MAPPED;
                break
        }
    }

    function disableMapping(component) {
        switch (component.mapping) {
            case MAPPING_STATUS.FINISHED_MAPPED:
                component.mapping = MAPPING_STATUS.FINISHED;
                break
        }
    }

    /**
     * If true mapping based on the status is used otherwise
     * a "disable" mapping should be used.
     */
    function isMappingEnabled(component) {
        switch (component.mapping) {
            case MAPPING_STATUS.FINISHED_MAPPED:
            case MAPPING_STATUS.FAILED:
                return true;
            default:
                return false;
        }
    }

    /**
     * If component has not changed, mapping is available and is
     * enabled, then it's used in the execution.
     */
    function isUsedForExecution(component) {
        return component.mapping === MAPPING_STATUS.FINISHED_MAPPED;
    }

    /**
     * Report change on the component and thus disable mapping.
     */
    function onChange(component) {
        switch (component.mapping) {
            case MAPPING_STATUS.FINISHED_MAPPED:
            case MAPPING_STATUS.FINISHED:
                component.mapping = MAPPING_STATUS.CHANGED;
                break;
        }
    }

    /**
     * If not changed mapping can be enabled / disabled.
     */
    function isChanged(component) {
        return component.mapping === MAPPING_STATUS.CHANGED;
    }

    /**
     * If if mapping for the component can be enabled.
     * It might be enabled now, that does not matter.
     */
    function canEnableMapping(component) {
        switch (component.mapping) {
            case MAPPING_STATUS.FINISHED_MAPPED:
            case MAPPING_STATUS.FINISHED:
                return true;
            default:
                return false;
        }
    }

    /**
     * True if mapping can be changed. If. if button changing the mapping
     * should be visible.
     */
    function canChangeMapping(component) {
        switch (component.mapping) {
            case MAPPING_STATUS.FINISHED_MAPPED:
            case MAPPING_STATUS.FINISHED:
                return true;
            default:
                return false;
        }
    }

    function isExecutionFinished() {
        if (this.data.execution.status.running === undefined) {
            return false;
        }
        return !this.data.execution.status.running;
    }

    function hasExecutionWorkingData() {
        if (this.data.execution.deleteWorkingData === true) {
            return false;
        } else {
            return true;
        }
    }

    function getDataUnit(component, bindingName) {
        for (let index in component.dataUnits) {
            const dataUnit = this.data.dataUnits[component.dataUnits[index]];
            if (dataUnit !== undefined && dataUnit.binding === bindingName) {
                return dataUnit;
            }
        }
    }

    function getExecutionIri() {
        return this.data.execution.iri;
    }

    function createModel(jsonldService) {
        return {
            "data": {
                "pipeline": {},
                "components": {},
                "dataUnits": {},
                "metadata": {
                    "hasWorkingData": void 0
                },
                "execution": {
                    "status": {},
                    "iri": void 0
                }
            },
            "jsonldService": jsonldService,
            //
            "loadJsonLd": loadModel,
            "getComponents": getComponents,
            "getComponentStatus": getComponentStatus,
            "mapping": {
                "enable": enableMapping,
                "disable": disableMapping,
                "isEnabled": isMappingEnabled,
                "isUsedForExecution": isUsedForExecution,
                "onChange": onChange,
                "isChanged": isChanged,
                "canEnableMapping": canEnableMapping,
                "canChangeMapping": canChangeMapping
            },
            "isFinished": isExecutionFinished,
            "hasWorkingData": hasExecutionWorkingData,
            "getDataUnit": getDataUnit,
            "getIri": getExecutionIri
        }
    }

    let LP;
    let SKOS;
    let jsonld;
    let modelLoader;
    let MAPPING_STATUS;

    const module = {
        "create": createModel
    };

    if (typeof define === "function" && define.amd) {
        define([
            "vocabulary",
            "jsonld",
            "app/modules/execution-model-loader",
            "app/modules/mapping"
        ], (vocabulary, _jsonld, _loader, _mapping) => {
            LP = vocabulary.LP;
            SKOS = vocabulary.SKOS;
            jsonld = _jsonld;
            modelLoader = _loader;
            MAPPING_STATUS = _mapping.MAPPING_STATUS;
            return module;
        });
    }

})();
