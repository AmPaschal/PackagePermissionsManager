import os
import json
import sys

parent_dir = "./applicationDependencies"
def get_control_keys(controlFiles):
    control_keys = []
    for file in controlFiles:
        with open(f"{parent_dir}/{file}", 'r') as f:
            control_keys.append(json.load(f).keys())
    control_key_set = set(control_keys)
    return control_key_set




controlFiles = []
with open(f"{parent_dir}/allControlFiles.txt", 'r') as f:
    controlFiles = [line.strip() for line in f.readlines()]
    
key_set = get_control_keys(controlFiles)

with open("controlKeys.txt", "w") as of:
    for key in key_set:
        of.write(f"{key}\n")
    