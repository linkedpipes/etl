import typing
import time

import linkedpipes

COMPONENT = "http://linkedpipes.com/ontology/Component"
HAS_TEMPLATE = "http://linkedpipes.com/ontology/template"

def test_basic_workflow():
    """
    Basic workflow using storage, executor-monitor and executor.

    Scenario:
    First we create a pipeline.

    Next we create two template one using next other. We try to modify
    configuration to make sure that it works.

    We update the pipeline to use one of the created templates and
    check its usage statistics.

    At the end we execute the pipeline twice. First execution is just
    plain execution, second execution is using inputs. We check only
    for pipeline status.

    At the end we remove all created resources.

    Required for:
    """

    pipelines_before = linkedpipes.list_pipelines()
    jar_before, reference_before = linkedpipes.list_templates()

    pipeline_iri = create_pipeline()
    turtle_holder_iri, person_holder_iri = create_templates()
    update_template(turtle_holder_iri)
    update_configuration(turtle_holder_iri, person_holder_iri)
    update_pipeline(pipeline_iri, person_holder_iri)
    check_usage(pipeline_iri, turtle_holder_iri, person_holder_iri)
    executions = execute(pipeline_iri)

    delete_resources(
        [pipeline_iri],
        [turtle_holder_iri, person_holder_iri],
        executions)

    pipelines_after = linkedpipes.list_pipelines()
    _, reference_after = linkedpipes.list_templates()
    assert pipelines_before == pipelines_after, \
        "Failed to clean up pipelines."
    assert reference_before == reference_after, \
        "Failed to clean up pipelines."


def create_pipeline():
    first_iri, _ = linkedpipes.create_pipeline("./data/pipeline-000.trig")
    second_iri, _ = linkedpipes.create_pipeline("./data/pipeline-000.trig")
    # TODO Pipeline IRI should be determined by the server by default!
    assert first_iri == second_iri, "Pipeline IRI should not change."
    return first_iri


def create_templates():
    turtle_holder_iri, _ = linkedpipes.create_template(
        "http://etl.linkedpipes.com/resources/components/e-textHolder/0.0.0",
        "Turtle holderrr",
        configuration_file="./data/template-configuration-000.jsonld")
    assert turtle_holder_iri is not None, "Resource must not be None."
    person_holder_iri, _ = linkedpipes.create_template(
        turtle_holder_iri,
        "Person holder",
        configuration_file="./data/template-configuration-001.jsonld")
    assert turtle_holder_iri is not None, "Resource must not be None."
    return turtle_holder_iri, person_holder_iri


def update_template(turtle_holder_iri):
    """
    Update template definition. We can not replace the definition,
    like for pipelines. Instead, we can only modify some part of it.
    """
    content_before = linkedpipes.get_template(turtle_holder_iri)
    assert content_before[0]["@id"] == turtle_holder_iri, \
        f"Unexpected resource:\n{content_before}"
    assert content_before[0][linkedpipes.LP.PREF_LABEL.value][0][
               "@value"] == "Turtle holderrr"

    linkedpipes.update_template(turtle_holder_iri, "./data/template-000.jsonld")

    content_after = linkedpipes.get_template(turtle_holder_iri)
    assert content_before[0]["@id"] == turtle_holder_iri, \
        f"Unexpected resource:\n{content_after}"
    assert content_after[0][linkedpipes.LP.PREF_LABEL.value][0][
               "@value"] == "Turtle holder"


def update_configuration(turtle_holder_iri, person_holder_iri):
    # Update template configuration.

    configuration = paser_text_holder_configuration(
        linkedpipes.get_template_configuration(turtle_holder_iri))
    assert configuration["fileName"] == "000-name"

    linkedpipes.update_template_configuration(
        turtle_holder_iri, "./data/template-configuration-002.jsonld")

    configuration = paser_text_holder_configuration(
        linkedpipes.get_template_configuration(turtle_holder_iri))
    assert configuration["fileName"] == "000-name-force"

    # Check effective configurations. These are configurations after
    # inheritance is applied.

    configuration = paser_text_holder_configuration(
        linkedpipes.get_template_effective_configuration(turtle_holder_iri))
    assert configuration["fileName"] == "000-name-force", \
        f"Actual: {configuration}"
    assert configuration["fileNameControl"] == "Forced", \
        f"Actual: {configuration}"
    assert configuration["content"] == "000-content", \
        f"Actual: {configuration}"
    assert configuration["contentControl"] == "None", \
        f"Actual: {configuration}"

    configuration = paser_text_holder_configuration(
        linkedpipes.get_template_effective_configuration(person_holder_iri))
    assert configuration["fileName"] == "000-name-force", \
        f"Actual: {configuration}"
    assert configuration["fileNameControl"] == "Forced", \
        f"Actual: {configuration}"
    assert configuration["content"] == "001-content", \
        f"Actual: {configuration}"
    assert configuration["contentControl"] == "None", \
        f"Actual: {configuration}"

    # Check new configuration, this is the one used for new components.

    configuration = paser_text_holder_configuration(
        linkedpipes.get_template_new_configuration(turtle_holder_iri))
    assert configuration["fileName"] == "000-name-force", \
        f"Actual: {configuration}"
    assert configuration["fileNameControl"] == "Inherit", \
        f"Actual: {configuration}"
    assert configuration["content"] == "000-content", \
        f"Actual: {configuration}"
    assert configuration["contentControl"] == "Inherit", \
        f"Actual: {configuration}"

    configuration = paser_text_holder_configuration(
        linkedpipes.get_template_new_configuration(person_holder_iri))
    assert configuration["fileName"] == "001-name", \
        f"Actual: {configuration}"
    assert configuration["fileNameControl"] == "Inherit", \
        f"Actual: {configuration}"
    assert configuration["content"] == "001-content", \
        f"Actual: {configuration}"
    assert configuration["contentControl"] == "Inherit", \
        f"Actual: {configuration}"


def paser_text_holder_configuration(content: any):
    resource = content[0]
    prefix = "http://plugins.linkedpipes.com/ontology/e-textHolder#"
    file_name_control = resource[prefix + "fileNameControl"][0]["@id"]
    file_name_control = file_name_control[file_name_control.rfind("/") + 1:]

    content_control = resource[prefix + "contentControl"][0]["@id"]
    content_control = content_control[content_control.rfind("/") + 1:]
    return {
        "fileName": resource[prefix + "fileName"][0]["@value"],
        "fileNameControl": file_name_control,
        "content": resource[prefix + "content"][0]["@value"],
        "contentControl": content_control,
    }


def delete_resources(
        pipelines: typing.List,
        templates: typing.List,
        executions: typing.List):
    for iri in pipelines:
        linkedpipes.delete_pipeline(iri)
    for iri in templates:
        linkedpipes.delete_template(iri)
    for iri in executions:
        linkedpipes.delete_execution(iri)


def update_pipeline(pipeline_iri: str, template_iri: str):
    pipeline = linkedpipes.get_pipeline(pipeline_iri)
    next_graph = []
    for resource in pipeline[0]["@graph"]:
        if COMPONENT in resource["@type"]:
            template = resource[HAS_TEMPLATE][0]["@id"]
            if "e-textHolder" in template:
                resource = {
                    **resource,
                    HAS_TEMPLATE: {"@id": template_iri}
                }
        next_graph.append(resource)

    next_pipeline = [{
        "@graph": next_graph,
        "@id": pipeline[0]["@id"]
    }]

    linkedpipes.update_pipeline(next_pipeline)

    # Confirm changes in the pipeline. We just check there is no
    # template we have substituted.

    pipeline = linkedpipes.get_pipeline(pipeline_iri)
    for resource in pipeline[0]["@graph"]:
        if COMPONENT in resource["@type"]:
            template = resource[HAS_TEMPLATE][0]["@id"]
            if "e-httpGetFile" in template:
                assert False, "There should be no component with 'e-httpGetFile"


def check_usage(pipeline_iri, turtle_holder_iri, person_holder_iri):
    pipelines, templates = linkedpipes.get_template_usage(turtle_holder_iri)
    assert pipeline_iri in pipelines, "Missing pipeline in usage report."
    # TODO This should work.
    # assert person_holder_iri in templates, "Missing template in usage report."

    pipelines, templates = linkedpipes.get_template_usage(person_holder_iri)
    assert pipeline_iri in pipelines, "Missing pipeline in usage report."


def execute(pipeline_iri: str):
    pipeline = linkedpipes.get_pipeline(pipeline_iri, templates=True)
    execution_iri = linkedpipes.create_execution(pipeline)
    execution_with_input_iri = linkedpipes.create_execution(pipeline, [
        ("input", ("content.txt", "Content...", "text/plain")),
        ("input", ("backup.txt", "Backup...", "text/plain"))
    ])

    all_finished = False
    while not all_finished:
        all_finished = True
        executions = linkedpipes.list_executions()
        for execution in executions:
            if execution["status"] not in ["finished", "failed", "cancelled"]:
                all_finished = False
        time.sleep(3)

    executions = linkedpipes.list_executions()
    for execution in executions:
        assert execution["status"] == "finished", "Execution failed."

    return [execution_iri, execution_with_input_iri]
