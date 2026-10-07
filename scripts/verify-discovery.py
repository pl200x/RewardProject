#!/usr/bin/env python3
"""Real registry and business services. Run through verify-discovery.sh for isolated storage."""
import base64
import concurrent.futures
import datetime
import hashlib
import hmac
import json
import os
from pathlib import Path
import socket
import subprocess
import threading
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode
from urllib.request import Request, urlopen

ROOT = Path(__file__).resolve().parent.parent
LOGS = ROOT / "target" / "discovery-smoke"
LOGS.mkdir(parents=True, exist_ok=True)
PROCESSES = {}
HANDLES = []
STARTED_NAMES = set()
PORTS = set()
RESULTS = {}


def port():
    while True:
        with socket.socket() as sock:
            sock.bind(("127.0.0.1", 0))
            value = sock.getsockname()[1]
        if value not in PORTS:
            PORTS.add(value)
            return value


def request(base, path, body=None, method=None, headers=None):
    headers = dict(headers or {})
    headers.setdefault("Accept", "application/json")
    if isinstance(body, dict):
        body = json.dumps(body).encode()
        headers.setdefault("Content-Type", "application/json")
    req = Request(base + path, data=body, method=method, headers=headers)
    try:
        with urlopen(req, timeout=10) as response:
            raw = response.read()
            return response.status, json.loads(raw) if raw else None, response.headers
    except HTTPError as error:
        raw = error.read()
        return error.code, json.loads(raw) if raw else None, error.headers


def await_condition(condition, label, timeout=70):
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        try:
            if condition():
                return
        except (OSError, URLError, ValueError):
            pass
        for name, process in PROCESSES.items():
            if process.poll() is not None:
                raise AssertionError(f"{name} exited; see {LOGS / (name + '.log')}")
        time.sleep(.3)
    raise AssertionError("Timed out: " + label)


def start(name, module, number, extra=()):
    handle = (LOGS / f"{name}.log").open("a" if name in STARTED_NAMES else "w")
    STARTED_NAMES.add(name)
    HANDLES.append(handle)
    args = ["java", "-Xmx192m", "-XX:ActiveProcessorCount=2",
            f"-Dlog.path={LOGS / name}", f"-DLOG_HOME={LOGS / name}", "-jar",
            str(ROOT / module / "target" / f"{module}-0.0.1-SNAPSHOT.jar"),
            f"--server.port={number}", *extra]
    PROCESSES[name] = subprocess.Popen(args, cwd=ROOT, stdout=handle, stderr=subprocess.STDOUT)
    return f"http://127.0.0.1:{number}"


def stop(name):
    process = PROCESSES.pop(name, None)
    if process is not None:
        process.terminate()
        try:
            process.wait(timeout=12)
        except subprocess.TimeoutExpired:
            process.kill()
            process.wait(timeout=5)


def check(label, condition):
    assert condition, label
    RESULTS[label] = "VERIFIED"
    print("VERIFIED:", label, flush=True)


def successful(result):
    status, body, _ = result
    return status == 200 and body and body.get("baseVO", body).get("success") is True


def token(user_id):
    def encode(value):
        return base64.urlsafe_b64encode(json.dumps(value).encode()).rstrip(b"=")
    message = encode({"alg": "HS256", "typ": "JWT"}) + b"." + encode(
        {"sub": str(user_id), "iat": int(time.time()), "exp": int(time.time()) + 600})
    signature = hmac.new(os.environ["JWT_SECRET"].encode(), message, hashlib.sha256).digest()
    return (message + b"." + base64.urlsafe_b64encode(signature).rstrip(b"=")).decode()


class ModelFixture(BaseHTTPRequestHandler):
    """Exercise the real authenticated chatbot/tool path without calling an external API."""
    def do_POST(self):
        body = json.loads(self.rfile.read(int(self.headers["Content-Length"])))
        if any(m.get("role") == "tool" for m in body["messages"]):
            message = {"role": "assistant", "content": "Discovery verified"}
            reason = "stop"
        else:
            message = {"role": "assistant", "content": None, "tool_calls": [
                {"id": "tool-1", "type": "function", "function": {
                    "name": "getRewardConfigurations", "arguments": "{}"}}]}
            reason = "tool_calls"
        result = {"id": "test-completion", "object": "chat.completion", "created": int(time.time()),
                  "model": "test-model", "choices": [{"index": 0, "message": message, "finish_reason": reason}],
                  "usage": {"prompt_tokens": 1, "completion_tokens": 1, "total_tokens": 2}}
        data = json.dumps(result).encode()
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def log_message(self, *_):
        pass


def main():
    fixture = ThreadingHTTPServer(("127.0.0.1", 0), ModelFixture)
    threading.Thread(target=fixture.serve_forever, daemon=True).start()
    try:
        registry_port = port()
        registry_args = ["--eureka.server.use-read-only-response-cache=false",
                         "--eureka.server.enable-self-preservation=false"]
        registry = start("registry", "service-registry", registry_port, registry_args)
        await_condition(lambda: request(registry, "/actuator/health")[0] == 200, "registry health")
        common = [f"--eureka.client.service-url.defaultZone={registry}/eureka/",
                  "--eureka.client.registry-fetch-interval-seconds=1",
                  "--eureka.client.instance-info-replication-interval-seconds=1",
                  "--eureka.client.initial-instance-info-replication-interval-seconds=0",
                  "--eureka.instance.lease-renewal-interval-in-seconds=1",
                  "--eureka.instance.hostname=127.0.0.1",
                  "--eureka.instance.prefer-ip-address=false",
                  "--observability.instance-header-enabled=true",
                  "--logging.level.com.netflix.discovery=WARN"]
        bases = {}
        for name, module in [("prize-a", "PrizeCenter"), ("prize-b", "PrizeCenter"),
                             ("reward-a", "Multi_reward"), ("reward-b", "Multi_reward"),
                             ("music", "Music_management"), ("chatbot", "ai_chatbot")]:
            extra = [*common, f"--eureka.instance.instance-id={name}"]
            if name == "reward-a":
                extra += ["--reward.reconciliation.enabled=true", "--reward.reconciliation.cron=*/2 * * * * *"]
            if name == "chatbot":
                extra += [f"--spring.ai.openai.base-url=http://127.0.0.1:{fixture.server_port}"]
            bases[name] = start(name, module, port(), extra)
        for name, base in bases.items():
            await_condition(lambda b=base: request(b, "/actuator/health")[0] == 200, name + " health")

        def instances(service):
            status, body, _ = request(registry, "/eureka/apps/" + service.upper())
            if status != 200:
                return []
            values = body["application"]["instance"]
            return [v for v in (values if isinstance(values, list) else [values]) if v["status"] == "UP"]

        await_condition(lambda: len(instances("prize-service")) == 2 and len(instances("reward-service")) == 2
                        and len(instances("music-service")) == 1 and len(instances("chatbot-service")) == 1,
                        "all Boot 3/Boot 4 clients registered UP")
        check("All four business services register with distinct instance IDs", True)
        auth = {"Authorization": "Bearer " + token(1), "X-Request-ID": "smoke-playback"}
        check("Anonymous music write remains unauthorized", request(bases["music"], "/music/play", {})[0] == 401)
        check("Anonymous chatbot remains unauthorized", request(bases["chatbot"], "/chatbot/chat", b"")[0] == 401)
        now = datetime.datetime.now().replace(microsecond=0)
        def playback(index, duration=30):
            result = request(bases["music"], "/music/play", {
                "userId": 1, "musicId": 1, "duration": duration,
                "syncTime": (now + datetime.timedelta(seconds=index)).strftime("%Y-%m-%d %H:%M:%S"),
                "scene": "Listening_Music", "code": "coin"}, headers=auth)
            assert successful(result), result

        for index in range(12):
            playback(index)
        check("Authenticated Music -> Reward -> Prize business flow", True)

        def chat(index):
            body = urlencode({"sessionId": "discovery-test-" + str(index), "userInput": "Explain reward rules"}).encode()
            result = request(bases["chatbot"], "/chatbot/chat", body, headers={
                **auth, "Content-Type": "application/x-www-form-urlencoded"})
            assert successful(result), result

        for index in range(4):
            chat(index)
        check("Authenticated Boot 4 Chatbot -> Boot 3 Reward tool calls", True)
        for name in ("reward-a", "reward-b"):
            text = (LOGS / f"{name}.log").read_text()
            check(name + " handles discovered playback and configuration requests",
                  "path=/sync" in text and "path=/config/searchall" in text)

        def request_count(name, path):
            return sum("event=HTTP_REQUEST_COMPLETE " in line and f"path={path} " in line
                       for line in (LOGS / f"{name}.log").read_text().splitlines())

        # Both callers have now used both instances; measure after discovery has converged.
        sync_before = [request_count(name, "/sync") for name in ("reward-a", "reward-b")]
        config_before = [request_count(name, "/config/searchall") for name in ("reward-a", "reward-b")]
        for index in range(12, 24):
            playback(index, duration=0)
        for index in range(4, 8):
            chat(index)
        check("Music round robin distributes 12 playback requests 6/6",
              [request_count(name, "/sync") - before
               for name, before in zip(("reward-a", "reward-b"), sync_before)] == [6, 6])
        check("Chatbot round robin distributes 4 configuration requests 2/2",
              [request_count(name, "/config/searchall") - before
               for name, before in zip(("reward-a", "reward-b"), config_before)] == [2, 2])
        for caller, targets in [("music", ("reward-a", "reward-b")), ("chatbot", ("reward-a", "reward-b")),
                                ("reward-a", ("prize-a", "prize-b"))]:
            def logged_targets(c=caller, ids=targets):
                lines = (LOGS / f"{c}.log").read_text().splitlines()
                return all(any("event=LOAD_BALANCER_SELECTED " in line and f"targetInstance={i} " in line
                               and "requestId=" in line for line in lines) for i in ids)
            await_condition(logged_targets, caller + " selected-instance logs")
            check(caller + " logs both selected downstream instances", True)

        # Round robin applies across all paths to one service, so count and list may alternate targets.
        await_condition(lambda: all("path=/prize_reward/" in (LOGS / f"{n}.log").read_text()
                                    for n in ("prize-a", "prize-b")), "read-only reconciliation on both instances")
        check("Both real Prize instances handle discovered reconciliation", True)

        # Keep the existing idempotency contract when an uncertain delivery is replayed.
        delivery = {"code": "gem", "amount": 1, "outbizno": "discovery-smoke-replay"}
        assert successful(request(bases["prize-a"], "/prize/send_reward", delivery))
        check("Prize replay across instances returns success", successful(
            request(bases["prize-b"], "/prize/send_reward", delivery)))

        assert successful(request(bases["prize-a"], "/prize/add", {
            "code": "smoke-stock", "name": "test", "description": "isolated stock test", "storage": 100}))

        def inventory():
            result = request(bases["prize-a"], "/prize/getallprize")
            assert successful(result), result
            return next(p["storage"] for p in result[1]["prizeVOList"] if p["code"] == "smoke-stock")

        before = inventory()
        def award(index):
            result = request(bases["prize-a" if index % 2 == 0 else "prize-b"],
                             "/prize/send_reward", {"code": "smoke-stock", "amount": 1,
                             "outbizno": "concurrent-smoke-" + str(index)})
            assert successful(result), result
            return True
        with concurrent.futures.ThreadPoolExecutor(max_workers=8) as workers:
            check("Concurrent awards across both Prize JVMs all succeed", all(workers.map(award, range(32))))
        check("Concurrent awards decrement inventory exactly once each", inventory() == before - 32)

        stop("prize-b")
        await_condition(lambda: len(instances("prize-service")) == 1, "graceful deregistration")
        check("Graceful shutdown removes only the stopped instance", True)
        stop("registry")
        result = request(bases["music"], "/music/play", {
            "userId": 1, "musicId": 1, "duration": 30,
            "syncTime": (now + datetime.timedelta(seconds=50)).strftime("%Y-%m-%d %H:%M:%S"),
            "scene": "Listening_Music", "code": "coin"}, headers=auth)
        check("Warm callers continue during registry outage", successful(result))
        registry = start("registry", "service-registry", registry_port, registry_args)
        await_condition(lambda: len(instances("reward-service")) == 2, "registry recovery and re-registration")
        check("Registry restart recovers client registration", True)

        # A moved instance advertises its new port; callers retain their logical URLs.
        relocated_port = port()
        bases["prize-b"] = start("prize-b", "PrizeCenter", relocated_port,
                                 [*common, "--eureka.instance.instance-id=prize-b"])
        await_condition(lambda: any(i["instanceId"] == "prize-b" and i["port"]["$"] == relocated_port
                                    for i in instances("prize-service")), "relocated instance registered")
        await_condition(lambda: "path=/prize_reward/" in (LOGS / "prize-b.log").read_text().rsplit(
            "Starting PrizeCenterApplication", 1)[-1], "caller discovers relocated instance")
        check("A relocated Prize port is discovered without caller configuration changes", True)
    finally:
        for name in list(PROCESSES):
            if name != "registry":
                stop(name)
        stop("registry")
        fixture.shutdown()
        fixture.server_close()
        for handle in HANDLES:
            handle.close()
        (LOGS / "results.json").write_text(json.dumps(RESULTS, indent=2) + "\n")


if __name__ == "__main__":
    main()
