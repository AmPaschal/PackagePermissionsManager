import os
import json
import sys

parent_dir = "./applicationDependencies"
def get_control_keys(controlFiles):
    control_keys = []
    for file in controlFiles:
        with open(f"{parent_dir}/{file}", 'r') as f:
            control_data = json.load(f)
            control_key = set(control_data.keys())
            control_keys.append(control_key)
    return control_keys




controlFiles = []
with open(f"{parent_dir}/allControlFiles.txt", 'r') as f:
    controlFiles = [line.strip() for line in f.readlines()]
    
key_set = get_control_keys(controlFiles)

with open("controlKeys.txt", "w") as of:
    for key in key_set:
        of.write(f"{key}\n")
    