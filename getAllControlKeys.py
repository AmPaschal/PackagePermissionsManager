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




controlFiles = []
with open(f"{parent_dir}/allControlFiles.txt", 'r') as f:
    controlFiles = [line.strip() for line in f.readlines()]
    
keys = get_control_keys(controlFiles)
key_set = list(set(keys))
with open("controlKeys.txt", "w") as of:
    for key in key_set:
        of.write(f"{key}\n")
    