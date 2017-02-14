import run_kde

run_kde.run_benchmark(
    df_path="bigdata/gauss_2.csv",
    n=1000000,
    numScore=10000,
    tol=0.1,
    cols=list(range(2)),
    use_std=True,
)

