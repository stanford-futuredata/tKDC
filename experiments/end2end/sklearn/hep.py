import run_kde

run_kde.run_benchmark(
    df_path="bigdata/hep.csv",
    # n=2242560,
    n=1000000,
    numScore=100,
    tol=0.1,
    cols=list(range(1,28)),
    use_std=True,
)

