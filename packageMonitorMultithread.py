import psycopg2
import subprocess
import os
import shutil
import requests
import psutil
import json
import time
import logging
import sys
from concurrent.futures import ThreadPoolExecutor
from threading import Lock
github_api_url = "https://api.github.com/search/repositories"
output_directory = "/home/robin489/vulnRecreation/dependentPackages"
github_access_token = ""
logging.basicConfig(filename='inital_link_processing.log', level=logging.INFO, format='%(asctime)s - %(message)s', datefmt='%Y-%m-%d %H:%M:%S')
if not os.path.exists(output_directory):
    os.makedirs(output_directory)
failure_count = 0
success_count = 0
timeout_counter = 0
counter_lock = Lock()

def process_row(row):
    global timeout_counter
    global success_count
    global failure_count
    try:
        logging.info(f"Cloning {row}")
        clone_url = f"https://{github_access_token}@{row[0].split('//')[1]}"
        subprocess.run(["git","clone", clone_url])
        logging.info(f"Cloned {row}")
    
        # Extracting the repository name
        repo_name = row[0].split("/")[-1].split(".")[0]
        path_exists = os.path.exists(repo_name)
        if not path_exists:
            repo_name = row[0].split("/")[-1]
            
        # Set environment variable MAVEN_OPTS
        os.environ["MAVEN_OPTS"] = "-javaagent:/home/robin489/vulnRecreation/PackagePermissionsManager/target/PackagePermissionsManager-1.0-SNAPSHOT-perm-agent.jar=m10," + repo_name
            
        logging.info(f"Running maven test on {repo_name}")
        # Running the test suite using mvn as root
        process = subprocess.check_output(["sudo", "-E", "mvn","test","-Dmaven.test.failure.ignore=true"], cwd=repo_name, stderr=subprocess.PIPE, text=True, timeout=600)
            
        
        success_count+= 1
        success_msg = f"Succesfully wrote {repo_name}\n"
        logging.info(success_msg)
        package_name = row[0].split("/")[-1].split(".")[0]
        params = {
        "q": f"\"{package_name}\" in:dependency",
        "sort": "stars",
        "order": "desc",
        "per_page": 10
        }
        headers = {
        "Authorization": f"token {github_access_token}"
    }
        response = requests.get(github_api_url, params=params,headers=headers)

        if response.status_code == 200:
            logging.info("Received response from Github")
            result = response.json()
            dependent_packages = [item['html_url'] for item in result["items"] if package_name not in item['html_url']]
            dep_size = len(dependent_packages)
            logging.info(f"Before removal dep_size: {dep_size}")
            
            
            file_path = os.path.join(output_directory, f"{repo_name}depends")
            logging.info(f"Writing dependency information to {repo_name}depends")
            with open(file_path, 'w') as f:
                json.dump(dependent_packages, f, indent=4)
        else:
            print(f"Failed to retrieve data from GitHub API for {package_name}")
            logging.error(f"Failed to retrieve data from GitHub API for {package_name}")
    except subprocess.TimeoutExpired:
        timeout_counter += 1
        logging.info(f"Timeout occured while running 'mvn test' in {repo_name}")
    except subprocess.CalledProcessError:
        failure_count += 1
        logging.info(f"{repo_name} had an error while running mvn test")
    except Exception as error:
        logging.info(f"Error processing {row[0]}: {error}")
        exc_type, exc_obj, tb = sys.exc_info()
        f = tb.tb_frame
        lineno = tb.tb_lineno
        logging.error(f"Exceotion line number : {lineno}")
        
    
    log_file.flush()
    shutil.rmtree(repo_name, ignore_errors=True)
    subprocess.run(["find", "/tmp/surefire-root", "-mindepth", "1", "-delete"])
# Establish a connection to the database
try:
    connection = psycopg2.connect(
        user="postgres",
        password="postgres",
        host="localhost",
        port="5432",
        database="packages_production"
    )

    cursor = connection.cursor()
    logging.info("Starting db query")
    # Execute the query and fetch the first 1000 results
    query = """SELECT repository_url
FROM (
    SELECT DISTINCT repository_url, dependent_packages_count
    FROM packages
    WHERE ecosystem LIKE 'maven' AND repository_url LIKE '%github%' AND repository_url NOT LIKE '%logging-log4j2%'
) AS subquery
ORDER BY dependent_packages_count DESC
LIMIT 2482;"""

    cursor.execute(query)

    # Fetch all the results
    rows = cursor.fetchall()
    log_file = open("mvn_test_errors.log", "w")
    rows = list(set(rows))
    
    with ThreadPoolExecutor(max_workers=5) as executor:
        executor.map(process_row, rows)
        
        # Cloning each GitHub repository
        
            
    logging.info(f"Number of successes: {success_count}\n")
    logging.info(f"Number of failures: {failure_count}\n")
except (Exception, psycopg2.Error) as error:
    #print(sys.exec_traceback.tb_lineno)
    print("Error while connecting to PostgreSQL", error)
    logging.error(f"Exception thrown {error}\n")
    #logging.error(sys.exec_traceback)

finally:
    # Close the connection
    if connection:
        cursor.close()
        connection.close()
        print("PostgreSQL connection is closed")
        log_file.write("Exiting")
