#!/bin/bash

PROJECT_ROOT="/home/ubuntu/StudyHub_BE" # 프로젝트 루트
JAR_FILE="$PROJECT_ROOT/build/libs/StudyArcade_BE-0.0.1-SNAPSHOT.jar" # JAR_FILE (어쩌구저쩌구.jar)

# service_url.inc 에서 현재 서비스를 하고 있는 WAS의 포트 번호 가져오기
CURRENT_PORT=$(cat /home/ubuntu/service_url.inc | grep -Po '[0-9]+' | tail -1)
TARGET_PORT=0

echo "> Current port of running WAS is ${CURRENT_PORT}."

if [ ${CURRENT_PORT} -eq 8081 ]; then
  TARGET_PORT=8082 # 현재포트가 8081이면 8082로 배포
elif [ ${CURRENT_PORT} -eq 8082 ]; then
  TARGET_PORT=8081 # 현재포트가 8082라면 8081로 배포
else
  echo "> Not connected to nginx" # nginx가 실행되고 있지 않다면 에러 코드
  exit 1 # 에러 발생 시 스크립트 종료
fi

# 새로운 JAR 파일 업로드
echo "> Uploading new JAR file to port ${TARGET_PORT}."
scp ${JAR_FILE} localhost:${TARGET_PORT}/app.jar

# 현재 포트의 PID를 불러온다
TARGET_PID=$(lsof -Fp -i TCP:${TARGET_PORT} | grep -Po 'p[0-9]+' | grep -Po '[0-9]+')

# PID를 이용해 해당 포트 서버를 종료
if [ ! -z ${TARGET_PID} ]; then
  echo "> Sending shutdown signal to ${TARGET_PORT}."
  curl -X POST http://localhost:${TARGET_PORT}/shutdown # 서버 종료 요청 보내기
  sleep 10 # 서버 종료 대기 시간 (10초) - 적절한 시간 설정 필요
fi

# 새로운 서버 실행
echo "> Starting new WAS at ${TARGET_PORT}."
nohup java -jar -Dserver.port=${TARGET_PORT} ${JAR_FILE} > /home/ubuntu/logs/nohup_${TARGET_PORT}.out 2>&1 &

echo "> Now new WAS runs at ${TARGET_PORT}."
exit 0
