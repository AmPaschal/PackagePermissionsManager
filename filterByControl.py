import os
import json
import sys

def filter_control(controlFile, testFile, outputFile, directory_path):
    with open(f"{directory_path}/{controlFile}", 'r') as control_file:
        control_data = json.load(control_file)
    
    with open(f"{directory_path}/{testFile}", 'r') as depend_file:
        depend_data = json.load(depend_file)
    
    # Extracting the keys from the control_data dictionary
    control_keys = set(control_data.keys())
    
    # Filtering the depend_data dictionary based on the keys from control_data
    filtered_depend_data = {key: value for key, value in depend_data.items() if key not in control_keys}
    
    # Writing the filtered depend data back to a new JSON file
    with open(f"{directory_path}/{outputFile}", 'w') as filtered_depend_file:
        json.dump(filtered_depend_data, filtered_depend_file, indent=4)
    print("Took {testFile} filtered by {controlFile} and outputted to {outputFIle}")



def insert_control(input_string):
    return input_string.replace('Direct', 'ControlDirect').replace("Transitive","ControlTransitive")
# Replace 'your_directory_path' with the path to your directory
directory_path = sys.argv[1]


# Getting all files in the directory
files_in_directory = os.listdir(directory_path)

# Filtering out files that do not contain 'control' in their names
files_without_control = [file for file in files_in_directory if 'Control' not in file]

for file in files_without_control:
    print(f"Original filename: {file}")
    controlFile = insert_control(file)
    print(f"Changed filename: {controlFile}")
    testFile = file
    outputFile = "filtered" + file
    filter_control(controlFile, testFile, outputFile, directory_path)
    