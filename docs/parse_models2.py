import re

with open("docs/API _ Impact CO₂.html", "r", encoding="utf-8") as f:
    text = f.read()

# Let's find definitions of components
# Swagger usually embed the spec in a script tag or similar
matches = re.findall(r'<script id="swagger-data" type="application/json">(.*?)</script>', text, re.DOTALL)
if matches:
    print("Found swagger json!")
    open("docs/swagger.json", "w", encoding="utf-8").write(matches[0])
else:
    print("No swagger-data script. Searching for spec url...")
    print(re.findall(r'url: "(.*?)"', text))

    # Just look for '{"openapi":"3' or '{"swagger":"2'
    spec = re.search(r'(\{[\s\r\n]*"openapi".*?\})\s*</script>', text, re.DOTALL)
    if spec:
         open("docs/swagger.json", "w", encoding="utf-8").write(spec.group(1))
         print("Dumped spec!")

    else:
         print(text[:1000]) # Print beginning

