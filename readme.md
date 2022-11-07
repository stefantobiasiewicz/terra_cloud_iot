


####Troubleshoot with generating files
After mvn scope - generate-sources all generated files are in target/generated-sources ... and after mvn clean install during scope compile -maven can't see this files. This files need to be add as sources to project using plugin: http://www.mojohaus.org/build-helper-maven-plugin/usage.html