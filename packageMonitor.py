import psycopg2
import subprocess
import os
import shutil
import requests
import json
import sys
from concurrent.futures import ThreadPoolExecutor
from threading import Lock
github_api_url = "https://api.github.com/search/repositories"
output_directory = "/home/robin489/vulnRecreation/dependentPackages"
if not os.path.exists(output_directory):
    os.makedirs(output_directory)
failure_count = 0
success_count = 0
counter_lock = Lock()

def process_row(row):
    global success_count
    global failure_count
    subprocess.run(["git", "clone", row[0]])

    # Extracting the repository name
    repo_name = row[0].split("/")[-1].split(".")[0]
    path_exists = os.path.exists(repo_name)
    if not path_exists:
        repo_name = row[0].split("/")[-1]
        
    # Set environment variable MAVEN_OPTS
    os.environ["MAVEN_OPTS"] = "-javaagent:/home/robin489/vulnRecreation/PackagePermissionsManager/target/PackagePermissionsManager-1.0-SNAPSHOT-perm-agent.jar=m10," + repo_name
        

    # Running the test suite using mvn as root
    process = subprocess.run(["sudo", "-E", "mvn", "test", "-Dmaven.test.failure.ignore=true"], cwd=repo_name, stderr=subprocess.PIPE, text=True)
        
    if process.returncode != 0:
        error_msg = f"Error occurred while running 'mvn test' in {repo_name}:\n"
        error_msg += process.stderr + "\n\n"
        outputFilePath = "/home/robin489/vulnRecreation/jsons/" + repo_name + "*"
        failFolder = "/home/robin489/vulnRecreation/jsons/failures"
        #subprocess.run(["mv", outputFilePath, failFolder ])
        print(error_msg)
        log_file.write(error_msg)
        failure_count += 1
    else:
        success_count+= 1
        success_msg = f"Succesfully wrote {repo_name}\n"
        log_file.write(success_msg)
        package_name = row[0].split("/")[-1].split(".")[0]
        params = {
        "q": f"\"{package_name}\" in:dependency",
        "sort": "stars",
        "order": "desc",
        "per_page": 10
        }
        response = requests.get(github_api_url, params=params)

        if response.status_code == 200:
            result = response.json()
            dependent_packages = [item['html_url'] for item in result["items"]]
            file_path = os.path.join(output_directory, f"{repo_name}depends")
            with open(file_path, 'w') as f:
                json.dump(dependent_packages, f, indent=4)
        else:
            print(f"Failed to retrieve data from GitHub API for {package_name}")
            log_file.write(f"Failed to retrieve data from GitHub API for {package_name}")
    
    log_file.flush()
    shutil.rmtree(repo_name, ignore_errors=True)
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

    # Execute the query and fetch the first 1000 results
    query = """SELECT repository_url
FROM (
    SELECT DISTINCT repository_url, dependent_packages_count
    FROM packages
    WHERE ecosystem LIKE 'maven' AND repository_url LIKE '%github%'
) AS subquery
ORDER BY dependent_packages_count DESC
LIMIT 100;"""

    cursor.execute(query)

    # Fetch all the results
    rows = cursor.fetchall()
    log_file = open("mvn_test_errors.log", "w")
    rows = list(set(rows))
    
    with ThreadPoolExecutor(max_workers=5)
        
        # Cloning each GitHub repository
        
            
    log_file.write(f"Number of successes: {success_count}\n")
    log_file.write(f"Number of failures: {failure_count}\n")
except (Exception, psycopg2.Error) as error:
    print(sys.exec_traceback.tb_lineno)
    print("Error while connecting to PostgreSQL", error)
    log_file.write("Exception thrown\n")
    log_file.write(sys.exec_traceback)

finally:
    # Close the connection
    if connection:
        cursor.close()
        connection.close()
        print("PostgreSQL connection is closed")
        log_file.write("Exiting")
