## About
[XSPARQL](https://github.com/semantalytics/xsparql) is a language combining xQuery and SPARQL. 
It could be used instead of XSLT for transformation of XML files. 

Useful link:
* [xsparql-language-specification](https://www.w3.org/Submission/xsparql-language-specification/)
* [github wiki](https://github.com/semantalytics/xsparql/wiki) 

## Plugin specifics
* Please note that the plugin doesn't currently support the db connections.
* All files added to the input of the component can be accessed within the script by the variable $input.
```
declare namespace foaf = "http://xmlns.com/foaf/0.1/";

declare option saxon:output "method=text";
declare variable $input as xs:string external;

let $check := xs:gYearMonth("2016-12")
let $en := "en"
for $x in doc($input)/LOUFile/ContentDate
construct {foaf:dfd foaf:fdsf {$x}.}
```
* Currently if a file doesn't provide an output exception will be thrown
* All XSPARQL debug info can be found in the pipeline log

## Prerequisites
* [XSPARQL](https://github.com/semantalytics/xsparql) 

## Installation
- You need to download/package the xsparql-cli-jar-with-dependencies.jar
- Then you need change the system-path in the pom file for the jar.