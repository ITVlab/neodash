#!/bin/sh
# Copyright 2013 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

PLATFORM=android-21
OUT_PATH=../build/javadoc

cd `dirname $0`

source _locals.sh
javadoc -linkoffline http://developer.android.com/reference ${ANDROID_SDK}/docs/reference \
        -sourcepath ../src/main/java:../build-aidl \
        -classpath ${ANDROID_SDK}/platforms/${PLATFORM}/android.jar:${ANDROID_SDK}/tools/support/annotations.jar \
        -d ${OUT_PATH} \
        -notree -nonavbar -noindex -notree -nohelp -nodeprecated \
        -stylesheetfile javadoc_stylesheet.css \
        -windowtitle "DashClock API" \
        -doctitle "DashClock API" \
        com.google.android.apps.dashclock.api \
        com.google.android.apps.dashclock.api.host

cp prettify* ${OUT_PATH}/resources/
cp index.html ${OUT_PATH}/

python tweak_javadoc_html.py ${OUT_PATH}/
