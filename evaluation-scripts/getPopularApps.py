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

    
    
def get_dependent_repositories(repo_url, github_access_token, min_stars):
    print(f"Getting dependent repositories for {repo_url}")
    repo_name = repo_url[0].split("/")[-1].split(".")[0]

    params = {
    "q": f"\"{repo_name}\" in:dependency",
    "sort": "stars",
    "order": "desc",
    "per_page": 10
    }
    headers = {
    "Authorization": f"Bearer {github_access_token}"
}
    response = requests.get(github_api_url, params=params,headers=headers)
    remaining_requests = response.headers.get('X-RateLimit-Remaining')
    retry_after = response.headers.get('Retry-After')
    limit_reset = response.headers.get('X-ratelimit-reset')
    if remaining_requests:
        print(f"Remaining requests: {remaining_requests}")
    else:
        print("No remaining requests")
    if limit_reset:
        print(f"Rate limit reset: {limit_reset}")
    else:
        print("Rate limit reset not provided")
    if response.status_code == 200:
        print("Received response from Github")
        result = response.json()
        dependent_packages = [(item['html_url'], item['stargazers_count']) for item in result["items"] if repo_name not in item['html_url']]
        if int(remaining_requests) == 1:
            current_epoch_time = int(time.time())
            time_difference = int(limit_reset) - current_epoch_time + 5
            if time_difference > 0:
                print(f"Waiting for {time_difference} seconds for limit reset")
                time.sleep(time_difference)
                print("Time target reached continuing")
            else:
                print("Target time has already passed")
        else:
            print("Request limit not reached")
        return dependent_packages
    else:
        print(f"Error fetching dependent repositories for url:{repo_url} and repo_name:{repo_name}. Status code: {response.status_code}")
        current_epoch_time = int(time.time())
        time_difference = int(limit_reset) - current_epoch_time + 5
        if time_difference > 0:
            print(f"Waiting for {time_difference} seconds for limit reset")
            time.sleep(time_difference)
            print("Time target reached continuing")
        else:
            print("Target time has already passed")
        
        return get_dependent_repositories(repo_url, github_access_token, min_stars)
    
            
            
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
    dependent_repositories.sort(key = lambda x: x[1], reverse=True)
    output_file_path = 'resulting_list.txt'
    
    with open(output_file_path, 'w') as file:
        for repo in dependent_repositories_union:
            file.write(f"{repo[0]}, {repo[1]}\n")

    print(f"Result written to {output_file_path}")

    
    
except psycopg2.Error as e:
    print(f"Error with db service: {e}")