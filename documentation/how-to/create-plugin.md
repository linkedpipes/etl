# How to : Create a Plugin
Component should contain:
* Main class
* Class with vocabulary
* Configuration class

Class with vocabulary should be final, package private. 
IRIs used in the configuration should be stored here.
The class should have empty private constructor.

Configuration is a public, annotated class. 
Must contain public constructor.
All collections must be initialized to writable default object.
