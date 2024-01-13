import re
import json
root_dir = "./applicationDependencies/"
def filterControl(control_keys, files):
    filtered_files = []
    for file in files:
        with open(f"{root_dir}{file}", "r") as orig:
            orig_data = json.load(orig)
        filtered_orig_data = {key: value for key, value in orig_data.items() if key not in control_keys}
        filtered_file_name = file.split(".json")[0] + "filtered.json"
        filtered_files.append(filtered_file_name)
        with open(f"{root_dir}{filtered_file_name}", "w") as filtered:
            json.dump(filtered_orig_data, filtered, indent=4)
    return(filtered_files)
            
def parseAppFile(file):
    repo_name =  file.split("/")[0]
    output = []
    with open(f"{root_dir}{file}") as f:
        file_data = json.load(f)
        for key, value in file_data.items():
            hasFs = 0
            hasNet = 0
            hasExec = 0
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
            output.append(f"{repo_name}:{key},{hasFs},{hasNet},{hasExec}")
    return(output)
            
                 
            



control_keys = []
with open("controlKeys.txt", "r") as f:
    control_keys = [line.strip() for line in f.readlines()]
    
app_files = []
with open(f"{root_dir}appFiles.txt") as f:
    app_files = [line.strip() for line in f.readlines()]
    
filtered_files = filterControl(control_keys, app_files)

full_output = []
for file in filtered_files:
    full_output.append(parseAppFile(file))

with open(f"{root_dir}appPermSummary.txt", "w") as f:
    for line in full_output:
        f.write(f"{line}\n")
