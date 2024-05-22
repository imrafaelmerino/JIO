```shell

NAME="jio-exp"
VERSION="3.0.0-RC2"
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
