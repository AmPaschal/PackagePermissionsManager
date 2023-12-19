import statistics
import random
# File containing the list of repositories and their dependencies
input_file = "./applicationDependencies/dependency_list.txt"

sampled_output_file = "./applicationDependencies/shuffledApps.txt"
# File to store the sorted and filtered results
output_file = "./applicationDependencies/sorted_filtered_dependency_list.txt"

# Function to parse the number of dependencies
def get_num_dependencies(entry):
    return int(entry.split()[-2])

# Reading the content from the file
entries = []
with open(input_file, "r") as f:
    entries = [line.strip() for line in f.readlines()]

# Sorting the entries by the number of dependencies
sorted_entries = sorted(entries, key=get_num_dependencies, reverse=True)

# Filtering out entries with less than 10 dependencies
filtered_entries = [entry for entry in sorted_entries if get_num_dependencies(entry) >= 10]

# Writing the sorted and filtered results to a new file
with open(output_file, "w") as f:
    for entry in filtered_entries:
        f.write("%s\n" % entry)

# Calculating summary statistics for the filtered list
num_entries = len(filtered_entries)
max_dependencies = max(map(get_num_dependencies, filtered_entries))
min_dependencies = min(map(get_num_dependencies, filtered_entries))
average_dependencies = sum(map(get_num_dependencies, filtered_entries)) / num_entries if num_entries > 0 else 0
median_dependencies = statistics.median(map(get_num_dependencies, filtered_entries))

# Printing summary statistics
print(f"Number of filtered entries: {num_entries}")
print(f"Maximum number of dependencies: {max_dependencies}")
print(f"Minimum number of dependencies: {min_dependencies}")
print(f"Average number of dependencies: {average_dependencies}")
print(f"Median number of dependencies: {median_dependencies}")

random.shuffle(filtered_entries)

# Writing the sampled URLs to a new file
with open(sampled_output_file, "w") as f:
    for url in filtered_entries:
        f.write("%s\n" % url.split()[0])

print(f"Sampled URLs have been saved to {sampled_output_file}.")

print(f"Sorted and filtered dependencies have been saved to {output_file}.")
