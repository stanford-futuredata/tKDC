
import run_kde

run_kde.run_benchmark(
    df_path="bigdata/bgauss.csv",
    n=10000,
    numScore=10000,
    tol=0.1,
    cols=list(range(2)),
    use_std=True,
)
