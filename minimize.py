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

if len(sys.argv) != 4:
    print("Usage: python script.py input_json output_json num_common_prefixes")
    sys.exit(1)

input_json_file = sys.argv[1]
output_json_file = sys.argv[2]
num_common_prefixes = int(sys.argv[3])

# Load the JSON data
with open(input_json_file, 'r') as json_file:
    data = json.load(json_file)

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

# Save the modified JSON data
with open(output_json_file, 'w') as json_file:
    json.dump(data, json_file, indent=2)

print(f"Modified JSON data saved to {output_json_file}")
