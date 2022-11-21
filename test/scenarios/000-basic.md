# Test Scenario : 000-Basic
This scenario test basic usage on fresh instance.

## Setup
* Clean LinkedPipes installation.
* All components up and running.

## Steps : Pipeline and Component
* Open pipeline list, it should be empty.
* Open execution list, it should be empty.
* Open component list, it should be empty.
* Create new pipeline, change pipeline name and description.
  Save pipeline.
* Navigate to pipeline list, there should be the pipeline.
* Open the pipeline.
* Add _Text Holder_ and connect it with _Files to Rdf_.
* Save pipeline, close pipeline, open pipeline.
* Create template from the _Text Holder_ called _TriG Holder_.
  Set name to ```file.trig``` and force it.
  Save the template.
* Create another template from _Text Holder_ called _TriG Example_.
  Set content to ```<http://resource> a <http://Example>.```.
  Save the template.
* Open _Text holder_ configuration.
  File name should not be visible, content should be inherited from parent.
  Parents should be _TriG Example_ and  _TriG Holder_.
* Save and close pipeline.
* Navigate to template list, there should be two templates.
* Open _TriG Holder_, check list of usage and hierarchy.
  It should be visible that template is used in the pipeline.
* Open _TriG Example_, check list of usage and hierarchy.
  It should be visible that template is used in the pipeline.
  Change content to forced and save the template.
* Open the pipeline. 
* Open _Text Holder_ , there should be no configuration to configure.
* Create pipeline copy from editor.
* Open pipeline list.
* Create another copy.
* Remove all copies.

## Steps : Pipeline Execution
* Open the pipeline and execute it.
  You should now be in execution mode.
* Wait for pipeline to finish and open debug data for all inputs and outputs.
* Toggle data invalidation for _Files to Rdf_ and execute the pipeline again.
* In the new execution only the _Files to Rdf_ should be executed.
* Again check for all debug data.
* Copy pipeline from execution view.
* Navigate to pipeline list and execute pipeline from there.
* Navigate to execution list.
* Delete the second (later) execution.
