SOURCES_DIR:=sources
OBJ_DIR:=bin
OUT_DIR:=bin

OBJ_SOURCES:=$(SOURCES_DIR)/GaussTransform.cpp \
	     $(SOURCES_DIR)/ImprovedFastGaussTransform.cpp \
	     $(SOURCES_DIR)/ImprovedFastGaussTransformChooseParameters.cpp \
	     $(SOURCES_DIR)/ImprovedFastGaussTransformChooseTruncationNumber.cpp \
	     $(SOURCES_DIR)/mtimer.cpp \
		 $(SOURCES_DIR)/KCenterClustering.cpp	
		

ERROR_SOURCES:=$(SOURCES_DIR)/getError.cpp
TEST_SOURCES:=$(SOURCES_DIR)/genTestFile.cpp

KDE_EVAL_UNORM:=IFGT_UNORM
KDE_EVAL_BUILD:=EVAL_IFGT
KDE_EVAL_BF:=EVAL_BF

GCC:=g++ -O3 -g

IFGT: 
	@mkdir -p $(OUT_DIR)
	$(GCC) -o $(OUT_DIR)/$(KDE_EVAL_BUILD) $(OPTIONS) $(OBJ_SOURCES) $(SOURCES_DIR)/$(KDE_EVAL_BUILD).cpp $(LIBRARIES)

BF:
	@mkdir -p $(OUT_DIR)
	$(GCC) -o $(OUT_DIR)/$(KDE_EVAL_BF) $(OPTIONS) $(OBJ_SOURCES) $(SOURCES_DIR)/$(KDE_EVAL_BF).cpp $(LIBRARIES)

ERR:
	$(GCC) -o $(OUT_DIR)/getError $(ERROR_SOURCES)

TEST:
	$(GCC) -o $(OUT_DIR)/genTest $(TEST_SOURCES)
clean:
	rm -f $(OUT_DIR)/*
 
