
import run_kde

run_kde.run_benchmark(
    df_path="bigdata/hep.csv",
    n=10500000,
    numScore=200,
    tol=0.1,
    cols=list(range(1,2)),
    use_std=True,
    denorm=True
)

