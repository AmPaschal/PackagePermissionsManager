import os
import json
import sys

parent_dir = "./applicationDependencies"
def get_control_keys(controlFiles):
    control_keys = []
    for file in controlFiles:
        with open(f"{parent_dir}/{file}", 'r') as f:
            control_data = json.load(f)
            for key in control_data.keys():
                control_keys.append(key)
    return control_keys

def filterControl(control_keys, files):
    for file in files:
        with open(f"../fullRun/{file}", "r") as orig:
            orig_data = json.load(orig)
        filtered_orig_data = {key: value for key, value in orig_data.items() if key not in control_keys}
        with open(f"../fullRun/filtered{file}", "w") as filtered:
            json.dump(filtered_orig_data, filtered, indent=4)

controlFiles = []
with open(f"{parent_dir}/allControlFiles.txt", 'r') as f:
    controlFiles = [line.strip() for line in f.readlines()]
    
keys = get_control_keys(controlFiles)
key_set = list(set(keys))
with open("controlKeys.txt", "w") as of:
    for key in key_set:
        of.write(f"{key}\n")


goodRepos = []
with open(f"../allFiles.txt", "r") as f:
    goodRepos = [line.strip() for line in f.readlines()]
    
goodFileNames = []
for repo in goodRepos:
    goodFileNames.append(f"{repo}Direct.json")
    
filterControl(key_set, goodFileNames)
