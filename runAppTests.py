import subprocess
import os
import json
import shutil
import sys
import logging
from concurrent.futures import ThreadPoolExecutor
github_access_token = ""
logging.basicConfig(filename='applicationDependency.log', level=logging.INFO, format='%(asctime)s - %(message)s', datefmt='%Y-%m-%d %H:%M:%S')
timeout_counter = 0
success_counter = 0
failure_counter = 0
input_file = "./applicationDependencies/shuffledApps.txt"
start_num = 0
end_num = 10
def process_output_string(input_string):
    tests_run_line = re.findall(r'Tests run: (\d+)', input_string)
    if tests_run_line:
        tests_run_count = sum(map(int, tests_run_line))
        return f"{tests_run_count}"
    else:
        return "0"
def process_github_link(link):
    global timeout_counter
    global success_counter
    global failure_counter
    skip = False
    try:
        repo_name = link.split("/")[-1].split(".")[0]
        
        dir_path = f"./applicationDependencies/{repo_name}"
        if not skip:
            logging.info(f"Cloning into {link}")
            clone_url = f"https://{github_access_token}@{link.split('//')[1]}"
            subprocess.run(["git", "clone", clone_url])
            logging.info(f"Finsished cloning {link}")
            # Extracting the repository name
            
            
    
            # Set environment variable MAVEN_OPTS
            os.environ["MAVEN_OPTS"] = "-javaagent:/home/robin489/vulnRecreation/PackagePermissionsManager/target/PackagePermissionsManager-1.0-SNAPSHOT-perm-agent.jar=m10," + repo_name
            logging.info(f"Repo name is {repo_name} and directory is {dir_path}")
            # Running the test suite using mvn as root with environment variables preserved
            process = subprocess.check_output(["sudo", "-E", "mvn", "test", "-Dmaven.test.failure.ignore=true"], cwd=repo_name, stderr=subprocess.STDOUT, text=True, timeout=600)
    
            success_counter += 1
            logging.info(f"Successfully processed {link}")
            output = process_output_string(process)
            logging.info(f"Number of maven tests: {output}")

        # Deleting the cloned repository
        
    except subprocess.TimeoutExpired:
        timeout_counter += 1
        error_msg = f" Timeout error occurred while running 'mvn test' in {repo_name}:\n"
        #error_msg += process.stdout + process.stderr + "\n\n"
        direct_file_name = dir_path + "/Direct.json"
        indirect_file_name = dir_path + "/Transitive.json"
        if os.path.exists(direct_file_name):
            try:
                os.remove(direct_file_name)
                os.remove(indirect_file_name)
                logging.info(f"Json at {direct_file_name} and {indirect_file_name} has been removed")
            except OSError as e:
                logging.error(f"There was an error removing the file: {e}")
        else:
            logging.error(f"The file at {direct_file_name} does not exist.")
        logging.error(error_msg)
    
    except subprocess.CalledProcessError as e:
        error_msg = f" Error occurred while running 'mvn test' in {repo_name}:\n"
        error_msg += f" Return code: {e.returncode}"
        #error_msg += process.stdout + process.stderr + "\n\n"
        direct_file_name = dir_path + "/Direct.json"
        indirect_file_name = dir_path + "/Transitive.json"
        failure_counter+= 1
        if os.path.exists(direct_file_name):
            try:
                os.remove(direct_file_name)
                os.remove(indirect_file_name)
                logging.info(f"Json at {direct_file_name} and {indirect_file_name} has been removed")
            except OSError as e:
                logging.error(f"There was an error removing the file: {e}")
        else:
            logging.error(f"The file at {direct_file_name} does not exist.")
        logging.error(error_msg)
        logging.info(f"Error output: {e.output}")
    except Exception as error:
        logging.error(f"Error processing {link}: {error}")
        exc_type, exc_obj, tb = sys.exc_info()
        lineno = tb.tb_lineno
        logging.error(f"Excecution line number : {lineno}")
    finally:
        if not skip:
            shutil.rmtree(repo_name, ignore_errors=True)
            logging.info(f"{repo_name} has been completed and directory removed")
        logging.info(f"Finsihed {repo_name} but processing was skipped")
        
with open(input_file, 'r') as f:
    application_urls = f.readlines()
    with ThreadPoolExecutor() as executor:
        for url in application_urls[start_num:end_num]:
            process_github_link(url.strip())
            
logging.info(f"Successes: {success_counter} Failures: {failure_counter} Timouts: {timeout_counter}")
        

