
def test_export_and_disk_reload():
    """
    Test storage component for import and export functionality.

    Scenario:
    Create pipeline with two templates, text holder and files to RDF.

    Create another pipeline that executed the first pipeline.

    Export all data using "Archive, use labels as file name."
    As a result the file names in archive should be same as names of the
    pipelines.

    Now we need to stop and clear the instance - alternatively we can
    just manually delete everything.

    Now we need to copy the pipelines into the storage directory from
    the archive. Next step is to reload the instance using management
    API endpoint at .../api/v1/management/reload .

    We update the first pipeline title and make sure that it is saved
    to the same file as imported. In other words no pipeline copy
    is created.

    As the last step we execute the second pipeline and check that both
    pipelines got executed.

    Required for:
    - SK-NKOD
        Test a way how we can deploy pipelines from shared repository.
        https://github.com/datova-kancelaria/
    """
    raise NotImplementedError()
