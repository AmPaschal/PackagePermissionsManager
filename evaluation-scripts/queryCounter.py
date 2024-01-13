import psycopg2

connection = psycopg2.connect(
        user="postgres",
        password="postgres",
        host="localhost",
        port="5432",
        database="packages_production"
    )
cursor = connection.cursor()

    # Execute the query and fetch the first 1000 results
query = """SELECT repository_url
FROM (
    SELECT DISTINCT repository_url, dependent_packages_count
    FROM packages
    WHERE ecosystem LIKE 'maven' AND repository_url LIKE '%github%'
) AS subquery
ORDER BY dependent_packages_count DESC
LIMIT 1000;"""

cursor.execute(query)

    # Fetch all the results

rows = cursor.fetchall()
row_size = len(rows)
log_file = open("mvn_test_errors.log", "w")
rows = list(set(rows))
set_size = len(rows)

print(f"Row size:{row_size}, Set size:{set_size}")