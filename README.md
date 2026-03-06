# Legends of Sword and Wand 

A distributed dungeon RPG built as 5 independent Spring Boot microservices.
Each service runs in its own Docker container and communicates through REST APIs.

---

## How the system is structured

Instead of building one big application, we split the game into services that each do one thing:

| Service         | Port | What it does                                    |
|-----------------|------|-------------------------------------------------|
| profile-service | 5000 | User registration, login, and profile data      |
| battle-service  | 5001 | Runs turn-based combat (stateless per battle)   |
| pve-service     | 5002 | Campaign flow, room generation, scoring         |
| data-service    | 5003 | Saves and loads campaign progress to MySQL      |
| pvp-service     | 5004 | PvP invitations and match result recording      |
| MySQL           | 3306 | Central database (used by profile + data)       |

---

## What you need installed

| Tool           | Version |
|----------------|---------|
| Java (JDK)     | 17      |
| Maven          | 3.9+    |
| Docker Desktop | 24+     |
| Docker Compose | 2.20+   |

> **Note:** If you're running with Docker, you don't need Java or Maven installed locally — the Docker build handles compilation inside the container.

---

## Running everything with Docker (recommended)

This is the easiest way. One command starts all 5 services and the database.

```bash
# 1. Clone the repo
git clone https://github.com/<your-org>/legends-of-sword-and-wand.git
cd legends-of-sword-and-wand

# 2. Start everything (first run will take a few minutes to download images)
docker compose up --build

# 3. Check that all services came up — each should return {"status":"ok"}
curl http://localhost:5000/api/profile/health
curl http://localhost:5001/api/battle/health
curl http://localhost:5002/api/pve/health
curl http://localhost:5003/api/data/health
curl http://localhost:5004/api/pvp/health

# 4. Shut everything down and remove volumes
docker compose down -v
```

---

## Running a single service locally (for development)

If you want to run just one service without Docker, you need MySQL running first.

### Step 1 — Set up MySQL

```bash
mysql -u root -p
CREATE DATABASE legends_db;
CREATE USER 'legends_user'@'localhost' IDENTIFIED BY 'legendspass';
GRANT ALL PRIVILEGES ON legends_db.* TO 'legends_user'@'localhost';
FLUSH PRIVILEGES;
```

### Step 2 — Start the service

```bash
cd profile-service
mvn spring-boot:run
```

Repeat in separate terminals for each service you need. The data-service and profile-service need MySQL; battle-service and pve-service are self-contained.

---

## Running the tests

```bash
cd profile-service && mvn test
cd battle-service  && mvn test
cd pve-service     && mvn test
cd data-service    && mvn test
cd pvp-service     && mvn test
```

- **profile-service** and **data-service** tests use an H2 in-memory database — no MySQL needed
- **battle-service** and **pve-service** tests are plain unit tests with no database at all
- **pvp-service** tests use MockMvc to test the HTTP layer directly

---

## API Reference

All requests and responses use `Content-Type: application/json`.

---

### Profile Service — `http://localhost:5000`

| Method | Endpoint                       | Request                                 | Description                  |
|--------|--------------------------------|-----------------------------------------|------------------------------|
| POST   | `/api/profile/register`        | `{"username":"...","password":"..."}`   | Create a new account         |
| POST   | `/api/profile/login`           | `{"username":"...","password":"..."}`   | Log in, returns profile data |
| GET    | `/api/profile/{userId}`        | —                                       | Get profile for dashboard    |
| PATCH  | `/api/profile/{userId}/stats`  | `?newScore=500&ranking=3`               | Update score and ranking     |
| GET    | `/api/profile/health`          | —                                       | Health check                 |

**Register / Login response example:**
```json
{
  "success": true,
  "userId": 1,
  "username": "heroPlayer",
  "scores": 0,
  "rankings": 0,
  "campaignProgress": 0
}
```

---

### Battle Service — `http://localhost:5001`

| Method | Endpoint                          | Request                                     | Description                   |
|--------|-----------------------------------|---------------------------------------------|-------------------------------|
| POST   | `/api/battle/{battleId}/start`    | `{"playerParty":[...],"enemyParty":[...]}`  | Start a new battle session    |
| POST   | `/api/battle/{battleId}/action`   | `?action=ATTACK`                            | Execute one action this turn  |
| GET    | `/api/battle/health`              | —                                           | Health check                  |

**Available actions:** `ATTACK` `DEFEND` `WAIT` `CAST`

The `battleId` is a string you choose (e.g. `"user1_battle"`). It links follow-up action requests to the right battle session.

**Action response example:**
```json
{
  "actionResult": "Arthur attacks Goblin for 12 damage. Goblin HP: 38/50",
  "playerParty": [{"name":"Arthur","hp":100,"alive":true}],
  "enemyParty":  [{"name":"Goblin","hp":38,"alive":true}],
  "battleOver": false,
  "winner": "Battle not over."
}
```

---

### PvE Service — `http://localhost:5002`

| Method | Endpoint                       | Request                                            | Description                          |
|--------|--------------------------------|----------------------------------------------------|--------------------------------------|
| POST   | `/api/pve/{userId}/start`      | `[{"name":"Arthur","heroClass":"WARRIOR"}, ...]`  | Start a new campaign                 |
| POST   | `/api/pve/{userId}/next-room`  | —                                                  | Advance to the next room             |
| GET    | `/api/pve/{userId}/campaign`   | —                                                  | Get current campaign state           |
| POST   | `/api/pve/{userId}/restore`    | `{"currentRoom":5,"gold":300,"heroes":[...]}`     | Restore a previously saved campaign  |
| GET    | `/api/pve/{userId}/score`      | —                                                  | Calculate and return final score     |
| POST   | `/api/pve/{userId}/end`        | —                                                  | End and clear the campaign session   |
| GET    | `/api/pve/health`              | —                                                  | Health check                         |

**Hero classes:** `WARRIOR` `ORDER` `CHAOS` `MAGE`

**next-room response (battle room) example:**
```json
{
  "success": true,
  "currentRoom": 1,
  "roomType": "BATTLE",
  "message": "A battle room! You encounter: Goblin 1 (Lvl 1) Orc 2 (Lvl 2)",
  "enemies": [...],
  "expReward": 150,
  "goldReward": 225
}
```

---

### Data Service — `http://localhost:5003`

| Method | Endpoint                               | Request          | Description                              |
|--------|----------------------------------------|------------------|------------------------------------------|
| POST   | `/api/data/campaign/save`              | SaveRequest JSON | Save campaign progress (UC5 — Exit)      |
| GET    | `/api/data/campaign/{userId}`          | —                | Load active campaign (UC6 — Continue)    |
| PATCH  | `/api/data/campaign/{userId}/complete` | —                | Mark campaign as completed (room 30)     |
| DELETE | `/api/data/party/{partyId}`            | —                | Delete a saved party                     |
| GET    | `/api/data/parties/{userId}`           | —                | List all saved parties for a user        |
| GET    | `/api/data/health`                     | —                | Health check                             |

**SaveRequest body example:**
```json
{
  "userId": 1,
  "partyName": "The Round Table",
  "currentRoom": 12,
  "gold": 500,
  "heroes": [
    {
      "name": "Arthur", "heroClass": "WARRIOR", "level": 5,
      "attack": 20, "defense": 10, "hp": 80, "maxHp": 100,
      "mana": 40, "maxMana": 80, "experience": 1200
    }
  ]
}
```

---

### PvP Service — `http://localhost:5004`

| Method | Endpoint          | Request                                        | Description                              |
|--------|-------------------|------------------------------------------------|------------------------------------------|
| POST   | `/api/pvp/invite` | `{"fromUserId":1,"toUsername":"player2"}`      | Send a PvP invitation to another player  |
| POST   | `/api/pvp/accept` | `{"inviteId":1,"toUserId":2}`                  | Accept a pending invitation              |
| POST   | `/api/pvp/result` | `{"winnerUserId":1,"loserUserId":2}`           | Record the outcome of a completed match  |
| GET    | `/api/pvp/health` | —                                              | Health check                             |

**Invite response:**
```json
{
  "inviteId": 1,
  "status": "PENDING"
}
```

**Accept response:**
```json
{
  "status": "ACCEPTED"
}
```

**Accept error (invite not found) — 400 Bad Request:**
```json
{
  "error": "Invite not found"
}
```

**Result response:**
```json
{
  "status": "RECORDED",
  "winnerUserId": 1,
  "loserUserId": 2
}
```

> **Note:** For Deliverable 1, the PvP service handles the invitation flow only. Full integration with party selection and live battle execution is planned for Deliverable 2.

---

## CI/CD Pipeline

The `.github/workflows/ci-cd.yml` pipeline has three stages:

1. **On every push or pull request to `main` / `develop`** — runs unit tests for all 5 services in parallel
2. **On push to `main` only** — builds Docker images and pushes them to GitHub Container Registry (GHCR)
3. **After a successful build** — spins up the full system with `docker compose` and hits every health endpoint to confirm everything starts correctly

The only secret you need is `GITHUB_TOKEN`, which GitHub provides automatically — no setup required.

---

## Project Structure

```
legends-of-sword-and-wand/
├── docker-compose.yml
├── .github/workflows/ci-cd.yml
│
├── profile-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/legends/profile/
│       ├── controller/  ProfileController.java, HealthController.java
│       ├── service/     AccountManager.java
│       ├── repository/  UserRepository.java
│       ├── model/       UserProfile.java
│       ├── dto/         AuthRequest.java, AuthResponse.java
│       └── security/    SecurityConfig.java
│
├── battle-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/legends/battle/
│       ├── controller/  BattleController.java, HealthController.java
│       ├── service/     Battle.java, BattleService.java
│       ├── model/       Unit.java, Hero.java, Enemy.java, Action.java
│       └── dto/         BattleRequest.java, BattleResponse.java, UnitDTO.java
│
├── pve-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/legends/pve/
│       ├── controller/  PveCampaignController.java, HealthController.java
│       ├── service/     PveController.java, RoomFactory.java
│       ├── model/       Hero.java, Enemy.java, Party.java, Campaign.java
│       │               Room.java, BattleRoom.java, InnRoom.java
│       └── dto/         HeroRequest.java, CampaignResponse.java, SavedStateRequest.java
│
├── data-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/legends/data/
│       ├── controller/  DataController.java, HealthController.java
│       ├── service/     GameSaveDAO.java
│       ├── repository/  PartyRepository.java
│       ├── model/       Party.java, HeroEntity.java
│       └── dto/         HeroDTO.java, SaveRequest.java, CampaignState.java
│
└── pvp-service/
    ├── Dockerfile
    ├── pom.xml
    └── src/main/java/com/legends/pvp/
        ├── controller/  PvpController.java, HealthController.java
        ├── service/     PvpService.java
        └── dto/         InviteRequest.java, AcceptInviteRequest.java, ResultRequest.java
```
