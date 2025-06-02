#!/bin/bash

echo "윷놀이 게임 (Swing 버전) 컴파일 및 실행"
echo "======================================="

# bin 디렉토리 생성
mkdir -p bin

# 컴파일
echo "컴파일 중..."
javac -d bin src/main/java/com/cas/yutnoriswing/model/*.java src/main/java/com/cas/yutnoriswing/view/*.java src/main/java/com/cas/yutnoriswing/controller/*.java src/main/java/com/cas/yutnoriswing/*.java src/main/java/module-info.java

if [ $? -eq 0 ]; then
    echo "컴파일 완료!"
    echo "게임 실행 중..."
    # 실행
    java --module-path bin -m com.cas.yutnoriswing/com.cas.yutnoriswing.YutnoriGameSwing
else
    echo "컴파일 실패!"
    exit 1
fi 