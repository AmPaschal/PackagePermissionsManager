import os
import git
import xml.etree.ElementTree as ET
import shutil
# List of GitHub URLs

parent_directory = "./applicationDependencies"
# Directory to clone repositories
clone_directory = parent_directory + "/cloned_repositories"

# Directory to store the list of dependencies
output_file = parent_directory + "/dependency_list.txt"

# Function to count the number of dependencies in pom.xml
def count_dependencies(pom_path, repo_name):
    print("Parsing POM file")
    tree = ET.parse(pom_path)
    root = tree.getroot()
    dependencies = root.findall(".//{http://maven.apache.org/POM/4.0.0}dependency")
    dependDir = f"{parent_directory}/{repo_name}"
    dependListFile = f"{dependDir}/{repo_name}dependList.txt"
    if not os.path.exists(dependDir):
        os.makedirs(dependDir)
    with open(dependListFile, "w") as f:
        for dependency in dependencies:
            artifact_id = dependency.find("{http://maven.apache.org/POM/4.0.0}artifactId").text
            f.write(f"{artifact_id}\n")
            
    print(f"Found {len(dependencies)} dependencies in {repo_name}")    
    return len(dependencies)

input_file = "valid_repository_urls.txt"
github_urls = []
if not os.path.exists(parent_directory):
    os.makedirs(parent_directory)
if os.path.exists(input_file):
    with open(input_file, "r") as f:
        github_urls = [line.strip() for line in f.readlines()]
# Cloning repositories and counting dependencies
dependency_list = []
if not os.path.exists(clone_directory):
    os.makedirs(clone_directory)

for url in github_urls:
    repo_name = url.split("/")[-1]
    repo_path = os.path.join(clone_directory, repo_name)

    if not os.path.exists(repo_path):
        print(f"Starting clone of {repo_path}")
        git.Git(clone_directory).clone(url)
        print("Clone finished")

    pom_path = os.path.join(repo_path, "pom.xml")
    if os.path.exists(pom_path):
        num_dependencies = count_dependencies(pom_path, repo_name)
        dependency_list.append(f"{url} has {num_dependencies} dependencies.")

# Writing dependencies to a file
with open(output_file, "w") as f:
    for item in dependency_list:
        f.write("%s\n" % item)
shutil.rmtree(clone_directory, ignore_errors=True)
print(f"List of dependencies has been saved to {output_file}.")

