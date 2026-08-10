# 💧 WaterLog
하루 물 섭취량을 기록하고, 나의 수분 섭취 패턴을 확인하기 위한 목적으로 개발했습니다.<br/>
물 마시는 습관을 기르고, 주간 / 월간 통계를 통해 건강한 수분 섭취 습관을 만들 수 있도록 제작했습니다.
## PlayStore 다운로드 링크 ⬇️
[PalyStore 바로가기](https://play.google.com/store/apps/details?id=com.juwon.waterlog)
## 개발 기간
2025.10.25 ~ 2025.10.27 / <br/>
2026.02.17 ~ 2026.4.03
<br/><br/>
## ⚒️기술스택
|분류|
|:---:|
`Kotlin` `Jetpack Compose` `Multi-Module` `MVVM` `Clean Architecture` `Hilt` `Flow` `Vico Chart` `Firebase Auth/Firestore` `Firebase Functions` `Firebase Messaging(FCM)` `OpenAI API`

<br/><br/>
## 이런 분들을 위해 만들었어요
- 건강한 수분섭취습관을 만들고 싶은 분
- 나의 물 섭취 패턴을 데이터로 확인하고 싶은 분
- 목표 물 섭취량을 정하고, 꾸준히 관리하고 싶은 분
- 수분습관을 게임처럼 관리하고 싶은 분

<br/><br/>
## 💻 주요기능
## 회원가입/로그인
- Kakao, Naver, Google 소셜 로그인 기능을 제공합니다
- 이메일/비밀번호 회원가입,로그인
<p align="left">
  <img src="https://github.com/user-attachments/assets/4918c6f0-32f2-4206-9c98-842fede5849b" width="180" height="400" />
</p>

## 물 섭취 기록
- +,- 버튼을 눌러 물섭취량을 간편하게 기록할 수 있어요
<p align="left">
  <img src="https://github.com/user-attachments/assets/594e6a56-dc2a-4294-bba6-6395a148e8e0" width="180" height="400" />
</p>

## 주간 / 월간 수분 섭취 통계
- 최근 7일 물섭취량 / 월간 평균 섭취량을 그래프로 확인할 수 있어요
<p align="left">
  <img src="https://github.com/user-attachments/assets/51cae54e-2ba5-4d49-8725-311b13d57737" width="180" height="400" />
  <img src="https://github.com/user-attachments/assets/e3da403c-31a3-4915-a60e-bd42be659f1c" width="180" height="400" />
</p>

## 알림 기능
- 아래 3가지 상황에 푸시알림이 와요
- 읽은 알림은 회색으로 표시돼요
1. 처음 회원가입해서 들어왔을때
2. 오늘 물섭취량(8잔) 달성했을때
3. 앱에 안들어왔을때
<p align="left">
  <img src="https://github.com/user-attachments/assets/a8d75f9b-72b2-4cdd-ab54-8ff2f5d152df" width="180" height="400" />
</p>

## 뱃지기능
- 목표를 달성하면 잠겨있는 뱃지를 획득할 수 있어요
- 꾸준한 목표달성을 위한 동기부여가 돼요
1. 하루 2L 달성했을때
2. 7일연속 2L 달성했을때
3. 한달연속 2L 달성했을때
4. 6개월 연속 2L 달성했을때
<p align="left">
  <img src="https://github.com/user-attachments/assets/51b9501f-948c-4221-98de-4c90e364eeed" width="180" height="400" />
</p>

## AI 물섭취 질문기능
- AI 수분섭취 도우미인 위티한테 질문할 수 있어요
- 질문은 하루 3번으로 제한돼요
<p align="left">
  <img src="https://github.com/user-attachments/assets/b72b9696-7e3f-47a0-a1f9-ee854fd4b378" width="180" height="400" />
</p>

## 광고 기능
- Google AdMob을 연동하여 화면 일부에 배너 광고를 노출하고,
보상형 광고 시청을 통해 잠금된 음료를 해제할 수 있도록 구현했어요
- 사용자는 광고 시청 후 다양한 종류의 음료를 기록할 수 있습니다.

<br/><br/>
## 1차 개발 완료 기능
- 소셜 로그인, 이메일 로그인
- 물 섭취 기록 기능
- 주간 수분 섭취량, 월간 평균 섭취량 그래프 기능
- 뱃지 기능 (목표 달성률 기반 뱃지부여)
- AI 물섭취 질문기능 (AI 도우미)
- 알림 기능
- 광고 기능
- 로그아웃,회원탈퇴 기능

<br/><br/>
## 2차 개발 도입예정 기능
- Wear OS 연동

---

### 서비스 구조 도식화

<img src="https://github.com/user-attachments/assets/2a4703b2-f93d-4256-ac2b-a7a0a1806e74" />

---

### 구조설계하며 고민했던 사항들

[Multi Module 단위 구조 설계](https://blush-zephyr-e3e.notion.site/Multi-Module-3335dca21215807c99fad6258976d43f?pvs=143)

[Clean Architecture + MVVM 패턴을 채택한 이유](https://blush-zephyr-e3e.notion.site/Clean-Architecture-MVVM-3335dca212158043b45ac61580bed575?pvs=143)

