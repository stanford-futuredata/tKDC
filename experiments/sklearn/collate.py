import os
outdir = "./output"
files = os.listdir(outdir)
key_strings = ["Scored in", "Trained in", "# Kernels"]

for fname in files:
    with open (outdir+"/"+fname, "r") as f:
        lines = f.readlines()
        print(fname)
        for l in lines:
            for ks in key_strings:
                if ks in l:
                    idx = l.find(ks)
                    print(l[idx:-1])
        print("---\n")
