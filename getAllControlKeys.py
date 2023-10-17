import os
import json
import sys

parent_dir = "./applicationDependencies"

def parsePermFile(file):
    hasFs = 0
    hasNet = 0
    hasExec = 0
    print(f"{file}")
    repo_name = file.split('filtered')[1].split('Direct')[0]
    
    with open(f"../fullRunJson/{file}", "r") as f:
        file_data = json.load(f)
        if repo_name == "antlr4":
            print(f"File data: {file_data}")
        for key, value in file_data.items():
                if repo_name == "antlr4":
                    print(f"Obj: {key} : {value}")
                if 'allowed_paths' in value:
                    allowed_paths = value['allowed_paths']
                    if len(allowed_paths) > 0:
                        hasFs = 1
                if 'allowedUrls' in value:
                    allowed_urls = value['allowedUrls']
                    if len(allowed_urls) > 0:
                        hasNet = 1
                if 'allowedCommands' in value:
                    allowed_commands = value['allowedComands']
                    if len(allowed_commands) > 0:
                        hasExec = 1
    return(f"{repo_name},{hasFs},{hasNet},{hasExec}")
        
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
        with open(f"../fullRunJson/{file}", "r") as orig:
            orig_data = json.load(orig)
        filtered_orig_data = {key: value for key, value in orig_data.items() if key not in control_keys}
        with open(f"../fullRunJson/filtered{file}", "w") as filtered:
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

filteredFiles = []
for file in os.listdir("../fullRunJson"):
    if "filtered" in file:
        filteredFiles.append(file)
outputs = []        
for file in filteredFiles:
    outputs.append(parsePermFile(file))

with open("permissionSummary.txt", "w") as f:
    for output in outputs:
        f.write(f"{output}\n")


