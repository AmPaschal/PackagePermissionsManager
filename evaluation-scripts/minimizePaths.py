import json
import sys
from collections import defaultdict

def minimize_paths(paths):
    trie = defaultdict(dict)
    for path in paths:
        components = [component for component in path.split('/') if component]
        # print(components)
        current_node = trie
        for component in components:
            if component not in current_node:
                current_node[component] = {}
            current_node = current_node[component]
        # print(trie)
    def walk_trie(trie, current_path=''):
        minimized_paths = []
        for component in trie:
            children = trie[component]
            # Add remaining leaf items to minimized_paths
            # print(f"component: {component} and children: {children}")
            if not children:
                # print(f"current path: {current_path}")
                minimized_paths.append(current_path + '/' + component)
            elif len(children) > 4:
                minimized_paths.append(current_path + '/' + component)
                trie[component].clear()
            else:
                minimized_paths.extend(walk_trie(children, current_path + '/' + component))
        
        return minimized_paths
    return walk_trie(trie)



# Get json file and output file from input argument

# Example usage
if (len(sys.argv) != 3):
    print("Usage: python minimizePaths.py <input-json-file> <output-json-file>")
    sys.exit(1)
# json_file = '/home/pamusuo/research/permissions-manager/repos/PPMProfiler/next-jsm-policy.json'
# output_file = '/home/pamusuo/research/permissions-manager/repos/PPMProfiler/next-jsm-policy-mod.json'

json_file = sys.argv[1]
output_file = sys.argv[2]

# Load the JSON file
with open(json_file, 'r') as f:
    policy = json.load(f)

for package_name, package_policy in policy.items():
    if 'fs.read.allowed' in package_policy and package_policy["fs.read.allowed"]:
        package_policy['fs.read.allowed'] = minimize_paths(package_policy['fs.read.allowed'])
    if 'fs.write.allowed' in package_policy and package_policy["fs.write.allowed"]:
        package_policy['fs.write.allowed'] = minimize_paths(package_policy['fs.write.allowed'])
    
    if 'fs.read.transitive' in package_policy and package_policy["fs.read.transitive"]:
        package_policy['fs.read.transitive'] = minimize_paths(package_policy['fs.read.transitive'])
    if 'fs.write.transitive' in package_policy and package_policy["fs.write.transitive"]:
        package_policy['fs.write.transitive'] = minimize_paths(package_policy['fs.write.transitive'])

    policy[package_name].update(package_policy)



# Save the updated JSON data
with open(output_file, 'w') as f:
    json.dump(policy, f, indent=4)


# def minimize_paths(paths):
    
#     # Some inconsistencies
#     # The file has timestamp in its name - mostly a lot

#     # Group paths by their parent directory
#     parent_dirs = defaultdict(list)
#     for path in paths:
#         parent_dir = os.path.dirname(path)
#         parent_dirs[parent_dir].append(path)

#     # Minimize the paths
#     minimized_paths = []
#     minization_success = False
#     while paths:
#         path = paths.pop(0)
#         parent_dir = os.path.dirname(path)
#         if len(parent_dirs[parent_dir]) > 3:
#             minimized_paths.append(parent_dir)
#             paths = [p for p in paths if p not in parent_dirs[parent_dir]]
#             parent_dirs[parent_dir] = []
#             minization_success = True
#         else:
#             minimized_paths.append(path)

    
#     if minization_success:
#         return minimize_paths(minimized_paths)
#     else:
#         return minimized_paths

# # Example usage
# json_file = '/home/pamusuo/research/permissions-manager/repos/PPMProfiler/next-jsm-policy.json'
# output_file = '/home/pamusuo/research/permissions-manager/repos/PPMProfiler/next-jsm-policy-mod.json'

# # Load the JSON file
# with open(json_file, 'r') as f:
#     policy = json.load(f)

# for package_name, package_policy in policy.items():
#     if 'fs.read.allowed' in package_policy:
#         package_policy['fs.read.allowed'] = minimize_paths(package_policy['fs.read.allowed'])
#     if 'fs.write.allowed' in package_policy:
#         package_policy['fs.write.allowed'] = minimize_paths(package_policy['fs.write.allowed'])
    
#     if 'fs.read.direct' in package_policy:
#         package_policy['fs.read.direct'] = minimize_paths(package_policy['fs.read.direct'])
#     if 'fs.write.allowed' in package_policy:
#         package_policy['fs.write.direct'] = minimize_paths(package_policy['fs.write.direct'])

#     policy[package_name].update(package_policy)



# # Save the updated JSON data
# with open(output_file, 'w') as f:
#     json.dump(policy, f, indent=4)

