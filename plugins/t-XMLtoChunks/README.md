# About 
The component is intended to split large XML files. 
Files which can't be processed in memory.

# Usage
In the configuration you can specify:
 * The approximate size per chunk
 * All nodes which shouldn't be split - for cases where splitting 
 whithin the node leads to data losses. 
 Splitting the node LOUFile between two files, will lead to data inconsistency. 
 ```xml
<LOUFiles xmlns="http://ns.c-lei.org/leidata/1">
        <LOUFile>
          <DownloadDate>2016-02-21T04:00:20-06:00</DownloadDate>
          <DownloadURL>https://www.ceireg.de/download/?download.dumptype=full&amp;download.dumpdate:2=2016&amp;download.dumpdate:1=02&amp;download.dumpdate:0=21</DownloadURL>
          <ContentDate>2016-02-21T11:00:10.885+01:00</ContentDate>
          <Originator>39120001KULK7200U106</Originator>
          <RecordCount>1313</RecordCount>
          <ManagingLOUCount managingLOU="39120001KULK7200U106">1313</ManagingLOUCount>
          <SchemaValid>true</SchemaValid>
        </LOUFile>
</LOUFiles>
```
In order to avoid this we can specify in the configuration panel:
 - NamespaceURI = http://ns.c-lei.org/leidata/1
 - Local name = LOUFile