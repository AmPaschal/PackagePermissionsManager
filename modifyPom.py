import xml.etree.ElementTree as ET
import re


def modifyPom(repo_name, dir_name):
    # Parse the pom.xml file
    tree = ET.parse(f'{dir_name}/pom.xml')
    root = tree.getroot()
    
    # Define the namespace
   # namespace = {'mvn': 'http://maven.apache.org/POM/4.0.0'}
    
    # Check if the Surefire plugin is already present
    surefire_plugin_present = False
    for plugin in root.findall('.//{http://maven.apache.org/POM/4.0.0}plugin'):
        artifact_id = plugin.find('.//{http://maven.apache.org/POM/4.0.0}plugin')
        if artifact_id is not None and artifact_id.text == 'maven-surefire-plugin':
            surefire_plugin_present = True
            # Check if the configuration block exists
            configuration = plugin.find('.//{http://maven.apache.org/POM/4.0.0}plugin')
            if configuration is None:
                configuration = ET.SubElement(plugin, 'configuration')
            # Add or update additionalClassPathElements and argLine
            additional_classpath_elements = configuration.find('.//{http://maven.apache.org/POM/4.0.0}plugin')
            if additional_classpath_elements is None:
                additional_classpath_elements = ET.SubElement(configuration, 'additionalClassPathElements')
            additional_classpath_element = ET.SubElement(additional_classpath_elements, 'additionalClassPathElement')
            additional_classpath_element.text = '/home/robin489/vulnRecreation/PackagePermissionsManager/target/PackagePermissionsManager-1.0-SNAPSHOT-shaded.jar'  # Replace with your additional element path
    
            arg_line = configuration.find('.//{http://maven.apache.org/POM/4.0.0}plugin')
            if arg_line is None:
                arg_line = ET.SubElement(configuration, 'argLine')
            arg_line.text = f'-javaagent:/home/robin489/vulnRecreation/PackagePermissionsManager/target/PackagePermissionsManager-1.0-SNAPSHOT-perm-agent.jar=m10,{repo_name}'  # Replace with your desired argLine
            break
    
    # Write the changes back to the pom.xml file
    tree.write(f'{dir_name}/pom.xml', encoding='UTF-8', xml_declaration=True)
    with open('pom.xml', 'r') as file:
        file_content = file.read()

    # Remove 'ns0:' from the content
    updated_content = re.sub(r'ns0:', '', file_content)

# Write the updated content back to the file
    with open('pom.xml', 'w') as file:
        file.write(updated_content)

modifyPom("unassigned/test", "./junit4")