.PHONY: up down logs test

up:
	docker compose up -d

down:
	docker compose down

logs:
	docker compose logs -f

test:
	mvn -q verify
