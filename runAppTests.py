import subprocess
import os
import json
import shutil
import sys
import logging
import re
import modifyPom
from concurrent.futures import ThreadPoolExecutor
github_access_token = ""
logging.basicConfig(filename='applicationDependency.log', level=logging.INFO, format='%(asctime)s - %(message)s', datefmt='%Y-%m-%d %H:%M:%S')
timeout_counter = 0
success_counter = 0
failure_counter = 0
input_file = "./applicationDependencies/shuffledApps.txt"
start_num = 0
end_num = 1
succesful_runs = []
failed_runs = []
timeout_runs = []
control_failure = []
test_count = []
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
    global succesful_runs
    global failed_runs
    global control_failure
    global timeout_runs
    skip = False
    success_occured = False
    try:
        link = link.strip()
        repo_name = link.split("/")[-1].split(".")[0]
        
        dir_path = f"./applicationDependencies/{repo_name}"
        if not skip:
            logging.info(f"Cloning into {link}")
            clone_url = f"https://{github_access_token}@{link.split('//')[1]}"
            subprocess.run(["git", "clone", clone_url])
            logging.info(f"Finsished cloning {link}")
            # Extracting the repository name
            direct_file_name = dir_path + "/Direct.json"
            indirect_file_name = dir_path + "/Transitive.json"
            if os.path.exists(direct_file_name):
                try:
                    os.remove(direct_file_name)
                    os.remove(indirect_file_name)
                    logging.info(f"Json at {direct_file_name} and {indirect_file_name} has been removed")
                except OSError as e:
                    logging.error(f"There was an error removing the file: {e}")
            
    
            # Set environment variable MAVEN_OPTS
            
            modifyPom.modify_pom_xml(f'{dir_path}/{dir_path}/pom.xml',f'{dir_path}/{dir_path}/pom.xml' , f'{repo_name}/')
            logging.info(f"Repo name is {repo_name} and directory is {dir_path}")
            # Running the test suite using mvn as root with environment variables preserved
            process = subprocess.check_output(["sudo", "-E", "mvn", "test", "-Dmaven.test.failure.ignore=true"], cwd=repo_name, stderr=subprocess.STDOUT, text=True, timeout=600)
    
            success_counter += 1
            succesful_runs.append(repo_name)
            success_occured = True
            logging.info(f"Successfully processed {link}")
            output = process_output_string(process)
            logging.info(f"Number of maven tests: {output}")
            test_count.append((repo_name, output))
            
           
        # Deleting the cloned repositoryron["
        
    except subprocess.TimeoutExpired:
        timeout_counter += 1
        timeout_runs.append(repo_name)
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
        failed_runs.append(repo_name)
        if success_occured:
            logging.error(f"An error occured running the control test for {repo_name}")
            control_failure.append(repo_name)
            failure_counter -= 1
            failed_runs.remove(failed_runs[len(failed_runs) - 1])
            control_path_direct = dir_path + "/ControlDirect.json"
            control_path_indirect = dir_path + "/ControlTransitive.json"
            if os.path.exists(control_path_direct):
                try:
                    os.remove(control_path_direct)
                    os.remove(control_path_indirect)
                except OSError as e:
                    logging.error(f"There was an error removing the file: {e}")
        if os.path.exists(direct_file_name) and not success_occured:
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
        else:
            logging.info(f"Finsihed {repo_name} but processing was skipped")
        
with open(input_file, 'r') as f:
    application_urls = f.readlines()
    logging.info(f"Using scrambled file lines {start_num} to {end_num}")
    focus_urls = application_urls[start_num:end_num]
    with ThreadPoolExecutor() as executor:
        executor.map(process_github_link, focus_urls)
                    
logging.info(f"Successes: {success_counter} Failures: {failure_counter} Timeouts: {timeout_counter}")
logging.info(f"Succesful Runs: {succesful_runs}")
logging.info(f"Failed Runs: {failed_runs}")
logging.info(f"Timeout Runs: {timeout_runs}") 
logging.info(f"Test Count: {test_count}")
logging.info(f"Control failures: {control_failure}")    

