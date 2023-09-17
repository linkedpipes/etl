import json

import linkedpipes


def test_private_configuration():
    """
    Import a pipeline with private configuration and templates.
    The private configuration should be saved, but not exported.

    Test converts pipeline to string and check that there are some secretes.
    Next pipeline is exported with private configuration removed.
    The same check is carried out again.

    This test does not remove data from the instance.

    Required for:
    - *
    """
    pipelines, templates = linkedpipes.import_pipeline(
        "./data/pipeline-001.trig", keep_suffix=False)
    assert len(pipelines) == 1

    pipeline_iri = pipelines[0]["local"]
    pipeline = linkedpipes.get_pipeline(
        pipeline_iri, templates=False)
    pipeline_remove_private = linkedpipes.get_pipeline(
        pipeline_iri, templates=False, remove_private_config=True)

    # Remove pipelines and templates.
    for item in pipelines:
        linkedpipes.delete_pipeline(item["local"])
    for item in templates:
        linkedpipes.delete_template(item["local"])

    # We test after removing data to keep tje instance clear,
    # in case of failures.

    # There should be secrets in full export.
    assert "secret" in json.dumps(pipeline), \
        "There should be secrets in the export."

    # There should NOT be secrets in export with private configuration.
    lines = json.dumps(pipeline_remove_private, indent=1).split("\n")
    for index in range(0,len(lines)):
        if "secret" in lines[index]:
            for print_index in range(max(0, index - 3), index + 1):
                if print_index == index:
                    print(">>", lines[index])
                else:
                    print(lines[print_index])

    assert "secret" not in json.dumps(pipeline_remove_private), \
        "Some private configuration was not removed"
