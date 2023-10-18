import xml.etree.ElementTree as ET

def modify_pom_xml(oldFile, newFile, repoName):
    try:
        # Parse the XML file
        tree = ET.parse(oldFile)
        root = tree.getroot()

        # Locate the <plugins> element
        plugins = root.find('.//plugins')

        if plugins is None:
            raise Exception("No root-level <plugins> tag found.")

        surefire_plugin = None

        # Search for maven-surefire-plugin
        for plugin in plugins.findall('plugin'):
            artifact_id = plugin.find('artifactId')
            if artifact_id is not None and artifact_id.text == 'maven-surefire-plugin':
                surefire_plugin = plugin
                break

        if surefire_plugin is None:
            # If plugin not found, add it
            surefire_plugin = ET.fromstring(f'''<plugin>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.1.2</version>
                <configuration>
                    <additionalClassPathElement>/home/robin489/vulnRecreation/PackagePermissionsManager/target/PackagePermissisionsManager-1.0-shaded.jar</additionalClassPathElement>
                    <argLine>-javaagent:/home/robin489/vulnRecreation/PackagePermissionsManager/target/PackagePermissionsManager-1.0-SNAPSHOT-perm-agent.jar=m10,{repoName}</argLine>
                </configuration>
                <dependencies>
            </plugin>''')
            plugins.append(surefire_plugin)
        else:
            # If plugin found, update or append configuration
            configuration = surefire_plugin.find('configuration')
            
            if configuration is not None and configuration.find('argLine') is not None:
                raise Exception("argLine node already present.")
            
            if configuration is None:
                configuration = ET.Element('configuration')
                surefire_plugin.append(configuration)

            additionalClassPathElement = configuration.find('additionalClassPathElement')
            if additionalClassPathElement is None:
                additionalClassPathElement = ET.Element('additionalClassPathElement')
                configuration.append(additionalClassPathElement)
            additionalClassPathElement.text = '/home/robin489/vulnRecreation/PackagePermissionsManager/target/PackagePermissisionsManager-1.0-shaded.jar'
            
            argLine = ET.Element('argLine')
            argLine.text = f'-javaagent:/home/robin489/vulnRecreation/PackagePermissionsManager/target/PackagePermissionsManager-1.0-SNAPSHOT-perm-agent.jar=m10,{repoName}'
            configuration.append(argLine)

        # Save the changes to newFile
        tree.write(newFile)

    except Exception as e:
        print(f"An error occurred: {e}")

# Example usage
modify_pom_xml('./junit4/pom.xml', './junit4/testpom.xml')