import pandas as pd
import numpy as np
import os
import json


def process_lines(lines):
    output_idx = -1
    radius = None
    percentile = None
    for i,line in enumerate(lines):
        if "Parsed Output" in line or "Final Output" in line:
            output_idx = i+1
        if "Radius:" in line:
            radius_loc = line.rindex(": ")
            radius = float(line[radius_loc+2:])
        if "percentile:" in line:
            p_loc = line.rindex(": ")
            percentile = float(line[p_loc + 2:])
    outstr = lines[output_idx].strip().replace("'",'"')
    vals = json.loads(outstr)
    vals["radius"] = radius
    vals["percentile"] = percentile
    return vals


rows = []
for root,dirs,files in os.walk("."):
    if "out" in root:
        for cur_out in files:
            f1, f2 = os.path.splitext(cur_out)
            if f2 == ".txt" or f2 == ".out":
                cur_path = os.path.join(root,cur_out)
                print(cur_path)
                with open(cur_path) as f:
                    res = process_lines(f.readlines())
                    res["out_path"] = cur_path
                    rows.append(res)

df = pd.DataFrame(rows)
df.to_csv("scale_r.csv", index=False)
