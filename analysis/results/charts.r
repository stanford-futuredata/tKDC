library(readr)
library(ggplot2)
library(dplyr)
library(reshape2)


fformat <- function(x) {
  xa = abs(x)
  if(xa < 1000) {
    return(format(x, digits=3))
  } else if(xa >= 1000 & xa < 1000000) {
    return(paste(format(x/1000, digits=3),"K",sep=" "))
  } else {
    return(paste(format(x/1000000, digits=3),"M",sep=" "))
  }
}

df <- read_csv("throughput.csv")
df$tpretty <- applydf$throughput

algorithms <- c("ic2", "ksBin", "sklearn", "naive")
ggplot(df, aes(algorithm, throughput, fill = dataset)) +
  scale_x_discrete(limits = algorithms) +
  geom_bar(position = "dodge", stat="identity") +
  facet_wrap(~ dimension, ncol=1, labeller = label_both) +
  geom_text(
    aes(label=sapply(throughput,FUN = fformat)), 
    size=3,
    vjust=-0.25,
    position=position_dodge(width=1.0))+
  scale_fill_grey() +
  theme_bw() +
  theme(legend.position="top") +
  scale_y_continuous(limits=c(1,1e7),breaks=c(1e1,1e2,1e3,1e4,1e5,1e6,1e7), trans="log10")
ggsave("../figures/kdethroughput_big.pdf", width=4, height=8, units="in")

theme(axis.text.x=element_text(angle=-45,hjust=0),legend.position="top")

acc <- read_csv("rawacc.csv")
acc$rdim <- ceiling(acc$dim / 2) * 2
acc_f <- filter(acc, acc$alg != "sklearn_t0" & acc$alg != "ks_unbinned")

ggplot(acc_f, aes(alg, recall, fill = data)) +
  geom_bar(position="dodge", stat="identity") +
  facet_wrap(~ rdim, ncol=1, labeller = label_both) +
  scale_y_continuous(limits=c(0,1.1),breaks=c(0,.25,.5,.75,1.0)) +
  geom_text(
    aes(label=format(recall,digits=2)), 
    vjust=-0.3, 
    size=2, 
    position=position_dodge(width=1.0)) +
  scale_fill_grey() +
  theme_bw() +
  theme(legend.position="bottom")
ggsave("../figures/rawacc_big.pdf", width=4, height=8, units="in")

knn <- read_csv("knn.csv")
knn$rdim <- ceiling(knn$dim / 2) * 2
knnf <- filter(knn, knn$data != "shuttle")
ggplot(knnf, aes(alg, throughput, fill = data)) +
  geom_bar(position="dodge", stat="identity") +
  facet_wrap(~ rdim, ncol=1, labeller = label_both) +
  geom_text(
    aes(label=format(throughput,digits=2)), 
    vjust=-0.3, 
    size=2, 
    position=position_dodge(width=1.0)) +
  scale_fill_grey() +
  theme_bw() +
  theme(legend.position="bottom")
ggsave("../figures/knnthrough_big.pdf", width=4, height=8, units="in")



outlieracc <- read_csv("outlieracc.csv")
algorithms <- c("gaussian", "mcd", "gmm", "forest", "dbscan", "knn", "kde")
ggplot(outlieracc, aes(algorithm, num_true)) +
  scale_x_discrete(limits = algorithms) +
  geom_bar(stat="identity") +
  labs(title = "Room Occupancy Detection Rate", x= "Algorithm", y="Outliers Detected")
ggsave("../figures/outlieracc.pdf", width=7, height=3, units="in")


outlieracc <- read_csv("outlieracc_shuttle.csv")
algorithms <- c("MCD", "EmpCov", "GMM", "OCSVM", "KNN", "KDE", "IC2:KNN", "IC2:KDE")
ggplot(outlieracc, aes(algorithm, TruePos / Anomalies)) +
  scale_x_discrete(limits = algorithms) +
  geom_bar(stat="identity") +
  labs(title = "Shuttle Outlier Detection Rate", x= "Algorithm", y="Recall")
ggsave("../figures/outlieracc_shuttle.pdf", width=7, height=3, units="in")

algorithms <- c("MCD", "EmpCov", "GMM", "OCSVM", "KNN", "KDE", "IC2:KNN", "IC2:KDE")
ggplot(outlieracc, aes(algorithm, 43500 / Runtime)) +
  scale_x_discrete(limits = algorithms) +
  geom_bar(stat="identity") +
  labs(title = "Shuttle Outlier Throughput", x= "Algorithm", y="Throughput")
ggsave("../figures/outlierthru_shuttle.pdf", width=7, height=3, units="in")
