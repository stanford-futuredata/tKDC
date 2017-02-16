import run_kde

run_kde.run_benchmark(
    df_path="bigdata/homesensor.csv",
    n=928991,
    numScore=10000,
    tol=0.1,
    cols=list(range(2,12)),
    use_std=True,
)

