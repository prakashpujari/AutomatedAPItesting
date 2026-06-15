# Automated API Testing Platform

A LangGraph-based multi-agent automated API testing platform built with Spring Boot (backend) and React/Vite (frontend).

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          API Testing Platform                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐                 │
│  │   Frontend   │    │   Backend    │    │   External   │                 │
│  │  (React/Vite)│    │ (Spring Boot)│    │    LLM API   │                 │
│  │  :3000       │    │   :8081      │    │  (Groq/OpenAI)│                │
│  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘                 │
│         │                   │                   │                          │
│         │ POST /api/        │                   │                          │
│         │ orchestrate       │                   │                          │
│         │                   │                   │                          │
│         │                   ▼                   │                          │
│         │         ┌─────────────────┐           │                          │
│         │         │  Orchestrator   │           │                          │
│         │         │  (LangGraph)    │           │                          │
│         │         └─────┬─────────┬─┘           │                          │
│         │               │         │               │                          │
│         │               ▼         ▼               │                          │
│         │       ┌───────────┐  ┌───────────┐       │                          │
│         │       │  Agent 1  │  │  Agent 2  │  ...  │                          │
│         │       │           │  │           │       │                          │
│         │       └─────┬─────┘  └─────┬─────┘       │                          │
│         │             │              │               │                          │
│         │             └──────┬───────┘               │                          │
│         │                    │                       │                          │
│         │                    ▼                       │                          │
│         │           ┌─────────────────┐             │                          │
│         │           │  Shared Context │◄────────────┼─── LLM calls           │
│         │           │  (Map<String,   │             │   (LLM_GATEWAY_URL)      │
│         │           │      Object>)    │             │                          │
│         │           └─────────────────┘             │                          │
│         │                    │                       │                          │
│         │                    ▼                       │                          │
│         │           ┌─────────────────┐             │                          │
│         │           │  ExecutionAgent │             │                          │
│         │           │  (JUnit Tests)  │             │                          │
│         │           └────────┬────────┘             │                          │
│         │                    │                       │                          │
│         │                    ▼                       │                          │
│         │           ┌─────────────────┐             │                          │
│         │           │ Test Results    │             │                          │
│         │           └────────┬────────┘             │                          │
│         │                    │                       │                          │
│         │                    ▼                       │                          │
│         │           ┌─────────────────┐             │                          │
│         │           │ ReportingAgent  │             │                          │
│         │           │ (PDF/Excel/HTML)│             │                          │
│         │           └────────┬────────┘             │                          │
│         │                    │                       │                          │
│         │                    ▼                       │                          │
│         │           ┌─────────────────┐             │                          │
│         │           │   Response      │             │                          │
│         │           │   to Frontend   │             │                          │
│         │           └────────┬────────┘             │                          │
│         │                    │                       │                          │
│         └────────────────────┴───────────────────────┘                          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Agent Pipeline Flow

```
┌─────────────────┐
│   Input: API    │
│   Sources       │
│ (JSON array)    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐     ┌─────────────────┐
│ apiDiscovery    │────▶│ specAnalysis    │
│ Agent           │     │ Agent           │
└─────────────────┘     └────────┬────────┘
                                   │
                                   ▼
┌─────────────────┐     ┌─────────────────┐
│ testData Agent  │◀────┤ testGeneration  │
└─────────────────┘     │ Agent            │
                        └────────┬────────┘
                                 │
                                 ▼
                        ┌─────────────────┐
                        │ execution       │
                        │ Agent           │
                        └────────┬────────┘
                                 │
                                 ▼
                        ┌─────────────────┐
                        │ validation      │
                        │ Agent           │
                        └────────┬────────┘
                                 │
                                 ▼
                        ┌─────────────────┐
                        │ failureAnalysis │
                        │ Agent           │
                        └────────┬────────┘
                                 │
                                 ▼
                        ┌─────────────────┐
                        │ jira Agent      │
                        └────────┬────────┘
                                 │
                                 ▼
                        ┌─────────────────┐
                        │ reporting Agent   │
                        └────────┬────────┘
                                 │
                                 ▼
                        ┌─────────────────┐
                        │ optimization    │
                        │ Agent           │
                        └────────┬────────┘
                                 │
                                 ▼
                        ┌─────────────────┐
                        │ Final Output    │
                        │ (JSON + PDF/Excel)│
                        └─────────────────┘
```

## Prerequisites

- **Java 17+** (for Spring Boot backend)
- **Maven 3.9+** (for building backend)
- **Node.js 20+** (for Vite frontend)
- **npm 10+** (for package management)

## Environment Configuration

Create a `.env` file in the project root with:

```bash
# LLM Gateway Configuration (required for test generation)
LLM_GATEWAY_URL=https://api.groq.com/openai/v1
LLM_API_KEY=your-groq-api-key

# Jira Configuration (optional - for ticket creation)
JIRA_URL=https://your-company.atlassian.net
JIRA_USER=your-email@example.com
JIRA_TOKEN=your-jira-api-token
JIRA_PROJECT=API
```

## Step-by-Step Execution

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/AutomatedAPItesting.git
cd AutomatedAPItesting
```

### 2. Start the Backend

```bash
cd backend
# Option A: Using Maven directly
mvn spring-boot:run

# Option B: With environment variables
LLM_GATEWAY_URL="https://api.groq.com/openai/v1" LLM_API_KEY="your-key" mvn spring-boot:run

# Option C: Build and run JAR
mvn clean package -DskipTests
java -jar target/apitesting-0.0.1-SNAPSHOT.jar
```

The backend will start on **port 8081**.

### 3. Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend will start on **port 3000** (proxied to backend).

### 4. Verify Services

```bash
# Check backend health
curl http://localhost:8081/api/health
# Expected: {"status":"UP"}

# Check frontend proxy
curl http://localhost:3000/api/health
# Expected: {"status":"UP"}
```

### 5. Run a Test Case

Using curl:
```bash
curl -X POST http://localhost:3000/api/orchestrate \
  -H "Content-Type: application/json" \
  -d '{"apiSources":"[]"}'
```

Or use the UI at `http://localhost:3000` to enter API sources and click "Run Tests".

### 6. Using the UI - Step by Step with Examples

Open your browser to `http://localhost:3000` and follow these steps:

#### Example 1: Empty Sources (Quick Test)

1. **Leave the textarea empty or enter `[]`**
   - This tests the pipeline without actual API sources
   - Useful for verifying the system works end-to-end

2. **Click "Run Tests"**
   - The button will show "Running..." while processing
   - Wait for the results section to appear

3. **View Results**
   - The Results section shows all 10 agent outputs
   - Without LLM gateway, you'll see empty responses with `NO_TEST_CODE` in execution

#### Example 2: OpenAPI Specification URL

```json
[{"url": "https://petstore3.swagger.io/api/v3/openapi.json"}]
```

This will:
- Fetch the Petstore OpenAPI spec
- Analyze endpoints (/pet, /store, /user)
- Generate tests for CRUD operations
- Execute tests (will fail since we're testing against real unauthorized API)
- Generate reports based on results

#### Example 3: Multiple API Sources

```json
[
  {"url": "https://jsonplaceholder.typicode.com/swagger"},
  {"type": "postman", "url": "https://api.example.com/collection.json"},
  {"type": "openapi", "content": "inline spec content"}
]
```

#### Example 4: GitHub Repository with API Definitions

```json
[
  {"type": "github", "url": "https://github.com/user/api-specs", "path": "openapi.yaml"}
]
```

#### UI Input Field Guide

| Field | Description | Example |
|-------|-------------|---------|
| `url` | OpenAPI/Postman collection URL | `"https://api.example.com/openapi.json"` |
| `type` | Source type (openapi, postman, github, azure) | `"openapi"` |
| `content` | Inline specification content | `"content": "# yaml spec"` |
| `path` | File path within repo (for GitHub/Azure) | `"spec/api.yaml"` |

### 7. Response Format

The `/api/orchestrate` endpoint returns a JSON object with these keys:

```json
{
  "apiDiscovery": "...",     // Discovered API catalog
  "specAnalysis": "...",     // Testing strategy
  "testData": "...",         // Generated test data
  "testGeneration": "...",   // Generated Java test code
  "execution": "...",        // JUnit execution results
  "validation": "...",       // SLA validation results
  "failureAnalysis": "...",  // Root cause analysis
  "jira": "PROJ-1234",     // Jira ticket key (if created)
  "reporting": "...",        // HTML report content
  "optimization": "...",   // Optimization suggestions
  "finalContext": {...},    // All intermediate data
  "pdfReportBase64": "...", // PDF report (base64)
  "excelReportBase64": "..." // Excel report (base64)
}
```

### 8. Testing with LLM Enabled

Set these environment variables before starting the backend for full functionality:

**Windows (PowerShell):**
```powershell
$env:LLM_GATEWAY_URL = "https://api.groq.com/openai/v1"
$env:LLM_API_KEY = "your-groq-api-key"
$env:JIRA_URL = "https://your-company.atlassian.net"
$env:JIRA_USER = "your-email@example.com"
$env:JIRA_TOKEN = "your-jira-token"
$env:JIRA_PROJECT = "API"
```

**Linux/macOS:**
```bash
export LLM_GATEWAY_URL="https://api.groq.com/openai/v1"
export LLM_API_KEY="your-groq-api-key"
export JIRA_URL="https://your-company.atlassian.net"
export JIRA_USER="your-email@example.com"
export JIRA_TOKEN="your-jira-token"
export JIRA_PROJECT="API"
```

Then restart the backend and run tests.

## Test Case Execution Flow

1. **Frontend** sends POST request to `/api/orchestrate` with `apiSources` JSON array
2. **Backend** calls `Orchestrator.run()` which executes agents sequentially:
   - `ApiDiscoveryAgent` - Discovers APIs from provided sources
   - `SpecAnalysisAgent` - Analyzes specs and creates testing strategy
   - `TestDataAgent` - Generates synthetic test data
   - `TestGenerationAgent` - Generates RestAssured test code
   - `ExecutionAgent` - Writes and runs tests via JUnit
   - `ValidationAgent` - Validates results against SLAs
   - `FailureAnalysisAgent` - Root cause analysis on failures
   - `JiraAgent` - Creates Jira tickets for high-confidence failures
   - `ReportingAgent` - Generates HTML/PDF/Excel reports
   - `OptimizationAgent` - Suggests optimizations for next run

## Project Structure

```
AutomatedAPItesting/
├── backend/                    # Spring Boot application
│   ├── src/main/java/
│   │   └── com/example/apitest/
│   │       ├── Application.java           # Main entry point
│   │       ├── agent/                     # 10 LangGraph agents
│   │       ├── web/                       # REST controller
│   │       ├── config/                    # Security, LLM, Jira configs
│   │       └── service/                   # LLM and Jira services
│   ├── src/main/resources/
│   │   └── application.yml                # Server config (port 8081)
│   └── pom.xml                            # Maven dependencies
├── frontend/                   # React/Vite application
│   ├── src/
│   │   ├── App.tsx              # Main UI component
│   │   ├── api.ts               # API client
│   │   └── index.tsx            # Entry point
│   ├── package.json
│   └── vite.config.ts           # Vite config with proxy to :8081
├── .gitignore
└── README.md
```

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/health` | GET | Health check |
| `/api/orchestrate` | POST | Run full API testing pipeline |

## Common Development Commands

```bash
# Backend - from backend/ directory
mvn clean compile       # Compile the project
mvn test              # Run unit tests
mvn spring-boot:run   # Start development server

# Frontend - from frontend/ directory
npm install           # Install dependencies
npm run dev           # Start dev server (port 3000)
npm run build         # Build for production
npm run preview       # Preview production build
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Port 8081 in use | Change `server.port` in `application.yml` or kill process |
| LLM calls return empty | Set `LLM_GATEWAY_URL` and `LLM_API_KEY` env vars |
| Build fails with encoding warnings | Set `export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"` |
| Tests fail to compile | Remove malformed generated test files from `src/test/java/com/example/apitest/generated/` |

---
*Generated with Claude Code*