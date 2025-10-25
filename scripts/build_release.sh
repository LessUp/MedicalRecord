#!/usr/bin/env bash
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="${DIR%/scripts}"

if [[ ! -x "${ROOT}/gradlew" ]]; then
  echo "[info] gradlew 不存在，先执行 setup_gradle_wrapper.sh"
  bash "${DIR}/setup_gradle_wrapper.sh"
fi

"${ROOT}/gradlew" :app:assembleRelease
APK="${ROOT}/app/build/outputs/apk/release/app-release-unsigned.apk"
if [[ -f "$APK" ]]; then
  echo "[ok] Release APK 生成（未签名）：$APK"
  echo "[hint] 如需签名发布，请使用 Android Studio 或在 app/build.gradle.kts 配置 signingConfigs"
else
  echo "[warn] 未找到 Release APK，请检查构建输出目录" >&2
fi
