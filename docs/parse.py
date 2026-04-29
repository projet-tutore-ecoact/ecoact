import re

with open("docs/API _ Impact CO₂.html", "r", encoding="utf-8") as f:
    text = f.read()

# Extract paths like data-path="/themes"
paths = set(re.findall(r'data-path=\"([^\"]+)\"', text))
print("Paths:", paths)

