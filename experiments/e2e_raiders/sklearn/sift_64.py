import run_kde

run_kde.run_benchmark(
    df_path="bigdata/sift.csv",
    n=11164866,
    numScore=60,
    tol=0.1,
    cols=list(range(64)),
    use_std=True,
    denorm=True
)

