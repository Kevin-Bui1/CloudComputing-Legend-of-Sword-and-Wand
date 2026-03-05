# Legends of Sword and Wand - Deliverable 1 (Microservices)

This repo contains a minimal Spring Boot microservice implementation aligned with the SDD diagrams:
- profile-service (5000)
- battle-service (5001)
- pve-service (5002)
- data-service (5003)
- pvp-service (5004)

## Quick start (local)
1) Start MySQL and load schema:
- Create database `legends`
- Run `sql/schema.sql`

2) Start services (in separate terminals):
- `mvn -pl services/data-service spring-boot:run`
- `mvn -pl services/profile-service spring-boot:run`
- `mvn -pl services/battle-service spring-boot:run`
- `mvn -pl services/pve-service spring-boot:run`
- `mvn -pl services/pvp-service spring-boot:run`

## Database config
Set env vars (or use defaults):
- DB_URL (default: jdbc:mysql://localhost:3306/legends)
- DB_USER (default: root)
- DB_PASSWORD (default: password)

## Health checks
- http://localhost:5000/health
- http://localhost:5001/health
- http://localhost:5002/health
- http://localhost:5003/health
- http://localhost:5004/health
