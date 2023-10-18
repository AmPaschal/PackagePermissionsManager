import xml.etree.ElementTree as ET

def add_surefire_plugin_to_pom(pom_file_path, additional_class_path_element, arg_line):
    # Parse the XML
    tree = ET.parse(pom_file_path)
    root = tree.getroot()

    # Check if maven-surefire-plugin already exists
    surefire_plugin = root.find(".//{http://maven.apache.org/POM/4.0.0}build//{http://maven.apache.org/POM/4.0.0}plugins//{http://maven.apache.org/POM/4.0.0}plugin//{http://maven.apache.org/POM/4.0.0}artifactId/[text()='maven-surefire-plugin']")

    # If the plugin already exists, modify its configuration
    if surefire_plugin is not None:
        configuration = surefire_plugin.find("./../{http://maven.apache.org/POM/4.0.0}configuration")

        additional_class_path_element_tag = ET.SubElement(configuration, "additionalClassPathElements")
        additional_class_path_element_tag.text = additional_class_path_element

        arg_line_tag = ET.SubElement(configuration, "argLine")
        arg_line_tag.text = arg_line

    # If the plugin doesn't exist, create a new plugin with the required configuration
    else:
        build = root.find("{http://maven.apache.org/POM/4.0.0}build")
        if build is None:
            build = ET.SubElement(root, "build")

        plugins = build.find("{http://maven.apache.org/POM/4.0.0}plugins")
        if plugins is None:
            plugins = ET.SubElement(build, "plugins")

        plugin = ET.SubElement(plugins, "plugin")
        ET.SubElement(plugin, "groupId").text = "org.apache.maven.plugins"
        ET.SubElement(plugin, "artifactId").text = "maven-surefire-plugin"
        ET.SubElement(plugin, "version").text = "3.0.0"

        configuration = ET.SubElement(plugin, "configuration")

        additional_class_path_element_tag = ET.SubElement(configuration, "additionalClassPathElements")
        additional_class_path_element_tag.text = additional_class_path_element

        arg_line_tag = ET.SubElement(configuration, "argLine")
        arg_line_tag.text = arg_line

    # Write back to the pom.xml file
    tree.write(pom_file_path)

# Example usage
repo_name = "test"
pom_file_path = './junit4/pom.xml'
additional_class_path_element = '/home/robin498/vulnRecreation/PackagePermissionsManager/target/PackagePermissionsManager-1.0-SNAPSHOT-shaded.jar'
arg_line = '-javaagent:/home/robin489/vulnRecreation/PackagePermissionsManager/target/PackagePermissionsManager-1.0-SNAPSHOT-perm-agent.jar'

add_surefire_plugin_to_pom(pom_file_path, additional_class_path_element, arg_line)
