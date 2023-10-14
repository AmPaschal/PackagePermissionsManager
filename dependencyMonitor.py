import subprocess
import os
import json
import logging
from concurrent.futures import ThreadPoolExecutor

# Directory containing the GitHub links
github_links_directory = "/home/robin489/vulnRecreation/dependentPackages"
logging.basicConfig(filename='github_link_processing.log', level=logging.INFO, format='%(asctime)s - %(message)s', datefmt='%Y-%m-%d %H:%M:%S')
# Function to process each GitHub link
def process_github_link(link,file):
    try:
        # Cloning each GitHub repository
        subprocess.run(["git", "clone", link])

        # Extracting the repository name
        repo_name = link.split("/")[-1].split(".")[0]
        path_exists = os.path.exists(repo_name)
        if not path_exists:
            repo_name = link.split("/")[-1]

        # Set environment variable MAVEN_OPTS
        os.environ["MAVEN_OPTS"] = "-javaagent:/home/robin489/vulnRecreation/PackagePermissionsManager/target/PackagePermissionsManager-1.0-SNAPSHOT-perm-agent.jar=m10," + file +"/" + repo_name

        # Running the test suite using mvn as root with environment variables preserved
        process = subprocess.run(["sudo", "-E", "mvn", "test", "-Dmaven.test.failure.ignore=true"], cwd=repo_name, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)

        if process.returncode != 0:
            error_msg = f"Error occurred while running 'mvn test' in {repo_name}:\n"
            error_msg += process.stdout + process.stderr + "\n\n"
            logging.error(error_msg)
        else:
            logging.info(f"Successfully processed {link}")

        # Deleting the cloned repository
        subprocess.run(["rm", "-rf", repo_name])

    except Exception as error:
        logging.error(f"Error processing {link}: {error}")

# Process each GitHub link in the directory
with ThreadPoolExecutor(max_workers=5) as executor:
    for file in os.listdir(github_links_directory):
        if file.endswith("depends"):
            with open(os.path.join(github_links_directory, file), 'r') as f:
                dependent_packages = json.load(f)
                for link in dependent_packages:
                    process_github_link(link,file)
