# 🪸 DiveLink

## 🐬 프로젝트 설명

**DiveLink**는 프리다이빙 로그 기록, 연습 공지 등록, 회원들의 장비 대여 신청으로 인한 대여료 및 대여품 갯수 자동 합산 기능을 지원하는 웹 기반 백엔드 시스템입니다.  
강사는 연습 공지를 작성하고, 장비 대여료를 관리하며, 참가자의 다이빙 로그를 확인할 수 있습니다.  
회원은 다이빙 로그를 작성하고, 연습에 필요한 장비를 신청할 수 있습니다.

---

## 🧩 전체 기능 목록



### 👤 공통 기능 (User & Admin)

- 회원가입 / 로그인 / 로그아웃
- 비밀번호 암호화 저장
- 역할(Role)에 따른 권한 분리 (USER / ADMIN)

---

### 🧑‍🏫 관리자(Admin) 기능

#### 📦 장비 관리
- 장비 등록 (이름, 가격, 수량)
- 장비 수정 대신 신규 생성 (히스토리 유지) => 수정 할 경우 새로운 equipment_set id 생성
- 장비 목록 조회
<!--
#### 🧰 장비 세트 관리
- 장비 세트 생성 (여러 장비 묶음)
- 장비 세트에 장비 추가 / 제외
- 세트 이름 지정, 생성자 정보 기록
- 장비 세트 목록 조회
-->
#### 📢 연습 공지 관리
- 연습 공지 등록 (제목, 일시, 장소, 최대 인원, 장비 세트, 납부 방식)
- 납부 방식: FREE(완전 무료) / INDIVIDUAL(연습 자체는 무료이나, 장비대여료는 존재) / FLAT(장비 대여료 포함 정액제)
- 공지 목록 및 상세 보기
- 공지별 신청자 조회

#### 🎉 이벤트 관리
- 다이빙 투어, 송년회, 모임 공지에 활용
- 이벤트 게시글 등록 / 목록 / 상세 조회 / 삭제
- 이미지 첨부 가능

#### 🌊 다이빙 로그 코멘트
- 사용자가 남긴 다이빙 로그 열람
- 코멘트 작성 가능
- 코멘트 수정 / 삭제
---

### 👥 사용자(User) 기능

#### 📝 연습 신청
- 연습 공지 조회 및 신청
- 장비 선택 (장비별 수량, 자동 대여료 계산)
- 신청 상태 관리
  - APPLIED(대여 엾이 연습 신청 한 경우) / PAYMENT_PENDING(입금 대기 중) / CONFIRM_REQUESTED(입금 확인요청) / PAYMENT_CONFIRMED(입금 확인) / CANCELLED(사용자 신청 취소) / REJECTED(강사측 신청 취소 - 지속적인 미입금)

#### 🧾 장비 대여 정보
- 신청한 장비 및 가격 저장
- PracticeApplicationEquipment 테이블에 개별 저장
- 연습별 장비 대여 내역 확인

#### 🌊 다이빙 로그
- 다이빙 일지 등록
  - 날짜, 시간, 장소, 수트, 웨이트, FIM/CWT/DYN, 최장잠수시간, 메모
 <!-- - 공개 범위 설정: PRIVATE / BUDDY_ONLY / PUBLIC-->
- 다이빙 로그 목록 및 상세 보기
- 다이빙 로그 수정
- 로그 삭제는 불가 (번호 순서 유지 목적)

#### 🎫 이벤트 참여
- 이벤트 게시글 보기
- 이벤트 신청 / 취소 / 상태 확인

---

### 🛠 내부 처리 로직

#### 📦 재고 확인
- 공지의 장비 세트 기준으로 신청된 장비 수량 집계
- practice_notice의 equipment_set.id와 연결된 Equipment.quantity 를 practice_application_equipment와 비교하여 초과 여부 판단

#### 💰 대여료 계산
- 납부 방식별 계산 분기:
  - FREE → 0원
  - INDIVIDUAL → 신청 장비 가격 총합
  - FLAT → 고정 금액

#### 🧩 장비 스냅샷 처리
- 공지 생성 시점의 세트 ID를 practice_notice에 저장
- 장비 정보 변경이 있어도 이전 연습공지에는 영향 없음

#### 🧾 상태값 Enum 관리
- 사용자 역할: ADMIN / USER
- 납부 방식: FREE / INDIVIDUAL / FLAT
- 신청 상태: APPLIED / PAYMENT_PENDING / ...
  <!--- 공개 범위: PRIVATE / BUDDY_ONLY / PUBLIC-->
---

## 🗂 ERD

<img width="1931" height="1541" alt="제목 없는 다이어그램 drawio (1)" src="https://github.com/user-attachments/assets/465ef137-9fdb-4060-b9b1-49ae9f1f0441" />

---

## 🛠 사용 기술 (Tech Stack)

### 📌 Backend
- Java 21
- Spring Boot 3.5.4
- Spring Web / Spring Security / Spring Data JPA
- Spring Boot Validation
- Spring Boot DevTools
- Spring Boot Actuator

### 📌 Database
- MariaDB

### 📌 Build & Dev Tools
- Gradle
- Git, GitHub
- IntelliJ IDEA

---

## 🧠 Trouble Shooting
- `master` / `main` 브랜치 이슈 → 기본 브랜치 변경으로 해결
- JPA 연관관계 정리 중 고민: 장비 세트 & 신청 장비 분리 구조 설계
- 다이빙 넘버 삭제 문제 → 로그는 삭제 금지로 정책 결정

---
