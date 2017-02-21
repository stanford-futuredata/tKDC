ds = [1,2,3,4,8,16,27]
for i, d in enumerate(ds):
    output = """
import run_kde

run_kde.run_benchmark(
    df_path="bigdata/hep.csv",
    n=10500000,
    numScore=200,
    tol=0.1,
    cols=list(range(1,{d})),
    use_std=True,
    denorm=True
)

""".format(
        d=d+1,
    )
    with open("./hep_{d}.py".format(d=d), 'w') as f:
        f.write(output)