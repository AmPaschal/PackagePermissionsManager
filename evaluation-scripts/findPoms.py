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
def has_pom_file(repo_url, access_token):
    headers = {
        "Authorization": f"Bearer {access_token}"
    }
    repo_parts = repo_url.split('/')
    owner = repo_parts[-2]
    repo_name = repo_parts[-1]

    # Construct the API URL for the repository contents
    api_url = f"https://api.github.com/repos/{owner}/{repo_name}/contents/"
    pom_response = requests.get(api_url, headers=headers)
    remaining_requests = pom_response.headers.get('X-RateLimit-Remaining')
    limit_reset = pom_response.headers.get('X-ratelimit-reset')
    if remaining_requests:
        print(f"Remaining requests: {remaining_requests}")
    else:
        print("No remaining requests")
    if limit_reset:
        print(f"Rate limit reset: {limit_reset}")
    else:
        print("Rate limit reset not provided")
    print(f"Github Response code {pom_response.status_code}")
    if  remaining_requests and int(remaining_requests) == 1:
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
    if pom_response.status_code == 200:
        repo_contents = pom_response.json()
        for content in repo_contents:
            if 'pom.xml' in content['name']:
                print("Found pom.xml")
                return True
        print("Did not find pom.xml")
        return False
    else:
        print(f"Github return with code {pom_response.status_code}")
        return False
        


github_access_token = ""

print("Testing with junit library (known to have pom")
print(has_pom_file("https://github.com/junit-team/junit4", github_access_token))
print("Testing finished")
with open('filteredResults.txt', 'r') as file:
    repository_urls = file.read().splitlines()
   
valid_repository_urls = []
for url in repository_urls:
    pom_exists = has_pom_file(url,github_access_token)
    if pom_exists:
        valid_repository_urls.append(url)
        
with open('valid_repository_urls.txt', 'w') as file:
    file.write('\n'.join(valid_repository_urls))

