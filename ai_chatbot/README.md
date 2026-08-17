# AI Chatbot local development

This service runs on port `8083` and calls the RewardProject reward service on
port `8080`. It is now a module inside RewardProject, so the normal root scripts
build and start it together with the other services:

```bash
cd RewardProject
cp .env.local.example .env.local
# Put an OpenAI project API key in the git-ignored .env.local.
./scripts/local-infra.sh start
./scripts/start-all.sh
```

For a chatbot-only process, `./ai_chatbot/scripts/start-local.sh` loads the root
`.env`, root `.env.local`, and then the legacy `ai_chatbot/.env` override.

The endpoint requires a JWT issued by the music service. After logging in, an
authenticated request has this shape (do not paste a real token into a tracked
file or shell history on a shared machine):

```bash
curl -X POST "http://localhost:8083/chatbot/chat" \
  -H "Authorization: Bearer $TOKEN" \
  --data-urlencode "sessionId=local-test" \
  --data-urlencode "userInput=查询我最近一个月的奖励记录"
```

The current agent flow is still under development: reward-record lookup is
partially wired, while play-record, total-duration, and general-chat branches
are placeholders. The local environment is intended to support that work
without requiring AWS or a deployed stack.
