import psycopg2
import subprocess
import os

# Establish a connection to the database
try:
    connection = psycopg2.connect(
        user="postgres",
        password="postgres",
        host="localhost",
        port="5432",
        database="packages_production"
    )

    cursor = connection.cursor()

    # Execute the query and fetch the first 1000 results
    query = """SELECT repository_url FROM packages 
               WHERE ecosystem LIKE 'maven' 
               AND repository_url LIKE '%github%' 
               ORDER BY dependent_packages_count DESC 
               LIMIT 100"""

    cursor.execute(query)

    # Fetch all the results
    rows = cursor.fetchall()

    for row in rows:
        # Cloning each GitHub repository
        subprocess.run(["git", "clone", row[0]])

        # Extracting the repository name
        repo_name = row[0].split("/")[-1].split(".")[0]

        # Set environment variable MAVEN_OPTS
        os.environ["MAVEN_OPTS"] = "-javaagent:/home/robin489/vulnRecreation/PackagePermissionsManager/target/PackagePermissionsManager-1.0-SNAPSHOT-perm-agent.jar=m10," + repo_name
        

        # Running the test suite using mvn as root
        subprocess.run(["sudo", "mvn", "test"], cwd=repo_name)

except (Exception, psycopg2.Error) as error:
    print("Error while connecting to PostgreSQL", error)

finally:
    # Close the connection
    if connection:
        cursor.close()
        connection.close()
        print("PostgreSQL connection is closed")
