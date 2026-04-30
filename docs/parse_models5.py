import re

with open("docs/API _ Impact CO₂.html", "r", encoding="utf-8") as f:
    text = f.read()

import json
items = re.findall(r'"example":\s*({[^}]+\})', text)
for i in items[:10]:
    print("Example:", i[:200])

codes = re.findall(r'<span class="token string">"([^"]+)"</span><span class="token operator">:</span>\s*<span class="token (number|string|boolean|punctuation)">([^<]+)</span>', text)
print("\nTokens count:", len(codes))
# print(codes[:50])

print("\nFull text extraction of examples via soup")
from html.parser import HTMLParser
class MyParser(HTMLParser):
    def __init__(self):
        super().__init__()
        self.in_example = False
        self.buf = ""
    def handle_starttag(self, tag, attrs):
        if tag == "code" and dict(attrs).get("class") == "language-json":
            self.in_example = True
    def handle_data(self, data):
        if self.in_example:
            self.buf += data
    def handle_endtag(self, tag):
        if tag == "code" and self.in_example:
            self.in_example = False
            self.buf += "\n===---===\n"

p = MyParser()
p.feed(text)
print(p.buf[:1000])


