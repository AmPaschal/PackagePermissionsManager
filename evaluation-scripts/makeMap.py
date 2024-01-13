import psycopg2
import xrange
repo_name = "jhipster-registry"
try:
    connection = psycopg2.connect(
        user="postgres",
        password="postgres",
        host="localhost",
        port="5432",
        database="packages_production"
    )
    
    cursor = connection.cursor() 

    github_urls = []
    with open(f"./applicationDependencies/{repo_name}/{repo_name}dependList.txt", 'r') as depFile:
        dependencies = depFile.read().splitlines()
        for dep in dependencies:
            query = f"""SELECT repository_url
                    FROM packages
                    WHERE name like '{dep}' AND repository_url LIKE '%github%' """
            cursor.execute(query)
            data = cursor.fetchall()
            link = None
            if data:
                link = data[0][0]
            if not link:
                print(f"{dep} has no results in db")
                github_urls.append(None)
            else:
                print(f"Found {link} from lookup of {dep}")
                github_urls.append(link)
    with open(f"./applicationDependencies/{repo_name}/{repo_name}dependMap.txt", 'w') as f:
        for x in xrange(len(dependencies)):
            f.write(f"{dependencies[x]} {github_urls[x]}\n")
                
except psycopg2.Error as e:
   print(f"Error while connecting to DB: {e}")