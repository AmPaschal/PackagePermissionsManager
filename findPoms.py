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
    pom_response = requests.get(repo_url + "/contents/pom.xml", headers=headers)
    remaining_requests = pom_response.headers.get('X-RateLimit-Remaining')
    limit_reset = pom_response.headersget('X-ratelimit-reset')
    if remaining_requests:
        print(f"Remaining requests: {remaining_requests}")
    else:
        print("No remaining requests")
    if limit_reset:
        print(f"Rate limit reset: {limit_reset}")
    else:
        print("Rate limit reset not provided")
    print(f"Github Response code {pom_response.status_code}")
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
    return pom_response.status_code == 200


github_access_token = ""
with open('repository_urls.txt', 'r') as file:
    repository_urls = file.read().splitlines()
   
valid_repository_urls = []
for url in repository_urls:
    pom_exists = has_pom_file(url,github_access_token)
    if pom_exists:
        valid_repository_urls.append(url)
        
with open('valid_repository_urls.txt', 'w') as file:
    file.write('\n'.join(valid_repository_urls))

