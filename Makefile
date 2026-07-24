.PHONY: help env up down restart logs build \
	be-wrapper be-run be-build be-test be-lint be-clean \
	fe-install fe-run fe-build fe-test fe-lint fe-typecheck \
	clean

help:
	@echo "Common targets:"
	@echo "  make env          Create .env from .env.example if missing"
	@echo "  make up           Start postgres, backend, frontend via docker compose"
	@echo "  make down         Stop and remove containers"
	@echo "  make restart      Restart all services"
	@echo "  make logs         Tail logs for all services"
	@echo "  make build        Rebuild all docker images"
	@echo ""
	@echo "  make be-wrapper   Generate backend/gradlew (run once, requires local Gradle)"
	@echo "  make be-run       Run the backend locally (needs local Postgres, see .env)"
	@echo "  make be-build     Build the backend jar"
	@echo "  make be-test      Run backend tests"
	@echo "  make be-lint      Run backend static checks (ktlint via Gradle)"
	@echo ""
	@echo "  make fe-install   Install frontend dependencies"
	@echo "  make fe-run       Run the frontend dev server locally"
	@echo "  make fe-build     Build the frontend for production"
	@echo "  make fe-test      Run frontend tests"
	@echo "  make fe-lint      Run frontend eslint"
	@echo "  make fe-typecheck Run frontend TypeScript type checking"
	@echo ""
	@echo "  make clean        Remove build outputs and docker volumes"

env:
	@test -f .env || (cp .env.example .env && echo "Created .env from .env.example")

up: env
	docker compose up --build

down:
	docker compose down

restart: down up

logs:
	docker compose logs -f

build:
	docker compose build

# --- Backend (Gradle/Kotlin) ---

be-wrapper:
	cd backend && gradle wrapper --gradle-version 8.10.2

be-run:
	cd backend && ./gradlew bootRun

be-build:
	cd backend && ./gradlew bootJar

be-test:
	cd backend && ./gradlew test

be-lint:
	cd backend && ./gradlew ktlintCheck

be-clean:
	cd backend && ./gradlew clean

# --- Frontend (React/TS/Vite) ---

fe-install:
	cd frontend && npm install

fe-run:
	cd frontend && npm run dev

fe-build:
	cd frontend && npm run build

fe-test:
	cd frontend && npm run test

fe-lint:
	cd frontend && npm run lint

fe-typecheck:
	cd frontend && npm run typecheck

clean: be-clean
	rm -rf frontend/dist frontend/node_modules
	docker compose down -v
