#!/usr/bin/env bash
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="${DIR%/scripts}"
APK="${ROOT}/app/build/outputs/apk/debug/app-debug.apk"

if ! command -v adb >/dev/null 2>&1; then
  echo "[error] 未找到 adb，请安装 Android Platform Tools" >&2
  exit 1
fi

if [[ ! -f "$APK" ]]; then
  echo "[info] 未找到 Debug APK，先构建"
  bash "${DIR}/build_debug.sh"
fi

adb install -r "$APK"
echo "[ok] 已安装 Debug APK"
