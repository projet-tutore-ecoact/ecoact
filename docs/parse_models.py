import re
import json

with open("docs/API _ Impact CO₂.html", "r", encoding="utf-8") as f:
    text = f.read()

# Find all blocks of JSON in the openapi schema or descriptions
# Let's just find the JSON examples for ECV and transport
match_ecv = re.search(r'ECV.*?(?=\{)', text, re.IGNORECASE)
if match_ecv:
    print(text[match_ecv.start():match_ecv.start()+1000])
else:
    print("ECV not found")

# Better: search for common keywords like "ecv", "transport"
examples = re.findall(r'"example":\s*({[^}]+})', text)
for i, ex in enumerate(examples[:5]):
    print(f"Example {i}: {ex[:200]}")

