
import run_kde

run_kde.run_benchmark(
    df_path="bigdata/bgauss.csv",
    n=7290000,
    numScore=13717,
    tol=0.1,
    cols=list(range(2)),
    use_std=True,
)
