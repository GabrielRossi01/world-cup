# ⚽ World Cup 2026 — AI Assistant

Aplicação web **full-stack** construída com **Spring Boot + Vaadin** para demonstrar as capacidades do ecossistema **Spring AI** aplicadas a um tema atual: a **Copa do Mundo FIFA 2026**, que ocorre nos Estados Unidos, Canadá e México entre 11 de junho e 19 de julho de 2026, com 48 seleções participantes.

***

## 🎯 Objetivo

Desenvolver um agente conversacional inteligente capaz de:

- Responder perguntas sobre a Copa do Mundo 2026 (calendário, seleções, grupos, estádios)
- Consultar banco de dados estruturado via **Tool Calling**
- Enriquecer respostas com contexto do torneio via **RAG (Retrieval-Augmented Generation)**
- Manter o histórico da conversa via **memória por sessão**
- Gerar e persistir **previsões estruturadas** de partidas com Structured Output

***

## 🧠 Requisitos Técnicos Spring AI

| Requisito | Como foi implementado |
|---|---|
| **Spring AI Chat** | `ChatClient` integrado ao Groq (LLaMA 3.3 70B) via endpoint OpenAI-compatible |
| **Tool Calling** | `WorldCupTools` — 3 ferramentas: `getTeamDebut`, `getTeamSchedule`, `getUpcomingMatches` |
| **Structured Output** | `PredictionService.generateAndSavePrediction()` — retorna `MatchAnalysis` tipado via `.entity()` |
| **RAG** | `RagInjection` carrega PDF oficial da Copa no `SimpleVectorStore`; `QuestionAnswerAdvisor` recupera contexto antes da geração |
| **Agent** | `ChatService` — orquestra memória (`MessageWindowChatMemory`), RAG e Tool Calling em fluxo único |

***

## 🛠️ Tecnologias

| Tecnologia | Função |
|---|---|
| **Spring Boot** | Framework principal |
| **Spring AI** | Abstração para LLMs, embeddings, vector store, advisors |
| **Vaadin Flow** | Frontend server-side em Java — sem JavaScript |
| **Groq API** | Provider de LLM — modelo LLaMA 3.3 70B via endpoint OpenAI-compatible |
| **Ollama** | Provider local para **modelo de embeddings** (`all-minilm`) |
| **SimpleVectorStore** | Vector store em memória para o RAG |
| **H2 Database** | Banco relacional em memória para desenvolvimento |
| **Lombok** | Redução de boilerplate nas entidades |
| **Spring Data JPA** | Repositórios e consultas JPQL |

***

## 🔍 Por que o Ollama é fundamental neste projeto

O **Ollama** é o componente responsável por transformar texto em **vetores numéricos (embeddings)** — representações matemáticas que capturam o significado semântico do conteúdo.

Sem embeddings, o RAG simplesmente não funciona. O processo é:

1. O PDF do calendário oficial da Copa é lido e quebrado em chunks de texto
2. Cada chunk é enviado ao modelo `all-minilm` rodando no Ollama
3. O modelo retorna um vetor de 384 dimensões representando aquele trecho
4. Esses vetores são armazenados no `SimpleVectorStore`
5. Quando o usuário faz uma pergunta, ela também é vetorizada pelo Ollama
6. Os chunks semanticamente mais próximos são recuperados e injetados no contexto da resposta

```
Pergunta do usuário
       │
       ▼
OllamaEmbeddingModel (all-minilm)
       │
       ▼
Busca por similaridade no VectorStore
       │
       ▼
Contexto relevante injetado no prompt
       │
       ▼
Resposta enriquecida pelo LLM
```

O modelo `all-minilm` é leve, rápido e suficiente para buscas de contexto em português. Toda a inferência de embeddings ocorre **localmente**, sem custo e sem dependência de internet para essa etapa.

> ⚠️ **O Ollama precisa estar rodando localmente com o modelo `all-minilm` disponível para a aplicação inicializar corretamente.**

***

## ✅ Pré-requisitos

- **Java 25+**
- **Maven**
- **Ollama** instalado e rodando localmente
- **Chave de API Groq** (gratuita em [console.groq.com](https://console.groq.com))

***

## 📦 Instalação

### 1. Instalar o Ollama

**Windows (PowerShell como Administrador):**

```powershell
irm https://ollama.com/install.ps1 | iex
```

**Linux / macOS:**

```bash
curl -fsSL https://ollama.com/install.sh | sh
```

Após a instalação, feche e reabra o terminal. Verifique:

```powershell
ollama --version
```

### 2. Baixar o modelo de embeddings

```powershell
ollama pull all-minilm
```

Este comando baixa o modelo `all-minilm` (~45 MB), que será usado exclusivamente para geração de embeddings no RAG. Após o download, o Ollama expõe automaticamente o serviço em `http://localhost:11434`.

Verifique se o modelo está disponível:

```powershell
ollama list
```

### 3. Configurar a variável de ambiente

**Windows (PowerShell):**

```powershell
$env:GROQ_API_KEY = "gsk_sua_chave_aqui"
```

**Linux / macOS:**

```bash
export GROQ_API_KEY=gsk_sua_chave_aqui
```

> Obtenha sua chave gratuita em [console.groq.com/keys](https://console.groq.com/keys).

### 4. Adicionar o PDF no projeto

Coloque o arquivo do calendário oficial da Copa no caminho:

```
src/main/resources/doc/fwc26-match-schedule.pdf
```

Este PDF é carregado automaticamente no `VectorStore` na inicialização da aplicação.

### 5. Executar a aplicação

```bash
mvn spring-boot:run
```

Acesse em: **http://localhost:8080**

***

## 💬 Como usar

### Chat principal

A interface principal exibe um campo `MessageInput` onde você pode fazer perguntas livremente ao agente. Exemplos:

```
"Quando o Brasil estreia na Copa?"
"Qual é a agenda completa da Argentina?"
"Quem são os favoritos no Grupo A?"
"Me fale sobre o estilo de jogo da França"
"Gere uma análise do confronto Brasil x Alemanha"
```

O agente:
- usa **Tool Calling** automaticamente para perguntas sobre datas e agenda
- usa **RAG** para enriquecer respostas com contexto do torneio
- mantém **memória** do histórico da conversa na sessão atual
- responde em streaming (os tokens chegam progressivamente na tela)

### Grid de previsões

A parte inferior da tela exibe um grid com todas as previsões de partidas geradas e salvas pelo agente via Structured Output.

***

## 🔧 Possíveis problemas

| Erro | Causa | Solução |
|---|---|---|
| `model "all-minilm" not found` | Modelo não baixado | Rodar `ollama pull all-minilm` |
| `ollama: command not found` | Ollama não instalado | Executar o script de instalação e reabrir o terminal |
| `conversationId cannot be null` | Advisor de memória sem ID | Verificar se o `ChatMemory.CONVERSATION_ID` está sendo passado no advisor |
| `model decommissioned` | Modelo desatualizado no Groq | Atualizar `spring.ai.openai.chat.options.model` para `llama-3.3-70b-versatile` |
| `Table VECTOR_STORE not found` | PgVector ativo sem PostgreSQL | Remover dependência do PgVector e usar `SimpleVectorStore` |
| `Two ChatModel beans found` | Dois providers de chat ativos | Adicionar `spring.autoconfigure.exclude` para desabilitar `OllamaChatAutoConfiguration` |

***

## 📚 Fluxo do Agente

```
Usuário envia mensagem
        │
        ▼
  MessageChatMemoryAdvisor     ← injeta histórico da conversa
        │
        ▼
  QuestionAnswerAdvisor        ← recupera contexto do VectorStore (RAG)
        │                         via OllamaEmbeddingModel (all-minilm)
        ▼
  ChatClient (Groq/LLaMA)      ← LLM processa mensagem + contexto + memória
        │
        ├── Tool Calling? ──────► WorldCupTools
        │                         ├── getTeamDebut(teamName)
        │                         ├── getTeamSchedule(teamName)
        │                         └── getUpcomingMatches(teamName)
        │
        ▼
  Flux<String>                 ← resposta em streaming
        │
        ▼
  HomeView (Vaadin)            ← renderiza progressivamente no browser
```

***

## 📊 Modelos utilizados

| Modelo | Provider | Uso |
|---|---|---|
| `llama-3.3-70b-versatile` | Groq (cloud) | Chat, análise, structured output |
| `all-minilm` | Ollama (local) | Embeddings para RAG |
