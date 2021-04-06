#!/bin/bash
set -e

if [ "$2" == "" ]; then
  echo "Need 2 arguments: configuration target"
  exit 1
fi

# Compiler options
MAKE_JOBS=8

# Locations
SCRIPT_DIR="$( cd "$( dirname "$0" )" && pwd )"
PREFIX="$(pwd)/build/$2"

cd "${SCRIPT_DIR}/openssl"

# TODO: investigate enable-ec_nistp_64_gcc_128

./Configure \
  no-shared \
  --prefix="${PREFIX}" \
  $1

make clean
make depend

mkdir -p "${PREFIX}" &> /dev/null

make -j"${MAKE_JOBS}" build_libs
make install_dev
make distclean
