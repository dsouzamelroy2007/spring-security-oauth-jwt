.PHONY: up down logs test

RUN_DIR := .run

# Boots postgres + redis (docker compose), then builds and launches the three
# app modules as local background JVM processes -- not containers. Their
# issuer/audience configuration is baked to localhost (see
# integration-tests/.../CrossModuleTestSupport's own doc comment on the same
# constraint), so a real container network would need its own host/container/
# browser addressing story; local processes sidestep that entirely for a
# one-command dev run. authorization-server must be healthy before the other
# two start: both bff-client (OIDC client registration) and resource-server
# (JWKS/issuer validation) resolve it eagerly at startup, not lazily.
#
# Each target's recipe is one continued shell line (trailing "\"), not one
# shell invocation per line -- GNU Make's ".ONESHELL:" (3.82+) isn't
# supported by the GNU Make 3.81 macOS still ships, and this needs the same
# shell across lines both to capture "$$!" right after the "nohup ... &"
# that set it, and for the "for"/"if" in `down` to parse as one script.
up: ; \
	set -e; \
	docker compose up -d; \
	echo "Waiting for postgres..."; \
	until docker compose exec -T postgres pg_isready -U expensetracker -d expensetracker > /dev/null 2>&1; do sleep 1; done; \
	echo "Waiting for redis..."; \
	until docker compose exec -T redis redis-cli ping > /dev/null 2>&1; do sleep 1; done; \
	mvn -q -DskipTests package; \
	mkdir -p $(RUN_DIR); \
	echo "Starting authorization-server (:9000)..."; \
	nohup java -jar authorization-server/target/authorization-server-*-exec.jar > $(RUN_DIR)/authorization-server.log 2>&1 & echo $$! > $(RUN_DIR)/authorization-server.pid; \
	until curl -sf http://localhost:9000/actuator/health > /dev/null 2>&1; do sleep 1; done; \
	echo "Starting resource-server (:8082) and bff-client (:8080)..."; \
	nohup java -jar resource-server/target/resource-server-*-exec.jar > $(RUN_DIR)/resource-server.log 2>&1 & echo $$! > $(RUN_DIR)/resource-server.pid; \
	nohup java -jar bff-client/target/bff-client-*-exec.jar > $(RUN_DIR)/bff-client.log 2>&1 & echo $$! > $(RUN_DIR)/bff-client.pid; \
	until curl -sf http://localhost:8082/actuator/health > /dev/null 2>&1; do sleep 1; done; \
	until curl -sf -u actuator:actuator-demo-password http://localhost:8090/actuator/health > /dev/null 2>&1; do sleep 1; done; \
	echo "Expense Tracker is up: authorization-server :9000, resource-server :8082, bff-client :8080 (SPA at /), bff-client actuator :8090"; \
	echo "Try: curl http://localhost:8080/whoami   (or open http://localhost:8080/ in a browser)"; \
	echo "Logs: $(RUN_DIR)/*.log   Stop: make down"

down: ; \
	set -e; \
	for name in bff-client resource-server authorization-server; do \
		if [ -f $(RUN_DIR)/$$name.pid ]; then \
			kill $$(cat $(RUN_DIR)/$$name.pid) 2>/dev/null || true; \
			rm -f $(RUN_DIR)/$$name.pid; \
		fi; \
	done; \
	docker compose down

logs:
	docker compose logs -f

test:
	mvn -q verify
