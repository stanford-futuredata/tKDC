
import run_kde

run_kde.run_benchmark(
    df_path="bigdata/pmnist.csv",
    n=70000,
    numScore=3000,
    tol=0.01,
    cols=list(range(16)),
    bwMult=3.0,
    use_std=True,
    denorm=True,
)

