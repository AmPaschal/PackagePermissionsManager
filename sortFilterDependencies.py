# File containing the list of repositories and their dependencies
input_file = "./applicationDependencies/dependency_list.txt"

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

print(f"Sorted and filtered dependencies have been saved to {output_file}.")
