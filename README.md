# 🌳 STUDY HUB 🌳
![1](https://github.com/Study-Hub-09/StudyHub_BE/assets/129656095/c3896e16-d210-4ecd-beb7-e8e1063a48cc)



>🧑‍💻 개발 기간 : 2023.05.19 ~ 2023.06.30</p> 👉 서비스 URL : https://www.study-hub.shop </P>
<br>

## 🌱 서비스 소개 <br>

![2](https://github.com/Study-Hub-09/StudyHub_FE/assets/129656095/3ad9985c-0c44-41b4-96c3-a79c43c0639c)
![3](https://github.com/Study-Hub-09/StudyHub_FE/assets/129656095/eec6f7a3-68e5-4409-bd51-37d6149c1953)
![4](https://github.com/Study-Hub-09/StudyHub_FE/assets/129656095/3fd0f811-1177-4726-b58d-4d0b327b6fc2)



<br>

## 🌿 핵심 기능 <br>
<details>
  <summary>🧑‍💻 화상 스터디</summary>
  <br>
    - 스터디룸에 접속한 유저는 webRTC를 활용하여 본인의 화면을 송출하는 동시에, 다른 유저의 공부하는 모습을 실시간으로 확인<br>
<br>
  
![1](https://github.com/Study-Hub-09/StudyHub_FE/assets/129656095/f2c557a9-fbf3-4e4f-8e5a-16312200b0cd)
  화면 송출 시 

![2](https://github.com/Study-Hub-09/StudyHub_FE/assets/129656095/cecd6959-d641-4365-81ac-91fdcf84a2b6)
  화면 차단 시


</details>
<details>
  <summary>⌨️ 그룹 채팅 </summary>
  <br>
    - 소켓 통신을 바탕으로 스터디 룸에서 채팅 기능 제공<br>
<br>
  
![3](https://github.com/Study-Hub-09/StudyHub_FE/assets/129656095/421bebce-5523-4c28-9898-4698b8cd6710)


</details>
<details>
  <summary>🕑 타이머 기능 & 통계 </summary>
  <br>
    - 매일 자정을 기준으로 DB에 저장된 공부 시간을 참조하여, 일일 공부시간 및 누적 공부시간 확인<br>
<br>
<img width="640" alt="통계" src="https://github.com/Study-Hub-09/StudyHub_FE/assets/129656095/8e50b6e3-0e47-46d5-849d-7ea5e015b180">



</details>

<br>

## 🪴 Project Architecture <br>
![STUB architecture](https://github.com/Study-Hub-09/StudyHub_FE/assets/129656095/7719562c-1e99-4148-ad3b-51056303d1ea)



<br>

## 🌲 기술적 의사 결정 <br>
### BACK-END

| 사용 기술 | 기술 설명 |
| --- | --- |
| **CI/CD - EC2 + Github Action & Nginx** | 프론트 팀이 원활하게 작업하기 위해서는 최대한 빨리 서버가 띄워져야 한다고 생각했으며,<br> dev branch에 코드가 merge 될 때마다 배포를 위해 반복적인 작업 최소화하고 서버 개발에만 집중할 수 있도록 CI/CD 환경을 구축하기로 결정하였습니다.<br> Jenkins와 github action + EC2 두 가지 선택지 중 GitHub Action이 GitHub 와의 연동이 편하고 빠른 시간 내에 배포할 수 있는 장점이 있으며,<br> GitHub에서 발생하는 이벤트를 처리할 수 있다는 점을 통해 해당 선택지를 도입하게 되었습니다.<br> 또한 배포할 때마다 서버가 잠시 중단되는 상황에 의해 유저들이 서비스를 이용 중에 중단되는 불편함이 있어 무중단 배포인 Nginx를 도입하게 되었습니다. |
| **OpenVidu** | N대N 화상 채팅을 구현하기 위해서 시그널링 서버를 통해서 P2P로 연결하는 방식과 미디어 서버를 두는 방식이 존재하는데 P2P로 구현하기에는 클라이언트에 부담이 너무 크며, OpenVidu 플랫폼을 이용하여 미디어 서버를 둠과 동시에 시그널링 처리도 맡길 수 있기 때문에 결정하게 되었습니다. |
| **Stomp** | Stomp 프로토콜은 메시지 전송을 위한 프로토콜로써 pub/sub 기반으로 동작하며 메시지 헤더를 가지고 있기 때문에 메시지의 송신, 수신에 대한 처리를 명확하게 정의 할 수 있는 장점이 있습니다.<br> 추가적인 작업 없이 메시지 발행 시 엔드포인트만 조정하여 쉬운 전송/수신 또한 가능하여 도입하게 되었습니다. |
| **Oauth2** | 소셜 로그인을 도입하게 된 가장 큰 이유는 사용자의 편의성 증대를 위해서 입니다.<br> 클릭 몇 번 만으로 로그인을 할 수 있는 편리성을 통해 사용자들이 이 웹을 더 찾을 수 있는 이유를 제공하기 위해서 입니다. |
| **Sentry** | Sentry는 실시간 오류 모니터링 및 로깅 플랫폼으로, 애플리케이션에서 발생하는 예외, 오류, 경고 등을 실시간으로 수집하고 중앙화된 대시보드에서 확인할 수 있도록 지원합니다.<br> 이러한 이유로 Sentry를 도입하여 애플리케이션의 로깅과 예외 처리를 효과적으로 관리할 수 있게 되었습니다. |
| **Redis** | Redis는 오픈 소스 인 메모리 키 값 데이터 구조 스토어로서 빠르게 접근할 필요가 있는 데이터를 저장하는데 유용합니다. 클라이언트가 자주 접근하는 토큰 정보나 데이터 캐싱에 사용하기 위해서 도입하였습니다. |
| **Spring Batch** | Spring Batch는 대용량 일괄처리 편의를 위한 다양한 기능을 제공합니다.<br> 특정한 시점에 스케줄러를 통해 자동화된 작업과 트랜잭션 관리와 함께 통계 처리를 맡기기 위해 도입하였습니다. |
| **JUnit5 + Mockito** | 테스트 코드를 사용하면 개발 과정 중에 발생할 수 있는 예상치 못한 문제를 컴파일 시점에서 찾을 수 있습니다.<br> 이는 런타임 오류를 방지하고, 버그를 사전에 발견하여 디버깅 시간을 절약할 수 있습니다.<br> 또한 작성한 코드가 의도한 대로 동작하는지 확인할 수 있습니다.<br> 테스트 코드는 예상되는 입력과 출력을 정의하고, 코드 실행 결과를 확인하여 코드의 정확성을 검증하는 역할을 합니다.<br> 이를 통해 코드 변경이나 리팩토링 후에도 기능의 동작이 올바른지 확인할 수 있기 때문에 도입하게 되었습니다. |
<br>

## 🧨 트러블 슈팅
### BACK END
<details>
  <summary> 💥 OpenVidu 세션이 종료되는 이슈 </summary>
    <br>
  
  **`문제`**
    
  OpenVidu 서버에 생성된 세션이 주기적으로 삭제되거나, 한번이라도 Connection이 발생한 후 Connection이 하나도 존재하지 않게될 경우 세션이 삭제되는 문제 발생
     
    
  **`시도`**
    
  1. env 파일을 수정하여 **OPENVIDU_SESSIONS_GARBAGE_INTERVAL** 값을 0으로 수정하면 Garbage Collector가 비활성화됨과 동시에**OPENVIDU_SESSIONS_GARBAGE_THRESHOLD** 속성 또한 무효화가 되기 때문에 Interval 값을 0으로 수정
  2. 유저가 이미 삭제된 세션으로 접근할 경우 새로운 세션을 생성해서 해당 세션에 유저를 입장시키는 방법
     
    
  **`해결`**
    
  스터디룸에 유저 입장 -> OpenVidu 서버에 활성화되어 있는 세션 목록 로드 -> 입장하고자 하는 세션 ID가 존재할 경우 해당 세션으로 입장 -> 해당 세션이 없을 경우 입장하고자 했던 세션 ID와 똑같은 ID로 Custom 세션 생성 및 입장
  과정을 통해 유저 입장에선 세션이 종료되지 않고 계속 존재하는 것처럼 인식할 수 있도록 수정하여 해결

  **`해결 코드`**
  
  <img width="599" alt="스크린샷 2023-06-27 023723" src="https://github.com/Study-Hub-09/StudyHub_BE/assets/129656095/38e4028a-737c-4506-9490-f7ebb8568d44">

</details>

<details>
  <summary> 💥 방 생성 및 방 입장 동시성 이슈 </summary>
    <br>
  
  **`문제`**
    
  1. 한 방에 입장은 9명까지 가능
  2. 방 생성 시 한 유저 당 3개의 방을 생성 가능
  3. 방 입장 시 한 유저가 같은 방을 여러 번 입장할 수 없고, 이미 입장한 방 이외에 다른 방 입장 불가
     
  위 조건들에 대한 예외 처리는 되어있지만, Jmeter를 이용한 동시성 테스트 결과 전부 동시성 제어가 되지 않아 원하는 대로 처리가 되지 않음
     
    
  **`시도`**
    
  1. RabbitMQ와 같은 메세지 브로커를 이용하여 요청을 메세지 큐에 담아서 처리하고 요청 자체에 대한 동시성 제어를 시도
  2. 동기화 기법 및 DB Lock 사용
     
    
  **`해결`**
    
  Pessimistic Lock을 이용하여 DB에 동시 접근을 막아서 해결

  **`해결 전, 후 테스트`**
  
  ![전](https://github.com/Study-Hub-09/StudyHub_BE/assets/129656095/a759bc8f-c2c1-498f-bfe6-1aaeef67020e) 해결 전  ![후](https://github.com/Study-Hub-09/StudyHub_BE/assets/129656095/cc6c6c38-98d1-448a-ae7e-ef105488753e) 해결 후


  

</details>
<br>

## 📊 개선 사항
### BACK END
<details>
  <summary> ❕마이페이지 조회 성능 개선을 위한 Redis 캐싱 방안 </summary>
    <br>
  
  **`기술 도입 배경`**
    
  유저로부터 접근이 잦은 마이페이지를 조회할 때마다 통계를 계산하고 조회하는 쿼리가 생성되고 있는데 이를 개선할 수는 없을까?
     
    
  **`기술 구현 과정`**
    
  Redis에 통계 데이터를 저장하여 캐싱을 적용하고, 수정이 필요한 당일 데이터에 변경이 일어날 때만 통계 데이터에 값을 수정. <br> 이에 따라,  마이페이지를 조회할 때마다 쿼리를 생성하지 않아도되고 데이터를 조회하는 속도 또한 향상될 수 있도록 하였다.
     
    
  **`고민`**
    
  단순히 Key Value 구조로만 저장하기에는 모든 멤버의 일별, 주별, 월별 데이터를 다른 멤버들과 구분되게 넣기가 어려운데, 어떤 방법이 있을까?

  **`해결 방안`**
  
  Redis가 제공하는 자료구조 중 Value로 또 다른 Key Value Map을 넣을 수 있는 Hash 자료 구조를 이용하여 모든 멤버들의 통계 데이터를 구분해서 넣을 수 있도록 하였다.

</details>
<details>
  <summary> ❕스터디 룸 만료 처리와 자동화된 통계 데이터 관리 방법 </summary>
    <br>
  
  **`기술 도입 배경`**
    
  만료기한이 된 스터디룸 처리와 함께 통계 데이터를 효과적으로 자동화해서 처리할 수 있는 방법이 무엇이 있을까?
     
    
  **`기술 구현 과정`**
    
  대용량 일괄처리 기능을 제공하는 Spring Batch와 작업을 자동화할 수 있는 Spring Schedular를 사용하였다.
스케줄러를 매일 자정에 한번 씩 수행되도록 설정하고 1. 만료된 방을 삭제 2. 사용되지 않는 OpenVidu 세션 삭제 3. 통계 데이터 저장
을 3개의 Step으로 구성하여 일괄 처리를 자동화하였다.
     
    
  **`고민`**
    
  처리 방식을 어떻게 정할 것인가?

  **`해결 방안`**
  
  실행할 작업이 복잡하고 읽기, 처리 쓰기와 관련된 작업을 수행해야하는 통계 작업은 Chunk 방식으로 처리하였고, 비교적으로 처리가 단순하고 처리량이 많지 않은 나머지 작업들은 Tasklet 방식으로 처리하였다.

</details>
<br>

## 👨‍👩‍👧‍👦 스터브 팀
- Front: 강한빛([Github](https://github.com/hanbitk)), 김시옥([Github](https://github.com/gigupc11)), 신민철([Github](https://github.com/MinCheolS))
- Back: 김근보([Github](https://github.com/CaptainGombo)), 박성우([Github](https://github.com/seongwop)), 한승희([Github](https://github.com/seunghee58)))

|             [강한빛](https://github.com/hanbitk)             |              [김시옥](https://github.com/gigupc11)              |             [🚩신민철](https://github.com/MinCheolS)             |              [🚩김근보](https://github.com/CaptainGombo)              |               [박성우](https://github.com/seongwop)               |             [한승희](https://github.com/seunghee58)             |
|:-------------------------------------------------------------:|:-------------------------------------------------------------:|:-------------------------------------------------------------:|:------------------------------------------------------------:|:-------------------------------------------------------------:|:-------------------------------------------------------------:|
|![image (7)](https://github.com/Study-Hub-09/StudyHub_FE/assets/129656095/f7977f3d-6ac2-4bf8-baff-2cc30cfbb472)|![image (3)](https://github.com/Study-Hub-09/StudyHub_FE/assets/129656095/09563e9c-eaf0-46da-9990-2bda6bf28585)|![image (2)](https://github.com/Study-Hub-09/StudyHub_FE/assets/129656095/92355f44-899c-4946-b5b0-d1a72b61d30d)|![image (6)](https://github.com/Study-Hub-09/StudyHub_FE/assets/129656095/63db2705-9c30-448e-8ce1-130b52d6816f)|![image (5)](https://github.com/Study-Hub-09/StudyHub_FE/assets/129656095/c0d17c6c-b7b8-4d70-938d-0b69902411ba)|![image](https://github.com/Study-Hub-09/StudyHub_FE/assets/129656095/426406ea-e059-40b3-af8e-c698c8099851)|
|                           FRONT-END                           |                           FRONT-END                           |                           FRONT-END                            |                           BACK-END                           |                           BACK-END                            |                           BACK-END                            |
<br>
