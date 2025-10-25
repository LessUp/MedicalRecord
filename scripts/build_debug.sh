#!/usr/bin/env bash
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="${DIR%/scripts}"

if [[ ! -x "${ROOT}/gradlew" ]]; then
  echo "[info] gradlew 不存在，先执行 setup_gradle_wrapper.sh"
  bash "${DIR}/setup_gradle_wrapper.sh"
fi

"${ROOT}/gradlew" :app:assembleDebug
APK="${ROOT}/app/build/outputs/apk/debug/app-debug.apk"
if [[ -f "$APK" ]]; then
  echo "[ok] Debug APK 生成：$APK"
else
  echo "[warn] 未找到 APK，请检查构建输出目录" >&2
fi
