import numpy as np
import sklearn
import scipy.stats
import matplotlib
matplotlib.use("PDF")
import matplotlib.pyplot as plt
import timeit
import pandas as pd
import math
import itertools
import time



from sklearn.neighbors import (
    KernelDensity,
    KDTree,
)
from sklearn.preprocessing import (
    RobustScaler
)

df_combined = pd.read_csv("../../data/room2.csv")
df_occ = df_combined[df_combined.Occupancy == 1]
df_un = df_combined[df_combined.Occupancy == 0]
dfo_sorted = df_occ.sort_values(by=['date'])
np.random.seed(0)
df_mixed = pd.concat([df_un,df_occ.sample(200, random_state=0)])
# df_mixed = pd.concat([df_un,dfo_sorted[:200]])
# df_mixed["Light"] = df_mixed["Light"] + np.random.rand(len(df_mixed))
# mixed_data = df_mixed[["Temperature", "Humidity", "CO2", "Light", "HumidityRatio"]].values
mixed_data = df_mixed[["Temperature", "CO2"]].values
np.random.shuffle(mixed_data)
def estimate_kde_bw(data):
    q3 = np.percentile(data, 75, axis=0)
    q1 = np.percentile(data, 25, axis=0)
    iqr = q3 - q1
    bw = iqr * (data.shape[0])**(-1.0/(data.shape[1]+4))
    return bw
bw = estimate_kde_bw(mixed_data)
norm_data = mixed_data / bw

def gen_gmm():
    import sklearn.mixture

    np.random.seed(0)
    gmm = sklearn.mixture.GaussianMixture(
        n_components=10,
        covariance_type="full",
        random_state=0,
    )
    gmm.fit(mixed_data)
    x = np.arange(19,25,.1)
    y = np.arange(500,2100,10)
    X,Y = np.meshgrid(x,y,indexing="ij")
    Z = np.zeros((len(x),len(y)))
    for i in range(len(x)):
        for j in range(len(y)):
            Z[i,j]=gmm.score_samples([[X[i,j],Y[i,j]]])[0]
    plt.figure()
    plt.contour(X,Y,Z,levels=np.arange(-15,-5,.5))
    plt.colorbar()
    plt.title("GMM, N=10")
    plt.xlabel("Temperature")
    plt.ylabel("CO2")
    plt.savefig("GMMContour.pdf")

def gen_kde_small():
    tol = .1
    kde = KernelDensity(
        bandwidth=.25,
        kernel='gaussian',
        algorithm='kd_tree',
        rtol=tol,
    )
    np.random.seed(0)
    sdata = mixed_data[:1000]
    bw = estimate_kde_bw(sdata)
    norm_data = sdata / bw
    kde.fit(norm_data)

    x = np.arange(19,25,.1)
    y = np.arange(500,2100,10)
    X,Y = np.meshgrid(x,y,indexing="ij")
    Z = np.zeros((len(x),len(y)))
    for i in range(len(x)):
        for j in range(len(y)):
            Z[i,j]=kde.score_samples([[X[i,j],Y[i,j]]] / bw)[0]
    plt.figure()
    plt.contourf(X,Y,Z,levels=np.arange(-30,1,.5))
    plt.colorbar()
    plt.title("Kernel Density Estimators, N=1000")
    plt.xlabel("Temperature")
    plt.ylabel("CO2")
    plt.savefig("KDESmall.pdf")

gen_kde_small()