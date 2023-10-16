import psycopg2
import subprocess
import os
import shutil
import requests
import psutil
import json
import time
import re
import logging
import sys
from concurrent.futures import ThreadPoolExecutor
from threading import Lock
#GITHUB_API_URL = "https://api.github.com/repos/{}/dependent-repositories?per_page=10"
GITHUB_CONTENTS_API_URL = "https://api.github.com/repos/{}/contents"
github_api_url = "https://api.github.com/search/repositories"
def has_pom_file(repo_url, access_token):
    print("Checking for pom file")
    repo_name = repo_url[0].split("/")[-2] + "/" + repo_url[0].split("/")[-1]
    headers = {"Authorization": f"token {access_token}"}
    url = GITHUB_CONTENTS_API_URL.format(repo_name)
    response = requests.get(url, headers=headers)

    if response.status_code == 200:
        print("Github response received for pom file")
        files = [file['name'] for file in response.json()]
        return 'pom.xml' in files
    else:
        print("Github did not issue a normal response for dependent repos")
        print(f"Error fetching dependent repositories for url:{repo_url} and repo_name:{repo_name}. Status code: {response.status_code}")
        return False
def get_dependent_repositories(repo_url, access_token, min_stars):
    print(f"Getting dependent repositories for {repo_url}")
    repo_name = repo_url[0].split("/")[-2] + "/" + repo_url[0].split("/")[-1]

    params = {
    "q": f"\"{repo_name}\" in:dependency",
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
        dependent_packages = [item['html_url'] for item in result["items"] if repo_name not in item['html_url']]
        return dependent_packages
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
    #The 2482 number is due to duplicates in the db the resulting set is 1000 results
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
    github_urls = list(set(rows))
    github_access_token = ""
    dependent_repositories_union = []
    min_stars = 1000
    print("Finished db query")

    for url in github_urls:
        dependent_repositories = get_dependent_repositories(url, github_access_token, min_stars)
    
        if dependent_repositories:
            dependent_repositories_union.extend(dependent_repositories[:10])
    output_file_path = 'resulting_list.txt'
    with open(output_file_path, 'w') as file:
        for repo in dependent_repositories_union:
            file.write(repo + '\n')

    print(f"Result written to {output_file_path}")

    
    
except psycopg2.Error as e:
    print(f"Error with db service: {e}")