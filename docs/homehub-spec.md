# HomeHub – Especificação Completa do Projeto

**Versão do Documento:** 1.0.0
**Data de Referência:** 2026-04-03
**Autor:** vinicius-mascarenhas
**Objetivo deste documento:** Fornecer a um agente de IA ou desenvolvedor TODAS as informações necessárias para implementar o projeto HomeHub do zero, sem ambiguidades.

---

## 1. VISÃO GERAL DO PROJETO

### 1.1 O que é o HomeHub

HomeHub é um "portal web self-hosted" que agrega múltiplos serviços containerizados atrás de uma autenticação robusta. Funciona como uma "homepage" pessoal onde o usuário faz login uma vez e acessa todos os serviços integrados (download de vídeos, media server, torrent client, backup de fotos, workspace de notas, monitoramento).

### 1.2 Propósito

- **Projeto pessoal de portfólio ("cartão de visita")** – deve demonstrar qualidade profissional em código, arquitetura e UI.
- Servir como **"plataforma utilitária real"** para uso doméstico.
- Arquitetura modular que permite **"plug & play"** de novos serviços.

### 1.3 Restrições de Ambiente

| Restrição                | Valor                                                                    |
| :----------------------- | :----------------------------------------------------------------------- |
| **Hardware alvo**        | Raspberry Pi 4 (4GB ou 8GB RAM)                                          |
| **Storage**              | SSD externo via USB 3.0 (256GB+)                                         |
| **SO**                   | Raspberry Pi OS Lite 64-bit ou Ubuntu Server 24.04 ARM64                 |
| **Usuários simultâneos** | Máximo 5                                                                 |
| **Acesso inicial**       | LAN local apenas (escalável para remoto via Cloudflare Tunnel no futuro) |
| **Arquitetura CPU**      | ARM64 (aarch64) – todas as imagens Docker DEVEM suportar ARM64           |

### 1.4 Requisitos Não-Funcionais

- Consumo de RAM total de todos os serviços: **máximo ~3.5GB** (para deixar headroom no Pi de 4GB).
- Tempo de resposta do portal: **< 500ms** para páginas estáticas.
- Tempo de cold start (docker-compose up): **< 3 minutos**.
- Projeto inteiramente containerizado via Docker Compose – **NENHUMA** dependência instalada diretamente no host exceto Docker.
- Todo código em **TypeScript strict** (frontend) e **Java 21** (backend).

---

## 2. ARQUITETURA GERAL

### 2.1 Diagrama de Arquitetura

```text
+-------------------------------------------------------------+
|                      RASPBERRY PI 4                         |
|                                                             |
|  +-------------------------------------------------------+  |
|  |           Traefik (Reverse Proxy)                     |  |
|  |           :80 / :443 (HTTPS)                          |  |
|  |           Auto-discovery via Docker labels            |  |
|  +----------+--------------+--------------+--------------+  |
|             |              |              |                 |
|  +----------v--+  +--------v---+  +-------v---+  +-------+  |
|  |   Backend   |  |   MeTube   |  | Jellyfin  |  | qBit- |  |
|  |  API+Auth   |  |   :8081    |  |  :8096    |  | torrent| |
|  |   :8080     |  |            |  |           |  | :8080 |  |
|  +------+------+  +------------+  +-----------+  +---+---+  |
|         |                                            |      |
|  +------v-------+  +------------+  +------------+  +-v-----+|
|  |  PostgreSQL  |  |    Redis   |  | Watchtower |  | Uptime| |
|  |    :5432     |  |    :6379   |  | (monitor)  |  | Kuma  | |
|  +--------------+  +------------+  +------------+  | :3001 | |
|                                                    +-------+|
|  +--------------+  +------------+                           |
|  |  Prometheus  |  |   Grafana  |                           |
|  |    :9090     |  |    :3000   |                           |
|  +--------------+  +------------+                           |
|                                                             |
|  +-------------------------------------------------------+  |
|  |                 Docker Volumes (SSD externo)          |  |
|  | /data/media  /data/photos  /data/downloads  /data/db  |  |
|  | /data/affine /data/grafana /data/backups              |  |
|  +-------------------------------------------------------+  |
+-------------------------------------------------------------+
```

### 2.2 Fluxo de Request

**Browser + Traefik (reverse proxy)**

- `/api/*` -> Backend Spring Boot (porta 8080)
- `/metube/*` -> MeTube container (porta 8081)
- `/jellyfin/*` -> Jellyfin container (porta 8096)
- `/torrents/*` -> qBittorrent container (porta 8080)
- `/photos/*` -> Immich container (porta 2283)
- `/notes/*` -> AFFiNE container (porta 3010)
- `/grafana/*` -> Grafana container (porta 3000)
- `/status/*` -> Uptime Kuma container (porta 3001)
- `/*` (default) -> Frontend React (porta 3000)

Todos os serviços (exceto `/api/auth/login` e `/api/auth/register`) passam por **Forward Auth**: Traefik envia o request primeiro ao backend para validar o JWT antes de encaminhar ao serviço destino.

### 2.3 Arquitetura Interna dos Módulos - Clean Architecture Pragmática

O backend segue uma **"Clean Architecture simplificada"** com separação `domain/` e `infrastructure/`:

- `domain/`
  - `model/` -> Entidades puras (POJOs sem anotações Spring/JPA)
  - `port/`
    - `in/` -> Interfaces de entrada (Use Cases)
    - `out/` -> Interfaces de saída (Repository ports, external services)
  - `service/` -> Implementação das regras de negócio (usa apenas ports)
- `infrastructure/`
  - `web/` -> Controllers REST (@RestController) e DTOs de request/response
  - `persistence/` -> JPA Entities, JPA Repositories, Adapters que implementam as ports out
  - `security/` -> Adapters de JWT, TOTP, password encoding

**Regra fundamental:** O pacote `domain/` NÃO importa NADA de Spring, JPA, Redis ou qualquer framework. Apenas Java puro. A camada `infrastructure/` implementa as interfaces definidas em `domain/port/`.

---

## 3. STACK TECNOLÓGICA - VERSÕES EXATAS

### 3.1 Backend

| Tecnologia                 | Versão                                  | Finalidade                        |
| :------------------------- | :-------------------------------------- | :-------------------------------- |
| **Java**                   | SDK 21.0.11 (LTS)                       | Linguagem principal do backend    |
| **Spring Boot**            | 3.3.x (latest patch)                    | Framework web + DI + configuração |
| **Spring Security**        | 6.x (incluso no Spring Boot 3.3)        | Autenticação e autorização        |
| **Spring Data JPA**        | Incluso no Spring Boot 3.3              | Acesso ao banco de dados          |
| **jjwt (io.jsonwebtoken)** | 0.12.x                                  | Geração e validação de JWT        |
| **BCrypt**                 | Incluso no Spring Security              | Hash de senhas                    |
| **TOTP (java-otp)**        | latest                                  | Autenticação de dois fatores      |
| **Maven**                  | 3.9.x                                   | Build tool                        |
| **Springdoc OpenAPI**      | 2.x                                     | Documentação Swagger automática   |
| **Flyway**                 | latest (compatível com Spring Boot 3.3) | Migrações de banco de dados       |
| **Docker base image**      | eclipse-temurin:21-jre-alpine           | Imagem leve para produção (~80MB) |

### 3.2 Frontend

| Tecnologia            | Versão                                        | Finalidade                        |
| :-------------------- | :-------------------------------------------- | :-------------------------------- |
| **React**             | 18.3.1                                        | Biblioteca de UI                  |
| **TypeScript**        | 5.x (latest)                                  | Tipagem estática                  |
| **Vite**              | 5.0.x                                         | Bundler e dev server              |
| **Tailwind CSS**      | 4.2.x                                         | Estilização utility-first         |
| **shadcn/ui**         | 2.x (latest via CLI)                          | Componentes UI                    |
| **React Router**      | 6.x (latest v6 patch)                         | Roteamento SPA                    |
| **Zustand**           | 5.0.x                                         | Gerenciamento de estado global    |
| **Axios**             | 1.7.x                                         | HTTP client com interceptors      |
| **Lucide React**      | 0.4.x                                         | Ícones SVG                        |
| **Framer Motion**     | latest                                        | Animações de transição (opcional) |
| **Vitest**            | latest                                        | Testes unitários                  |
| **Docker base image** | node:20-alpine (build) + nginx:alpine (serve) | Multi-stage build                 |

### 3.3 Infraestrutura

| Tecnologia         | Versão / Tag Docker                    | Finalidade                          |
| :----------------- | :------------------------------------- | :---------------------------------- |
| **Docker Engine**  | 24.x+                                  | Runtime de containers               |
| **Docker Compose** | v2.x (plugin)                          | Orquestração de containers          |
| **Traefik**        | v3.0.x (`traefik:v3.0`)                | Reverse proxy com auto-discovery    |
| **PostgreSQL**     | 16 (`postgres:16-alpine`)              | Banco de dados relacional           |
| **Redis**          | 7 (`redis:7-alpine`)                   | Cache, sessões, rate limiting       |
| **Prometheus**     | latest (`prom/prometheus:latest`)      | Coleta de métricas                  |
| **Grafana**        | latest (`grafana/grafana:latest`)      | Dashboards de monitoramento         |
| **Uptime Kuma**    | latest (`louislam/uptime-kuma:latest`) | Monitoramento de uptime             |
| **Watchtower**     | latest (`containrrr/watchtower`)       | Atualizações automáticas de imagens |

### 3.4 Serviços Integrados (Módulos)

| Serviço         | Imagem Docker                     | Função                                 | Porta Interna |
| :-------------- | :-------------------------------- | :------------------------------------- | :------------ |
| **MeTube**      | `alexta69/metube`                 | Download de vídeos YouTube/outros      | 8081          |
| **Jellyfin**    | `jellyfin/jellyfin`               | Media Server - streaming e metadados   | 8096          |
| **qBittorrent** | `linuxserver/qbittorrent`         | Cliente torrent com Web UI             | 8080          |
| **Immich**      | `immich-app/immich-server`        | Backup de fotos, reconhecimento facial | 2283          |
| **AFFiNE**      | `toeverything/affine-self-hosted` | Workspace de docs e whiteboards        | 3010          |

---

## 4. SISTEMA DE AUTENTICAÇÃO - ESPECIFICAÇÃO DETALHADA

### 4.1 Fluxo de Login

1.  Usuário submete `email` + `password` via POST `/api/auth/login`.
2.  Backend busca usuário no PostgreSQL por email.
3.  Compara senha enviada com hash BCrypt salvo no banco.
4.  **Se correta e 2FA NÃO está ativo:** gera JWT (access + refresh), retorna ambos.
5.  **Se senha correta E 2FA está ativo:** retorna `{ requires2FA: true, tempToken: "..." }`.
6.  Frontend mostra campo de código TOTP.
7.  Usuário submete código TOTP via POST `/api/auth/verify-2fa` com o `tempToken`.
8.  Backend valida código TOTP + se válido, gera JWT (access + refresh), retorna ambos.
9.  Frontend salva `accessToken` em memória (Zustand store) e `refreshToken` em httpOnly cookie.

### 4.2 Tokens JWT

| Token             | Duração    | Conteúdo do Payload                                     | Armazenamento       |
| :---------------- | :--------- | :------------------------------------------------------ | :------------------ | ----------------- |
| **Access Token**  | 15 minutos | `{ sub: email, userId: uuid, role: "ADMIN"              | "USER", iat, exp }` | Memória (Zustand) |
| **Refresh Token** | 7 dias     | `{ sub: email, userId: uuid, tokenid: uuid, iat, exp }` | HttpOnly cookie     |

- **Refresh flow:** Quando o frontend recebe 401, interceptor Axios chama POST `/api/auth/refresh`. Se sucesso, substitui access token. Se falhar, redireciona para `/login`.
- **Logout:** POST `/api/auth/logout` adiciona o refresh token a uma blacklist no Redis.

### 4.3 Forward Auth (proteção dos serviços)

Traefik envia cada request destinado a um serviço primeiro ao endpoint `GET /api/auth/verify` do backend.

1.  Extrai o JWT do header `Authorization: Bearer <token>` ou do cookie.
2.  Valida assinatura e expiração.
3.  Verifica permissão (baseado em role).
4.  Se OK -> retorna 200 (Traefik libera). Se inválido -> retorna 401 (Traefik bloqueia).

### 4.4 Rate Limiting

- **Limite:** 5 tentativas de login falhadas por IP em 1 minuto.
- **Bloqueio:** IP por 15 minutos (HTTP 429).
- **Global:** 100 requests por minuto por usuário autenticado.

### 4.5 Roles e Permissões

- **USER:** Pode acessar serviços (conforme permissão do ADMIN). Não gerencia usuários/serviços.
- **ADMIN:** Acesso total. Gerencia usuários e serviços.

### 4.6 Endpoints da API de Auth

| Método | Endpoint               | Resposta                                             | Auth?            |
| :----- | :--------------------- | :--------------------------------------------------- | :--------------- |
| POST   | `/api/auth/register`   | `{ user }`                                           | Não (ou ADMIN)   |
| POST   | `/api/auth/login`      | `{ accessToken, refreshToken }` ou `{ requires2FA }` | Não              |
| POST   | `/api/auth/verify-2fa` | `{ accessToken, refreshToken }`                      | Não              |
| POST   | `/api/auth/refresh`    | `{ accessToken }`                                    | Não (usa cookie) |
| POST   | `/api/auth/logout`     | `{ success }`                                        | Sim              |
| GET    | `/api/auth/verify`     | 200 ou 401 (Forward Auth)                            | Sim              |
| GET    | `/api/auth/me`         | `{ user }`                                           | Sim              |

---

## 5. ENDPOINTS DA API – ESPECIFICAÇÃO COMPLETA

### 5.1 Usuários (ADMIN only exceto onde indicado)

| Método | Endpoint                        | Descrição                             | Auth  |
| :----- | :------------------------------ | :------------------------------------ | :---- |
| GET    | `/api/users`                    | Lista todos os usuários               | ADMIN |
| GET    | `/api/users/:id`                | Detalhe de um usuário                 | ADMIN |
| POST   | `/api/users`                    | Cria novo usuário                     | ADMIN |
| PUT    | `/api/users/:id`                | Atualiza usuário (role, nome, status) | ADMIN |
| PATCH  | `/api/users/:id/toggle-active`  | Ativa/desativa usuário                | ADMIN |
| PATCH  | `/api/users/:id/reset-password` | Reseta senha (gera temporária)        | ADMIN |

### 5.2 Perfil (usuário autenticado)

| Método | Endpoint                   | Descrição               | Auth |
| :----- | :------------------------- | :---------------------- | :--- |
| GET    | `/api/profile`             | Dados do usuário logado | USER |
| PUT    | `/api/profile`             | Atualiza nome/email     | USER |
| PUT    | `/api/profile/password`    | Troca senha             | USER |
| POST   | `/api/profile/2fa/enable`  | Gera QR code para 2FA   | USER |
| POST   | `/api/profile/2fa/confirm` | Confirma ativação TOTP  | USER |
| POST   | `/api/profile/2fa/disable` | Desativa 2FA            | USER |

### 5.3 Service Registry

| Método | Endpoint                   | Descrição                          | Auth  |
| :----- | :------------------------- | :--------------------------------- | :---- |
| GET    | `/api/services`            | Lista serviços (filtrado por role) | USER  |
| GET    | `/api/services/:id`        | Detalhe de um serviço              | USER  |
| GET    | `/api/services/:id/health` | Status de saúde do serviço         | USER  |
| POST   | `/api/services`            | Registra novo serviço              | ADMIN |
| PUT    | `/api/services/:id`        | Atualiza serviço                   | ADMIN |
| DELETE | `/api/services/:id`        | Remove registro de serviço         | ADMIN |

### 5.4 System Health

| Método | Endpoint                    | Descrição                   | Auth |
| :----- | :-------------------------- | :-------------------------- | :--- |
| GET    | `/api/system/health`        | Métricas de CPU, RAM, Disco | USER |
| GET    | `/api/system/notifications` | Últimas 20 notificações     | USER |

---

## 6. FRONTEND – ESPECIFICAÇÃO DETALHADA

### 6.1 Stack (repetido para clareza)

React 18 + TypeScript + Vite 5 + Tailwind CSS 4 + shadcn/ui + React Router 6 + Zustand 5 + Axios + Lucide React

### 6.2 Estrutura de Pastas

```text
homehub-frontend/
├── public/
│   ├── favicon.svg
│   └── logo.svg
├── src/
│   ├── main.tsx
│   ├── App.tsx
│   ├── index.css
│   ├── routes/
│   ├── components/
│   ├── hooks/
│   ├── store/
│   ├── services/
│   └── types/
```

```text
homehub-backend/
├── pom.xml
├── README.md
├── docker/
│   └── Dockerfile
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── homehub/
│   │   │           ├── HomeHubApplication.java
│   │   │
│   │   │           ├── domain/
│   │   │           │   ├── model/
│   │   │           │   │   ├── user/
│   │   │           │   │   ├── auth/
│   │   │           │   │   ├── service/
│   │   │           │   │   └── system/
│   │   │           │   │
│   │   │           │   ├── port/
│   │   │           │   │   ├── in/
│   │   │           │   │   │   ├── auth/
│   │   │           │   │   │   ├── user/
│   │   │           │   │   │   ├── profile/
│   │   │           │   │   │   ├── service/
│   │   │           │   │   │   └── system/
│   │   │           │   │   │
│   │   │           │   │   └── out/
│   │   │           │   │       ├── persistence/
│   │   │           │   │       │   ├── user/
│   │   │           │   │       │   ├── auth/
│   │   │           │   │       │   ├── service/
│   │   │           │   │       │   └── system/
│   │   │           │   │       │
│   │   │           │   │       ├── cache/
│   │   │           │   │       ├── security/
│   │   │           │   │       └── external/
│   │   │           │   │
│   │   │           │   └── service/
│   │   │           │       ├── auth/
│   │   │           │       ├── user/
│   │   │           │       ├── profile/
│   │   │           │       ├── service/
│   │   │           │       └── system/
│   │   │
│   │   │           ├── infrastructure/
│   │   │           │   ├── web/
│   │   │           │   │   ├── controller/
│   │   │           │   │   │   ├── auth/
│   │   │           │   │   │   ├── user/
│   │   │           │   │   │   ├── profile/
│   │   │           │   │   │   ├── service/
│   │   │           │   │   │   └── system/
│   │   │           │   │   │
│   │   │           │   │   ├── dto/
│   │   │           │   │   │   ├── request/
│   │   │           │   │   │   │   ├── auth/
│   │   │           │   │   │   │   ├── user/
│   │   │           │   │   │   │   ├── profile/
│   │   │           │   │   │   │   └── service/
│   │   │           │   │   │   │
│   │   │           │   │   │   └── response/
│   │   │           │   │   │       ├── auth/
│   │   │           │   │   │       ├── user/
│   │   │           │   │   │       ├── profile/
│   │   │           │   │   │       ├── service/
│   │   │           │   │   │       └── system/
│   │   │           │   │   │
│   │   │           │   │   └── advice/
│   │   │           │   │       └── (exception handlers globais)
│   │   │           │   │
│   │   │           │   ├── persistence/
│   │   │           │   │   ├── entity/
│   │   │           │   │   │   ├── user/
│   │   │           │   │   │   ├── auth/
│   │   │           │   │   │   ├── service/
│   │   │           │   │   │   └── system/
│   │   │           │   │   │
│   │   │           │   │   ├── repository/
│   │   │           │   │   │   ├── user/
│   │   │           │   │   │   ├── auth/
│   │   │           │   │   │   ├── service/
│   │   │           │   │   │   └── system/
│   │   │           │   │   │
│   │   │           │   │   └── adapter/
│   │   │           │   │       ├── user/
│   │   │           │   │       ├── auth/
│   │   │           │   │       ├── service/
│   │   │           │   │       └── system/
│   │   │           │   │
│   │   │           │   ├── security/
│   │   │           │   │   ├── jwt/
│   │   │           │   │   ├── totp/
│   │   │           │   │   ├── config/
│   │   │           │   │   ├── filter/
│   │   │           │   │   └── adapter/
│   │   │           │   │
│   │   │           │   ├── cache/
│   │   │           │   │   ├── redis/
│   │   │           │   │   └── adapter/
│   │   │           │   │
│   │   │           │   ├── external/
│   │   │           │   │   ├── docker/
│   │   │           │   │   ├── monitoring/
│   │   │           │   │   └── adapter/
│   │   │           │   │
│   │   │           │   └── config/
│   │   │           │       ├── database/
│   │   │           │       ├── web/
│   │   │           │       ├── security/
│   │   │           │       └── openapi/
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── db/
│   │       │   └── migration/   (Flyway)
│   │       ├── static/
│   │       └── templates/
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── homehub/
│                   ├── domain/
│                   ├── infrastructure/
│                   └── integration/
│
└── docker-compose.yml (opcional para dev local)
```

---

## V1 – BACKEND: STATUS ATUAL DA IMPLEMENTAÇÃO (V1 – BACKEND)

### 1. Escopo Atual

A implementação atual cobre a primeira versão (V1) do sistema de autenticação, com foco em:

- Login de usuários
- Validação de credenciais
- Geração de Access Token (JWT)
- Arquitetura baseada em Clean Architecture (domain + ports + adapters)

Funcionalidades como:

- Refresh Token
- 2FA (TOTP)
- Logout / blacklist
- Rate limiting

**ainda NÃO foram implementadas** nesta etapa.

---

### 0.2 Arquitetura Implementada (Backend)

O backend segue uma abordagem de **Clean Architecture simplificada**, com separação clara entre domínio e infraestrutura:

```text
domain/
├── model/
│   └── user/
│       ├── User
│       ├── Email (Value Object)
│       └── Password (Value Object)
│
├── port/
│   ├── in/
│   │   └── auth/
│   │       └── LoginUseCase
│   │
│   └── out/
│       ├── UserRepository
│       ├── PasswordEncoder
│       └── TokenProvider
│
└── model/usecase/auth/
    └── LoginImp

infrastructure/
└── security/
    ├── password/
    │   └── BCryptPasswordAdapter (planejado)
    │
    └── jwt/
        └── JwtTokenAdapter (parcialmente implementado)
```

### 0.3 Fluxo de Login (Implementado)

Fluxo atual no backend:

- Recebe email e password
- Busca usuário via UserRepository
- Valida existência do usuário
- Executa regra de domínio user.validateUser() (usuário ativo)
- Valida senha via PasswordEncoder (abstração)
- Gera JWT via TokenProvider
- Retorna access token (String)

### 0.4 LoginUseCase

O login foi implementado como um Use Case, responsável apenas por orquestração:

Responsabilidades:

- NÃO contém lógica de criptografia
- NÃO contém lógica de JWT
- Apenas coordena o fluxo de autenticação

Dependências:

- UserRepository
- PasswordEncoder
- TokenProvider

### 0.5 Password Handling

A validação de senha foi abstraída via:
PasswordEncoder (port.out)

Responsabilidade:

- Comparar senha em texto com hash armazenado

Implementação concreta:

- Será feita via BCrypt (Spring Security) na camada de infraestrutura

### 0.6 Segurança (Estado Atual)

✔️ Implementado:

- Hash de senha (via abstração)
- JWT assinado com secret
- Expiração de token

### 0.7 Decisões de Design Importantes

- Uso de Value Objects (Email, Password)
- Separação clara entre:
  - domínio (regras)
  - infraestrutura (tecnologia)
- Uso de Ports (in/out) para desacoplamento
- JWT tratado como detalhe de infraestrutura

### 0.8 Próximos Passos Planejados

- Implementar PasswordEncoder (BCrypt)
- Finalizar JwtTokenProviderAdapter (ajustes de segurança)
- Implementar validação de JWT (verify)
- Criar filtro de autenticação (Spring Security)
- Implementar Refresh Token
- Integrar com Forward Auth (Traefik)

# HomeHub – Atualizações de Arquitetura (V1 Backend - Auth & Security)

## 📌 Contexto

Este documento descreve as principais decisões, mudanças e evoluções realizadas durante a implementação da V1 do backend do HomeHub, com foco no sistema de autenticação e camada de segurança.

- o estado atual do projeto
- decisões arquiteturais tomadas
- ajustes feitos em relação à especificação original

---

## 🔐 1. Evolução do Password Encoding (BCrypt)

### 1.1 Externalização de Configuração

Antes:

- Strength do BCrypt definido diretamente no código

Agora:

- Configuração movida para `application.yml`

```yaml
security:
  password:
    bcrypt:
      strength: 10
```

---

### 1.2 Introdução de `BCryptProperties`

Foi criada uma classe para binding de configuração:

```java
@ConfigurationProperties(prefix = "security.password.bcrypt")
public record BCryptProperties(int strength) {}
```

#### Motivações:

- Evitar uso de `@Value` espalhado
- Melhor organização e escalabilidade
- Alinhamento com boas práticas do Spring Boot

---

### 1.3 Configuração via Bean

Criação de bean dedicado:

```java
@Bean
public BCryptPasswordEncoder bCryptPasswordEncoder(BCryptProperties props) {
    return new BCryptPasswordEncoder(props.strength());
}
```

---

### 1.4 Adapter mantido na infraestrutura

```java
public class BCryptPasswordAdapter implements PasswordEncoder
```

#### Papel:

- Implementar port do domínio (`PasswordEncoder`)
- Encapsular dependência do Spring Security

#### Decisão:

- Não usar anotações Spring dentro do adapter
- Instância criada via configuração (`@Bean`)

---

## 🧠 3. Padrão adotado para Configuração

Foi estabelecido um padrão claro:

### ✔️ Properties

- Representam dados de configuração
- Mapeadas via `@ConfigurationProperties`
- Preferencialmente imutáveis (record)

### ✔️ Config Classes

- Responsáveis por criar beans (`@Bean`)
- Fazem o "wiring" da aplicação

---

### ❗ Decisão importante

Evitar uso de:

```java
@Value
```

---

## 🏗️ 4. Organização da Camada de Segurança

Estrutura atual:

```text
infrastructure/security/
├── jwt/
├── password/
```

### Decisão:

- Manter subpacotes por responsabilidade
- Evitar refatoração prematura

---

## ⚙️ 5. Estado Atual da Arquitetura (Importante)

### ✔️ Implementado

- Password encoding com BCrypt
- Externalização de configuração
- JWT generation funcional
- Separação via ports/adapters
- Injeção via configuração Spring

---

# HomeHub – Estado Atual do Projeto (Auth & JWT)

## 📍 Contexto

Este documento complementa o HomeHub_Spec.md e descreve o **estado atual da implementação**, com foco na autenticação.

O projeto está na fase:

**V1 — Fundação da Autenticação (concluída)**

## 🧠 Objetivo Atual do Projeto

O sistema já evoluiu de:

- Login simples

Para:

- Autenticação baseada em JWT
- Validação de token
- Preparação para autenticação centralizada (Forward Auth)

## 🔐 Fluxo de Autenticação

### Login

**Endpoint:**POST /api/auth/login

**Fluxo:**

- Recebe email e senha
- Converte para Value Objects (Email, Password)
- Valida credenciais
- Gera JWT

**Saída:**

- Token JWT (access token)

## 🔑 JWT (Estado Atual)

### Geração de Token

Implementado em:

- JwtTokenAdapter

**Claims utilizadas:**

- sub → email do usuário
- id → UUID do usuário
- role → papel do usuário
- iat → criação
- exp → expiração

### Validação de Token

O sistema realiza:

- Validação de assinatura
- Validação de expiração (via biblioteca)
- Extração de claims
- Conversão para modelo de domínio

## 👤 Modelo de Domínio (Auth)

### AuthenticatedUser

Representa o usuário autenticado no sistema:

- UUID userId
- Email email
- UserRole role

**Objetivo:**

- Evitar uso direto de claims no domínio
- Manter tipagem forte
- Base para autorização futura

## 🧩 TokenProvider (Contrato Atual)

Responsável por:

- Gerar token
- Validar e converter token

**Métodos:**

- generate(User user)
- parseAndValidate(String token)

## 🧩 Use Case de Verificação

### VerifyTokenUseCase

Responsável por:

- Delegar validação do token
- Garantir consistência do fluxo

**Comportamento:**

- Token válido → execução continua
- Token inválido → lança TokenInvalidException

## 🌐 Endpoint de Verificação

### GET /api/auth/verify

**Objetivo:**Endpoint técnico para validação de token.

Não é destinado ao frontend.

### Entrada

Header obrigatório:

Authorization: Bearer

### Fluxo

1.  Valida presença do header
2.  Valida prefixo "Bearer "
3.  Extrai token
4.  Executa VerifyTokenUseCase
5.  Retorna status HTTP

### Respostas

- 200 → token válido
- 401 → token inválido ou ausente

## ⚠️ Tratamento de Erros

### TokenInvalidException

- Usada para falhas de autenticação
- Retorna automaticamente 401

**Observação:**Ainda não existe um handler global (@ControllerAdvice)

## 🧱 Decisões Arquiteturais

### Clean Architecture

- Domínio não depende de Spring
- JWT isolado na infraestrutura
- Uso de Ports e Adapters

### Anti-Corruption Layer

Conversão feita em:

- JwtTokenAdapter

Evita:

- Vazamento de JWT para o domínio
- Acoplamento com biblioteca externa

## 🔌 Preparação para Integração

### Forward Auth (Traefik)

O sistema está preparado para:

- Autenticação centralizada
- Validação externa de requests

**Fluxo esperado:**

Client → Traefik → /verify → Backend

## 🚧 Próximos Passos

- Implementar filtro de segurança (Spring Security)
- Introduzir autorização por roles (RBAC)
- Criar handler global de exceções
- Melhorar validação de headers
- Integrar completamente com Traefik

---
