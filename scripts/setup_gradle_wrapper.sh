#!/usr/bin/env bash
set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="${DIR%/scripts}"

if [[ -x "${ROOT}/gradlew" ]]; then
  echo "[info] gradlew 已存在，跳过生成"
else
  if command -v gradle >/dev/null 2>&1; then
    echo "[info] 使用系统 gradle 生成 Wrapper (Gradle 8.7)"
    (cd "$ROOT" && gradle wrapper --gradle-version 8.7 --distribution-type all)
  else
    echo "[error] 未找到 gradle 命令。请先安装 Gradle，或用 Android Studio 打开工程并执行 Sync 后自动生成 Wrapper。" >&2
    exit 1
  fi
fi

"${ROOT}/gradlew" --version
