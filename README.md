# project-os

Yocto Project workspace for the `scarthgap` release, with a project-owned layer (`meta-project`), upstream layers managed by a local repo manifest, and container/CI automation for reproducible builds.

## Repository Layout

- `manifests/`: Repo manifests used to fetch/pin upstream layers.
- `layers/meta-project`: Project-owned layer (images, packagegroups, templates, docs).
- `layers/poky`: Upstream Poky checkout.
- `layers/meta-openembedded`: Upstream OpenEmbedded checkout.
- `layers/meta-security`: Upstream security layer checkout.
- `layers/meta-virtualization`: Upstream virtualization layer checkout.
- `layers/meta-updater`: Uptane/OTA layer checkout.
- `layers/meta-clang`: Clang/LLVM layer checkout.
- `layers/meta-intel`: Intel BSP layer checkout.
- `layers/meta-raspberrypi`: Raspberry Pi BSP layer checkout.
- `layers/meta-tegra`: NVIDIA Tegra BSP layer checkout.
- `containers/ubuntu-20.04`: Yocto build container definition and helper script.
- `containers/ubuntu-22.04`: Yocto build container definition and helper script.
- `containers/ubuntu-24.04`: Yocto build container definition and helper script.
- `.github/workflows/`: Build and container CI workflows.
- `.vscode/tasks.json`: Common Yocto tasks for local development.

## Supported Build Templates

Templates live in `layers/meta-project/conf/templates/`:

- `default`
- `qemux86-64`
- `qemuarm64`
- `raspberrypi5`
- `jetson-orin-nano-devkit-nvme`

Each template seeds `build/conf/local.conf` and `build/conf/bblayers.conf` for that target.

## Common BitBake Targets

- `core-image-project` (project image from `meta-project`)
- `core-image-minimal`
- `core-image-full-cmdline`

## Quick Start (Host Build)

### 1) Install required host tools

At minimum, make sure `curl`, `git`, and `lz4` are available. On Ubuntu:

```bash
sudo apt-get update
sudo apt-get install -y build-essential chrpath cpio debianutils diffstat file gawk gcc git \
  iputils-ping libacl1 libcrypt-dev locales python3 python3-git python3-jinja2 python3-pexpect \
  python3-pip python3-subunit socat texinfo unzip wget xz-utils zstd
sudo apt-get install -y curl git lz4 docker.io ripgrep
sudo usermod -aG docker $USER # logout/login
```

### 2) Install the Google repo launcher

```bash
mkdir -p "$HOME/.local/bin"
curl -fsSL https://storage.googleapis.com/git-repo-downloads/repo -o "$HOME/.local/bin/repo"
chmod +x "$HOME/.local/bin/repo"
export PATH="$HOME/.local/bin:$PATH"
```

### 3) Sync layers from manifest

```bash
cd <project-root>
repo init -u . -m manifests/default.xml
repo sync -j"$(nproc)"
```

### 4) Initialize the build directory

```bash
cd <project-root>
rm -rf build/conf
TEMPLATECONF=$(pwd)/layers/meta-project/conf/templates/default \
  source layers/poky/oe-init-build-env build
```

For another target template, change `default` to one of:
`qemux86-64`, `qemuarm64`, `raspberrypi5`, `jetson-orin-nano-devkit-nvme`.

<!-- copy and paste convenience commands
rm -rf build/conf
TEMPLATECONF=$(pwd)/layers/meta-project/conf/templates/qemux86-64 \
  . layers/poky/oe-init-build-env build
rm -rf build/conf
TEMPLATECONF=$(pwd)/layers/meta-project/conf/templates/raspberrypi5 \
  . layers/poky/oe-init-build-env build
rm -rf build/conf
TEMPLATECONF=$(pwd)/layers/meta-project/conf/templates/jetson-orin-nano-devkit-nvme \
  . layers/poky/oe-init-build-env build
-->

### 5) Build an image

```bash
bitbake core-image-project
```

Alternative targets:

```bash
bitbake core-image-minimal
bitbake core-image-full-cmdline
```

## Container-Based Build

Each Ubuntu version has a helper script named `docker-build-run.sh`.

Example (Ubuntu 22.04 container):

```bash
cd <project-root>
bash containers/ubuntu-22.04/docker-build-run.sh core-image-project
```

Other versions:

```bash
bash containers/ubuntu-20.04/docker-build-run.sh core-image-project
bash containers/ubuntu-24.04/docker-build-run.sh core-image-project
```

Optional environment variables accepted by the helper scripts:

```bash
IMAGE_TAG=project-os/ubuntu-22.04:latest \
BUILD_DIR=build \
BITBAKE_TEMPLATE=qemux86-64 \
BITBAKE_TARGET=core-image-minimal \
bash containers/ubuntu-22.04/docker-build-run.sh
```

## VS Code Tasks

The workspace ships with ready-to-run tasks in `.vscode/tasks.json`:

- `Yocto: Init and Sync Layers`
- `Yocto: Show Layers`
- `Yocto: Build core-image-project`
- `Yocto: Build core-image-minimal`
- `Yocto: Clean core-image-project`

## GitHub Actions Workflows

Current workflows under `.github/workflows/`:

- `build-core-image-project.yml`:
  manual build of `core-image-project` with selectable template.
- `build-core-image-full-cmdline.yml`:
  manual build of `core-image-full-cmdline` with selectable template.
- `build-containers-push.yml`:
  build and push container images for Ubuntu 20.04/22.04/24.04 to GHCR.

Image build workflows run on `self-hosted` runners and use reusable local actions:

- `.github/actions/repo-sync`
- `.github/actions/bitbake-build`

## Running QEMU Images

For QEMU templates, run images with `runqemu` after a successful build.

```bash
runqemu qemux86-64 core-image-project nographic slirp
```

For `qemuarm64` template:

```bash
runqemu qemuarm64 core-image-project nographic slirp
```

Deploy artifacts are under:

```text
build/tmp/deploy/images/<machine>/
```

## Optional: Local PR Service (prserv)

Use a local PR server to keep package revision increments consistent.

Start:

```bash
mkdir -p build/cache
bitbake-prserv --start \
  --host 127.0.0.1 \
  --port 8585 \
  --file build/cache/prserv.sqlite3 \
  --log build/cache/prserv.log
```

Enable in `build/conf/local.conf`:

```conf
PRSERV_HOST = "127.0.0.1:8585"
```

Stop:

```bash
bitbake-prserv --stop --host 127.0.0.1 --port 8585
```

## Optional: Sync Yocto Caches to S3

If you use shared caches, sync `build/downloads` and `build/sstate-cache` with `s3cmd`.

```bash
s3cmd sync build/downloads/ s3://<bucket>/<prefix>/downloads/
s3cmd sync build/sstate-cache/ s3://<bucket>/<prefix>/sstate-cache/
```

Use `--dry-run` first to review changes safely.

```
docker run -it --rm -v $(pwd):/home/yoctouser \
  crops/poky:ubuntu-22.04 --workdir /home/yoctouser
```
