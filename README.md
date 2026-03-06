# Legends of Sword and Wand — Deliverable 1

A distributed dungeon RPG implemented as Spring Boot microservices.

---

## Architecture Overview

| Service          | Port | Responsibility                              |
|------------------|------|---------------------------------------------|
| profile-service  | 5000 | User registration, login, profile data      |
| battle-service   | 5001 | Stateless turn-based combat engine          |
| pve-service      | 5002 | Campaign flow, room generation, scoring     |
| data-service     | 5003 | Campaign persistence (GameSaveDAO)          |
| MySQL            | 3306 | Centralised relational database             |

---

## Prerequisites

| Tool          | Version  |
|---------------|----------|
| Java (JDK)    | 17+      |
| Maven         | 3.9+     |
| Docker        | 24+      |
| Docker Compose| 2.20+    |
| MySQL         | 8.x (only needed for local dev without Docker) |

---

## Quick Start — Docker (Recommended)

```bash
# 1. Clone the repository
git clone https://github.com/<your-org>/legends-of-sword-and-wand.git
cd legends-of-sword-and-wand

# 2. (Optional) Override DB password
export DB_PASS=mysecurepassword

# 3. Build and start all services
docker compose up --build

# 4. Verify services are running
curl http://localhost:5000/api/profile/health   # Profile
curl http://localhost:5001/api/battle/health    # Battle
curl http://localhost:5002/api/pve/health       # PvE
curl http://localhost:5003/api/data/health      # Data

# 5. Stop everything
docker compose down -v
```

---

## Local Development (No Docker)

### 1. Start MySQL

```bash
mysql -u root -p
CREATE DATABASE legends_db;
CREATE USER 'legends_user'@'localhost' IDENTIFIED BY 'legendspass';
GRANT ALL PRIVILEGES ON legends_db.* TO 'legends_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Run each service individually

```bash
# Profile Service
cd profile-service
mvn spring-boot:run

# Battle Service (new terminal)
cd battle-service
mvn spring-boot:run

# Data Service (new terminal)
cd data-service
mvn spring-boot:run

# PvE Service (new terminal)
cd pve-service
mvn spring-boot:run
```

---

## Running Tests

```bash
# Run all tests for a service
cd profile-service && mvn test
cd battle-service  && mvn test
cd pve-service     && mvn test
cd data-service    && mvn test
```

Profile, Data services use **H2 in-memory** for tests — no MySQL required.
Battle and PvE services are pure unit tests with no database dependency.

---

## API Reference

### Profile Service — `http://localhost:5000`

| Method | Endpoint                        | Body / Params                              | Description                  |
|--------|---------------------------------|--------------------------------------------|------------------------------|
| POST   | `/api/profile/register`         | `{"username":"...","password":"..."}`      | Register new user            |
| POST   | `/api/profile/login`            | `{"username":"...","password":"..."}`      | Login; returns profile data  |
| GET    | `/api/profile/{userId}`         | —                                          | Get profile for dashboard    |
| PATCH  | `/api/profile/{userId}/stats`   | `?newScore=500&ranking=3`                  | Update score/ranking         |

### Battle Service — `http://localhost:5001`

| Method | Endpoint                              | Body / Params                              | Description              |
|--------|---------------------------------------|--------------------------------------------|--------------------------|
| POST   | `/api/battle/{battleId}/start`        | `{"playerParty":[...],"enemyParty":[...]}`  | Start battle session     |
| POST   | `/api/battle/{battleId}/action`       | `?action=ATTACK`                           | Execute one turn action  |

**Actions:** `ATTACK`, `DEFEND`, `WAIT`, `CAST`

### PvE Service — `http://localhost:5002`

| Method | Endpoint                        | Body / Params                    | Description                     |
|--------|---------------------------------|----------------------------------|---------------------------------|
| POST   | `/api/pve/{userId}/start`       | `[{"name":"...","heroClass":"WARRIOR"}, ...]` | Start new campaign |
| POST   | `/api/pve/{userId}/next-room`   | —                                | Advance to next room            |
| GET    | `/api/pve/{userId}/campaign`    | —                                | Get current campaign state      |
| POST   | `/api/pve/{userId}/restore`     | `{"currentRoom":5,"gold":300,"heroes":[...]}` | Restore saved campaign |
| GET    | `/api/pve/{userId}/score`       | —                                | Get final score                 |
| POST   | `/api/pve/{userId}/end`         | —                                | End campaign session            |

### Data Service — `http://localhost:5003`

| Method | Endpoint                              | Body / Params       | Description                     |
|--------|---------------------------------------|---------------------|---------------------------------|
| POST   | `/api/data/campaign/save`             | SaveRequest JSON    | Save campaign progress (UC5)    |
| GET    | `/api/data/campaign/{userId}`         | —                   | Load campaign state (UC6)       |
| PATCH  | `/api/data/campaign/{userId}/complete`| —                   | Mark campaign as completed      |
| DELETE | `/api/data/party/{partyId}`           | —                   | Delete a saved party            |
| GET    | `/api/data/parties/{userId}`          | —                   | List all saved parties          |

---

## CI/CD Pipeline

The `.github/workflows/ci-cd.yml` pipeline:

1. **On every push / PR** — runs unit tests for all 4 services in parallel
2. **On push to `main`** — builds and pushes Docker images to GitHub Container Registry (GHCR)
3. **After successful build** — runs an integration smoke test with `docker compose up`

### Environment secrets required in GitHub:
- `GITHUB_TOKEN` — automatically provided by GitHub Actions (no setup needed)
- Optionally add `DB_PASS` as a repository secret for production deployments

---

## Project Structure

```
legends-of-sword-and-wand/
├── docker-compose.yml
├── .github/
│   └── workflows/
│       └── ci-cd.yml
├── profile-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/legends/profile/
│       │   ├── ProfileServiceApplication.java
│       │   ├── controller/ProfileController.java
│       │   ├── service/AccountManager.java
│       │   ├── repository/UserRepository.java
│       │   ├── model/UserProfile.java
│       │   ├── dto/{AuthRequest,AuthResponse}.java
│       │   └── security/SecurityConfig.java
│       └── test/java/com/legends/profile/ProfileServiceTests.java
├── battle-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/legends/battle/
│       │   ├── BattleServiceApplication.java
│       │   ├── controller/BattleController.java
│       │   ├── service/{Battle,BattleService}.java
│       │   ├── model/{Unit,Hero,Enemy,Action}.java
│       │   └── dto/{BattleRequest,BattleResponse,UnitDTO}.java
│       └── test/java/com/legends/battle/BattleServiceTests.java
├── pve-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/legends/pve/
│       │   ├── PveServiceApplication.java
│       │   ├── controller/PveCampaignController.java
│       │   ├── service/{PveController,RoomFactory}.java
│       │   ├── model/{Hero,Enemy,Party,Campaign,Room,BattleRoom,InnRoom}.java
│       │   └── dto/{HeroRequest,CampaignResponse,SavedStateRequest}.java
│       └── test/java/com/legends/pve/PveServiceTests.java
└── data-service/
    ├── Dockerfile
    ├── pom.xml
    └── src/
        ├── main/java/com/legends/data/
        │   ├── DataServiceApplication.java
        │   ├── controller/DataController.java
        │   ├── service/GameSaveDAO.java
        │   ├── repository/PartyRepository.java
        │   ├── model/{Party,HeroEntity}.java
        │   └── dto/{HeroDTO,SaveRequest,CampaignState}.java
        └── test/java/com/legends/data/DataServiceTests.java
```
