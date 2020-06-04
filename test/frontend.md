# Frontend Tests

## Setup
 * Frontend is running
 * Storage is running
 * Executor monitor is running
 * Executor is running
 
## Test definitions

### 01
User can see pipelines list at ```http://localhost:8080/#/pipelines```.

### 02
User can see execution list at ```http://localhost:8080/#/executions```.

### 03
User can see template list at ```http://localhost:8080/#/templates```.

### 04
User can see personalization page at 
```http://localhost:8080/#/personalization```.

### 05
User can see about the page with the latest commit identification at
```http://localhost:8080/#/help```

### 06
User can open import dialog from ```http://localhost:8080/#/pipelines```.
Next user can import pipeline from 
```test/pipelines/Test pipeline 001.jsonld```.
User can see pipeline with 3 connected components. The pipeline
has "Test" tag assigned to it visible from pipeline detail dialog and 
pipeline list at ```http://localhost:8080/#/pipelines```.

### 07
User can filter pipeline(s) at ```http://localhost:8080/#/pipelines```.
Using tag ```Test``` which should be suggested and string ```pipeline```,
in both cases the user should see pipeline imported in test 06.
If user type ```002``` no pipeline should be visible. 

### 08
User opens pipeline from *06* and open the ```SPARQL update``` component.
User can see SPARQL query in the SPARQL editor, user can type
```; foaf:``` into the query, before fod, and gets suggestions.
User changes the query, click on ```Save changes``` and reopen the dialog.
There should be the updated query.

 