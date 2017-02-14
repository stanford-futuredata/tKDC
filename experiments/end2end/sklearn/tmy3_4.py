import run_kde

run_kde.run_benchmark(
    df_path="bigdata/tmy3.csv",
    n=2242560,
    numScore=10,
    tol=0.1,
    cols=list(range(4)),
    use_std=True,
)

