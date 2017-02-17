import run_kde

run_kde.run_benchmark(
    df_path="bigdata/otmy3.csv",
    n=1822080,
    # n=1000000,
    numScore=500,
    tol=0.1,
    cols=list(range(4)),
    use_std=True,
)

