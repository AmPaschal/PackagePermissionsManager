import os
import git
import xml.etree.ElementTree as ET
import shutil
from concurrent.futures import ThreadPoolExecutor
# List of GitHub URLs

parent_directory = "./applicationDependencies"
# Directory to clone repositories
clone_directory = parent_directory + "/cloned_repositories"

# Directory to store the list of dependencies
output_file = parent_directory + "/dependency_list.txt"

access_token = ""
# Function to count the number of dependencies in pom.xml
def count_dependencies(pom_path, repo_name):
    print("Parsing POM file for {repo_name}")
    tree = ET.parse(pom_path)
    root = tree.getroot()
    dependencies = root.findall(".//{http://maven.apache.org/POM/4.0.0}dependency")
    dependDir = f"{parent_directory}/{repo_name}"
    dependListFile = f"{dependDir}/{repo_name}dependList.txt"
    if not os.path.exists(dependDir):
        os.makedirs(dependDir)
    with open(dependListFile, "w") as f:
        for dependency in dependencies:
            try:
                artifact_id = dependency.find("{http://maven.apache.org/POM/4.0.0}artifactId").text
                f.write(f"{artifact_id}\n")
            except Exception as e:
                print(f"Exception thrown: {e}")
                print(f"Repo_name was: {repo_name}")
            
    print(f"Found {len(dependencies)} dependencies in {repo_name}")    
    return len(dependencies)
def process_url(url):
    repo_name = url.split("/")[-1]
    repo_path = os.path.join(clone_directory, repo_name)

    if not os.path.exists(repo_path):
        print(f"Starting clone of {url}")
        git_url = url.replace("https://", f"https://x-access-token:{access_token}@")
        git.Repo.clone_from(git_url, repo_path)
        print("Clone finished for {url}")

    pom_path = os.path.join(repo_path, "pom.xml")
    if os.path.exists(pom_path):
        num_dependencies = count_dependencies(pom_path, repo_name)
        shutil.rmtree(repo_path, ignore_errors=True)
        return (f"{url} has {num_dependencies} dependencies.")
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

with ThreadPoolExecutor() as executor:
    results = executor.map(process_url, github_urls)
    for result in results:
        if result:
            dependency_list.append(result)
    

# Writing dependencies to a file
with open(output_file, "w") as f:
    for item in dependency_list:
        f.write("%s\n" % item)

print(f"List of dependencies has been saved to {output_file}.")

