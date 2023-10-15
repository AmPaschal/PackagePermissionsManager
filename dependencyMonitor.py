import subprocess
import os
import json
import shutil
import sys
import logging
from concurrent.futures import ThreadPoolExecutor

# Directory containing the GitHub links
github_access_token = ""
github_links_directory = "/home/robin489/vulnRecreation/dependentPackages"
logging.basicConfig(filename='github_link_processing.log', level=logging.INFO, format='%(asctime)s - %(message)s', datefmt='%Y-%m-%d %H:%M:%S')
# Function to process each GitHub link
timeout_counter = 0
def process_github_link(link,file):
    global timeout_counter
    skip = False
    try:
        repo_name = link.split("/")[-1].split(".")[0]
        path_exists = os.path.exists(repo_name)
        if not path_exists:
            repo_name = link.split("/")[-1]
        dir_path = "/home/robin489/vulnRecreation/jsons/" + file    
        if not os.path.exists(dir_path):
            try:
                os.makedirs(dir_path)
                logging.info(f"Directory created at {dir_path}")
            except OSError as e:
                logging.error(f"Failed to create directory at {dir_path}: {e}")
        else:
            logging.info(f"The directory at {dir_path} already exists")
            logging.info("Skipping rest of processing")
            skip = True
        # Cloning each GitHub repository
        if not skip:
            logging.info(f"Cloning into {link}")
            clone_url = f"https://{github_access_token}@{link.split('//')[1]}"
            subprocess.run(["git", "clone", clone_url])
            logging.info(f"Finsished cloning {link}")
            # Extracting the repository name
            
            
    
            # Set environment variable MAVEN_OPTS
            os.environ["MAVEN_OPTS"] = "-javaagent:/home/robin489/vulnRecreation/PackagePermissionsManager/target/PackagePermissionsManager-1.0-SNAPSHOT-perm-agent.jar=m10," + file +"/" + repo_name
            logging.info(f"File name is {file} and repo name is {repo_name}")
            # Running the test suite using mvn as root with environment variables preserved
            process = subprocess.check_output(["sudo", "-E", "mvn", "test", "-Dmaven.test.failure.ignore=true"], cwd=repo_name, stderr=subprocess.PIPE, text=True, timeout=600)
    
            
            logging.info(f"Successfully processed {link}")

        # Deleting the cloned repository
        
    except subprocess.TimeoutExpired:
        timeout_counter += 1
        error_msg = f" Timeout error occurred while running 'mvn test' in {repo_name}:\n"
        #error_msg += process.stdout + process.stderr + "\n\n"
        direct_file_name = dir_path + "/" + repo_name + "Direct.json"
        indirect_file_name = dir_path + "/" + repo_name + "Transitive.json"
        if os.path.exists(direct_file_name):
            try:
                os.remove(direct_file_name)
                os.remove(indirect_file_name)
                logging.info(f"Json at {direct_file_name} has been removed")
            except OSError as e:
                print(f"There was an error removing the file: {e}")
        else:
            print(f"The file at {direct_file_name} does not exist.")
        logging.error(error_msg)
    
    except subprocess.CalledProcessError as e:
        error_msg = f" Error occurred while running 'mvn test' in {repo_name}:\n"
        error_msg += f" Return code: {e.returncode}"
        #error_msg += process.stdout + process.stderr + "\n\n"
        direct_file_name = dir_path + "/" + repo_name + "Direct.json"
        indirect_file_name = dir_path + "/" + repo_name + "Transitive.json"
        if os.path.exists(direct_file_name):
            try:
                os.remove(direct_file_name)
                os.remove(indirect_file_name)
                logging.info(f"Json at {direct_file_name} has been removed")
            except OSError as e:
                print(f"There was an error removing the file: {e}")
        else:
            print(f"The file at {direct_file_name} does not exist.")
        logging.error(error_msg)
    except Exception as error:
        logging.error(f"Error processing {link}: {error}")
        exc_type, exc_obj, tb = sys.exc_info()
        lineno = tb.tb_lineno
        logging.error(f"Exceotion line number : {lineno}")
    finally:
        shutil.rmtree(repo_name, ignore_errors=True)
        logging.info(f"{repo_name} has been completed and directory removed")
        

# Process each GitHub link in the directory
for file in os.listdir(github_links_directory):
    if file.endswith("depends"):
        with open(os.path.join(github_links_directory, file), 'r') as f:
            dependent_packages = json.load(f)
            with ThreadPoolExecutor(max_workers=5) as executor:   
                 for link in dependent_packages:
                     process_github_link(link,file)
