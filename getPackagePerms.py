# -*- coding: utf-8 -*-
"""
Created on Tue Oct 17 18:25:54 2023

@author: robin
"""
import os
import re
import json
root_dir = "./applicationDependencies"
def find_files_with_pattern(root_dir, pattern):
    matches = []
    for root, dirnames, filenames in os.walk(root_dir):
        for filename in filenames:
            if re.match(pattern, filename):
                matches.append(os.path.relpath(os.path.join(root, filename), root_dir))
    return matches

def filterControl(control_keys, files):
    filtered_files = []
    for file in files:
        with open(f"{file}", "r") as orig:
            orig_data = json.load(orig)
        filtered_orig_data = {key: value for key, value in orig_data.items() if key not in control_keys}
        filtered_file_name = file.split(".json")[0] + "filtered.json"
        filtered_files.append(filtered_file_name)
        with open(f"{filtered_file_name}", "w") as filtered:
            json.dump(filtered_orig_data, filtered, indent=4)
    return(filtered_files)

def parsePermFile(file):
    hasFs = 0
    hasNet = 0
    hasExec = 0
    print(f"{file}")
    file_segments = file.split("/")
    repo_name = file_segments[len(file_segments - 1)].split("filtered")[0]
    
    with open(f"{file}", "r") as f:
        file_data = json.load(f)
        for key, value in file_data.items():
                if 'allowedPaths' in value:
                    allowed_paths = value['allowedPaths']
                    if len(allowed_paths) > 0:
                        hasFs = 1
                if 'allowedUrls' in value:
                    allowed_urls = value['allowedUrls']
                    if len(allowed_urls) > 0:
                        hasNet = 1
                if 'allowedCommands' in value:
                    allowed_commands = value['allowedCommands']
                    if len(allowed_commands) > 0:
                        hasExec = 1
    return(f"{repo_name},{hasFs},{hasNet},{hasExec}")

file_pattern = r".+ControlDirect\.json"

control_files = []
for file in find_files_with_pattern(root_dir, file_pattern):
    control_files.append(f"{root_dir}/{file}")
    
print(f"Control files: {control_files}")
control_keys = []
with open("controlKeys.txt", "r") as f:
    control_keys = [line.strip() for line in f.readlines()]

file_pattern = r".+Direct\.json"
direct_files = find_files_with_pattern(root_dir, file_pattern)

target_files = []
for file in direct_files:
    if file not in control_files:
        target_files.append(file)

filtered_files = filterControl(control_keys, target_files)

outputs = []
for file in filtered_files:
    outputs.append(parsePermFile(file))
    
with open("permissionSummary.txt", "a") as f:
    for output in outputs:
        f.write(f"{output}\n")  


        