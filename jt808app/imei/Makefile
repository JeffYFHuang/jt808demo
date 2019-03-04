#--------------------------------------------------------------
#               Quectel OpenLinux
#--------------------------------------------------------------
QL_SDK_PATH   ?= $(shell pwd)/../..

QL_EXP_TARGETS = com_liteon_jt808_util_AtCommand
QL_EXP_LDLIBS  = libatcmd.so

#--------------------------------------------------------
# TOOLS BASIC CONFIG
# Note: No Need to change them
#--------------------------------------------------------
CPPFLAGS += -fPIC -I$(JAVA_HOME)/include \
            -I$(JAVA_HOME)/include/linux \
            -I./                     \
            -I./inc                  \
            -I../../include          \
            -I$(QL_SDK_PATH)/lib/interface/inc        \
            -I$(SDKTARGETSYSROOT)/usr/include               \
            -I$(SDKTARGETSYSROOT)/usr/include               \
            -I$(SDKTARGETSYSROOT)/usr/include/data          \
            -I$(SDKTARGETSYSROOT)/usr/include/dsutils       \
            -I$(SDKTARGETSYSROOT)/usr/include/qmi           \
            -I$(SDKTARGETSYSROOT)/usr/include/qmi-framework \


LDFLAGS += -L./ -L$(QL_SDK_PATH)/lib


all: $(QL_EXP_TARGETS)
.PHPNY: all


%:%.c
#	$(CC) $(CPPFLAGS) $(CFLAGS) $(LDFLAGS) $^ -o $@
	$(COMPILE.c) $^
	$(LINK.o) $@.o  -shared -o $(QL_EXP_LDLIBS)

clean:
	rm -rf $(QL_EXP_TARGETS) *.o

.PHONY:checkmake
checkmake:  
	@echo -e "CURDIR =		\n	${CURDIR}"  
	@echo -e "\nMAKE_VERSION =	\n	${MAKE_VERSION}"  
	@echo -e "\nMAKEFILE_LIST =	\n	${MAKEFILE_LIST}"  
	@echo -e "\nCOMPILE.c =		\n	${COMPILE.c}"
	@echo -e "\nCOMPILE.cc =	\n	${COMPILE.cc}"
	@echo -e "\nCOMPILE.cpp =	\n	${COMPILE.cpp}"
	@echo -e "\nLINK.cc =		\n	${LINK.cc}"
	@echo -e "\nLINK.o =		\n	${LINK.o}"
	@echo -e "\nCPPFLAGS =		\n	${CPPFLAGS}"
	@echo -e "\nCFLAGS =		\n	${CFLAGS}"
	@echo -e "\nCXXFLAGS =		\n	${CXXFLAGS}"
	@echo -e "\nLDFLAGS =		\n	${LDFLAGS}"
	@echo -e "\nLDLIBS =		\n	${LDLIBS}"
