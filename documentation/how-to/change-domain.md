# How to : Change domain 
All the resources, pipelines, execution, and templates are identified using IRI.
As IRI contains also the domain, transferring data from one domain to another may required updating the IRIs.
This is not necessary, yet may be desirable.

The easiest way to change the domain is to modify the relevant files directly.
The files are located in storage path subdirectories, namely pipelines, components and executions.
While it is possible to migrate all three, migration of execution is a more complicated.
That is why we ignore executions.

Please remember to back up your data!

For example, we may need to change domain from ```http://example.com/``` to ```https://linkedpipes.com/```.
Located in the storage directory, we can employ following commands:
```bash
find ./pipelines/ -name "*.trig" | xargs -I {} sed -i "s_<http://example.com/resources/_<https://linkedpipes.com/resources/_g" {}"
find ./templates/ -name "*.trig" | xargs -I {} sed -i "s_<http://example.com/resources/_<https://linkedpipes.com/resources/_g" {}"
```
Keep in mind that this approach change domain using string based approach. 
There is no distinction whether the string is replaced in pipeline resource or in the configuration.
It is thus highly recommended checking the data after the above operations.

Keep in mind that LinkedPipes ETL can work with IRIs from different domains.
As a result changing the domain may not be necessary.

In addition, you may also consider exporting and importing pipelines and templates using the user interface. 
