# 💸 SSeumx2 씀씀

아이폰 단축어를 통해 결제 문자를 Spring Boot 서버에서 처리하고, 파싱한 결제 정보를 Notion Database에 자동 저장하는 프로젝트입니다.

<br />

## 🏃🏻‍♀️ Status

현재 부지런히 개발 중입니다!

<br />

## 🔗 Flow

```text
iPhone Shortcuts
    ↓
Spring Boot API
    ↓
결제 문자 파싱
    ↓
Notion API
    ↓
Notion Database
```

<br />

## 💻 Tech Stack

- Java
- Spring Boot
- Notion API
- iOS Shortcuts

<br />

## 💥 Key Features

- 아이폰 단축어를 통한 결제 문자 전송
- 결제 금액 / 가맹점 / 결제수단 / 결제일시 파싱
- 결제 내역 카테고리 분류
- Notion Database 자동 저장
