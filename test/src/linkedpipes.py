# Contains definition of LinkedPipes:ETL API.
import typing
import enum
import json
import urllib.parse
import requests
import os

storage_url = os.getenv("LP_STORAGE", "http://localhost:8083")

executor_monitor_url = os.getenv("LP_EXECUTOR_MONITOR","http://localhost:8081")

JSON_LD = "application/ld+json"

JSON = "application/json"


class LP(enum.Enum):
    PIPELINE = "http://linkedpipes.com/ontology/Pipeline"
    JAR_TEMPLATE = "http://linkedpipes.com/ontology/JarTemplate"
    REFERENCE_TEMPLATE = "http://linkedpipes.com/ontology/Template"
    PREF_LABEL = "http://www.w3.org/2004/02/skos/core#prefLabel"
    HAS_TEMPLATE = "http://linkedpipes.com/ontology/template"
    EXECUTION = "http://etl.linkedpipes.com/ontology/Execution"
    TEMPLATE = "http://linkedpipes.com/ontology/Template"
    HAS_LOCAL_URL = "http://etl.linkedpipes.com/ontology/localResource"
    HAS_STORED = "http://etl.linkedpipes.com/ontology/stored"


def list_pipelines():
    response = _get_jsonld(_pipelines_v1("/list"))
    return _select_pipelines(response.json())


def _pipelines_v1(suffix: str) -> str:
    return storage_url + "/api/v1/pipelines" + suffix


def _get_jsonld(url: str, status_code=200) -> requests.Response:
    headers = {
        "Accept": JSON_LD
    }
    response = requests.get(url, headers=headers)
    assert response.headers['content-type'] == JSON_LD, \
        "Unexpected content type."
    assert response.status_code == status_code, \
        f"Unexpected status code: '{response.status_code}"
    return response


def _select_pipelines(content) -> typing.List:
    return [
        {
            "iri": resource["@id"]
        }
        for graph in content
        for resource in graph["@graph"]
        if LP.PIPELINE.value in resource.get("@type", [])
    ]


def get_pipeline(resource: str, templates=False, remove_private_config=False):
    url = _pipelines_v1("")
    url += f"?iri={urllib.parse.quote(resource)}"
    url += f"&templates={templates}"
    url += f"&removePrivateConfig={remove_private_config}"
    response = _get_jsonld(url)
    return response.json()


def create_pipeline(
        path: str,
        label: str = None,
        keep_suffix: bool = False,
        target_resource: str = None,
        import_pipeline: bool = True
):
    files = {
        "pipeline": (
            "pipeline.trig",
            open(path, "rb"),
            "application/trig"
        ),
        "options": (
            "options.jsonld",
            json.dumps({
                "@context": {
                    "lp": "http://linkedpipes.com/ontology/",
                    "etl": "http://etl.linkedpipes.com/ontology/",
                    "skos": "http://www.w3.org/2004/02/skos/core#"
                },
                "@type": "lp:UpdateOptions",
                "etl:pipeline": None,
                "etl:keepPipelineSuffix": keep_suffix,
                "etl:importPipeline": import_pipeline,
                "etl:targetResource": target_resource,
                "skos:prefLabel": label
            }),
            "application/ld+json",
        )
    }
    url = _pipelines_v1("")
    headers = {
        "Accept": JSON_LD
    }
    response = requests.post(url, files=files, headers=headers)
    assert response.headers['content-type'] == JSON_LD, \
        "Unexpected content type."
    assert response.status_code == 200, \
        f"Unexpected status code: '{response.status_code}"
    pipeline = response.json()
    resources = _select_pipelines(pipeline)
    assert len(resources) == 1, \
        f"Expected one pipeline got {len(resources)}."
    return resources[0]["iri"], pipeline


def _post_multipart(
        url: str, files: typing.Dict[str, any]
) -> requests.Response:
    headers = {
        "Accept": JSON_LD
    }
    response = requests.post(url, files=files, headers=headers)
    assert response.headers['content-type'] == JSON_LD, \
        "Unexpected content type."
    return response


def update_pipeline(content: any):
    files = {
        "pipeline": (
            "pipeline.trig",
            json.dumps(content),
            "application/ld+json"
        ),
    }
    url = _pipelines_v1("")
    response = requests.put(url, files=files)
    assert response.status_code == 200, \
        f"Unexpected status code '{response.status_code}'."
    return response


def delete_pipeline(resource: str):
    url = _pipelines_v1("")
    url += f"?iri={urllib.parse.quote(resource)}"
    response = requests.delete(url)
    assert response.status_code == 200, \
        f"Unexpected status code '{response.status_code}'."

def import_pipeline(
        path: str,
        import_templates: bool = True,
        update_templates: bool = True,
        keep_url: bool = False,
        keep_suffix: bool = False,
        target_resource: str = None,
        label: str = None
):
    files = {
        "content": (
            "content.trig",
            open(path, "rb"),
            "application/trig"
        ),
        "options": (
            "options.jsonld",
            json.dumps({
                "@context": {
                    "lp": "http://linkedpipes.com/ontology/",
                    "etl": "http://etl.linkedpipes.com/ontology/",
                    "skos": "http://www.w3.org/2004/02/skos/core#"
                },
                "@type": "lp:UpdateOptions",
                "etl:importNewTemplates": import_templates,
                "etl:updateExistingTemplates": update_templates,
                "etl:keepPipelineUrl": keep_url,
                "etl:keepPipelineSuffix": keep_suffix,
                "etl:targetResource": target_resource,
                "skos:prefLabel": label,
                "etl:importPipeline": True,

            }),
            "application/ld+json",
        )
    }
    url = _management_v1("/import")
    headers = {
        "Accept": JSON_LD
    }
    response = requests.post(url, files=files, headers=headers)
    assert response.headers['content-type'] == JSON_LD, \
        "Unexpected content type."
    assert response.status_code == 200, \
        f"Unexpected status code: '{response.status_code}"
    pipelines = []
    templates = []
    for resource in  response.json():
        types = resource["@type"]
        if LP.TEMPLATE.value in types:
            templates.append({
                "remote": resource["@id"],
                "local": resource[LP.HAS_LOCAL_URL.value][0]["@id"]
            })
        if LP.PIPELINE.value in types:
            pipelines.append({
                "remote": resource["@id"],
                "local": resource[LP.HAS_LOCAL_URL.value][0]["@id"]
            })
    return pipelines, templates


def _management_v1(suffix: str) -> str:
    return storage_url + "/api/v1/management" + suffix


def list_templates():
    response = _get_jsonld(_templates_v1("/list"))
    content = response.json()
    return _select_jar_templates(content), _select_reference_templates(content)


def _templates_v1(suffix: str) -> str:
    return storage_url + "/api/v1/components" + suffix


def _select_jar_templates(content: typing.List) -> typing.List:
    return [
        {
            "iri": resource["@id"]
        }
        for graph in content
        for resource in graph["@graph"]
        if LP.REFERENCE_TEMPLATE.value in resource.get("@type", [])
    ]


def _select_reference_templates(content: typing.List) -> typing.List:
    return [
        {
            "iri": resource["@id"]
        }
        for graph in content
        for resource in graph["@graph"]
        if LP.REFERENCE_TEMPLATE.value in resource.get("@type", [])
    ]


def get_template(resource: str):
    url = _templates_v1("/")
    url += f"?iri={urllib.parse.quote(resource)}"
    content = _get_jsonld(url, 200).json()
    assert len(content) == 1, "Expected only one graph."
    return content[0]["@graph"]


def get_template_configuration(resource: str):
    url = _templates_v1("/configuration")
    url += f"?iri={urllib.parse.quote(resource)}"
    content = _get_jsonld(url, 200).json()
    assert len(content) == 1, "Expected only one graph."
    return content[0]["@graph"]


def get_template_effective_configuration(resource: str):
    url = _templates_v1("/effective-configuration")
    url += f"?iri={urllib.parse.quote(resource)}"
    content = _get_jsonld(url, 200).json()
    assert len(content) == 1, "Expected only one graph."
    return content[0]["@graph"]


def get_template_new_configuration(resource: str):
    url = _templates_v1("/configuration-template")
    url += f"?iri={urllib.parse.quote(resource)}"
    content = _get_jsonld(url, 200).json()
    assert len(content) == 1, "Expected only one graph."
    return content[0]["@graph"]


def get_template_configuration_description(resource: str):
    url = _templates_v1("/configuration-description")
    url += f"?iri={urllib.parse.quote(resource)}"
    content = _get_jsonld(url, 200).json()
    assert len(content) == 1, "Expected only one graph."
    return content[0]["@graph"]


def get_template_usage(resource: str):
    url = _templates_v1("/usage")
    url += f"?iri={urllib.parse.quote(resource)}"
    content = _get_jsonld(url, 200).json()
    pipelines = []
    templates = []
    for resource in content:
        if LP.PIPELINE.value in resource["@type"]:
            pipelines.append(resource["@id"])
        if LP.REFERENCE_TEMPLATE.value in resource["@type"] and \
                not resource["@id"] == resource:
            templates.append(resource["@id"])
    return pipelines, templates


def update_template(resource: str, definition_file: any):
    files = {
        "component": (
            "configuration.jsonld",
            open(definition_file, "rb"),
            "application/ld+json",
        )
    }
    url = _templates_v1("/component")
    url += f"?iri={urllib.parse.quote(resource)}"
    response = requests.put(url, files=files)
    assert response.status_code == 200, \
        f"Unexpected status code '{response.status_code}'."


def update_template_configuration(resource: str, configuration_file: str):
    files = {
        "configuration": (
            "configuration.jsonld",
            open(configuration_file, "rb"),
            "application/ld+json",
        )
    }
    url = _templates_v1("/configuration")
    url += f"?iri={urllib.parse.quote(resource)}"
    response = requests.put(url, files=files)
    assert response.status_code == 200, \
        f"Unexpected status code '{response.status_code}'."


def create_template(
        parent: str, label: str,
        configuration: any = None, configuration_file: str = None
):
    headers = {
        "Accept": JSON_LD
    }
    files = {
        "component": (
            "component.jsonld",
            json.dumps({
                "@type": LP.REFERENCE_TEMPLATE.value,
                LP.PREF_LABEL.value: label,
                LP.HAS_TEMPLATE.value: {"@id": parent}
            }),
            "application/ld+json"
        ),
        "configuration": (
            "configuration.jsonld",
            open(configuration_file, "rb") if configuration_file is not None
            else json.dumps(configuration),
            "application/ld+json",
        )
    }
    url = _templates_v1("")
    response = requests.post(url, files=files, headers=headers)
    assert response.status_code == 200, \
        f"Unexpected status code '{response.status_code}'."
    assert response.headers.get('content-type', None) == JSON_LD, \
        f"Unexpected content type, headers:'{response.headers}'."
    content = response.json()
    return _select_reference_templates(content)[0]["iri"], content


def delete_template(resource: str):
    url = _templates_v1("")
    url += f"?iri={urllib.parse.quote(resource)}"
    response = requests.delete(url)
    assert response.status_code == 200, \
        f"Unexpected status code '{response.status_code}'."


def export_pipelines():
    ...


def import_pipelines():
    ...


def localize_pipeline():
    ...


def unpack_pipeline():
    ...


def list_executions():
    response = _get_jsonld(_execution_v1(""))
    content = response.json()
    return _select_executions(content)


def _select_executions(content) -> typing.List:
    HAS_STATUS = "http://etl.linkedpipes.com/ontology/status"

    def trim(string: str):
        return string[string.rfind("/") + 1:]

    return [
        {
            "iri": resource["@id"],
            "status": trim(resource[HAS_STATUS][0]["@id"])
        }
        for graph in content
        for resource in graph["@graph"]
        if LP.EXECUTION.value in resource.get("@type", [])
    ]


def _execution_v1(suffix: str) -> str:
    return executor_monitor_url + "/api/v1/executions" + suffix


def get_execution(resource: str):
    url = _execution_v1("/")
    url += f"?iri={urllib.parse.quote(resource)}"
    content = _get_jsonld(url, 200).json()
    assert len(content) == 1, "Expected only one graph."
    return content[0]["@graph"]


def get_overview(resource: str):
    url = _execution_v1("/overview")
    url += f"?iri={urllib.parse.quote(resource)}"
    content = _get_jsonld(url, 200).json()
    assert len(content) == 1, "Expected only one graph."
    return content[0]["@graph"]


def create_execution(pipeline: any, files: typing.List = None):
    files = [
        ("pipeline", (
            "pipeline.jsonld",
            json.dumps(pipeline),
            "application/ld+json"
        )),
        *(files if files is not None else [])
    ]
    url = _execution_v1("")
    headers = {
        "Accept": JSON_LD
    }
    response = requests.post(url, files=files, headers=headers)
    # TODO This is missing content-type!
    # assert response.headers['content-type'] == JSON, \
    #     "Unexpected content type."
    assert response.status_code == 200, \
        f"Unexpected status code: '{response.status_code}"
    execution = response.json()
    return execution["iri"]


def delete_execution(resource: str):
    url = _execution_v1("/")
    url += f"?iri={urllib.parse.quote(resource)}"
    response = requests.delete(url)
    assert response.status_code == 200, \
        f"Unexpected status code '{response.status_code}'."


def _debug_v1(suffix: str) -> str:
    return executor_monitor_url + "/api/v1/executions" + suffix
