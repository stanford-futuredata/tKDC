import numpy as np

ns = (10000 * 3**np.arange(0,9)).astype(int)
for i, n in enumerate(ns):
    output = """
import run_kde

run_kde.run_benchmark(
    df_path="bigdata/bgauss.csv",
    n={n},
    numScore={n_score},
    tol=0.1,
    cols=list(range(2)),
    use_std=True,
)
""".format(
        n=n,
        n_score=min(int(100000000.0/n * 100), n)
    )
    with open("./gauss_n{i}.py".format(i=i), 'w') as f:
        f.write(output)