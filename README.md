# 🌐 Internet-Based Fake News Detection System

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-007396?style=for-the-badge&logo=java&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)

A JavaFX desktop application that verifies the credibility of news articles in real time by cross-referencing multiple independent internet sources — combining domain reputation databases, live search APIs, and content analysis into a single, transparent verdict.

---

## 📖 Overview

Misinformation spreads faster than fact-checkers can keep up with. This project explores a practical, multi-source approach to verification: instead of trusting one signal, it runs four independent checks in parallel and combines them into a weighted, explainable result.

## 🔍 How It Works

The system runs **4 checks concurrently** (via multithreading, so the interface stays responsive):

| # | Check | Purpose |
|---|-------|---------|
| 1 | **Domain Reputation** | Matches the source against a curated list of 100+ trusted outlets and known misinformation domains |
| 2 | **DuckDuckGo Instant Answer API** | Confirms whether the headline appears in real search results |
| 3 | **Wikipedia Search API** | Verifies the source/topic is a notable, real-world entity |
| 4 | **Content Analysis** | Flags sensationalist patterns (excessive capitalization, exclamation-mark abuse) |

Each check is weighted (domain reputation weighted highest) and combined into a final scored report — **REAL**, **FAKE**, or **SUSPICIOUS** — with a 10-minute result cache to reduce redundant API calls.

## 🏗️ Architecture & Data Structures

Built with core computer science fundamentals rather than relying solely on libraries:

- **Binary Search Tree** — indexes articles by source for efficient lookup
- **Custom Linked List** — tracks each user's submission history
- **HashMap** — fast fake-source lookup with auto-flagging after repeated reports
- **CompletableFuture + JavaFX Service/Task** — non-blocking, concurrent API verification

## ✨ Features

- 🔐 Role-based authentication (User / Admin)
- 📝 Article submission with real-time verification
- 📊 Live analytics dashboard (verification counts, flagged sources, API status)
- 🕒 Full verification audit log with timestamps

## 🛠️ Tech Stack

- **Language:** Java
- **UI Framework:** JavaFX with custom CSS styling
- **External APIs:** DuckDuckGo Instant Answer API, Wikipedia Search API
- **Concurrency:** CompletableFuture, JavaFX Service/Task
- **IDE:** Eclipse with e(fx)clipse

## 📸 Screenshots

| Login Screen | Dashboard | Verification Report |
|---|---|---|
| _add screenshot_ | _add screenshot_ | _add screenshot_ |

## 🚀 Getting Started

### Prerequisites
- JDK 8 or higher
- JavaFX SDK (required separately if using JDK 11+)
- Eclipse IDE with the e(fx)clipse plugin (recommended)

### Installation
```bash
git clone https://github.com/YOUR_USERNAME/FakeNewsDetectionSystem.git
```
1. Open Eclipse → `File → Import → Existing Projects into Workspace`
2. Select the cloned folder
3. Configure the JavaFX JRE in the build path if prompted
4. Run `Main.java`

### Default Credentials
| Role | ID | Password |
|------|----|---|
| User | 1 | user123 |
| Admin | 1 | admin123 |

## 📌 Roadmap
- [ ] Integrate additional fact-checking APIs (e.g., Google Fact Check Tools)
- [ ] Persist data using a database instead of in-memory storage
- [ ] Add ML/NLP-based sentiment analysis for deeper content scoring

## 🤝 Contributing
Contributions, issues, and feature requests are welcome. Feel free to check the [issues page](../../issues).

## 📄 License
This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

## 🙋 Author
**Pardeep kumar**
[LinkedIn](your-linkedin-url) · [GitHub](your-github-url)
