import xml.etree.ElementTree as ET

def add_or_modify_surefire_plugin_configuration(pom_file_path, additional_class_path_element, arg_line):
    """Adds or modifies the Surefire plugin configuration in a Maven POM.xml file.

    Args:
        pom_file_path: The path to the POM.xml file.
        additional_class_path_element: The additionalClassPathElement to add or modify.
        arg_line: The argLine to add or modify.
    """

    with open(pom_file_path, "r") as f:
        pom = ET.parse(f)

    surefire_plugin = pom.find(".//plugin[@artifactId='maven-surefire-plugin']")

    if surefire_plugin is None:
        # Add the Surefire plugin
        surefire_plugin = ET.SubElement(pom, "plugin")
        surefire_plugin.set("groupId", "org.apache.maven.plugins")
        surefire_plugin.set("artifactId", "maven-surefire-plugin")
        surefire_plugin.set("version", "3.0.1")

    # Add or modify the configuration block
    configuration = surefire_plugin.find("configuration")
    if configuration is None:
        configuration = ET.SubElement(surefire_plugin, "configuration")

    additional_class_path_element_element = configuration.find("additionalClassPathElement")
    if additional_class_path_element_element is None:
        additional_class_path_element_element = ET.SubElement(configuration, "additionalClassPathElement")

    additional_class_path_element_element.text = additional_class_path_element

    arg_line_element = configuration.find("argLine")
    if arg_line_element is None:
        arg_line_element = ET.SubElement(configuration, "argLine")

    arg_line_element.text = arg_line

    with open(pom_file_path, "w") as f:
        f.write(ET.tostring(pom, encoding="utf-8"))


if __name__ == "__main__":
    pom_file_path = "./junit4/pom.xml"
    additional_class_path_element = "/home/robin489/vulnRecreation/PackagePermissionsManager/target/PackagePermissionsManager-1.0-SNAPSHOT-shaded.jar"
    arg_line = "-javaagent:/home/robin489/vulnRecreation/PackagePermissionsManager/target/PackagePermissionsManager-1.0-SNAPSHOT-perm-agent.jar"

    add_or_modify_surefire_plugin_configuration(pom_file_path, additional_class_path_element, arg_line)