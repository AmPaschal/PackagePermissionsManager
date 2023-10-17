import subprocess
import psycopg2
import os
import json
import shutil
import sys
import logging
import re
from functools import partial
from concurrent.futures import ThreadPoolExecutor
github_access_token = ""
parent_dir = "./applicationDependencies"
input_file = f"{parent_dir}/focusApps.txt"
logging.basicConfig(filename='dependencyTester.log', level=logging.INFO, format='%(asctime)s - %(message)s', datefmt='%Y-%m-%d %H:%M:%S')
timeout_counter = 0
success_counter = 0
failure_counter = 0
start_num = 0
end_num = 10
succesful_runs = []
failed_runs = []
timeout_runs = []
test_count = []
def process_output_string(input_string):
    tests_run_line = re.findall(r'Tests run: (\d+)', input_string)
    if tests_run_line:
        tests_run_count = sum(map(int, tests_run_line))
        return f"{tests_run_count}"
    else:
        return "0"
def process_github_link(app,link):
    global timeout_counter
    global success_counter
    global failure_counter
    global succesful_runs
    global failed_runs
    global timeout_runs
    global test_count
    skip = False
    try:
        link = link.strip()
        repo_name = link.split("/")[-1].split(".")[0]
        
        dir_path = f"{parent_dir}/{app}/packagePerms"
        if not skip:
            logging.info(f"Cloning into {link}")
            clone_url = f"https://{github_access_token}@{link.split('//')[1]}"
            subprocess.run(["git", "clone", clone_url])
            logging.info(f"Finsished cloning {link}")
            # Extracting the repository name
            direct_file_name = dir_path + f"/{repo_name}Direct.json"
            indirect_file_name = dir_path + "/{repo_name}Transitive.json"
            if os.path.exists(direct_file_name):
                try:
                    os.remove(direct_file_name)
                    os.remove(indirect_file_name)
                    logging.info(f"Json at {direct_file_name} and {indirect_file_name} has been removed")
                except OSError as e:
                    logging.error(f"There was an error removing the file: {e}")
            
    
            # Set environment variable MAVEN_OPTS
            os.environ["MAVEN_OPTS"] = f"-javaagent:/home/robin489/vulnRecreation/PackagePermissionsManager/target/PackagePermissionsManager-1.0-SNAPSHOT-perm-agent.jar=m10,{app}/packagePerms/{repo_name}"
            logging.info(f"Repo name is {repo_name} and directory is {dir_path}")
            logging.info(f"Running maven test for {repo_name}")
            # Running the test suite using mvn as root with environment variables preserved
            process = subprocess.check_output(["sudo", "-E", "mvn", "test", "-Dmaven.test.failure.ignore=true"], cwd=repo_name, stderr=subprocess.STDOUT, text=True, timeout=600)
    
            success_counter += 1
            succesful_runs.append(repo_name)
            logging.info(f"Successfully processed {link}")
            output = process_output_string(process)
            logging.info(f"Number of maven tests: {output}")
            test_count.append((repo_name, output))

        # Deleting the cloned repository
        
    except subprocess.TimeoutExpired:
        timeout_counter += 1
        timeout_runs.append(repo_name)
        error_msg = f" Timeout error occurred while running 'mvn test' in {repo_name}:\n"
        #error_msg += process.stdout + process.stderr + "\n\n"
        direct_file_name = dir_path + f"/{repo_name}Direct.json"
        indirect_file_name = dir_path + f"/{repo_name}Transitive.json"
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
        direct_file_name = dir_path + f"/{repo_name}Direct.json"
        indirect_file_name = dir_path + f"/{repo_name}Transitive.json"
        failure_counter+= 1
        failed_runs.append(repo_name)
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
        else:
            logging.info(f"Finsihed {repo_name} but processing was skipped")
            
try:
    connection = psycopg2.connect(
        user="postgres",
        password="postgres",
        host="localhost",
        port="5432",
        database="packages_production"
    )
    
    cursor = connection.cursor()            
    with open(input_file, 'r') as inputFile:
        focusApps = inputFile.read().splitlines()
        for app in focusApps:
            logging.info(f"Current App: {app}")
            cur_dir = f"{parent_dir}/{app}"
            dependency_file = f"{cur_dir}/{app}dependList.txt"
            if not os.path.exists(f"{cur_dir}/packagePerms"):
                os.makedirs(f"{cur_dir}/packagePerms")
                
            partial_func = partial(process_github_link,app)
            github_urls = []
            with open(dependency_file, 'r') as depFile:
                dependencies = depFile.read().splitlines()
                for dep in dependencies:
                    query = f"""SELECT repository_url
                            FROM packages
                            WHERE name like '{dep}' AND repository_url LIKE '%github%' """
                    cursor.execute(query)
                    data = cursor.fetchall()
                    link = None
                    if data:
                        link = data[0][0]
                    if not link:
                        logging.info(f"{dep} has no results in db")
                    else:
                        logging.info(f"Found {link} from lookup of {dep}")
                        github_urls.append(link)
                github_urls = list(set(github_urls))
                with ThreadPoolExecutor() as executor:
                    executor.map(partial_func, github_urls)
    logging.info(f"Successes: {success_counter} Failures: {failure_counter} Timeouts: {timeout_counter}")
    logging.info(f"Succesful Runs: {succesful_runs}")
    logging.info(f"Failed Runs: {failed_runs}")
    logging.info(f"Timeout Runs: {timeout_runs}") 
    logging.info(f"Test Count: {test_count}")    

                
                    

except psycopg2.Error as e:
    logging.info(f"Error while connecting to DB: {e}")
    

