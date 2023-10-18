#!/usr/bin/env python3

import xml.etree.ElementTree as ET
import sys

def serialize_element(element):
    if element.tag.startswith("{"):
        element.tag = element.tag.split("}", 1)[1]
    for child in element:
        serialize_element(child)

def modify_pom_xml(oldFile, newFile, repo_name):
    try:
        tree = ET.parse(oldFile)
        root = tree.getroot()
        namespaces = {'ns': 'http://maven.apache.org/POM/4.0.0'}

        plugins = root.find('.//ns:plugins', namespaces)

        if plugins is None:
            raise Exception("No <plugins> tag found.")

        surefire_plugin = None
        for plugin in plugins.findall('ns:plugin', namespaces):
            artifact_id = plugin.find('ns:artifactId', namespaces)
            if artifact_id is not None and artifact_id.text == 'maven-surefire-plugin':
                surefire_plugin = plugin
                break

        if surefire_plugin is None:
            surefire_plugin = ET.fromstring(f'''<plugin>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.1.2</version>
                <configuration>
                    <additionalClassPathElement>/home/robin489/vulnRecreation/PackagePermissionsManager/target/PackagePermissionsManager-1.0-SNAPSHOT-shaded.jar</additionalClassPathElement>
                    <argLne>-javaagent:/home/robin489/vulnRecreation/PackagePermissionsManager/target/PackagePermissionsManager-1.0-SNAPSHOT-perm-agent.jar=m10,{repo_name}</argLine>                </configuration>
            </plugin>''')
            plugins.append(surefire_plugin)
        else:
            configuration = surefire_plugin.find('ns:configuration', namespaces)
            if configuration is not None and configuration.find('ns:argLine', namespaces) is not None:
                raise Exception("argLine node already present.")
            if configuration is None:
                configuration = ET.Element('configuration')
                surefire_plugin.append(configuration)

            additionalClassPathElement = configuration.find('additionalClassPathElement')
            if additionalClassPathElement is None:
                additionalClassPathElement = ET.Element('additionalClassPathElement')
                configuration.append(additionalClassPathElement)
            additionalClassPathElement.text = '/home/robin489/vulnRecreation/PackagePermissionsManager/target/PackagePermissionsManager-1.0-SNAPSHOT-shaded.jar'
            additionalClassPathElement.tail = '\n'
            
            argLine = ET.Element('argLine')
            argLine.text = f'-javaagent:/home/robin489/vulnRecreation/PackagePermissionsManager/target/PackagePermissionsManager-1.0-SNAPSHOT-perm-agent.jar=m10,{repo_name}'
            argLine.tail = '\n'
            configuration.append(argLine)

        # Serialize without namespaces
        serialize_element(root)
        tree.write(newFile)

    except Exception as e:
        print(f"An error occurred: {e}")

if __name__ == "__main__":
    args = sys.argv[1:]
    
    modify_pom_xml("./junit4/pom.xml", "./junit4/testpom.xml","TEST")

