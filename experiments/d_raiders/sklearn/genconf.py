ds = [1, 2, 4, 8, 16, 32, 64, 128, 256]
for i, d in enumerate(ds):
    output = """
import run_kde

run_kde.run_benchmark(
    df_path="bigdata/pmnist.csv",
    n=70000,
    numScore=3000,
    tol=0.01,
    cols=list(range({d})),
    bwMult=3.0,
    use_std=True,
    denorm=True,
)

""".format(
        d=d,
    )
    with open("./mnist_{d}.py".format(d=d), 'w') as f:
        f.write(output)