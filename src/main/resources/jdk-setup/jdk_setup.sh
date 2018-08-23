#!/bin/bash
# -----------------------------------------------------------------------------
# Setup Script for jdk batch installation and configuration
# -----------------------------------------------------------------------------
# Permission check
if [ `whoami` != "root" ];then
	echo "ERROR:JDK must be installed by root."
	exit 1
fi

BASEDIR=$(dirname "$0")
INSTALLDIR="/usr/lib/jvm"
PREFIX="oracle-"

function install_jdk() {
	# Get the installation path
	JDK_VERSION=`tar -ztf $1 | awk -F "/" '{print $1}' | sort | uniq`
	JDK_PATH=$INSTALLDIR"/"$PREFIX$JDK_VERSION

	# Start to extract jdk tarball
	echo "Start extract $JDK_VERSION to $INSTALLDIR ..."
	mkdir -p $JDK_PATH && tar xzf $1 -C $JDK_PATH --strip-components 1

	# Install java and javac
	update-alternatives --install /usr/bin/java java $JDK_PATH/bin/java 2
	update-alternatives --install /usr/bin/javac javac $JDK_PATH/bin/javac 2

	echo "$JDK_VERSION installation finished"
}

# Start commands execution
for file in `ls $BASEDIR"/"*.tar.gz`
do
	if [ -f $BASEDIR"/"$file ];then
		install_jdk $BASEDIR"/"$file
	fi
done
# End commands execution
