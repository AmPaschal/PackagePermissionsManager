import json
import os
import sys

# Define the function to minimize the paths
def minimize_paths(path_list, num_common_prefixes):
    if not path_list:
        return []

    common_prefix = os.path.commonprefix(path_list)
    prefix_parts = common_prefix.strip('/').split('/')
    if len(prefix_parts) < num_common_prefixes:
        return path_list

    wildcard_path = '/'.join(prefix_parts[:num_common_prefixes]) + '/*'
    return [wildcard_path]

def process_json_file(json_file_path, num_common_prefixes):
    # Load the JSON data
    with open(json_file_path, 'r') as file:
        data = json.load(file)

    # Iterate through each app
    for app_name, app_data in data.items():
        if 'allowedPaths' in app_data:
            allowed_paths = app_data['allowedPaths']

            # Group paths based on common prefixes
            common_prefixes = {}
            for path in allowed_paths:
                prefix = '/'.join(path.strip('/').split('/')[:num_common_prefixes])
                if prefix in common_prefixes:
                    common_prefixes[prefix].append(path)
                else:
                    common_prefixes[prefix] = [path]

            # Minimize each subset of paths with at least 'num_common_prefixes' common prefixes
            minimized_paths = []
            for prefix, paths in common_prefixes.items():
                if len(paths) > 1:
                    minimized_paths.extend(minimize_paths(paths, num_common_prefixes))
                else:
                    minimized_paths.extend(paths)

            app_data['allowedPaths'] = minimized_paths
    new_file_path = json_file_path.split(".json")[0] + "min.json"

    # Save the modified JSON data back to the file
    with open(new_file_path, 'w') as file:
        json.dump(data, file, indent=2)

    print(f"Modified JSON data saved to {new_file_path}")

if len(sys.argv) != 3:
    print("Usage: python script.py directory_path num_common_prefixes")
    sys.exit(1)

directory_path = sys.argv[1]
num_common_prefixes = int(sys.argv[2])

# Process each JSON file in the directory
for file in os.listdir(directory_path):
    if file.endswith('.json'):
        json_file_path = os.path.join(directory_path, file)
        process_json_file(json_file_path, num_common_prefixes)
