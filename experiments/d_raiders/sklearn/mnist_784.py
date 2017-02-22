
import run_kde

run_kde.run_benchmark(
    df_path="bigdata/mnist.csv",
    n=70000,
    numScore=5000,
    tol=0.01,
    cols=list(range(1, 785)),
    bwValue=1000.0,
    denorm=True,
)

