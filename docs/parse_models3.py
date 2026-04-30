import re

with open("docs/API _ Impact CO₂.html", "r", encoding="utf-8") as f:
    text = f.read()

# search for "ECV" in the html
idx = text.find("Équivalents CO2")
if idx == -1: idx = text.find("ECV")

print(text[max(0, idx-500) : idx+2000])


