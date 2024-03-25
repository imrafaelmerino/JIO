```shell

NAME="jio-exp"
VERSION="3.0.0-RC1"
IMAGE="${NAME}:${VERSION}"

docker build -t ${IMAGE} .

```

```shell
cd scripts
./jio-exp-compile.sh --version ${VERSION}
```

```shell
./jio-exp-package.sh --version ${VERSION}
```