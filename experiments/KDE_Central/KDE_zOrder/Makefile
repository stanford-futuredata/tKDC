SOURCES_DIR:=sources
OBJ_DIR:=bin
OUT_DIR:=bin

OBJ_SOURCES:=$(SOURCES_DIR)/mtimer.cpp \

KDE_BUILD:=KDEZKern
KDE_BUILD_RS:=KDEZKern_RS


GCC:=g++ -O3 -g
LIBRARIES:=-lm

all: 
	@mkdir -p $(OUT_DIR)
	$(GCC) -o $(OUT_DIR)/$(KDE_BUILD) $(OBJ_SOURCES) $(SOURCES_DIR)/$(KDE_BUILD).cpp $(LIBRARIES)

RS: 
	$(GCC) -o $(OUT_DIR)/$(KDE_BUILD_RS) $(OBJ_SOURCES) $(SOURCES_DIR)/$(KDE_BUILD_RS).cpp $(LIBRARIES)

clean:
	rm -f $(OUT_DIR)/*
