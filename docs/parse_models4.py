import re
import sys

with open("docs/API _ Impact CO₂.html", "r", encoding="utf-8") as f:
    text = f.read()

# Extract paths:
paths = re.findall(r'data-path="([^"]+)"', text)
print("PATHS:")
print(set(paths))

# Search for examples:
print("\nJSON Examples:")
examples = re.findall(r'<div class="highlight-code"><pre class="example.*?><code class="language-json">(.*?)</code>', text, re.DOTALL)
for e in examples[:10]:
    print("---")
    res = re.sub(r'<[^>]+>', '', e).strip()
    print(res[:300])


